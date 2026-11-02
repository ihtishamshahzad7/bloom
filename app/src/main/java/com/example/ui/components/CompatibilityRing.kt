package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BloomCandleGold
import com.example.ui.theme.BloomRose

@Composable
fun CompatibilityRing(
  percentage: Int,
  modifier: Modifier = Modifier,
  size: Dp = 54.dp,
  strokeWidth: Dp = 4.5.dp,
  showText: Boolean = true
) {
  val animatedProgress by animateFloatAsState(
    targetValue = percentage / 100f,
    animationSpec = tween(durationMillis = 1000),
    label = "compat_ring"
  )

  Box(
    modifier = modifier.size(size),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.size(size)) {
      val strokePx = strokeWidth.toPx()
      val arcSize = this.size.width - strokePx

      // Track background
      drawArc(
        color = Color(0x26E85D8A),
        startAngle = -90f,
        sweepAngle = 360f,
        useCenter = false,
        style = Stroke(width = strokePx, cap = StrokeCap.Round)
      )

      // Active gradient ring
      val gradientBrush = Brush.sweepGradient(
        colors = listOf(
          BloomRose,
          BloomCandleGold,
          BloomRose
        )
      )

      drawArc(
        brush = gradientBrush,
        startAngle = -90f,
        sweepAngle = 360f * animatedProgress,
        useCenter = false,
        style = Stroke(width = strokePx, cap = StrokeCap.Round)
      )
    }

    if (showText) {
      Text(
        text = "$percentage%",
        style = MaterialTheme.typography.labelMedium.copy(
          fontWeight = FontWeight.Bold,
          fontSize = if (size < 50.dp) 11.sp else 13.sp,
          color = MaterialTheme.colorScheme.onSurface
        )
      )
    }
  }
}
