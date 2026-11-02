package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
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

@Composable
fun PremiumScreen(
  isPremium: Boolean,
  isCandleMode: Boolean,
  onActivatePremium: () -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedTierIndex by remember { mutableIntStateOf(0) } // 0: 3 Months, 1: 1 Month

  BackHandler {
    onBack()
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(if (isCandleMode) BloomCandleDarkBg else BloomBlush)
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(18.dp)
  ) {
    // Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(onClick = onBack) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = BloomRose)
        }
        Text(
          text = "Bloom Gold ✨",
          style = MaterialTheme.typography.displaySmall.copy(
            fontStyle = FontStyle.Italic,
            color = if (isCandleMode) BloomWarmGold else BloomDeepRose,
            fontWeight = FontWeight.Bold
          )
        )
        DecorativeCandle(candleHeight = 36.dp, candleWidth = 18.dp, isCandleMode = isCandleMode)
      }
    }

    // Hero Gold Banner
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .shadow(12.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              Brush.linearGradient(
                listOf(BloomRose, BloomCandleGold, Color(0xFFF28DB0))
              )
            )
            .padding(24.dp)
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Box(
              modifier = Modifier
                .size(60.dp)
                .background(Color.White.copy(alpha = 0.25f), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(34.dp))
            }
            Text(
              text = "Elevate Your Courtship Journey",
              style = MaterialTheme.typography.headlineMedium.copy(
                fontStyle = FontStyle.Italic,
                color = Color.White,
                fontWeight = FontWeight.Bold
              ),
              textAlign = TextAlign.Center
            )
            Text(
              text = "Designed for intentional young adults ready for lifelong marriage companionship.",
              style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.9f)),
              textAlign = TextAlign.Center
            )
          }
        }
      }
    }

    // Features List
    item {
      NeumorphicGlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        isCandleMode = isCandleMode
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          Text("Included in Gold Membership:", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

          PremiumFeatureRow("Unlimited Daily Blooms", "Connect beyond the 5 daily limit at your own pace", isCandleMode)
          PremiumFeatureRow("See Who Likes You Unblurred", "Directly view all admirers who bloomed your profile", isCandleMode)
          PremiumFeatureRow("5 Free Monthly Profile Boosts", "Multiply discovery by 3x during prime evening hours", isCandleMode)
          PremiumFeatureRow("Secure Family Share Links", "Unlimited cryptographic read-only preview tokens", isCandleMode)
          PremiumFeatureRow("Read Receipts & VIP Delivery", "Know when your thoughtful message has been read", isCandleMode)
        }
      }
    }

    // Tier Cards (3 Months Best Value vs 1 Month)
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        PricingTierCard(
          title = "3 Months",
          price = "$8.33 / mo",
          total = "$24.99 billed quarterly",
          badge = "Most Popular 🌸",
          isSelected = selectedTierIndex == 0,
          isCandleMode = isCandleMode,
          onClick = { selectedTierIndex = 0 },
          modifier = Modifier.weight(1f)
        )
        PricingTierCard(
          title = "1 Month",
          price = "$11.99 / mo",
          total = "$11.99 billed monthly",
          badge = "Flexible",
          isSelected = selectedTierIndex == 1,
          isCandleMode = isCandleMode,
          onClick = { selectedTierIndex = 1 },
          modifier = Modifier.weight(1f)
        )
      }
    }

    // Activate Button
    item {
      Button(
        onClick = {
          onActivatePremium()
          onBack()
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(54.dp)
          .testTag("activate_gold_button"),
        shape = RoundedCornerShape(9999.dp),
        colors = ButtonDefaults.buttonColors(containerColor = BloomRose)
      ) {
        Text(
          text = if (isPremium) "Gold Membership Active ✓" else "Activate Bloom Gold ✨",
          style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
        )
      }
      Text(
        text = "Cancel anytime. No automatic charges in preview mode.",
        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = BloomMuted, textAlign = TextAlign.Center),
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
      )
    }
  }
}

@Composable
private fun PremiumFeatureRow(
  title: String,
  description: String,
  isCandleMode: Boolean
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    Box(
      modifier = Modifier
        .size(24.dp)
        .background(BloomOnline, CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
    }
    Column {
      Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
      Text(description, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = BloomMuted))
    }
  }
}

@Composable
private fun PricingTierCard(
  title: String,
  price: String,
  total: String,
  badge: String,
  isSelected: Boolean,
  isCandleMode: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(22.dp),
    color = if (isSelected) {
      if (isCandleMode) BloomCandleDarkSurface else Color(0xFFFFF2F5)
    } else {
      if (isCandleMode) BloomCandleDarkCard else Color.White
    },
    border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) BloomRose else Color.Gray.copy(alpha = 0.25f)),
    modifier = modifier.clickable { onClick() }
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Surface(
        shape = RoundedCornerShape(9999.dp),
        color = BloomCandleGold.copy(alpha = 0.2f)
      ) {
        Text(badge, style = MaterialTheme.typography.labelSmall.copy(color = BloomCandleGold, fontSize = 9.sp, fontWeight = FontWeight.Bold), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
      }
      Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
      Text(price, style = MaterialTheme.typography.headlineSmall.copy(color = BloomRose, fontWeight = FontWeight.Bold))
      Text(total, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = BloomMuted, textAlign = TextAlign.Center))
    }
  }
}
