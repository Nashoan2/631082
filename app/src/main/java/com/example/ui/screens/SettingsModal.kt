package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.ripple
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.ExchangeRates
import com.example.data.StoreConfig
import com.example.ui.components.AlmamlakaLogoBadge
import com.example.ui.theme.computeShadedBorderColor
import com.example.ui.theme.mandatoryTextFieldColors
import com.example.ui.theme.parseHexColor
import com.example.ui.theme.standardAppTextFieldColors
import com.example.ui.viewmodel.InvoiceViewModel
import com.example.util.ArabicNumberHelper
import java.io.ByteArrayOutputStream

private fun processLogoUri(context: Context, uri: Uri): String? {
  return try {
    context.contentResolver.openInputStream(uri)?.use { stream ->
      val original = BitmapFactory.decodeStream(stream) ?: return null
      val maxDim = 512
      val scaled = if (original.width > maxDim || original.height > maxDim) {
        val ratio = minOf(maxDim.toFloat() / original.width, maxDim.toFloat() / original.height)
        Bitmap.createScaledBitmap(original, (original.width * ratio).toInt(), (original.height * ratio).toInt(), true)
      } else {
        original
      }
      val baos = ByteArrayOutputStream()
      scaled.compress(Bitmap.CompressFormat.PNG, 95, baos)
      val bytes = baos.toByteArray()
      Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
  } catch (_: Exception) {
    null
  }
}

/**
 * Custom 3 horizontal pill bars matching item 5 in the design screenshot
 */
@Composable
fun DatabaseStackIcon(modifier: Modifier = Modifier, tint: Color = Color.White) {
  Canvas(modifier = modifier.size(32.dp)) {
    val w = size.width
    val h = size.height
    val pillW = w * 0.78f
    val pillH = h * 0.19f
    val startX = (w - pillW) / 2f
    val yPositions = listOf(h * 0.16f, h * 0.41f, h * 0.66f)
    for (y in yPositions) {
      drawRoundRect(
        color = tint,
        topLeft = Offset(startX, y),
        size = Size(pillW, pillH),
        cornerRadius = CornerRadius(pillH / 2f, pillH / 2f)
      )
    }
  }
}

enum class SettingsCardType {
  STORE_INFO,
  UI_CUSTOMIZE,
  THEME_CUSTOMIZE,
  FIELD_SHADING,
  REPORTS_PRINTING,
  BACKUP_RESTORE,
  EXCHANGE_RATES,
  YEAR_END_CLOSING
}

@Composable
fun SettingsModal(viewModel: InvoiceViewModel, onDismiss: () -> Unit) {
  val uiState by viewModel.uiState.collectAsState()

  var showStoreConfigDialog by remember { mutableStateOf(false) }
  var showExchangeRatesDialog by remember { mutableStateOf(false) }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(
      usePlatformDefaultWidth = false
    )
  ) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            brush = Brush.verticalGradient(
              colors = listOf(
                Color(0xFF040618),
                Color(0xFF070E28),
                Color(0xFF05091E),
                Color(0xFF02040D)
              )
            )
          )
      ) {
        // Ambient upper neon glow effects
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
        ) {
          // Top center violet ambient glow
          Box(
            modifier = Modifier
              .size(240.dp)
              .align(Alignment.TopCenter)
              .offset(y = (-40).dp)
              .background(
                Brush.radialGradient(
                  colors = listOf(
                    Color(0x357C3AED),
                    Color(0x153B82F6),
                    Color.Transparent
                  )
                ),
                CircleShape
              )
          )

          // Glowing curved futuristic halo over the top
          Canvas(
            modifier = Modifier
              .fillMaxWidth()
              .height(130.dp)
          ) {
            val w = size.width
            val h = size.height
            val path = Path().apply {
              moveTo(-w * 0.15f, h * 0.1f)
              quadraticTo(w * 0.5f, h * 0.95f, w * 1.15f, h * 0.1f)
            }
            drawPath(
              path = path,
              brush = Brush.horizontalGradient(
                colors = listOf(
                  Color(0x008B5CF6),
                  Color(0x408B5CF6),
                  Color(0x9938BDF8),
                  Color(0x408B5CF6),
                  Color(0x008B5CF6)
                )
              ),
              style = Stroke(width = 3.dp.toPx())
            )
          }
        }

        Column(
          modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
        ) {
          // ==================== TOP BAR & HEADER ====================
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            // Action Buttons Row (Gear/Back on Left, Close on Right)
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Gear Icon Badge / Button (Left)
              val gearInteraction = remember { MutableInteractionSource() }
              val gearPressed by gearInteraction.collectIsPressedAsState()
              val gearScale by animateFloatAsState(
                targetValue = if (gearPressed) 0.88f else 1.0f,
                animationSpec = spring(stiffness = Spring.StiffnessHigh)
              )
              Box(
                modifier = Modifier
                  .size(48.dp)
                  .graphicsLayer {
                    scaleX = gearScale
                    scaleY = gearScale
                  }
                  .shadow(
                    elevation = if (gearPressed) 3.dp else 8.dp,
                    shape = RoundedCornerShape(14.dp),
                    spotColor = Color(0xFF38BDF8),
                    ambientColor = Color(0xFF1E3A8A)
                  )
                  .clip(RoundedCornerShape(14.dp))
                  .background(
                    Brush.verticalGradient(
                      listOf(Color(0xFF1E3A8A), Color(0xFF0F172A))
                    )
                  )
                  .border(
                    BorderStroke(
                      1.6.dp,
                      Brush.horizontalGradient(
                        listOf(Color(0xFF38BDF8), Color(0xFF2563EB), Color(0xFF38BDF8))
                      )
                    ),
                    RoundedCornerShape(14.dp)
                  )
                  .clickable(
                    interactionSource = gearInteraction,
                    indication = ripple(color = Color(0xFF38BDF8))
                  ) {
                    onDismiss()
                  },
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Settings,
                  contentDescription = "رجوع وإغلاق الإعدادات",
                  tint = Color.White,
                  modifier = Modifier.size(26.dp)
                )
              }

              // Close Icon Button (Right)
              val closeInteraction = remember { MutableInteractionSource() }
              val closePressed by closeInteraction.collectIsPressedAsState()
              val closeScale by animateFloatAsState(
                targetValue = if (closePressed) 0.88f else 1.0f,
                animationSpec = spring(stiffness = Spring.StiffnessHigh)
              )
              Box(
                modifier = Modifier
                  .size(48.dp)
                  .graphicsLayer {
                    scaleX = closeScale
                    scaleY = closeScale
                  }
                  .shadow(
                    elevation = if (closePressed) 3.dp else 8.dp,
                    shape = RoundedCornerShape(14.dp),
                    spotColor = Color(0xFF38BDF8),
                    ambientColor = Color(0xFF1E3A8A)
                  )
                  .clip(RoundedCornerShape(14.dp))
                  .background(
                    Brush.verticalGradient(
                      listOf(Color(0xFF1E3A8A), Color(0xFF0F172A))
                    )
                  )
                  .border(
                    BorderStroke(
                      1.6.dp,
                      Brush.horizontalGradient(
                        listOf(Color(0xFF38BDF8), Color(0xFF2563EB), Color(0xFF38BDF8))
                      )
                    ),
                    RoundedCornerShape(14.dp)
                  )
                  .clickable(
                    interactionSource = closeInteraction,
                    indication = ripple(color = Color(0xFF38BDF8))
                  ) { onDismiss() },
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "إغلاق",
                  tint = Color.White,
                  modifier = Modifier.size(26.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Title: "الإعدادات"
            Text(
              text = "الإعدادات",
              fontSize = 36.sp,
              fontWeight = FontWeight.Black,
              color = Color.White,
              letterSpacing = 0.5.sp,
              style = TextStyle(
                shadow = Shadow(
                  color = Color(0x9938BDF8),
                  offset = Offset(0f, 3f),
                  blurRadius = 10f
                )
              )
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle: "المطور عبد العزيز راجح"
            Text(
              text = "المطور عبد العزيز راجح",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White,
              textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Curved glowing light beam / neon sweep under the header
            Canvas(
              modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .padding(horizontal = 4.dp)
            ) {
              val w = size.width
              val h = size.height
              val path = Path().apply {
                moveTo(0f, 2f)
                quadraticTo(w * 0.5f, h * 0.95f, w, 2f)
              }
              // Outer neon aura
              drawPath(
                path = path,
                brush = Brush.horizontalGradient(
                  colors = listOf(
                    Color(0x0000E5FF),
                    Color(0x887C3AED),
                    Color(0xFF00E5FF),
                    Color(0x887C3AED),
                    Color(0x0000E5FF)
                  )
                ),
                style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
              )
              // Bright sharp core line
              drawPath(
                path = path,
                brush = Brush.horizontalGradient(
                  colors = listOf(
                    Color(0x00FFFFFF),
                    Color(0xCCBAE6FD),
                    Color(0xFFFFFFFF),
                    Color(0xCCBAE6FD),
                    Color(0x00FFFFFF)
                  )
                ),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
              )
            }
          }

          // ==================== SETTINGS CARDS LIST ====================
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp)
          ) {
            // Card 1: بيانات المتجر والشعار
            SettingsCard(
              type = SettingsCardType.STORE_INFO,
              title = "بيانات المتجر والشعار",
              subtitle = "اسم الشركة، الفرع، العنوان، الهاتف، تحميل الشعار والعلامة المائية والشروط والأحكام",
              onClick = { showStoreConfigDialog = true }
            )

            // Card 2: تخصيص أزرار الواجهة الرئيسية
            SettingsCard(
              type = SettingsCardType.UI_CUSTOMIZE,
              title = "تخصيص أزرار الواجهة الرئيسية",
              subtitle = "إعادة ترتيب أزرار الشاشة، التبديل بين صف أو صفين، والتحكم بالحجم",
              onClick = {
                viewModel.setUiCustomizerModalVisible(true)
              }
            )

            // Card 3: تخصيص مظهر وثيم التطبيق
            SettingsCard(
              type = SettingsCardType.THEME_CUSTOMIZE,
              title = "مظهر وثيم التطبيق",
              subtitle = "تغيير الثيم الملكي، الليلي، الكحلي، الزمردي وتخصيص ألوان الواجهة بالكامل",
              onClick = {
                viewModel.setAppThemeCustomizerModalVisible(true)
              }
            )

            // Card 4: تخصيص التقارير والطباعة
            SettingsCard(
              type = SettingsCardType.REPORTS_PRINTING,
              title = "تخصيص التقارير والطباعة",
              subtitle = "التحكم في أحجام الخطوط، تظليل وألوان الحقول، ألوان الجداول، وإظهار تفاصيل الأعمدة",
              onClick = {
                viewModel.setReportCustomizerModalVisible(true)
              }
            )

            // Card 5: النسخة الاحتياطية والاستعادة
            SettingsCard(
              type = SettingsCardType.BACKUP_RESTORE,
              title = "النسخة الاحتياطية والاستعادة",
              subtitle = "النسخ التلقائي اليومي، إدارة واسترجاع نسخ قاعدة بيانات Room محلياً وسحابياً",
              onClick = {
                viewModel.setRoomBackupModalVisible(true)
              }
            )

            // Card 6: تغيير أسعار العملات
            SettingsCard(
              type = SettingsCardType.EXCHANGE_RATES,
              title = "تغيير أسعار العملات",
              subtitle = "إدارة أسعار العملات المستخدمة في التطبيق والصرف الفوري",
              onClick = { showExchangeRatesDialog = true }
            )

            // Card 7: الإقفال السنوي
            SettingsCard(
              type = SettingsCardType.YEAR_END_CLOSING,
              title = "الإقفال السنوي",
              subtitle = "ترحيل العملاء والأرصدة كأرصدة افتتاحية وبدء ترقيم الفواتير والسندات من الرقم 1",
              onClick = {
                viewModel.setYearEndClosingModalVisible(true)
              }
            )

            Spacer(modifier = Modifier.height(20.dp).navigationBarsPadding())
          }
        }
      }
    }

    // ==================== SUB-DIALOGS ====================

    // 1. Store Config Dialog
    if (showStoreConfigDialog) {
      StoreConfigCustomizerDialog(
        viewModel = viewModel,
        onDismiss = { showStoreConfigDialog = false }
      )
    }

    // 2. Exchange Rates Dialog
    if (showExchangeRatesDialog) {
      ExchangeRatesCustomizerDialog(
        viewModel = viewModel,
        onDismiss = { showExchangeRatesDialog = false }
      )
    }

  }
}

/**
 * Settings Card Component matching the visual styling of الاعدادات.png
 */
@Composable
fun SettingsCard(
  type: SettingsCardType,
  title: String,
  subtitle: String,
  onClick: () -> Unit
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.965f else 1.0f,
    animationSpec = spring(stiffness = Spring.StiffnessHigh)
  )

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .graphicsLayer {
        scaleX = scale
        scaleY = scale
      }
      .shadow(
        elevation = if (isPressed) 3.dp else 10.dp,
        shape = RoundedCornerShape(22.dp),
        ambientColor = Color(0xFF0077B6),
        spotColor = Color(0xFF00D2FF)
      )
      .clip(RoundedCornerShape(22.dp))
      .clickable(
        interactionSource = interactionSource,
        indication = ripple(color = Color(0xFF38BDF8))
      ) { onClick() },
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    border = BorderStroke(
      if (isPressed) 2.2.dp else 1.8.dp,
      Brush.horizontalGradient(
        listOf(
          Color(0xFF00E5FF),
          Color(0xFF2563EB),
          Color(0xFF00D2FF),
          Color(0xFF00E5FF)
        )
      )
    )
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          brush = Brush.verticalGradient(
            colors = listOf(
              Color(0xFF071233),
              Color(0xFF03091F),
              Color(0xFF020515)
            )
          )
        )
        .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // --- 1. Left Icon Container (Vibrant 3D Purple/Violet Gradient Glossy Box) ---
        Box(
          modifier = Modifier
            .size(64.dp)
            .shadow(
              elevation = 8.dp,
              shape = RoundedCornerShape(18.dp),
              spotColor = Color(0xFFA855F7),
              ambientColor = Color(0xFF4C1D95)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(
              brush = Brush.verticalGradient(
                colors = listOf(
                  Color(0xFFB55FE6),
                  Color(0xFF8B2FC9),
                  Color(0xFF4C1D95)
                )
              )
            )
            .border(
              BorderStroke(
                1.2.dp,
                Brush.verticalGradient(
                  listOf(Color(0x88FFFFFF), Color(0x228B2FC9))
                )
              ),
              RoundedCornerShape(18.dp)
            ),
          contentAlignment = Alignment.Center
        ) {
          when (type) {
            SettingsCardType.STORE_INFO -> {
              Icon(
                imageVector = Icons.Default.Storefront,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(34.dp)
              )
            }
            SettingsCardType.UI_CUSTOMIZE -> {
              Icon(
                imageVector = Icons.Default.Brush,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
              )
            }
            SettingsCardType.THEME_CUSTOMIZE -> {
              Icon(
                imageVector = Icons.Default.Palette,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(34.dp)
              )
            }
            SettingsCardType.FIELD_SHADING -> {
              Icon(
                imageVector = Icons.Default.WaterDrop,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(34.dp)
              )
            }
            SettingsCardType.REPORTS_PRINTING -> {
              Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
              )
            }
            SettingsCardType.BACKUP_RESTORE -> {
              DatabaseStackIcon(tint = Color.White)
            }
            SettingsCardType.EXCHANGE_RATES -> {
              Text(
                text = "$",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center
              )
            }
            SettingsCardType.YEAR_END_CLOSING -> {
              Icon(
                imageVector = Icons.Default.LockReset,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(34.dp)
              )
            }
          }
        }

        // --- 2. Middle Text Block (Title & Subtitle, Arabic Right-Aligned) ---
        Column(
          modifier = Modifier
            .weight(1f)
            .padding(start = 12.dp, end = 10.dp),
          horizontalAlignment = Alignment.End
        ) {
          Text(
            text = title,
            fontSize = 17.5.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Right,
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = subtitle,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFFCAD5E8),
            lineHeight = 16.sp,
            textAlign = TextAlign.Right,
            modifier = Modifier.fillMaxWidth()
          )
        }

        // --- 3. Right Chevron Arrow (Vibrant Glowing Magenta/Purple Arrow) ---
        Icon(
          imageVector = Icons.Default.ChevronRight,
          contentDescription = null,
          tint = Color(0xFFE879F9),
          modifier = Modifier.size(28.dp)
        )
      }
    }
  }
}

/**
 * Store Info & Logo Dialog
 */
@Composable
private fun StoreConfigCustomizerDialog(
  viewModel: InvoiceViewModel,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsState()

  var storeNameAr by remember(uiState.storeConfig.storeNameAr) { mutableStateOf(uiState.storeConfig.storeNameAr) }
  var storeNameEn by remember(uiState.storeConfig.storeNameEn) { mutableStateOf(uiState.storeConfig.storeNameEn) }
  var addressAr by remember(uiState.storeConfig.addressAr) { mutableStateOf(uiState.storeConfig.addressAr) }
  var addressEn by remember(uiState.storeConfig.addressEn) { mutableStateOf(uiState.storeConfig.addressEn) }
  var phone by remember(uiState.storeConfig.phone) { mutableStateOf(uiState.storeConfig.phone) }
  var branch by remember(uiState.storeConfig.branch) { mutableStateOf(uiState.storeConfig.branch) }
  var wmAr by remember(uiState.storeConfig.wmAr) { mutableStateOf(uiState.storeConfig.wmAr) }
  var terms by remember(uiState.storeConfig.terms) {
    mutableStateOf(
      uiState.storeConfig.terms.ifBlank {
        "• البضاعة المباعة لا ترد ولا تستبدل بعد خروجها من المحل.\n• استلمت البضاعة الموضحة أعلاه كاملة ، سليمة ، ولعدد ذلك."
      }
    )
  }
  var logoBase64 by remember(uiState.storeConfig.logoBase64) { mutableStateOf(uiState.storeConfig.logoBase64) }

  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    if (uri != null) {
      val base64 = processLogoUri(context, uri)
      if (!base64.isNullOrBlank()) {
        logoBase64 = base64
        viewModel.updateStoreLogo(base64)
      } else {
        viewModel.showToast("❌ تعذر قراءة صورة الشعار.")
      }
    }
  }

  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    Dialog(
      onDismissRequest = onDismiss,
      properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
      Card(
        modifier = Modifier
          .fillMaxWidth(0.95f)
          .fillMaxSize(0.92f)
          .padding(vertical = 12.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
        ) {
          // Dialog Header
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(42.dp)
                  .background(
                    Brush.verticalGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9))),
                    RoundedCornerShape(12.dp)
                  ),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Storefront,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(24.dp)
                )
              }
              Column {
                Text(
                  text = "بيانات المتجر والشعار",
                  fontSize = 17.sp,
                  fontWeight = FontWeight.Black,
                  color = Color(0xFF1E1B4B)
                )
                Text(
                  text = "إدارة تفاصيل المحل والترويسة والشروط",
                  fontSize = 11.5.sp,
                  color = Color.Gray
                )
              }
            }

            IconButton(onClick = onDismiss) {
              Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.Gray)
            }
          }

          HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

          // Scrollable Form
          Column(
            modifier = Modifier
              .weight(1f)
              .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            // Logo Card
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F6FE)),
              border = BorderStroke(1.2.dp, Color(0xFFD8B4FE))
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Text(
                  text = "🖼️ شعار الشركة / المتجر",
                  fontWeight = FontWeight.ExtraBold,
                  fontSize = 14.sp,
                  color = Color(0xFF6D28D9)
                )

                AlmamlakaLogoBadge(size = 90.dp, logoBase64 = logoBase64)

                Text(
                  text = if (logoBase64.isNotBlank()) "✅ تم تحميل واستخدام شعار مخصص" else "📌 الشعار الحالي: الافتراضي للمملكة للإلكترونيات",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (logoBase64.isNotBlank()) Color(0xFF16A34A) else Color(0xFF6D28D9)
                )

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Button(
                    onClick = {
                      photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                      )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                  ) {
                    Text("📁 اختيار وتغيير الشعار", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = Color.White)
                  }

                  if (logoBase64.isNotBlank()) {
                    OutlinedButton(
                      onClick = {
                        logoBase64 = ""
                        viewModel.resetStoreLogo()
                      },
                      shape = RoundedCornerShape(10.dp)
                    ) {
                      Text("استعادة الافتراضي", fontSize = 11.5.sp, color = Color(0xFFDC2626))
                    }
                  }
                }
              }
            }

            // Input Fields
            OutlinedTextField(
              value = storeNameAr,
              onValueChange = { storeNameAr = it },
              label = { Text("اسم المحل / الشركة (عربي) *", fontWeight = FontWeight.ExtraBold) },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp),
              textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black),
              colors = standardAppTextFieldColors()
            )

            OutlinedTextField(
              value = storeNameEn,
              onValueChange = { storeNameEn = it },
              label = { Text("اسم المحل / الشركة (إنجليزي) *", fontWeight = FontWeight.ExtraBold) },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp),
              textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black),
              colors = standardAppTextFieldColors()
            )

            OutlinedTextField(
              value = addressAr,
              onValueChange = { addressAr = it },
              label = { Text("العنوان (عربي) *", fontWeight = FontWeight.ExtraBold) },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp),
              textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black),
              colors = standardAppTextFieldColors()
            )

            OutlinedTextField(
              value = addressEn,
              onValueChange = { addressEn = it },
              label = { Text("العنوان (إنجليزي)", fontWeight = FontWeight.ExtraBold) },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp),
              textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black),
              colors = standardAppTextFieldColors()
            )

            OutlinedTextField(
              value = phone,
              onValueChange = { phone = it },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
              label = { Text("رقم الهاتف / الجوال *", fontWeight = FontWeight.ExtraBold) },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp),
              textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black),
              colors = standardAppTextFieldColors()
            )

            OutlinedTextField(
              value = branch,
              onValueChange = { branch = it },
              label = { Text("الفرع", fontWeight = FontWeight.ExtraBold) },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp),
              textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black),
              colors = standardAppTextFieldColors()
            )

            OutlinedTextField(
              value = wmAr,
              onValueChange = { wmAr = it },
              label = { Text("العلامة المائية (عربي)", fontWeight = FontWeight.ExtraBold) },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp),
              textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black),
              colors = standardAppTextFieldColors()
            )

            OutlinedTextField(
              value = terms,
              onValueChange = { terms = it },
              label = { Text("الشروط والأحكام أسفل الفواتير والتقارير", fontWeight = FontWeight.ExtraBold) },
              modifier = Modifier.fillMaxWidth(),
              minLines = 3,
              shape = RoundedCornerShape(10.dp),
              textStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.Black),
              colors = standardAppTextFieldColors()
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.End
            ) {
              androidx.compose.material3.TextButton(
                onClick = {
                  terms = "• البضاعة المباعة لا ترد ولا تستبدل بعد خروجها من المحل.\n• استلمت البضاعة الموضحة أعلاه كاملة ، سليمة ، ولعدد ذلك."
                }
              ) {
                Text(
                  "🔄 استعادة الشروط الافتراضية",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF6D28D9)
                )
              }
            }
          }

          HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

          // Action Buttons
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedButton(
              onClick = onDismiss,
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.weight(1f).height(48.dp)
            ) {
              Text("إلغاء", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            }

            Button(
              onClick = {
                val updated = StoreConfig(
                  storeNameAr = storeNameAr,
                  storeNameEn = storeNameEn,
                  addressAr = addressAr,
                  addressEn = addressEn,
                  phone = phone,
                  branch = branch,
                  wmAr = wmAr,
                  terms = terms,
                  logoBase64 = logoBase64
                )
                viewModel.updateStoreConfig(updated)
                viewModel.showToast("✅ تم حفظ بيانات المتجر بنجاح!")
                onDismiss()
              },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9)),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.weight(1.5f).height(48.dp)
            ) {
              Text("💾 حفظ التغييرات", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.White)
            }
          }
        }
      }
    }
  }
}

/**
 * Currency Exchange Rates Dialog
 */
@Composable
private fun ExchangeRatesCustomizerDialog(
  viewModel: InvoiceViewModel,
  onDismiss: () -> Unit
) {
  val uiState by viewModel.uiState.collectAsState()

  var usdToYer by remember { mutableStateOf(uiState.exchangeRates.usdToYer.toString()) }
  var sarToYer by remember { mutableStateOf(uiState.exchangeRates.sarToYer.toString()) }
  var usdToSar by remember { mutableStateOf(uiState.exchangeRates.usdToSar.toString()) }

  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    Dialog(
      onDismissRequest = onDismiss,
      properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
      Card(
        modifier = Modifier
          .fillMaxWidth(0.92f)
          .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          // Header
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(42.dp)
                  .background(
                    Brush.verticalGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9))),
                    RoundedCornerShape(12.dp)
                  ),
                contentAlignment = Alignment.Center
              ) {
                Text("$", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
              }
              Column {
                Text(
                  text = "تغيير أسعار العملات",
                  fontSize = 17.sp,
                  fontWeight = FontWeight.Black,
                  color = Color(0xFF1E1B4B)
                )
                Text(
                  text = "إدارة وتحديث أسعار الصرف في التطبيق",
                  fontSize = 11.5.sp,
                  color = Color.Gray
                )
              }
            }

            IconButton(onClick = onDismiss) {
              Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.Gray)
            }
          }

          HorizontalDivider()

          OutlinedTextField(
            value = usdToYer,
            onValueChange = { usdToYer = it },
            label = { Text("سعر الدولار (USD) مقابل الريال اليمني (YER)", fontWeight = FontWeight.Bold) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(10.dp),
            textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827)),
            colors = standardAppTextFieldColors()
          )

          OutlinedTextField(
            value = sarToYer,
            onValueChange = { sarToYer = it },
            label = { Text("سعر الريال السعودي (SAR) مقابل الريال اليمني (YER)", fontWeight = FontWeight.Bold) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(10.dp),
            textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827)),
            colors = standardAppTextFieldColors()
          )

          OutlinedTextField(
            value = usdToSar,
            onValueChange = { usdToSar = it },
            label = { Text("سعر الدولار (USD) مقابل الريال السعودي (SAR)", fontWeight = FontWeight.Bold) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(10.dp),
            textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827)),
            colors = standardAppTextFieldColors()
          )

          HorizontalDivider()

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedButton(
              onClick = onDismiss,
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.weight(1f).height(48.dp)
            ) {
              Text("إلغاء", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            }

            Button(
              onClick = {
                val cleanU2Y = ArabicNumberHelper.toEngDigits(usdToYer).trim()
                val cleanS2Y = ArabicNumberHelper.toEngDigits(sarToYer).trim()
                val cleanU2S = ArabicNumberHelper.toEngDigits(usdToSar).trim()
                val u2y = cleanU2Y.toDoubleOrNull() ?: 1000.0
                val s2y = cleanS2Y.toDoubleOrNull() ?: 370.0
                val u2s = cleanU2S.toDoubleOrNull() ?: 3.75
                val rates = ExchangeRates(
                  usdToYer = u2y,
                  sarToYer = s2y,
                  usdToSar = u2s,
                  yerToUsd = if (u2y > 0) 1.0 / u2y else 0.001,
                  yerToSar = if (s2y > 0) 1.0 / s2y else 0.0027,
                  sarToUsd = if (u2s > 0) 1.0 / u2s else 0.2667
                )
                viewModel.updateExchangeRates(rates)
                viewModel.showToast("✅ تم حفظ وتطبيق أسعار العملات الجديدة!")
                onDismiss()
              },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9)),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.weight(1.5f).height(48.dp)
            ) {
              Text("💾 حفظ الأسعار", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.White)
            }
          }
        }
      }
    }
  }
}



val SHADED_FIELD_COLOR_PALETTE = listOf(
  "#FFF0F3" to "وردي ناعم (الافتراضي)",
  "#FCE4EC" to "وردي فاتح",
  "#FFF9E6" to "أصفر هادئ",
  "#FFFDE7" to "أصفر ناعم",
  "#FFF8E1" to "عنبري خفيف",
  "#FFF3E0" to "برتقالي باستيل",
  "#FBE9E7" to "مرجاني ناعم",
  "#E8F5E9" to "أخضر نعناعي",
  "#E0F2F1" to "فيروزي فاتح",
  "#E1F5FE" to "سماوي هادئ",
  "#E8EAF6" to "نيلي ناعم",
  "#F3E5F5" to "لافندر بنفسجي",
  "#F5F5F5" to "رمادي فاتح"
)

@Composable
fun ShadedFieldColorCustomizerDialog(
  initialHex: String,
  initialAlpha: Float,
  onDismiss: () -> Unit,
  onSave: (String, Float) -> Unit,
  onReset: () -> Unit
) {
  var selectedHex by remember { mutableStateOf(initialHex) }
  var alphaValue by remember { mutableStateOf(initialAlpha.coerceIn(0.05f, 1.0f)) }

  val previewColor = remember(selectedHex, alphaValue) {
    val base = parseHexColor(selectedHex, Color(0xFFFFF0F3))
    base.copy(alpha = alphaValue)
  }
  val previewBorder = remember(previewColor) {
    computeShadedBorderColor(previewColor)
  }

  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    Dialog(
      onDismissRequest = onDismiss,
      properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
      Card(
        modifier = Modifier
          .fillMaxWidth(0.94f)
          .padding(vertical = 16.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp)
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          // Dialog Title Bar
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .background(
                    Brush.verticalGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9))),
                    RoundedCornerShape(12.dp)
                  ),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  Icons.Default.WaterDrop,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(22.dp)
                )
              }
              Column {
                Text(
                  "تغيير ألوان وتظليل الحقول",
                  fontWeight = FontWeight.Black,
                  fontSize = 16.sp,
                  color = Color(0xFF1E1B4B)
                )
                Text(
                  "تخصيص لون الحقول المظللة فقط ودرجة شفافيتها",
                  fontSize = 11.sp,
                  color = Color.Gray
                )
              }
            }

            IconButton(onClick = onDismiss) {
              Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.Gray)
            }
          }

          HorizontalDivider()

          // Live Preview Box
          Text(
            "معاينة شكل الحقل المظلل المخصص:",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = Color(0xFF333333)
          )

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .background(previewColor, RoundedCornerShape(10.dp))
              .border(1.5.dp, previewBorder, RoundedCornerShape(10.dp))
              .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
          ) {
            Text(
              "نص تجريبي داخل الحقل المظلل (المعاينة الحية)",
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium,
              color = Color(0xFF333333)
            )
          }

          // Palette Selection
          Text(
            "اختر لون التظليل:",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = Color(0xFF333333)
          )

          LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            items(SHADED_FIELD_COLOR_PALETTE) { (hex, name) ->
              val isSelected = selectedHex.equals(hex, ignoreCase = true)
              val chipColor = parseHexColor(hex)

              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .clickable { selectedHex = hex }
                  .padding(4.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(40.dp)
                    .background(chipColor, CircleShape)
                    .border(
                      width = if (isSelected) 2.5.dp else 1.dp,
                      color = if (isSelected) Color(0xFF7C3AED) else Color(0xFFCCCCCC),
                      shape = CircleShape
                    ),
                  contentAlignment = Alignment.Center
                ) {
                  if (isSelected) {
                    Icon(
                      Icons.Default.Check,
                      contentDescription = "تم الاختيار",
                      tint = Color(0xFF6D28D9),
                      modifier = Modifier.size(20.dp)
                    )
                  }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = name.split(" ").firstOrNull() ?: "",
                  fontSize = 10.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  color = if (isSelected) Color(0xFF6D28D9) else Color.DarkGray
                )
              }
            }
          }

          // Transparency / Opacity Slider
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                "الشفافية (Opacity):",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFF333333)
              )
              Text(
                "${(alphaValue * 100).toInt()}%",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                color = Color(0xFF6D28D9)
              )
            }

            Slider(
              value = alphaValue,
              onValueChange = { alphaValue = it },
              valueRange = 0.10f..1.0f,
              steps = 18,
              colors = SliderDefaults.colors(
                thumbColor = Color(0xFF6D28D9),
                activeTrackColor = Color(0xFF7C3AED),
                inactiveTrackColor = Color(0xFFDDD6FE)
              )
            )
          }

          HorizontalDivider()

          // Action Buttons: Reset, Cancel, Save
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedButton(
              onClick = onReset,
              colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.weight(1f).height(46.dp)
            ) {
              Icon(
                Icons.Default.RestartAlt,
                contentDescription = null,
                modifier = Modifier.size(16.dp).padding(end = 4.dp)
              )
              Text("استعادة الافتراضي", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
              onClick = { onSave(selectedHex, alphaValue) },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9)),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.weight(1f).height(46.dp)
            ) {
              Text("💾 حفظ وتطبيق", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
          }
        }
      }
    }
  }
}
