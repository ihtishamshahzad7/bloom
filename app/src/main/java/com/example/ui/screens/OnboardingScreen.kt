package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.ui.components.NeumorphicGlassCard
import com.example.ui.theme.*

data class OnboardingStep(
  val tag: String,
  val title: String,
  val emoji: String,
  val headline: String,
  val description: String,
  val accentColor: Color
)

@Composable
fun OnboardingScreen(
  isCandleMode: Boolean,
  onFinishOnboarding: () -> Unit,
  modifier: Modifier = Modifier
) {
  val steps = remember {
    listOf(
      OnboardingStep(
        tag = "Meet",
        title = "Meet with Intention",
        emoji = "🌸",
        headline = "Only 5 Curated Blooms Per Day",
        description = "Say goodbye to endless, exhausting swiping. Bloom prioritizes emotional presence and depth by presenting five handpicked, serious young adults daily.",
        accentColor = BloomRose
      ),
      OnboardingStep(
        tag = "Match",
        title = "Match on What Matters",
        emoji = "💞",
        headline = "Values, Faith & Life Harmony",
        description = "Discover matches who share your commitment to family traditions, moral integrity, communication gentleness, and long-term companionship.",
        accentColor = BloomCandleGold
      ),
      OnboardingStep(
        tag = "Marry-minded & Safe",
        title = "Marry-Minded & Safe",
        emoji = "🛡️",
        headline = "Strict 18+ Sanctuary & Verified Profiles",
        description = "Photo consent guards, public-only date suggestions, trusted family preview links, and zero tolerance for scams or under-18 accounts.",
        accentColor = BloomOnline
      )
    )
  }

  var currentStepIndex by remember { mutableIntStateOf(0) }
  val currentStep = steps[currentStepIndex]

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        if (isCandleMode) {
          Brush.verticalGradient(listOf(BloomCandleDarkBg, Color(0xFF221128), BloomCandleDarkBg))
        } else {
          Brush.verticalGradient(listOf(BloomBlush, BloomBlushMid, BloomLavender.copy(alpha = 0.5f)))
        }
      )
      .systemBarsPadding()
      .padding(20.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 480.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Top Skip Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        DecorativeCandle(candleHeight = 36.dp, candleWidth = 18.dp, isCandleMode = isCandleMode)

        TextButton(
          onClick = onFinishOnboarding,
          modifier = Modifier.testTag("onboarding_skip_button")
        ) {
          Text(
            text = "Skip",
            style = MaterialTheme.typography.labelMedium.copy(
              color = if (isCandleMode) BloomCandleDarkMuted else BloomMuted,
              fontWeight = FontWeight.Bold
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Center Glass Card with Current Step Details
      NeumorphicGlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        isCandleMode = isCandleMode
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(26.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          Box(
            modifier = Modifier
              .size(80.dp)
              .shadow(8.dp, CircleShape)
              .background(
                Brush.linearGradient(listOf(currentStep.accentColor.copy(alpha = 0.2f), currentStep.accentColor.copy(alpha = 0.4f))),
                CircleShape
              ),
            contentAlignment = Alignment.Center
          ) {
            Text(currentStep.emoji, fontSize = 40.sp)
          }

          Surface(
            shape = RoundedCornerShape(9999.dp),
            color = currentStep.accentColor.copy(alpha = 0.15f),
            border = BorderStroke(1.dp, currentStep.accentColor.copy(alpha = 0.4f))
          ) {
            Text(
              text = currentStep.tag.uppercase(),
              style = MaterialTheme.typography.labelSmall.copy(
                color = currentStep.accentColor,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
              ),
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
          }

          Text(
            text = currentStep.headline,
            style = MaterialTheme.typography.headlineMedium.copy(
              fontStyle = FontStyle.Italic,
              fontWeight = FontWeight.Bold,
              color = if (isCandleMode) BloomWarmGold else BloomDeepRose,
              textAlign = TextAlign.Center
            )
          )

          Text(
            text = currentStep.description,
            style = MaterialTheme.typography.bodyMedium.copy(
              color = if (isCandleMode) BloomCandleDarkMuted else BloomPlum,
              lineHeight = 22.sp,
              textAlign = TextAlign.Center
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Bottom Navigation & Indicator Dots
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // Indicator Dots
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          steps.indices.forEach { index ->
            val isSelected = index == currentStepIndex
            Box(
              modifier = Modifier
                .width(if (isSelected) 28.dp else 8.dp)
                .height(8.dp)
                .clip(RoundedCornerShape(9999.dp))
                .background(if (isSelected) BloomRose else Color.Gray.copy(alpha = 0.35f))
            )
          }
        }

        // Action Button: "Next" or "Get Started"
        Button(
          onClick = {
            if (currentStepIndex < steps.size - 1) {
              currentStepIndex++
            } else {
              onFinishOnboarding()
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .testTag("onboarding_next_button"),
          shape = RoundedCornerShape(9999.dp),
          colors = ButtonDefaults.buttonColors(containerColor = BloomRose)
        ) {
          Text(
            text = if (currentStepIndex == steps.size - 1) "Enter Bloom (18+ Verification) 🌸" else "Next Step",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }
      }
    }
  }
}
