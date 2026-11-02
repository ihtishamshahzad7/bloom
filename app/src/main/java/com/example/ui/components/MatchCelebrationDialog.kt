package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.ui.window.Dialog
import com.example.model.UserProfile
import com.example.ui.theme.*

@Composable
fun MatchCelebrationDialog(
  matchedProfile: UserProfile?,
  isCandleMode: Boolean,
  onDismiss: () -> Unit,
  onStartChat: (UserProfile) -> Unit
) {
  if (matchedProfile == null) return

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp)
        .testTag("match_celebration_dialog"),
      shape = RoundedCornerShape(32.dp),
      colors = CardDefaults.cardColors(
        containerColor = if (isCandleMode) BloomCandleDarkCard else Color(0xFFFFF7F9)
      ),
      elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(
            Brush.verticalGradient(
              colors = if (isCandleMode) listOf(BloomCandleDarkCard, BloomCandleDarkBg)
                       else listOf(BloomBlush, Color.White)
            )
          )
          .padding(24.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          // Decorative candle with warm pulse
          DecorativeCandle(
            modifier = Modifier.padding(top = 4.dp),
            candleHeight = 36.dp,
            candleWidth = 18.dp,
            isCandleMode = isCandleMode
          )

          Text(
            text = "It's a Match 💞",
            style = MaterialTheme.typography.displaySmall.copy(
              fontStyle = FontStyle.Italic,
              color = BloomRose
            ),
            textAlign = TextAlign.Center
          )

          Text(
            text = "You and ${matchedProfile.name} have mutually bloomed. A quiet, romantic connection awaits.",
            style = MaterialTheme.typography.bodyMedium.copy(
              color = if (isCandleMode) BloomCandleDarkMuted else BloomPlum.copy(alpha = 0.8f)
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
          )

          // Intersecting Avatars with rose-gold rings
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // User Avatar (Mia)
            Box(
              modifier = Modifier
                .size(76.dp)
                .shadow(8.dp, CircleShape)
                .border(3.dp, Brush.sweepGradient(listOf(BloomRose, BloomCandleGold, BloomRose)), CircleShape)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFFF28DB0), Color(0xFFB79CF0)))),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "M",
                style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold)
              )
            }

            // Glowing heart center connector
            Box(
              modifier = Modifier
                .offset(x = (-8).dp)
                .size(36.dp)
                .background(BloomRose, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
              )
            }

            // Partner Avatar
            Box(
              modifier = Modifier
                .offset(x = (-16).dp)
                .size(76.dp)
                .shadow(8.dp, CircleShape)
                .border(3.dp, Brush.sweepGradient(listOf(BloomCandleGold, BloomRose, BloomCandleGold)), CircleShape)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(matchedProfile.avatarColorStart), Color(matchedProfile.avatarColorEnd)))),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = matchedProfile.initials,
                style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold)
              )
            }
          }

          // Compatibility badge
          Surface(
            shape = RoundedCornerShape(9999.dp),
            color = if (isCandleMode) BloomCandleDarkSurface else BloomBlushMid.copy(alpha = 0.6f),
            border = BorderStroke(1.dp, BloomRose.copy(alpha = 0.3f))
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = BloomRose,
                modifier = Modifier.size(14.dp)
              )
              Text(
                text = "${matchedProfile.compatibilityPercent}% Compatibility Score",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.SemiBold,
                  color = BloomRose
                )
              )
            }
          }

          Spacer(modifier = Modifier.height(4.dp))

          // Primary Action: Say Hello
          Button(
            onClick = {
              onStartChat(matchedProfile)
              onDismiss()
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("send_greeting_button"),
            colors = ButtonDefaults.buttonColors(
              containerColor = BloomRose
            ),
            shape = RoundedCornerShape(9999.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Send,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Send Sincere Greeting",
              style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
          }

          // Secondary Action: Keep Discovering
          OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("keep_discovering_button"),
            shape = RoundedCornerShape(9999.dp),
            border = BorderStroke(1.dp, if (isCandleMode) BloomCandleDarkBorder else BloomRose.copy(alpha = 0.5f))
          ) {
            Text(
              text = "Keep Discovering",
              style = MaterialTheme.typography.labelMedium.copy(
                color = if (isCandleMode) BloomCandleDarkText else BloomPlum
              )
            )
          }
        }
      }
    }
  }
}
