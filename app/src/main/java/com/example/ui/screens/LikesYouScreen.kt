package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
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
import com.example.model.UserProfile
import com.example.ui.components.CompatibilityRing
import com.example.ui.components.NeumorphicGlassCard
import com.example.ui.theme.*

@Composable
fun LikesYouScreen(
  admirers: List<UserProfile>,
  isPremium: Boolean,
  isCandleMode: Boolean,
  onOpenProfile: (UserProfile) -> Unit,
  onOpenPremium: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
  ) {
    Spacer(modifier = Modifier.height(16.dp))

    // Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "Likes You 💕",
          style = MaterialTheme.typography.displaySmall.copy(
            fontStyle = FontStyle.Italic,
            color = if (isCandleMode) BloomWarmGold else BloomDeepRose
          )
        )
        Text(
          text = "${admirers.size} admirers sent heartfelt blooms your way",
          style = MaterialTheme.typography.bodySmall.copy(
            color = if (isCandleMode) BloomCandleDarkMuted else BloomMuted
          )
        )
      }

      Button(
        onClick = onOpenPremium,
        shape = RoundedCornerShape(9999.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (isPremium) BloomOnline else BloomCandleGold
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        modifier = Modifier.testTag("premium_pill_btn")
      ) {
        Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = if (isPremium) "Gold Active" else "Bloom Gold",
          style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Non-premium blur teaser banner
    if (!isPremium) {
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isCandleMode) BloomCandleDarkCard else Color(0xFFFFF7F9),
        border = BorderStroke(1.dp, BloomCandleGold.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text("✨", fontSize = 20.sp)
            Column {
              Text("See Who Bloomed You", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
              Text("Upgrade to Bloom Gold to unblur all admirers instantly", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = BloomMuted))
            }
          }

          Button(
            onClick = onOpenPremium,
            shape = RoundedCornerShape(9999.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BloomRose),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
          ) {
            Text("Unlock", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
          }
        }
      }
      Spacer(modifier = Modifier.height(14.dp))
    }

    // Admirers Grid
    LazyVerticalGrid(
      columns = GridCells.Fixed(2),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      contentPadding = PaddingValues(bottom = 96.dp)
    ) {
      items(admirers, key = { it.id }) { admirer ->
        AdmirerGridItem(
          admirer = admirer,
          isPremium = isPremium,
          isCandleMode = isCandleMode,
          onClick = {
            if (isPremium) {
              onOpenProfile(admirer)
            } else {
              onOpenPremium()
            }
          }
        )
      }
    }
  }
}

@Composable
private fun AdmirerGridItem(
  admirer: UserProfile,
  isPremium: Boolean,
  isCandleMode: Boolean,
  onClick: () -> Unit
) {
  NeumorphicGlassCard(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("admirer_card_${admirer.id}"),
    shape = RoundedCornerShape(24.dp),
    isCandleMode = isCandleMode
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(170.dp)
          .background(
            Brush.verticalGradient(
              listOf(Color(admirer.avatarColorStart), Color(admirer.avatarColorEnd))
            )
          )
      ) {
        // Initials / Avatar placeholder
        Box(
          modifier = Modifier
            .size(70.dp)
            .align(Alignment.Center)
            .shadow(6.dp, CircleShape)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.3f)),
          contentAlignment = Alignment.Center
        ) {
          Text(admirer.initials, style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))
        }

        // Blur Overlay for non-premium
        if (!isPremium) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(Color.White.copy(alpha = 0.55f))
              .blur(16.dp),
            contentAlignment = Alignment.Center
          ) {
            Surface(
              shape = CircleShape,
              color = Color.Black.copy(alpha = 0.6f),
              modifier = Modifier.size(36.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Color.White, modifier = Modifier.size(18.dp))
              }
            }
          }
        }

        // Compatibility Tag
        Surface(
          shape = RoundedCornerShape(9999.dp),
          color = Color(0xCC000000),
          modifier = Modifier
            .padding(8.dp)
            .align(Alignment.TopEnd)
        ) {
          Text(
            text = "${admirer.compatibilityPercent}% Match",
            style = MaterialTheme.typography.labelSmall.copy(color = BloomWarmGold, fontSize = 9.sp),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }
      }

      Column(
        modifier = Modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Text(
          text = if (isPremium) "${admirer.name}, ${admirer.age}" else "Verified Admirer",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )
        Text(
          text = if (isPremium) "📍 ${admirer.city}" else "Liked your daily bloom",
          style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = BloomMuted)
        )
      }
    }
  }
}
