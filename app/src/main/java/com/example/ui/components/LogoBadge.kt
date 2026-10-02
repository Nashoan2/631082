package com.example.ui.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R

@Composable
fun AlmamlakaLogoBadge(
  modifier: Modifier = Modifier,
  size: Dp = 90.dp,
  logoBase64: String = ""
) {
  val customBitmap = remember(logoBase64) {
    if (logoBase64.isNotBlank()) {
      try {
        val decoded = Base64.decode(logoBase64, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decoded, 0, decoded.size)?.asImageBitmap()
      } catch (_: Exception) {
        null
      }
    } else null
  }

  Box(
    modifier = modifier
      .size(size)
      .clip(CircleShape)
      .background(Color(0xFF5E258D))
      .border(3.dp, Color(0xFFDCDCDC), CircleShape)
      .border(5.dp, Color(0xFF5E258D), CircleShape),
    contentAlignment = Alignment.Center
  ) {
    if (customBitmap != null) {
      Image(
        bitmap = customBitmap,
        contentDescription = "Company Logo",
        modifier = Modifier
          .fillMaxSize()
          .clip(CircleShape),
        contentScale = ContentScale.Crop
      )
    } else {
      Image(
        painter = painterResource(id = R.drawable.app_logo),
        contentDescription = "Almamlaka Logo",
        modifier = Modifier
          .fillMaxSize()
          .clip(CircleShape),
        contentScale = ContentScale.Crop
      )
    }
  }
}

