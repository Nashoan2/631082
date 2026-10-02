package com.example.util

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.PixelCopy
import android.view.View
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

object ScreenshotHelper {

  // Keeps offscreen webviews alive during screenshot capture to prevent GC
  private val activeScreenshotWebViews = mutableSetOf<WebView>()

  /**
   * Captures an HTML document (e.g. account statement or invoice) into a high-resolution,
   * uncropped bitmap off-screen, completely independent of screen orientation (portrait or landscape)
   * or scroll position.
   */
  fun captureHtmlToBitmap(
    context: Context,
    html: String,
    title: String = "معاينة",
    targetWidth: Int = 1080,
    showToast: Boolean = true,
    fallbackBitmap: Bitmap? = null,
    onSuccess: (Uri?) -> Unit = {}
  ) {
    val activity = findActivity(context)
    val runContext = activity ?: context
    val mainHandler = Handler(Looper.getMainLooper())

    mainHandler.post {
      try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
          try {
            WebView.enableSlowWholeDocumentDraw()
          } catch (_: Exception) {}
        }

        val webView = WebView(runContext)
        activeScreenshotWebViews.add(webView)

        webView.settings.apply {
          javaScriptEnabled = true
          domStorageEnabled = true
          useWideViewPort = true
          loadWithOverviewMode = true
          cacheMode = WebSettings.LOAD_NO_CACHE
        }

        // Layout offscreen initially
        webView.layout(0, 0, targetWidth, 1200)

        var hasExecuted = false
        webView.webViewClient = object : WebViewClient() {
          override fun onPageFinished(view: WebView?, url: String?) {
            if (hasExecuted) return
            hasExecuted = true

            // Wait a brief delay for Google Fonts and layout reflow to settle completely
            mainHandler.postDelayed({
              try {
                view?.evaluateJavascript(
                  """
                  (function() {
                    var card = document.querySelector('.invoice-card') || document.body;
                    var rect = card.getBoundingClientRect();
                    var docH = Math.max(document.body.scrollHeight, document.documentElement.scrollHeight);
                    return Math.ceil(Math.max(rect.bottom + 30, docH));
                  })()
                  """.trimIndent()
                ) { jsHeightResult ->
                  try {
                    val density = view.resources.displayMetrics.density
                    val jsH = jsHeightResult?.replace("\"", "")?.trim()?.toDoubleOrNull()?.toInt() ?: 0

                    val contentHeightDp = view.contentHeight
                    val calculatedHeight = if (jsH > 0) {
                      (jsH * density).toInt()
                    } else {
                      (contentHeightDp * density).toInt()
                    }

                    // Provide generous height so nothing is ever clipped, autoCrop will trim trailing space perfectly
                    val renderHeight = maxOf(calculatedHeight + 120, 800).coerceAtMost(8000)

                    view.layout(0, 0, targetWidth, renderHeight)

                    val bitmap = Bitmap.createBitmap(targetWidth, renderHeight, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    view.draw(canvas)

                    activeScreenshotWebViews.remove(webView)
                    saveBitmapAndNotify(
                      context = runContext,
                      bitmap = bitmap,
                      title = title,
                      showToast = showToast,
                      onSuccess = onSuccess
                    )
                  } catch (_: Exception) {
                    activeScreenshotWebViews.remove(webView)
                    if (fallbackBitmap != null) {
                      saveBitmapAndNotify(runContext, fallbackBitmap, title, showToast, onSuccess)
                    } else {
                      captureAndSaveScreenshot(runContext, null, title, showToast, onSuccess)
                    }
                  }
                }
              } catch (e: Exception) {
                activeScreenshotWebViews.remove(webView)
                if (fallbackBitmap != null) {
                  saveBitmapAndNotify(runContext, fallbackBitmap, title, showToast, onSuccess)
                } else {
                  captureAndSaveScreenshot(runContext, null, title, showToast, onSuccess)
                }
              }
            }, 350)
          }
        }

        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
      } catch (e: Exception) {
        if (fallbackBitmap != null) {
          saveBitmapAndNotify(runContext, fallbackBitmap, title, showToast, onSuccess)
        } else {
          captureAndSaveScreenshot(runContext, null, title, showToast, onSuccess)
        }
      }
    }
  }

  fun findActivity(context: Context): Activity? {
    var ctx = context
    while (ctx is ContextWrapper) {
      if (ctx is Activity) return ctx
      ctx = ctx.baseContext
    }
    return null
  }

  /**
   * Trims excessive outer white/blank background around the invoice/statement card,
   * completely eliminating trailing bottom white space and awkward side gaps.
   */
  fun autoCropWhitespace(source: Bitmap, margin: Int = 24): Bitmap {
    if (source.isRecycled || source.width <= 100 || source.height <= 100) return source
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && source.config == Bitmap.Config.HARDWARE) {
      return source
    }

    val width = source.width
    val height = source.height

    // Sample corner pixels to recognize background color (usually #FFFFFF)
    val c0 = source.getPixel(0, 0)
    val c1 = source.getPixel(width - 1, 0)
    val c2 = source.getPixel(0, height - 1)
    val c3 = source.getPixel(width - 1, height - 1)

    fun isBackground(pixel: Int): Boolean {
      val a = (pixel ushr 24) and 0xFF
      if (a < 10) return true
      val r = (pixel ushr 16) and 0xFF
      val g = (pixel ushr 8) and 0xFF
      val b = pixel and 0xFF
      // Near white check
      if (r > 248 && g > 248 && b > 248) return true

      // Corner match check in case of off-white background
      for (corner in intArrayOf(c0, c1, c2, c3)) {
        val cr = (corner ushr 16) and 0xFF
        val cg = (corner ushr 8) and 0xFF
        val cb = corner and 0xFF
        if (kotlin.math.abs(r - cr) < 8 && kotlin.math.abs(g - cg) < 8 && kotlin.math.abs(b - cb) < 8) {
          return true
        }
      }
      return false
    }

    val rowPixels = IntArray(width)

    // 1. Scan from bottom up to find lastContentY
    var lastContentY = height - 1
    var foundBottom = false
    for (y in height - 1 downTo 0) {
      source.getPixels(rowPixels, 0, width, 0, y, width, 1)
      for (x in 0 until width step 2) {
        if (!isBackground(rowPixels[x])) {
          lastContentY = y
          foundBottom = true
          break
        }
      }
      if (foundBottom) break
    }

    // 2. Scan from top down to find firstContentY
    var firstContentY = 0
    var foundTop = false
    for (y in 0..lastContentY) {
      source.getPixels(rowPixels, 0, width, 0, y, width, 1)
      for (x in 0 until width step 2) {
        if (!isBackground(rowPixels[x])) {
          firstContentY = y
          foundTop = true
          break
        }
      }
      if (foundTop) break
    }

    if (!foundBottom || !foundTop || firstContentY >= lastContentY) {
      return source
    }

    // 3. Scan within detected vertical range to find firstContentX and lastContentX
    var firstContentX = width - 1
    var lastContentX = 0
    val contentSpanY = lastContentY - firstContentY + 1
    val stepY = (contentSpanY / 150).coerceAtLeast(1)

    for (y in firstContentY..lastContentY step stepY) {
      source.getPixels(rowPixels, 0, width, 0, y, width, 1)
      for (x in 0 until width) {
        if (!isBackground(rowPixels[x])) {
          if (x < firstContentX) firstContentX = x
          if (x > lastContentX) lastContentX = x
        }
      }
    }

    if (firstContentX >= lastContentX) {
      return source
    }

    val safeMargin = margin.coerceIn(8, 48)
    val cropLeft = (firstContentX - safeMargin).coerceAtLeast(0)
    val cropTop = (firstContentY - safeMargin).coerceAtLeast(0)
    val cropRight = (lastContentX + safeMargin).coerceAtMost(width)
    val cropBottom = (lastContentY + safeMargin).coerceAtMost(height)

    val cropW = cropRight - cropLeft
    val cropH = cropBottom - cropTop

    if (cropW < 50 || cropH < 50 || (cropW == width && cropH == height)) {
      return source
    }

    return try {
      Bitmap.createBitmap(source, cropLeft, cropTop, cropW, cropH)
    } catch (_: Exception) {
      source
    }
  }

  fun saveBitmapAndNotify(
    context: Context,
    bitmap: Bitmap,
    title: String = "معاينة",
    showToast: Boolean = true,
    onSuccess: (Uri?) -> Unit = {},
    autoCrop: Boolean = true
  ) {
    val density = context.resources.displayMetrics.density
    val marginPx = (20 * density).toInt().coerceIn(16, 40)
    val finalBitmap = if (autoCrop) autoCropWhitespace(bitmap, marginPx) else bitmap
    val activity = findActivity(context)
    val savedUri = saveBitmapToGallery(context, finalBitmap, title)
    val runContext = activity ?: context
    if (activity != null) {
      activity.runOnUiThread {
        if (showToast) {
          Toast.makeText(
            runContext,
            "📸 تم حفظ لقطة الشاشة لـ ($title) في الصور بنجاح!",
            Toast.LENGTH_LONG
          ).show()
        }
        onSuccess(savedUri)
      }
    } else {
      if (showToast) {
        Toast.makeText(
          runContext,
          "📸 تم حفظ لقطة الشاشة لـ ($title) في الصور بنجاح!",
          Toast.LENGTH_LONG
        ).show()
      }
      onSuccess(savedUri)
    }
  }

  fun captureAndSaveScreenshot(
    context: Context,
    targetRectInWindow: Rect? = null,
    title: String = "معاينة",
    showToast: Boolean = true,
    onSuccess: (Uri?) -> Unit = {}
  ) {
    val activity = findActivity(context)
    if (activity == null) {
      if (showToast) {
        Toast.makeText(context, "⚠️ تعذر التقاط لقطة الشاشة (النافذة غير متاحة)", Toast.LENGTH_SHORT).show()
      }
      onSuccess(null)
      return
    }

    val window = activity.window
    val decorView = window?.decorView
    if (decorView == null || decorView.width <= 0 || decorView.height <= 0) {
      if (showToast) {
        Toast.makeText(context, "⚠️ تعذر التقاط لقطة الشاشة (أبعاد الشاشة غير مكتملة)", Toast.LENGTH_SHORT).show()
      }
      onSuccess(null)
      return
    }

    val width = decorView.width
    val height = decorView.height

    // Calculate crop rectangle if specified
    val cropRect: Rect = if (targetRectInWindow != null && targetRectInWindow.width() > 10 && targetRectInWindow.height() > 10) {
      Rect(
        targetRectInWindow.left.coerceIn(0, width - 1),
        targetRectInWindow.top.coerceIn(0, height - 1),
        targetRectInWindow.right.coerceIn(1, width),
        targetRectInWindow.bottom.coerceIn(1, height)
      )
    } else {
      val location = IntArray(2)
      decorView.getLocationInWindow(location)
      Rect(location[0], location[1], location[0] + width, location[1] + height)
    }

    val targetWidth = cropRect.width().coerceAtLeast(1)
    val targetHeight = cropRect.height().coerceAtLeast(1)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      try {
        val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)

        PixelCopy.request(
          window,
          cropRect,
          bitmap,
          { copyResult ->
            if (copyResult == PixelCopy.SUCCESS) {
              saveBitmapAndNotify(activity, bitmap, title, showToast, onSuccess)
            } else {
              // Fallback to view draw
              val fallbackBitmap = captureViewFallback(decorView, cropRect)
              saveBitmapAndNotify(activity, fallbackBitmap, title, showToast, onSuccess)
            }
          },
          Handler(Looper.getMainLooper())
        )
      } catch (e: Exception) {
        val fallbackBitmap = captureViewFallback(decorView, cropRect)
        saveBitmapAndNotify(activity, fallbackBitmap, title, showToast, onSuccess)
      }
    } else {
      val fallbackBitmap = captureViewFallback(decorView, cropRect)
      saveBitmapAndNotify(activity, fallbackBitmap, title, showToast, onSuccess)
    }
  }

  // Overload for backward compatibility when only title is passed
  fun captureAndSaveScreenshot(
    context: Context,
    title: String,
    showToast: Boolean = true
  ) {
    captureAndSaveScreenshot(context, null, title, showToast) {}
  }

  private fun captureViewFallback(view: View, cropRect: Rect? = null): Bitmap {
    val fullBitmap = Bitmap.createBitmap(
      view.width.coerceAtLeast(1),
      view.height.coerceAtLeast(1),
      Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(fullBitmap)
    view.draw(canvas)

    if (cropRect != null && cropRect.width() > 0 && cropRect.height() > 0) {
      val safeX = cropRect.left.coerceIn(0, fullBitmap.width - 1)
      val safeY = cropRect.top.coerceIn(0, fullBitmap.height - 1)
      val safeW = cropRect.width().coerceAtMost(fullBitmap.width - safeX)
      val safeH = cropRect.height().coerceAtMost(fullBitmap.height - safeY)
      if (safeW > 0 && safeH > 0) {
        return Bitmap.createBitmap(fullBitmap, safeX, safeY, safeW, safeH)
      }
    }
    return fullBitmap
  }

  private fun saveBitmapToGallery(context: Context, bitmap: Bitmap, title: String): Uri? {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
    val cleanTitle = title.replace(Regex("[^a-zA-Z0-9ء-ي_]"), "_")
    val fileName = "Screenshot_${cleanTitle}_$timeStamp.png"

    val bmpToSave = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && bitmap.config == Bitmap.Config.HARDWARE) {
      try {
        bitmap.copy(Bitmap.Config.ARGB_8888, false) ?: bitmap
      } catch (_: Exception) {
        bitmap
      }
    } else {
      bitmap
    }

    return try {
      val resolver = context.contentResolver
      val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/المملكة")
          put(MediaStore.Images.Media.IS_PENDING, 1)
        }
      }

      val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
      if (uri != null) {
        resolver.openOutputStream(uri)?.use { out ->
          bmpToSave.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          contentValues.clear()
          contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
          resolver.update(uri, contentValues, null, null)
        }
        uri
      } else {
        // Fallback to app external pictures directory
        val picturesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: context.filesDir
        val file = File(picturesDir, fileName)
        FileOutputStream(file).use { out ->
          bmpToSave.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        Uri.fromFile(file)
      }
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }
}
