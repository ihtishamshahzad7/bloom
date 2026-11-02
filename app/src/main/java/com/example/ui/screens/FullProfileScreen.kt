package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserProfile
import com.example.ui.components.CompatibilityRing
import com.example.ui.components.NeumorphicGlassCard
import com.example.ui.theme.*

@Composable
fun FullProfileScreen(
  profile: UserProfile,
  isCandleMode: Boolean,
  onBack: () -> Unit,
  onLike: () -> Unit,
  onPass: () -> Unit,
  onMessage: () -> Unit,
  onReport: (reason: String, details: String) -> Unit,
  onBlock: () -> Unit,
  modifier: Modifier = Modifier
) {
  val haptic = LocalHapticFeedback.current
  var selectedPhotoIndex by remember { mutableIntStateOf(0) }
  var showMenu by remember { mutableStateOf(false) }
  var showReportDialog by remember { mutableStateOf(false) }
  var reportReason by remember { mutableStateOf("Inappropriate Communication") }
  var reportDetails by remember { mutableStateOf("") }

  BackHandler {
    onBack()
  }

  Box(modifier = modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .background(if (isCandleMode) BloomCandleDarkBg else BloomBlush),
      contentPadding = PaddingValues(bottom = 110.dp)
    ) {
      // 1. PHOTO PAGER / CAROUSEL
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(340.dp)
        ) {
          val currentColorPair = profile.photoColorPairs.getOrElse(selectedPhotoIndex) {
            Pair(profile.avatarColorStart, profile.avatarColorEnd)
          }

          // Photo background
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(
                Brush.verticalGradient(
                  listOf(Color(currentColorPair.first), Color(currentColorPair.second))
                )
              ),
            contentAlignment = Alignment.Center
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(130.dp)
                  .shadow(16.dp, CircleShape)
                  .border(3.dp, Color.White, CircleShape)
                  .clip(CircleShape)
                  .background(Color.White.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = profile.initials,
                  style = MaterialTheme.typography.displayMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                )
              }
              Surface(
                shape = RoundedCornerShape(9999.dp),
                color = Color(0x66000000)
              ) {
                Text(
                  text = "Photo ${selectedPhotoIndex + 1} of ${profile.photoColorPairs.size}",
                  style = MaterialTheme.typography.labelSmall.copy(color = Color.White),
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
              }
            }
          }

          // Top Bar Overlay
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .statusBarsPadding()
              .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            IconButton(
              onClick = onBack,
              modifier = Modifier.background(Color.Black.copy(alpha = 0.35f), CircleShape).testTag("full_profile_back_button")
            ) {
              Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            Box {
              IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.background(Color.Black.copy(alpha = 0.35f), CircleShape).testTag("full_profile_menu_button")
              ) {
                Icon(Icons.Outlined.MoreVert, contentDescription = "Menu", tint = Color.White)
              }
              DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
              ) {
                DropdownMenuItem(
                  text = { Text("Report Profile", color = BloomDanger) },
                  onClick = {
                    showMenu = false
                    showReportDialog = true
                  },
                  leadingIcon = { Icon(Icons.Default.Report, contentDescription = null, tint = BloomDanger) }
                )
                DropdownMenuItem(
                  text = { Text("Block Member") },
                  onClick = {
                    showMenu = false
                    onBlock()
                    onBack()
                  },
                  leadingIcon = { Icon(Icons.Default.Block, contentDescription = null) }
                )
              }
            }
          }

          // Indicator Dots at Bottom of Photo
          Row(
            modifier = Modifier
              .align(Alignment.BottomCenter)
              .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            profile.photoColorPairs.indices.forEach { idx ->
              val isSel = idx == selectedPhotoIndex
              Box(
                modifier = Modifier
                  .size(if (isSel) 10.dp else 8.dp)
                  .clip(CircleShape)
                  .background(if (isSel) Color.White else Color.White.copy(alpha = 0.5f))
                  .clickable { selectedPhotoIndex = idx }
              )
            }
          }
        }
      }

      // 2. PROFILE DETAILS CONTENT
      item {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          // Name, Age, Compatibility Ring
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                  text = "${profile.name}, ${profile.age}",
                  style = MaterialTheme.typography.displaySmall.copy(
                    fontStyle = FontStyle.Italic,
                    color = if (isCandleMode) BloomWarmGold else BloomDeepRose,
                    fontWeight = FontWeight.Bold
                  )
                )
                if (profile.isVerified) {
                  Icon(Icons.Default.Verified, contentDescription = "Verified", tint = BloomOnline, modifier = Modifier.size(20.dp))
                }
              }
              Text(
                text = "📍 ${profile.city} • ${profile.distanceMiles} miles away",
                style = MaterialTheme.typography.bodySmall.copy(color = BloomMuted)
              )
              Text(
                text = "🎓 ${profile.education} • ${profile.occupation}",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
              )
            }

            CompatibilityRing(percentage = profile.compatibilityPercent, size = 56.dp, strokeWidth = 4.dp)
          }

          // Intentions Badge
          Surface(
            shape = RoundedCornerShape(9999.dp),
            color = if (isCandleMode) BloomCandleDarkCard else BloomBlushMid.copy(alpha = 0.6f),
            border = BorderStroke(1.dp, BloomRose.copy(alpha = 0.3f))
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text("💍", fontSize = 14.sp)
              Text(
                text = profile.intentions,
                style = MaterialTheme.typography.labelSmall.copy(
                  color = BloomRose,
                  fontWeight = FontWeight.Bold
                )
              )
            }
          }

          // Bio Card
          NeumorphicGlassCard(
            modifier = Modifier.fillMaxWidth(),
            isCandleMode = isCandleMode
          ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Text(
                text = "About Me",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                text = profile.bio,
                style = MaterialTheme.typography.bodyMedium.copy(
                  lineHeight = 22.sp,
                  color = if (isCandleMode) BloomCandleDarkText else BloomPlum
                )
              )
            }
          }

          // Prompts
          NeumorphicGlassCard(
            modifier = Modifier.fillMaxWidth(),
            isCandleMode = isCandleMode
          ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Text(
                text = profile.promptQuestion,
                style = MaterialTheme.typography.titleSmall.copy(
                  color = BloomRose,
                  fontStyle = FontStyle.Italic,
                  fontWeight = FontWeight.Bold
                )
              )
              Text(
                text = "“${profile.promptAnswer}”",
                style = MaterialTheme.typography.bodyMedium.copy(
                  color = if (isCandleMode) BloomCandleDarkText else BloomPlum
                )
              )
            }
          }

          // Core Values & Faith
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Core Values & Foundations", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
              profile.values.forEach { v ->
                Surface(
                  shape = RoundedCornerShape(9999.dp),
                  color = if (isCandleMode) BloomCandleDarkSurface else Color.White,
                  border = BorderStroke(1.dp, BloomRose.copy(alpha = 0.3f))
                ) {
                  Text(
                    text = "✦ $v",
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = if (isCandleMode) BloomCandleDarkText else BloomDeepRose
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                  )
                }
              }
            }
          }

          // Interests
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Shared Passions", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              items(profile.interestChips) { chip ->
                Surface(
                  shape = RoundedCornerShape(9999.dp),
                  color = if (isCandleMode) BloomCandleDarkCard else BloomBlush,
                  border = BorderStroke(1.dp, BloomRose.copy(alpha = 0.25f))
                ) {
                  Text(
                    text = chip,
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = if (isCandleMode) BloomCandleDarkText else BloomDeepRose
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                  )
                }
              }
            }
          }
        }
      }
    }

    // 3. FLOATING BOTTOM ACTION DOCK (Pass, Message, Like with Haptic Feedback)
    Surface(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .navigationBarsPadding()
        .padding(horizontal = 24.dp, vertical = 12.dp),
      shape = RoundedCornerShape(9999.dp),
      color = if (isCandleMode) BloomCandleDarkCard.copy(alpha = 0.95f) else Color.White.copy(alpha = 0.95f),
      shadowElevation = 16.dp,
      border = BorderStroke(1.dp, BloomRose.copy(alpha = 0.3f))
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(28.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Pass
        IconButton(
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onPass()
            onBack()
          },
          modifier = Modifier.size(50.dp).testTag("full_profile_pass_btn")
        ) {
          Icon(Icons.Default.Close, contentDescription = "Pass", tint = Color.Gray, modifier = Modifier.size(26.dp))
        }

        // Message
        IconButton(
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onMessage()
          },
          modifier = Modifier.size(50.dp).testTag("full_profile_msg_btn")
        ) {
          Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Message", tint = BloomLavenderDeep, modifier = Modifier.size(24.dp))
        }

        // Like (Triggers Haptic)
        IconButton(
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onLike()
            onBack()
          },
          modifier = Modifier
            .size(54.dp)
            .shadow(8.dp, CircleShape)
            .background(Brush.linearGradient(listOf(BloomRose, BloomDeepRose)), CircleShape)
            .testTag("full_profile_like_btn")
        ) {
          Icon(Icons.Default.Favorite, contentDescription = "Like", tint = Color.White, modifier = Modifier.size(28.dp))
        }
      }
    }
  }

  // Report Dialog
  if (showReportDialog) {
    AlertDialog(
      onDismissRequest = { showReportDialog = false },
      title = { Text("Report ${profile.name} 🛡️", color = BloomDanger, fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("Help maintain Bloom as a safe, respectful sanctuary. Please select the primary concern:")
          listOf(
            "Inappropriate or disrespectful messages",
            "Suspected financial scam / asking for money",
            "Underage concern (must be 18+)",
            "Commercial advertising / spam"
          ).forEach { reason ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { reportReason = reason }
                .padding(vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              RadioButton(
                selected = reportReason == reason,
                onClick = { reportReason = reason },
                colors = RadioButtonDefaults.colors(selectedColor = BloomDanger)
              )
              Text(reason, style = MaterialTheme.typography.bodySmall)
            }
          }
          OutlinedTextField(
            value = reportDetails,
            onValueChange = { reportDetails = it },
            placeholder = { Text("Optional details...") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onReport(reportReason, reportDetails)
            showReportDialog = false
            onBack()
          },
          colors = ButtonDefaults.buttonColors(containerColor = BloomDanger)
        ) {
          Text("Submit Report & Block")
        }
      },
      dismissButton = {
        TextButton(onClick = { showReportDialog = false }) { Text("Cancel") }
      }
    )
  }
}
