package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.StoryItem
import com.example.model.StoryType
import com.example.ui.theme.*

@Composable
fun StoriesRow(
  stories: List<StoryItem>,
  isCandleMode: Boolean,
  onAddStoryClick: () -> Unit,
  onStoryClick: (StoryItem) -> Unit,
  modifier: Modifier = Modifier
) {
  LazyRow(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 8.dp),
    horizontalArrangement = Arrangement.spacedBy(14.dp),
    contentPadding = PaddingValues(horizontal = 16.dp)
  ) {
    // Current user's "Add Story" avatar
    item {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.clickable { onAddStoryClick() }.testTag("add_story_item")
      ) {
        Box(contentAlignment = Alignment.Center) {
          Box(
            modifier = Modifier
              .size(62.dp)
              .shadow(4.dp, CircleShape)
              .clip(CircleShape)
              .background(
                Brush.linearGradient(
                  if (isCandleMode) listOf(Color(0xFF45244E), Color(0xFF2B1632))
                  else listOf(BloomPeach, BloomBlush)
                )
              )
              .border(
                1.5.dp,
                if (isCandleMode) BloomWarmGold.copy(alpha = 0.5f) else BloomRose.copy(alpha = 0.5f),
                CircleShape
              ),
            contentAlignment = Alignment.Center
          ) {
            Text(
              "M",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = if (isCandleMode) BloomWarmGold else BloomDeepRose
              )
            )
          }

          // Small "+" badge
          Box(
            modifier = Modifier
              .size(20.dp)
              .align(Alignment.BottomEnd)
              .shadow(2.dp, CircleShape)
              .background(BloomRose, CircleShape)
              .border(2.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = "Add story",
              tint = Color.White,
              modifier = Modifier.size(12.dp)
            )
          }
        }

        Text(
          text = "Your Story",
          style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 11.sp,
            color = if (isCandleMode) BloomCandleDarkMuted else BloomPlum
          ),
          maxLines = 1
        )
      }
    }

    // Story circles for other users
    items(stories, key = { it.id }) { story ->
      val ringBrush = if (story.isViewedByMe) {
        Brush.linearGradient(listOf(Color.LightGray.copy(alpha = 0.6f), Color.LightGray.copy(alpha = 0.6f)))
      } else {
        Brush.sweepGradient(listOf(BloomWarmGold, BloomRose, BloomLavender, BloomWarmGold))
      }

      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
          .clickable { onStoryClick(story) }
          .testTag("story_item_${story.id}")
      ) {
        Box(
          modifier = Modifier
            .size(64.dp)
            .border(2.5.dp, ringBrush, CircleShape)
            .padding(3.dp)
            .clip(CircleShape)
            .background(
              Brush.linearGradient(listOf(Color(story.avatarColorStart), Color(story.avatarColorEnd)))
            ),
          contentAlignment = Alignment.Center
        ) {
          if (story.storyType == StoryType.TEXT_STATUS && story.moodEmoji != null) {
            Text(story.moodEmoji, fontSize = 22.sp)
          } else {
            Text(
              text = story.userInitials,
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            )
          }
        }

        Text(
          text = story.userName.split(" ").firstOrNull() ?: story.userName,
          style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 11.sp,
            fontWeight = if (!story.isViewedByMe) FontWeight.Bold else FontWeight.Normal,
            color = if (isCandleMode) BloomCandleDarkMuted else BloomPlum
          ),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }
  }
}

@Composable
fun StoryViewerDialog(
  story: StoryItem,
  isCandleMode: Boolean,
  onDismiss: () -> Unit,
  onReplyInChat: (String) -> Unit,
  onReportStory: (String) -> Unit
) {
  var replyText by remember { mutableStateOf("") }
  var showViewersDialog by remember { mutableStateOf(false) }
  var showReportDialog by remember { mutableStateOf(false) }
  var reportReason by remember { mutableStateOf("") }

  // 5-second automatic story progress simulation
  var progress by remember { mutableFloatStateOf(0f) }
  LaunchedEffect(story.id) {
    val startTime = System.currentTimeMillis()
    val duration = 5000L
    while (System.currentTimeMillis() - startTime < duration) {
      progress = (System.currentTimeMillis() - startTime).toFloat() / duration
      kotlinx.coroutines.delay(50)
    }
    progress = 1f
    onDismiss()
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.95f))
    ) {
      // Story Content Canvas
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              listOf(Color(story.bgGradientStart), Color(story.bgGradientEnd))
            )
          ),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
          modifier = Modifier.padding(32.dp)
        ) {
          if (story.moodEmoji != null) {
            Text(story.moodEmoji, fontSize = 64.sp)
            Spacer(modifier = Modifier.height(16.dp))
          }
          if (story.textCaption.isNotEmpty()) {
            Text(
              text = story.textCaption,
              style = MaterialTheme.typography.headlineMedium.copy(
                fontStyle = FontStyle.Italic,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
              )
            )
          }
          if (story.storyType == StoryType.PHOTO) {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = Color.White.copy(alpha = 0.2f),
              border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
              modifier = Modifier.padding(16.dp)
            ) {
              Text(
                "📸 Romantic Memory · Expires in 24h",
                style = MaterialTheme.typography.labelSmall.copy(color = Color.White),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
              )
            }
          }
        }
      }

      // Top Overlay: Progress Bar + User Profile + Close & Report buttons
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .align(Alignment.TopCenter)
          .padding(top = 40.dp, start = 16.dp, end = 16.dp)
      ) {
        LinearProgressIndicator(
          progress = { progress },
          modifier = Modifier
            .fillMaxWidth()
            .height(3.dp)
            .clip(CircleShape),
          color = Color.White,
          trackColor = Color.White.copy(alpha = 0.3f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.3f)),
              contentAlignment = Alignment.Center
            ) {
              Text(story.userInitials, color = Color.White, fontWeight = FontWeight.Bold)
            }
            Column {
              Text(
                story.userName,
                style = MaterialTheme.typography.titleSmall.copy(
                  color = Color.White,
                  fontWeight = FontWeight.Bold
                )
              )
              Text(
                "Shared 2h ago · 24h Story",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = Color.White.copy(alpha = 0.8f),
                  fontSize = 10.sp
                )
              )
            }
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { showReportDialog = true }) {
              Icon(Icons.Default.Flag, contentDescription = "Report story", tint = Color.White.copy(alpha = 0.8f))
            }
            IconButton(onClick = onDismiss) {
              Icon(Icons.Default.Close, contentDescription = "Close story", tint = Color.White)
            }
          }
        }
      }

      // Bottom Overlay: Viewers Pill & Reply input
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .align(Alignment.BottomCenter)
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Story Viewers Count Pill
        Surface(
          shape = RoundedCornerShape(9999.dp),
          color = Color.Black.copy(alpha = 0.45f),
          border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
          modifier = Modifier
            .clickable { showViewersDialog = true }
            .align(Alignment.CenterHorizontally)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(Icons.Default.Visibility, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            Text(
              "${story.viewsCount} admirers viewed · Tap to inspect",
              style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontSize = 11.sp)
            )
          }
        }

        // Quick Reply Bar into chat
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = replyText,
            onValueChange = { replyText = it },
            placeholder = { Text("Send sincere reply...", color = Color.White.copy(alpha = 0.6f)) },
            shape = RoundedCornerShape(9999.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = BloomWarmGold,
              unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White
            ),
            modifier = Modifier.weight(1f)
          )

          IconButton(
            onClick = {
              if (replyText.isNotBlank()) {
                onReplyInChat("Replying to story: \"${story.textCaption}\"\n$replyText")
                replyText = ""
                onDismiss()
              }
            },
            modifier = Modifier
              .size(48.dp)
              .background(BloomRose, CircleShape)
          ) {
            Icon(Icons.Default.Send, contentDescription = "Send reply", tint = Color.White)
          }
        }
      }
    }

    // Story Viewers Modal
    if (showViewersDialog) {
      AlertDialog(
        onDismissRequest = { showViewersDialog = false },
        title = { Text("Story Viewers (${story.viewsCount})", fontStyle = FontStyle.Italic) },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("People who viewed this story:", style = MaterialTheme.typography.bodySmall)
            story.viewersList.take(6).forEach { viewer ->
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(viewer.avatarColorStart), Color(viewer.avatarColorEnd)))),
                  contentAlignment = Alignment.Center
                ) {
                  Text(viewer.userInitials, color = Color.White, fontSize = 12.sp)
                }
                Column {
                  Text(viewer.userName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                  Text(viewer.viewedAt, style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
                }
              }
            }
          }
        },
        confirmButton = {
          TextButton(onClick = { showViewersDialog = false }) {
            Text("Close", color = BloomRose)
          }
        }
      )
    }

    // Story Report Dialog
    if (showReportDialog) {
      AlertDialog(
        onDismissRequest = { showReportDialog = false },
        title = { Text("Report Inappropriate Story", fontStyle = FontStyle.Italic) },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Help safeguard our romantic, 18+ community:", style = MaterialTheme.typography.bodySmall)
            OutlinedTextField(
              value = reportReason,
              onValueChange = { reportReason = it },
              placeholder = { Text("Reason (e.g. offensive, spam, contact request)") },
              modifier = Modifier.fillMaxWidth()
            )
          }
        },
        confirmButton = {
          Button(
            onClick = {
              onReportStory(reportReason.ifEmpty { "Community policy violation" })
              showReportDialog = false
            },
            colors = ButtonDefaults.buttonColors(containerColor = BloomDanger)
          ) {
            Text("Report & Block")
          }
        },
        dismissButton = {
          TextButton(onClick = { showReportDialog = false }) {
            Text("Cancel")
          }
        }
      )
    }
  }
}
