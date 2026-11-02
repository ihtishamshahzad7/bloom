package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.sin
import kotlin.random.Random

data class PetalParticle(
  val initialXRatio: Float,
  val speedRatio: Float,
  val sizeDp: Float,
  val baseAlpha: Float,
  val rotationSpeed: Float,
  val swayFrequency: Float,
  val color: Color
)

@Composable
fun AmbientPetalsField(
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  isCandleMode: Boolean = false
) {
  if (!enabled) return

  val infiniteTransition = rememberInfiniteTransition(label = "ambient_petals")
  val time by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 14000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "petals_time"
  )

  // Stable list of 16 petals with romantic blush/rose/gold tints
  val petals = remember(isCandleMode) {
    val rng = Random(42)
    val colors = if (isCandleMode) {
      listOf(
        Color(0x66E85D8A),
        Color(0x4DF6C177),
        Color(0x55CDB8F0),
        Color(0x44C2416C)
      )
    } else {
      listOf(
        Color(0x73E85D8A),
        Color(0x55F28DB0),
        Color(0x66FFD3C2),
        Color(0x4DFFD8A8)
      )
    }
    List(16) { index ->
      PetalParticle(
        initialXRatio = rng.nextFloat(),
        speedRatio = 0.5f + rng.nextFloat() * 0.8f,
        sizeDp = 10f + rng.nextFloat() * 14f,
        baseAlpha = 0.35f + rng.nextFloat() * 0.45f,
        rotationSpeed = (rng.nextFloat() - 0.5f) * 360f,
        swayFrequency = 1.5f + rng.nextFloat() * 2.5f,
        color = colors[index % colors.size]
      )
    }
  }

  Canvas(modifier = modifier.fillMaxSize()) {
    val w = size.width
    val h = size.height
    if (w <= 0 || h <= 0) return@Canvas

    // Subtle bokeh glow orbs in corners
    val bokehBrush1 = Brush.radialGradient(
      colors = if (isCandleMode) listOf(Color(0x33F6C177), Color.Transparent)
               else listOf(Color(0x40FFD8A8), Color.Transparent),
      center = Offset(w * 0.85f, h * 0.12f),
      radius = w * 0.5f
    )
    drawCircle(brush = bokehBrush1, radius = w * 0.5f, center = Offset(w * 0.85f, h * 0.12f))

    val bokehBrush2 = Brush.radialGradient(
      colors = if (isCandleMode) listOf(Color(0x26CDB8F0), Color.Transparent)
               else listOf(Color(0x2EF9D3E3), Color.Transparent),
      center = Offset(w * 0.15f, h * 0.88f),
      radius = w * 0.45f
    )
    drawCircle(brush = bokehBrush2, radius = w * 0.45f, center = Offset(w * 0.15f, h * 0.88f))

    // Draw falling petals
    petals.forEachIndexed { i, p ->
      val progress = (time * p.speedRatio + (i.toFloat() / petals.size)) % 1f
      val y = progress * (h + 80f) - 40f
      val sway = sin((progress * Math.PI * 2 * p.swayFrequency) + i).toFloat() * 35f
      val x = (p.initialXRatio * w + sway) % w
      val currentRotation = (time * p.rotationSpeed) % 360f

      rotate(degrees = currentRotation, pivot = Offset(x, y)) {
        drawPetal(
          center = Offset(x, y),
          size = p.sizeDp * density,
          color = p.color.copy(alpha = p.baseAlpha)
        )
      }

      // Sparkles ✦ on a few particles
      if (i % 4 == 0) {
        val sparklePulse = (sin((time * 8 + i).toDouble()).toFloat() * 0.5f + 0.5f)
        drawSparkle(
          center = Offset((x + 20f) % w, (y - 15f + h) % h),
          size = 6f * density * sparklePulse,
          color = (if (isCandleMode) Color(0x99F6C177) else Color(0xBBFFE3EA))
        )
      }
    }
  }
}

private fun DrawScope.drawPetal(center: Offset, size: Float, color: Color) {
  val path = Path().apply {
    val halfW = size * 0.4f
    val halfH = size * 0.6f
    moveTo(center.x, center.y - halfH)
    cubicTo(
      center.x + halfW * 1.3f, center.y - halfH * 0.4f,
      center.x + halfW * 0.9f, center.y + halfH * 0.6f,
      center.x, center.y + halfH
    )
    cubicTo(
      center.x - halfW * 0.9f, center.y + halfH * 0.6f,
      center.x - halfW * 1.3f, center.y - halfH * 0.4f,
      center.x, center.y - halfH
    )
    close()
  }
  drawPath(path = path, color = color)
}

private fun DrawScope.drawSparkle(center: Offset, size: Float, color: Color) {
  drawLine(
    color = color,
    start = Offset(center.x - size, center.y),
    end = Offset(center.x + size, center.y),
    strokeWidth = 1.5f
  )
  drawLine(
    color = color,
    start = Offset(center.x, center.y - size),
    end = Offset(center.x, center.y + size),
    strokeWidth = 1.5f
  )
}
