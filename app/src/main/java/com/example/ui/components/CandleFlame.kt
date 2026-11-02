package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BloomCandleGold
import com.example.ui.theme.BloomRose
import com.example.ui.theme.BloomWarmGold

@Composable
fun DecorativeCandle(
  modifier: Modifier = Modifier,
  candleHeight: Dp = 48.dp,
  candleWidth: Dp = 22.dp,
  isCandleMode: Boolean = false
) {
  val infiniteTransition = rememberInfiniteTransition(label = "candle_flame")

  val flameScaleY by infiniteTransition.animateFloat(
    initialValue = 0.88f,
    targetValue = 1.15f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 950, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "flame_scale_y"
  )

  val flameSway by infiniteTransition.animateFloat(
    initialValue = -1.2f,
    targetValue = 1.2f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 650, easing = LinearOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "flame_sway"
  )

  val glowPulse by infiniteTransition.animateFloat(
    initialValue = 0.75f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1300, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "glow_pulse"
  )

  Box(
    modifier = modifier.size(candleWidth + 36.dp, candleHeight + 42.dp),
    contentAlignment = Alignment.BottomCenter
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height
      val candleW = candleWidth.toPx()
      val candleH = candleHeight.toPx()

      val candleLeft = (w - candleW) / 2f
      val candleTop = h - candleH

      // 1. Radial candle glow backdrop
      val glowCenter = Offset(w / 2f, candleTop - 12f)
      val glowRadius = (candleW * 2.8f) * glowPulse * (if (isCandleMode) 1.35f else 1.0f)
      val glowBrush = Brush.radialGradient(
        colors = listOf(
          BloomWarmGold.copy(alpha = if (isCandleMode) 0.55f else 0.40f),
          BloomCandleGold.copy(alpha = if (isCandleMode) 0.30f else 0.18f),
          Color.Transparent
        ),
        center = glowCenter,
        radius = glowRadius
      )
      drawCircle(brush = glowBrush, radius = glowRadius, center = glowCenter)

      // 2. Candle wax cylinder
      val waxBrush = Brush.verticalGradient(
        colors = listOf(
          Color(0xFFFFF9F5),
          Color(0xFFFFE6EB),
          Color(0xFFF2D1DC)
        ),
        startY = candleTop,
        endY = h
      )
      drawRoundRect(
        brush = waxBrush,
        topLeft = Offset(candleLeft, candleTop),
        size = Size(candleW, candleH),
        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
      )

      // Wax rim highlight
      drawOval(
        color = Color(0xFFFFFDFB),
        topLeft = Offset(candleLeft + 1f, candleTop - 3f),
        size = Size(candleW - 2f, 6.dp.toPx())
      )

      // 3. Black wick
      val wickX = w / 2f
      val wickTop = candleTop - 8f
      drawLine(
        color = Color(0xFF26181B),
        start = Offset(wickX, candleTop),
        end = Offset(wickX, wickTop),
        strokeWidth = 2.dp.toPx()
      )

      // 4. Animated flickering teardrop flame
      scale(
        scaleX = 1f,
        scaleY = flameScaleY,
        pivot = Offset(wickX, wickTop)
      ) {
        drawFlame(
          center = Offset(wickX + flameSway, wickTop - 11f),
          width = 11.dp.toPx(),
          height = 19.dp.toPx()
        )
      }
    }
  }
}

private fun DrawScope.drawFlame(center: Offset, width: Float, height: Float) {
  val path = Path().apply {
    val left = center.x - width / 2f
    val right = center.x + width / 2f
    val top = center.y - height / 2f
    val bottom = center.y + height / 2f

    moveTo(center.x, top)
    cubicTo(right + 2f, top + height * 0.45f, right, bottom, center.x, bottom)
    cubicTo(left, bottom, left - 2f, top + height * 0.45f, center.x, top)
    close()
  }

  // Outer orange-gold flame
  val outerBrush = Brush.radialGradient(
    colors = listOf(Color(0xFFFFF4D0), BloomCandleGold, BloomRose.copy(alpha = 0.8f)),
    center = center,
    radius = height * 0.6f
  )
  drawPath(path = path, brush = outerBrush)

  // Inner warm white core
  val innerPath = Path().apply {
    val iWidth = width * 0.45f
    val iHeight = height * 0.5f
    val iTop = center.y - iHeight * 0.1f
    val iBottom = center.y + iHeight * 0.8f
    moveTo(center.x, iTop)
    cubicTo(center.x + iWidth, iTop + iHeight * 0.4f, center.x + iWidth * 0.8f, iBottom, center.x, iBottom)
    cubicTo(center.x - iWidth * 0.8f, iBottom, center.x - iWidth, iTop + iHeight * 0.4f, center.x, iTop)
    close()
  }
  drawPath(path = innerPath, color = Color(0xFFFFFDF5))
}
