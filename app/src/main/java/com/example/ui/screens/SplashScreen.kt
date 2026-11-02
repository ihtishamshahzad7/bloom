package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DecorativeCandle
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
  isCandleMode: Boolean,
  onSplashFinished: () -> Unit,
  modifier: Modifier = Modifier
) {
  var startAnimation by remember { mutableStateOf(false) }

  val alphaAnim by animateFloatAsState(
    targetValue = if (startAnimation) 1f else 0f,
    animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
    label = "splash_alpha"
  )

  val scaleAnim by animateFloatAsState(
    targetValue = if (startAnimation) 1f else 0.75f,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
    label = "splash_scale"
  )

  LaunchedEffect(Unit) {
    startAnimation = true
    delay(2400)
    onSplashFinished()
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        if (isCandleMode) {
          Brush.verticalGradient(listOf(BloomCandleDarkBg, Color(0xFF1E0E24), BloomCandleDarkBg))
        } else {
          Brush.verticalGradient(listOf(BloomBlush, BloomBlushMid, BloomLavender.copy(alpha = 0.6f)))
        }
      )
      .clickable { onSplashFinished() }
      .testTag("splash_screen"),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(20.dp),
      modifier = Modifier
        .alpha(alphaAnim)
        .scale(scaleAnim)
        .padding(24.dp)
    ) {
      // Candle Flame lighting up
      DecorativeCandle(
        candleHeight = 58.dp,
        candleWidth = 26.dp,
        isCandleMode = isCandleMode
      )

      // Blooming Rose Icon
      Box(
        modifier = Modifier
          .size(92.dp)
          .shadow(16.dp, CircleShape)
          .background(
            Brush.linearGradient(listOf(BloomRose, BloomCandleGold)),
            CircleShape
          ),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "✿",
          fontSize = 46.sp,
          color = Color.White
        )
      }

      Text(
        text = "Bloom",
        style = MaterialTheme.typography.displayLarge.copy(
          fontStyle = FontStyle.Italic,
          color = if (isCandleMode) BloomWarmGold else BloomDeepRose,
          fontWeight = FontWeight.Bold
        )
      )

      Text(
        text = "Where Lifelong Connections Bloom",
        style = MaterialTheme.typography.titleMedium.copy(
          fontStyle = FontStyle.Italic,
          color = if (isCandleMode) BloomCandleDarkMuted else BloomPlum,
          textAlign = TextAlign.Center
        )
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "Safe • Intentional • Romantic Courtship",
        style = MaterialTheme.typography.labelSmall.copy(
          color = BloomRose,
          fontWeight = FontWeight.SemiBold,
          letterSpacing = 1.sp
        )
      )
    }
  }
}
