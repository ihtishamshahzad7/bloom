package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

@Composable
fun NeumorphicGlassCard(
  modifier: Modifier = Modifier,
  shape: Shape = RoundedCornerShape(28.dp),
  elevation: Dp = 8.dp,
  isCandleMode: Boolean = false,
  content: @Composable BoxScope.() -> Unit
) {
  val surfaceColor = if (isCandleMode) BloomCandleDarkCard.copy(alpha = 0.85f)
                     else BloomLightSurfaceHigh.copy(alpha = 0.78f)
  val borderColor = if (isCandleMode) BloomCandleDarkBorder.copy(alpha = 0.6f)
                    else BloomLightBorder

  Box(
    modifier = modifier
      .shadow(
        elevation = elevation,
        shape = shape,
        ambientColor = if (isCandleMode) Color(0x33000000) else BloomShadowDark,
        spotColor = if (isCandleMode) Color(0x40F6C177) else BloomShadowDark
      )
      .clip(shape)
      .background(surfaceColor)
      .border(
        BorderStroke(
          width = 1.dp,
          brush = Brush.verticalGradient(
            colors = listOf(
              borderColor,
              borderColor.copy(alpha = 0.2f)
            )
          )
        ),
        shape = shape
      ),
    content = content
  )
}
