package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.StoryItem
import com.example.model.StoryType
import com.example.model.StoryVisibility
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickCreateSheet(
  isCandleMode: Boolean,
  onDismiss: () -> Unit,
  onOpenPostComposer: () -> Unit,
  onCreateStory: (StoryItem) -> Unit,
  onOpenSelfie: () -> Unit,
  onCreateGroup: () -> Unit
) {
  var activeSubMode by remember { mutableStateOf<String?>(null) } // "story", "status", "reel"
  var statusText by remember { mutableStateOf("") }
  var selectedMoodEmoji by remember { mutableStateOf("🌸") }
  var storyVisibility by remember { mutableStateOf(StoryVisibility.EVERYONE) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = if (isCandleMode) BloomCandleDarkBg else Color(0xFFFFF7F9),
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp, vertical = 14.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      if (activeSubMode == null) {
        // Header
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "Create with Intention ✿",
            style = MaterialTheme.typography.headlineSmall.copy(
              fontStyle = FontStyle.Italic,
              fontWeight = FontWeight.Bold,
              color = if (isCandleMode) BloomWarmGold else BloomDeepRose
            )
          )
          Text(
            text = "Share with your circle, spark reflections, or gather friends.",
            style = MaterialTheme.typography.bodySmall.copy(
              color = if (isCandleMode) BloomCandleDarkMuted else BloomPlum,
              textAlign = TextAlign.Center
            )
          )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Center Quick Create: 5 Options per Section 8: Post, Story, Reel, Poll, Group
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceEvenly
        ) {
          QuickActionButton(
            icon = Icons.Outlined.Edit,
            title = "Post",
            subtitle = "Feed & photos",
            isCandleMode = isCandleMode,
            onClick = {
              onDismiss()
              onOpenPostComposer()
            }
          )
          QuickActionButton(
            icon = Icons.Outlined.AddAPhoto,
            title = "Story",
            subtitle = "24h moment",
            isCandleMode = isCandleMode,
            onClick = { activeSubMode = "story" }
          )
          QuickActionButton(
            icon = Icons.Outlined.VideoLibrary,
            title = "Reel",
            subtitle = "Short video",
            isCandleMode = isCandleMode,
            onClick = { activeSubMode = "reel" }
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceEvenly
        ) {
          QuickActionButton(
            icon = Icons.Outlined.Poll,
            title = "Poll",
            subtitle = "Ask circle",
            isCandleMode = isCandleMode,
            onClick = {
              onDismiss()
              onOpenPostComposer()
            }
          )
          QuickActionButton(
            icon = Icons.Outlined.Groups,
            title = "Group",
            subtitle = "Safe circle",
            isCandleMode = isCandleMode,
            onClick = {
              onDismiss()
              onCreateGroup()
            }
          )
        }
      } else if (activeSubMode == "story") {
        // Story creation
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          TextButton(onClick = { activeSubMode = null }) {
            Text("Back", color = BloomRose)
          }
          Text(
            "Share 24h Story",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = if (isCandleMode) BloomWarmGold else BloomDeepRose
            )
          )
          Spacer(modifier = Modifier.width(48.dp))
        }

        // Preview Card
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .shadow(8.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(
              Brush.linearGradient(
                if (isCandleMode) listOf(Color(0xFF45244E), Color(0xFF2B1632))
                else listOf(BloomPeach, BloomBlushMid)
              )
            )
            .padding(16.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Text(selectedMoodEmoji, fontSize = 42.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = statusText.ifEmpty { "Write your heartfelt reflection..." },
              style = MaterialTheme.typography.titleMedium.copy(
                fontStyle = FontStyle.Italic,
                color = if (isCandleMode) BloomWarmGold else BloomDeepRose,
                textAlign = TextAlign.Center
              )
            )
          }
        }

        // Mood Emoji Row
        Row(
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          listOf("🌸", "☕", "✨", "📖", "🕊️", "🌙", "🕯️").forEach { emoji ->
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (selectedMoodEmoji == emoji) BloomRose.copy(alpha = 0.3f) else Color.Transparent)
                .border(
                  if (selectedMoodEmoji == emoji) 2.dp else 1.dp,
                  if (selectedMoodEmoji == emoji) BloomRose else Color.Gray.copy(alpha = 0.3f),
                  CircleShape
                )
                .clickable { selectedMoodEmoji = emoji },
              contentAlignment = Alignment.Center
            ) {
              Text(emoji, fontSize = 20.sp)
            }
          }
        }

        OutlinedTextField(
          value = statusText,
          onValueChange = { statusText = it },
          placeholder = { Text("What is blooming in your heart today?") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp)
        )

        Button(
          onClick = {
            if (statusText.isNotBlank()) {
              val newStory = StoryItem(
                id = "story_${System.currentTimeMillis()}",
                userId = "u_mia",
                userName = "Mia",
                userInitials = "M",
                avatarColorStart = 0xFFF28DB0,
                avatarColorEnd = 0xFFB79CF0,
                storyType = StoryType.PHOTO,
                textCaption = statusText,
                moodEmoji = selectedMoodEmoji,
                bgGradientStart = if (isCandleMode) 0xFF45244E else 0xFFFF9BB3,
                bgGradientEnd = if (isCandleMode) 0xFF2B1632 else 0xFFFFD3C2,
                visibility = storyVisibility
              )
              onCreateStory(newStory)
              onDismiss()
            }
          },
          modifier = Modifier.fillMaxWidth(),
          colors = ButtonDefaults.buttonColors(containerColor = BloomRose)
        ) {
          Text("Share Story 🌸", fontWeight = FontWeight.Bold)
        }
      } else if (activeSubMode == "reel") {
        // Reel / Video Moment
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          TextButton(onClick = { activeSubMode = null }) {
            Text("Back", color = BloomRose)
          }
          Text(
            "Share Reel / Video",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = if (isCandleMode) BloomWarmGold else BloomDeepRose
            )
          )
          Spacer(modifier = Modifier.width(48.dp))
        }

        Box(
          modifier = Modifier
            .size(90.dp)
            .shadow(12.dp, CircleShape)
            .background(Brush.linearGradient(listOf(BloomRose, BloomWarmGold)), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.Videocam, contentDescription = "Record", tint = Color.White, modifier = Modifier.size(44.dp))
        }

        Text(
          text = "Capture a tranquil video moment (up to 60s) of heritage walks, rainfall, or floral crafts.",
          style = MaterialTheme.typography.bodySmall.copy(
            color = if (isCandleMode) BloomCandleDarkMuted else BloomPlum,
            textAlign = TextAlign.Center
          )
        )

        Button(
          onClick = {
            val newStory = StoryItem(
              id = "reel_${System.currentTimeMillis()}",
              userId = "u_mia",
              userName = "Mia",
              userInitials = "M",
              avatarColorStart = 0xFF9E77ED,
              avatarColorEnd = 0xFF6172F3,
              storyType = StoryType.VIDEO,
              textCaption = "📹 Peaceful rain over rooftop courtyards",
              moodEmoji = "🌧️",
              bgGradientStart = 0xFF9E77ED,
              bgGradientEnd = 0xFF6172F3
            )
            onCreateStory(newStory)
            onDismiss()
          },
          modifier = Modifier.fillMaxWidth(),
          colors = ButtonDefaults.buttonColors(containerColor = BloomRose)
        ) {
          Text("Publish Reel 🎬")
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
private fun QuickActionButton(
  icon: ImageVector,
  title: String,
  subtitle: String,
  isCandleMode: Boolean,
  onClick: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .width(96.dp)
      .clickable { onClick() }
      .testTag("quick_action_${title.lowercase().replace(" ", "_")}"),
    verticalArrangement = Arrangement.spacedBy(6.dp)
  ) {
    Box(
      modifier = Modifier
        .size(56.dp)
        .shadow(6.dp, CircleShape)
        .clip(CircleShape)
        .background(
          if (isCandleMode) Brush.linearGradient(listOf(Color(0xFF3B1F42), Color(0xFF28152E)))
          else Brush.linearGradient(listOf(Color.White, BloomPeach.copy(alpha = 0.5f)))
        )
        .border(
          1.5.dp,
          if (isCandleMode) BloomWarmGold.copy(alpha = 0.5f) else BloomRose.copy(alpha = 0.4f),
          CircleShape
        ),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = title,
        tint = if (isCandleMode) BloomWarmGold else BloomDeepRose,
        modifier = Modifier.size(26.dp)
      )
    }

    Text(
      text = title,
      style = MaterialTheme.typography.labelMedium.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        color = if (isCandleMode) Color.White else BloomPlum,
        textAlign = TextAlign.Center
      )
    )

    Text(
      text = subtitle,
      style = MaterialTheme.typography.labelSmall.copy(
        fontSize = 10.sp,
        color = if (isCandleMode) BloomCandleDarkMuted else BloomPlumMuted,
        textAlign = TextAlign.Center
      )
    )
  }
}
