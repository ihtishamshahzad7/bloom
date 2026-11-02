package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.FeedPostPoll
import com.example.model.PollOption
import com.example.model.PostAudience
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostComposerDialog(
  isCandleMode: Boolean,
  onDismiss: () -> Unit,
  onPublishPost: (
    caption: String,
    photoGradients: List<Pair<Long, Long>>,
    hashtags: List<String>,
    mentions: List<String>,
    audience: PostAudience,
    locationCity: String?,
    isStory: Boolean,
    poll: FeedPostPoll?
  ) -> Unit
) {
  var caption by remember { mutableStateOf("") }
  var selectedAudience by remember { mutableStateOf(PostAudience.EVERYONE) }
  var locationCity by remember { mutableStateOf<String?>("Lahore") }
  var postAsStory by remember { mutableStateOf(false) }

  // Up to 6 photos represented by aesthetic blush/candle palettes
  val availablePhotoPresets = listOf(
    Pair(0xFFF28DB0, 0xFF9E77ED),
    Pair(0xFFFF9BB3, 0xFFFFD3C2),
    Pair(0xFFE85D8A, 0xFFF6C177),
    Pair(0xFF9E77ED, 0xFF6172F3),
    Pair(0xFFFF8FA3, 0xFFCDB8F0),
    Pair(0xFF45244E, 0xFF2B1632)
  )
  val selectedPhotos = remember { mutableStateListOf<Pair<Long, Long>>() }

  // Poll state
  var isPollEnabled by remember { mutableStateOf(false) }
  var pollQuestion by remember { mutableStateOf("") }
  var pollOpt1 by remember { mutableStateOf("") }
  var pollOpt2 by remember { mutableStateOf("") }

  var errorMessage by remember { mutableStateOf<String?>(null) }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      shape = RoundedCornerShape(28.dp),
      color = if (isCandleMode) BloomCandleDarkBg else Color(0xFFFFF6F8),
      border = BorderStroke(
        1.5.dp,
        if (isCandleMode) BloomWarmGold.copy(alpha = 0.4f) else BloomRose.copy(alpha = 0.35f)
      ),
      modifier = Modifier
        .fillMaxWidth(0.94f)
        .fillMaxHeight(0.9f)
        .shadow(24.dp, RoundedCornerShape(28.dp))
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(20.dp)
      ) {
        // Top Bar
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Cancel", tint = if (isCandleMode) Color.White else BloomPlum)
          }

          Text(
            text = "Create Post ✿",
            style = MaterialTheme.typography.titleLarge.copy(
              fontStyle = FontStyle.Italic,
              fontWeight = FontWeight.Bold,
              color = if (isCandleMode) BloomWarmGold else BloomDeepRose
            )
          )

          Button(
            onClick = {
              if (caption.isBlank() && selectedPhotos.isEmpty() && (!isPollEnabled || pollQuestion.isBlank())) {
                errorMessage = "Please enter thoughts, pick photos, or create a poll."
                return@Button
              }
              // Extract hashtags and mentions
              val hashtags = Regex("#\\w+").findAll(caption).map { it.value }.toList()
              val mentions = Regex("@\\w+").findAll(caption).map { it.value }.toList()

              val poll = if (isPollEnabled && pollQuestion.isNotBlank() && pollOpt1.isNotBlank() && pollOpt2.isNotBlank()) {
                FeedPostPoll(
                  question = pollQuestion,
                  options = listOf(
                    PollOption(1, pollOpt1, 0),
                    PollOption(2, pollOpt2, 0)
                  )
                )
              } else null

              onPublishPost(
                caption,
                selectedPhotos.toList(),
                hashtags,
                mentions,
                selectedAudience,
                locationCity,
                postAsStory,
                poll
              )
            },
            colors = ButtonDefaults.buttonColors(containerColor = BloomRose),
            shape = RoundedCornerShape(9999.dp),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
            modifier = Modifier.testTag("publish_post_button")
          ) {
            Text("Publish", fontWeight = FontWeight.Bold, color = Color.White)
          }
        }

        HorizontalDivider(
          color = if (isCandleMode) BloomWarmGold.copy(alpha = 0.2f) else BloomRose.copy(alpha = 0.2f),
          modifier = Modifier.padding(vertical = 10.dp)
        )

        Column(
          modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          // Author & Audience Row
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
                  .size(44.dp)
                  .clip(CircleShape)
                  .background(Brush.linearGradient(listOf(Color(0xFFF28DB0), Color(0xFF9E77ED)))),
                contentAlignment = Alignment.Center
              ) {
                Text("M", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
              }

              Column {
                Text(
                  text = "Mia Rizvi 🌸",
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isCandleMode) Color.White else BloomPlum
                  )
                )
                Text(
                  text = "@mia_blossom • Verified 🛡️",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isCandleMode) BloomWarmGold else BloomRose,
                    fontSize = 11.sp
                  )
                )
              }
            }

            // Audience selector dropdown chips
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              PostAudience.values().forEach { aud ->
                FilterChip(
                  selected = selectedAudience == aud,
                  onClick = { selectedAudience = aud },
                  label = { Text(aud.label, fontSize = 10.sp) },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = BloomRose,
                    selectedLabelColor = Color.White
                  )
                )
              }
            }
          }

          // Caption Input Field
          OutlinedTextField(
            value = caption,
            onValueChange = {
              caption = it
              errorMessage = null
            },
            placeholder = {
              Text(
                "What is blossoming in your thoughts? Share an uplifting reflection, poem, or heartfelt question...",
                color = if (isCandleMode) BloomCandleDarkMuted else BloomPlumMuted,
                fontStyle = FontStyle.Italic
              )
            },
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(min = 110.dp),
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = BloomRose,
              unfocusedBorderColor = if (isCandleMode) Color.White.copy(alpha = 0.2f) else BloomBlushMid
            )
          )

          // Multi-photo selection (up to 6)
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                "Add Photos (up to 6):",
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = if (isCandleMode) BloomWarmGold else BloomDeepRose
                )
              )
              Text(
                "${selectedPhotos.size}/6 selected",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = if (isCandleMode) BloomCandleDarkMuted else BloomPlumMuted
                )
              )
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              itemsIndexed(availablePhotoPresets) { idx, preset ->
                val isSelected = selectedPhotos.contains(preset)
                Box(
                  modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(Color(preset.first), Color(preset.second))))
                    .border(
                      width = if (isSelected) 3.dp else 1.dp,
                      color = if (isSelected) Color.White else Color.Transparent,
                      shape = RoundedCornerShape(14.dp)
                    )
                    .clickable {
                      if (isSelected) {
                        selectedPhotos.remove(preset)
                      } else {
                        if (selectedPhotos.size < 6) {
                          selectedPhotos.add(preset)
                        }
                      }
                    },
                  contentAlignment = Alignment.Center
                ) {
                  if (isSelected) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = Color.White)
                  } else {
                    Icon(Icons.Outlined.AddPhotoAlternate, contentDescription = "Add", tint = Color.White.copy(alpha = 0.7f))
                  }
                }
              }
            }
          }

          // Location Tagging (City Only, Zero Exact Coordinates)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(Icons.Outlined.LocationOn, contentDescription = "City", tint = BloomRose, modifier = Modifier.size(18.dp))
              Text("City Tag:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              listOf("Lahore", "Islamabad", "Karachi", null).forEach { city ->
                FilterChip(
                  selected = locationCity == city,
                  onClick = { locationCity = city },
                  label = { Text(city ?: "None", fontSize = 11.sp) }
                )
              }
            }
          }

          // "Post as 24h Story" toggle
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isCandleMode) Color(0xFF331838) else BloomBlush.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  "Post as 24h Story",
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                  "Also share as an ephemeral status update",
                  style = MaterialTheme.typography.labelSmall.copy(color = BloomPlumMuted, fontSize = 10.sp)
                )
              }
              Switch(
                checked = postAsStory,
                onCheckedChange = { postAsStory = it },
                colors = SwitchDefaults.colors(checkedThumbColor = BloomRose, checkedTrackColor = BloomPeach)
              )
            }
          }

          // Poll Creator Toggle
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isCandleMode) Color(0xFF331838) else BloomBlush.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Icon(Icons.Outlined.Poll, contentDescription = "Poll", tint = BloomRose, modifier = Modifier.size(18.dp))
                  Text("Add Interactive Poll", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                }
                Switch(
                  checked = isPollEnabled,
                  onCheckedChange = { isPollEnabled = it },
                  colors = SwitchDefaults.colors(checkedThumbColor = BloomRose, checkedTrackColor = BloomPeach)
                )
              }

              if (isPollEnabled) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                  value = pollQuestion,
                  onValueChange = { pollQuestion = it },
                  placeholder = { Text("Ask a question for your circle...") },
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                  value = pollOpt1,
                  onValueChange = { pollOpt1 = it },
                  placeholder = { Text("Option 1") },
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                  value = pollOpt2,
                  onValueChange = { pollOpt2 = it },
                  placeholder = { Text("Option 2") },
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(12.dp)
                )
              }
            }
          }

          // Error or Safety notice
          if (errorMessage != null) {
            Text(
              text = errorMessage ?: "",
              color = BloomDanger,
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
            )
          }

          // Community Safety & 18+ Guarantee Note
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Outlined.Security, contentDescription = "Safe", tint = BloomOnline, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              "Protected by automated keyword filtering. 18+ only.",
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                color = if (isCandleMode) BloomCandleDarkMuted else BloomPlumMuted
              )
            )
          }
        }
      }
    }
  }
}
