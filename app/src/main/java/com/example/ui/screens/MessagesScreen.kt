package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.NeumorphicGlassCard
import com.example.ui.components.StoriesRow
import com.example.ui.theme.*

@Composable
fun MessagesScreen(
  profiles: List<UserProfile>,
  messagesMap: Map<String, List<ChatMessage>>,
  selectedProfileId: String?,
  stories: List<StoryItem>,
  stickerPacks: List<StickerPack>,
  liveLocationState: LiveLocationState,
  isCandleMode: Boolean,
  onSelectProfile: (String?) -> Unit,
  onSendRichMessage: (String, ChatMessage) -> Unit,
  onAddReaction: (String, String, String) -> Unit,
  onDeleteMessage: (String, String, Boolean) -> Unit,
  onGrantMediaConsent: (String, String) -> Unit,
  onMarkDisappearingViewed: (String, String) -> Unit,
  onFreezeStreak: (String) -> Unit,
  onStartLiveLocation: (String) -> Unit,
  onStopLiveLocation: () -> Unit,
  onBlockUser: (String) -> Unit,
  onAddStoryClick: () -> Unit,
  onStoryClick: (StoryItem) -> Unit,
  modifier: Modifier = Modifier
) {
  val activeProfile = profiles.find { it.id == selectedProfileId }

  if (activeProfile != null) {
    BackHandler {
      onSelectProfile(null)
    }

    RichChatThreadView(
      profile = activeProfile,
      messages = messagesMap[activeProfile.id] ?: emptyList(),
      stickerPacks = stickerPacks,
      liveLocationState = liveLocationState,
      isCandleMode = isCandleMode,
      onBack = { onSelectProfile(null) },
      onSendMessage = { chatMsg -> onSendRichMessage(activeProfile.id, chatMsg) },
      onAddReaction = { msgId, emoji -> onAddReaction(activeProfile.id, msgId, emoji) },
      onDeleteMessage = { msgId, forEveryone -> onDeleteMessage(activeProfile.id, msgId, forEveryone) },
      onGrantMediaConsent = { msgId -> onGrantMediaConsent(activeProfile.id, msgId) },
      onMarkDisappearingViewed = { msgId -> onMarkDisappearingViewed(activeProfile.id, msgId) },
      onFreezeStreak = { onFreezeStreak(activeProfile.id) },
      onStartLiveLocation = { onStartLiveLocation(activeProfile.name) },
      onStopLiveLocation = onStopLiveLocation,
      onBlockUser = {
        onBlockUser(activeProfile.name)
        onSelectProfile(null)
      },
      modifier = modifier
    )
  } else {
    ChatConversationsList(
      profiles = profiles,
      messagesMap = messagesMap,
      stories = stories,
      isCandleMode = isCandleMode,
      onSelectProfile = onSelectProfile,
      onAddStoryClick = onAddStoryClick,
      onStoryClick = onStoryClick,
      modifier = modifier
    )
  }
}

@Composable
private fun ChatConversationsList(
  profiles: List<UserProfile>,
  messagesMap: Map<String, List<ChatMessage>>,
  stories: List<StoryItem>,
  isCandleMode: Boolean,
  onSelectProfile: (String) -> Unit,
  onAddStoryClick: () -> Unit,
  onStoryClick: (StoryItem) -> Unit,
  modifier: Modifier = Modifier
) {
  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Gentle Conversations 💬",
            style = MaterialTheme.typography.headlineMedium.copy(
              fontStyle = FontStyle.Italic,
              fontWeight = FontWeight.Bold,
              color = if (isCandleMode) BloomWarmGold else BloomDeepRose
            )
          )
          Text(
            text = "End-to-end protected · 24h Bloom streaks active",
            style = MaterialTheme.typography.bodySmall.copy(
              color = if (isCandleMode) BloomCandleDarkMuted else BloomPlum
            )
          )
        }
      }
    }

    // Top Stories Row (Instagram-style)
    item {
      StoriesRow(
        stories = stories,
        isCandleMode = isCandleMode,
        onAddStoryClick = onAddStoryClick,
        onStoryClick = onStoryClick
      )
    }

    item {
      HorizontalDivider(
        color = if (isCandleMode) Color.White.copy(alpha = 0.1f) else BloomPlumMuted.copy(alpha = 0.2f),
        thickness = 1.dp
      )
    }

    val mutualMatches = profiles.filter { it.isMutualMatch }

    if (mutualMatches.isEmpty()) {
      item {
        EmptyChatPlaceholder(isCandleMode = isCandleMode)
      }
    } else {
      items(mutualMatches, key = { it.id }) { profile ->
        val thread = messagesMap[profile.id] ?: emptyList()
        val lastMsg = thread.lastOrNull()
        val streak = profile.bloomStreak

        NeumorphicGlassCard(
          isCandleMode = isCandleMode,
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectProfile(profile.id) }
            .testTag("chat_item_${profile.id}")
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            // Avatar with Online dot & Streak badge
            Box {
              Box(
                modifier = Modifier
                  .size(56.dp)
                  .shadow(4.dp, CircleShape)
                  .clip(CircleShape)
                  .background(
                    Brush.linearGradient(
                      listOf(Color(profile.avatarColorStart), Color(profile.avatarColorEnd))
                    )
                  ),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  profile.initials,
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                )
              }

              // Green dot if online
              if (profile.isOnline) {
                Box(
                  modifier = Modifier
                    .size(14.dp)
                    .background(BloomOnline, CircleShape)
                    .border(2.dp, if (isCandleMode) BloomCandleDarkBg else Color.White, CircleShape)
                    .align(Alignment.BottomEnd)
                )
              }
            }

            Column(modifier = Modifier.weight(1f)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Text(
                    text = profile.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                      fontWeight = FontWeight.Bold,
                      color = if (isCandleMode) Color.White else BloomPlum
                    )
                  )
                  if (profile.isVerified) {
                    Icon(
                      imageVector = Icons.Default.CheckCircle,
                      contentDescription = "Verified Identity",
                      tint = BloomOnline,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                }

                // Time / Streak chip
                Text(
                  text = lastMsg?.timestamp ?: "Just now",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isCandleMode) BloomCandleDarkMuted else BloomPlumMuted,
                    fontSize = 11.sp
                  )
                )
              }

              Spacer(modifier = Modifier.height(4.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = when (lastMsg?.messageType) {
                    MessageType.PHOTO -> "📷 Photo message"
                    MessageType.SELFIE -> "🤳 Real-time selfie"
                    MessageType.VIDEO -> "🎥 Video message (0:30)"
                    MessageType.VOICE_NOTE -> "🎙️ Voice reflection"
                    MessageType.LOCATION_PIN -> "📍 Location shared"
                    MessageType.STICKER -> "🌹 Sticker sent"
                    MessageType.DISAPPEARING_MEDIA -> "⏳ View once photo"
                    else -> lastMsg?.text ?: "Mutual bloom! Say Salam..."
                  },
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = if (isCandleMode) BloomCandleDarkMuted else BloomPlumMuted,
                    fontWeight = if (lastMsg != null && !lastMsg.isRead) FontWeight.Bold else FontWeight.Normal
                  ),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.weight(1f)
                )

                // Streak Flame Badge (🔥 14 ⏳)
                if (streak.currentStreakDays > 0) {
                  Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = if (streak.isExpiringSoon) BloomDanger.copy(alpha = 0.2f) else BloomCandleGold.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, if (streak.isExpiringSoon) BloomDanger else BloomCandleGold.copy(alpha = 0.5f))
                  ) {
                    Row(
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                      Text("🔥 ${streak.currentStreakDays}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isCandleMode) BloomWarmGold else BloomDeepRose)
                      if (streak.isExpiringSoon) {
                        Text("⏳", fontSize = 11.sp)
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RichChatThreadView(
  profile: UserProfile,
  messages: List<ChatMessage>,
  stickerPacks: List<StickerPack>,
  liveLocationState: LiveLocationState,
  isCandleMode: Boolean,
  onBack: () -> Unit,
  onSendMessage: (ChatMessage) -> Unit,
  onAddReaction: (String, String) -> Unit,
  onDeleteMessage: (String, Boolean) -> Unit,
  onGrantMediaConsent: (String) -> Unit,
  onMarkDisappearingViewed: (String) -> Unit,
  onFreezeStreak: () -> Unit,
  onStartLiveLocation: () -> Unit,
  onStopLiveLocation: () -> Unit,
  onBlockUser: () -> Unit,
  modifier: Modifier = Modifier
) {
  var inputText by remember { mutableStateOf("") }
  var showAttachmentSheet by remember { mutableStateOf(false) }
  var showStickerKeyboard by remember { mutableStateOf(false) }
  var isRecordingVoice by remember { mutableStateOf(false) }
  var isViewOnceSelected by remember { mutableStateOf(false) }
  var replyToMessage by remember { mutableStateOf<ChatMessage?>(null) }
  var selectedMessageForMenu by remember { mutableStateOf<ChatMessage?>(null) }
  var showMilestoneCelebration by remember { mutableStateOf(false) }
  var showScreenshotNotice by remember { mutableStateOf(false) }

  val streak = profile.bloomStreak

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(bottom = 80.dp)
  ) {
    // Top Chat App Bar
    Surface(
      color = if (isCandleMode) BloomCandleDarkBg.copy(alpha = 0.95f) else Color.White.copy(alpha = 0.9f),
      shadowElevation = 4.dp
    ) {
      Column {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          IconButton(onClick = onBack) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = if (isCandleMode) Color.White else BloomPlum
            )
          }

          Box {
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(profile.avatarColorStart), Color(profile.avatarColorEnd)))),
              contentAlignment = Alignment.Center
            ) {
              Text(profile.initials, color = Color.White, fontWeight = FontWeight.Bold)
            }
            if (profile.isOnline) {
              Box(
                modifier = Modifier
                  .size(11.dp)
                  .background(BloomOnline, CircleShape)
                  .border(1.5.dp, Color.White, CircleShape)
                  .align(Alignment.BottomEnd)
              )
            }
          }

          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              Text(
                profile.name,
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = if (isCandleMode) Color.White else BloomPlum
                )
              )
              if (profile.isVerified) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = BloomOnline, modifier = Modifier.size(15.dp))
              }
            }
            Text(
              text = if (profile.isOnline) "Active now · End-to-end encrypted" else profile.lastSeen,
              style = MaterialTheme.typography.labelSmall.copy(
                color = if (profile.isOnline) BloomOnline else (if (isCandleMode) BloomCandleDarkMuted else BloomPlumMuted),
                fontSize = 11.sp
              )
            )
          }

          // Streak Badge & Milestone Trigger in Header
          if (streak.currentStreakDays > 0) {
            Surface(
              shape = RoundedCornerShape(9999.dp),
              color = if (streak.isExpiringSoon) BloomDanger.copy(alpha = 0.2f) else BloomCandleGold.copy(alpha = 0.2f),
              border = BorderStroke(1.dp, if (streak.isExpiringSoon) BloomDanger else BloomCandleGold),
              modifier = Modifier.clickable { showMilestoneCelebration = true }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Text("🔥 ${streak.currentStreakDays}d", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isCandleMode) BloomWarmGold else BloomDeepRose)
                if (streak.isExpiringSoon) {
                  Text("⏳", fontSize = 11.sp)
                }
              }
            }
          }

          var showMoreMenu by remember { mutableStateOf(false) }
          Box {
            IconButton(onClick = { showMoreMenu = true }) {
              Icon(Icons.Outlined.MoreVert, contentDescription = "Options", tint = if (isCandleMode) Color.White else BloomPlum)
            }
            DropdownMenu(expanded = showMoreMenu, onDismissRequest = { showMoreMenu = false }) {
              DropdownMenuItem(
                text = { Text("Share Live Location (15m)") },
                onClick = {
                  showMoreMenu = false
                  onStartLiveLocation()
                },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = BloomRose) }
              )
              DropdownMenuItem(
                text = { Text("Use Streak Freeze ❄️") },
                onClick = {
                  showMoreMenu = false
                  onFreezeStreak()
                }
              )
              DropdownMenuItem(
                text = { Text("Report & Block User", color = BloomDanger) },
                onClick = {
                  showMoreMenu = false
                  onBlockUser()
                },
                leadingIcon = { Icon(Icons.Default.Block, contentDescription = null, tint = BloomDanger) }
              )
            }
          }
        }

        // Live Location Banner if sharing
        if (liveLocationState.isSharing) {
          Surface(
            color = BloomRose.copy(alpha = 0.15f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Navigation, contentDescription = null, tint = BloomRose, modifier = Modifier.size(16.dp))
                Text(
                  "Sharing live approximate location (${liveLocationState.remainingMinutes}m)",
                  style = MaterialTheme.typography.labelSmall.copy(color = BloomRose, fontWeight = FontWeight.Bold)
                )
              }
              TextButton(onClick = onStopLiveLocation) {
                Text("Stop", color = BloomDanger, fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }

    // Messages List
    LazyColumn(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      reverseLayout = false,
      verticalArrangement = Arrangement.spacedBy(10.dp),
      contentPadding = PaddingValues(vertical = 12.dp)
    ) {
      items(messages, key = { it.id }) { msg ->
        RichMessageBubble(
          message = msg,
          isCandleMode = isCandleMode,
          onLongPress = { selectedMessageForMenu = msg },
          onGrantConsent = { onGrantMediaConsent(msg.id) },
          onViewDisappearing = {
            onMarkDisappearingViewed(msg.id)
            showScreenshotNotice = true
          }
        )
      }
    }

    // Quoted Reply Preview Card
    if (replyToMessage != null) {
      Surface(
        color = if (isCandleMode) Color(0xFF3B1F42) else BloomPeach.copy(alpha = 0.5f),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text("Replying to ${replyToMessage?.senderName}", style = MaterialTheme.typography.labelSmall.copy(color = BloomRose, fontWeight = FontWeight.Bold))
            Text(replyToMessage?.text ?: "", style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
          }
          IconButton(onClick = { replyToMessage = null }) {
            Icon(Icons.Default.Close, contentDescription = "Cancel reply", modifier = Modifier.size(16.dp))
          }
        }
      }
    }

    // Bottom Input Bar
    Surface(
      color = if (isCandleMode) BloomCandleDarkBg else Color.White,
      shadowElevation = 8.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          // + Attachment Button
          IconButton(
            onClick = { showAttachmentSheet = true },
            modifier = Modifier
              .size(38.dp)
              .background(if (isCandleMode) Color.White.copy(alpha = 0.1f) else BloomBlush, CircleShape)
          ) {
            Icon(Icons.Default.Add, contentDescription = "Add Attachment", tint = BloomRose)
          }

          // Sticker / Emoji Button
          IconButton(
            onClick = { showStickerKeyboard = !showStickerKeyboard },
            modifier = Modifier.size(38.dp)
          ) {
            Text("🌹", fontSize = 18.sp)
          }

          // Disappearing Media "1" View Once Toggle Button
          IconButton(
            onClick = { isViewOnceSelected = !isViewOnceSelected },
            modifier = Modifier
              .size(38.dp)
              .background(
                if (isViewOnceSelected) BloomRose.copy(alpha = 0.2f) else Color.Transparent,
                CircleShape
              )
              .border(
                1.dp,
                if (isViewOnceSelected) BloomRose else Color.Gray.copy(alpha = 0.3f),
                CircleShape
              )
          ) {
            Text(
              "①",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = if (isViewOnceSelected) BloomRose else (if (isCandleMode) Color.White else Color.Gray)
            )
          }

          // Text Field
          OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            placeholder = { Text("Gentle message...", color = Color.Gray.copy(alpha = 0.6f)) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(9999.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = BloomRose,
              unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
            )
          )

          // Send / Mic Button
          if (inputText.isNotBlank()) {
            IconButton(
              onClick = {
                val newMsg = ChatMessage(
                  id = "m_${System.currentTimeMillis()}",
                  matchId = profile.id,
                  senderName = "Mia",
                  text = inputText,
                  isMine = true,
                  isRead = true,
                  isDelivered = true,
                  messageType = if (isViewOnceSelected) MessageType.DISAPPEARING_MEDIA else MessageType.TEXT,
                  isViewOnce = isViewOnceSelected,
                  replyToMessageId = replyToMessage?.id,
                  replyToText = replyToMessage?.text,
                  replyToSenderName = replyToMessage?.senderName
                )
                onSendMessage(newMsg)
                inputText = ""
                isViewOnceSelected = false
                replyToMessage = null
              },
              modifier = Modifier
                .size(42.dp)
                .background(BloomRose, CircleShape)
            ) {
              Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
            }
          } else {
            // Voice Note Hold-to-Record Simulation
            IconButton(
              onClick = {
                val voiceMsg = ChatMessage(
                  id = "m_v_${System.currentTimeMillis()}",
                  matchId = profile.id,
                  senderName = "Mia",
                  text = "Voice note (0:18)",
                  isMine = true,
                  isRead = true,
                  isDelivered = true,
                  messageType = MessageType.VOICE_NOTE,
                  voiceDurationSeconds = 18
                )
                onSendMessage(voiceMsg)
              },
              modifier = Modifier
                .size(42.dp)
                .background(Brush.linearGradient(listOf(BloomRose, BloomWarmGold)), CircleShape)
            ) {
              Icon(Icons.Outlined.Mic, contentDescription = "Record Voice", tint = Color.White)
            }
          }
        }

        // Sticker & Emoji Keyboard Drawer
        if (showStickerKeyboard) {
          StickerKeyboardDrawer(
            stickerPacks = stickerPacks,
            isCandleMode = isCandleMode,
            onSelectSticker = { pack, sticker ->
              val stickerMsg = ChatMessage(
                id = "m_stk_${System.currentTimeMillis()}",
                matchId = profile.id,
                senderName = "Mia",
                text = "${sticker.iconEmoji} ${sticker.label}",
                isMine = true,
                isRead = true,
                isDelivered = true,
                messageType = MessageType.STICKER,
                stickerPack = pack.name,
                stickerName = sticker.label,
                stickerEmoji = sticker.iconEmoji
              )
              onSendMessage(stickerMsg)
              showStickerKeyboard = false
            }
          )
        }
      }
    }
  }

  // Attachment Sheet Modal
  if (showAttachmentSheet) {
    ModalBottomSheet(onDismissRequest = { showAttachmentSheet = false }) {
      AttachmentSheetContent(
        isCandleMode = isCandleMode,
        onActionSelected = { action ->
          showAttachmentSheet = false
          when (action) {
            "selfie" -> {
              onSendMessage(
                ChatMessage(
                  id = "m_selfie_${System.currentTimeMillis()}",
                  matchId = profile.id,
                  senderName = "Mia",
                  text = "Verified Liveness Selfie",
                  isMine = true,
                  isRead = true,
                  messageType = MessageType.SELFIE,
                  mediaUrl = "selfie_verified_stream"
                )
              )
            }
            "photo" -> {
              onSendMessage(
                ChatMessage(
                  id = "m_photo_${System.currentTimeMillis()}",
                  matchId = profile.id,
                  senderName = "Mia",
                  text = "Chai moment with family 🌸",
                  isMine = true,
                  isRead = true,
                  messageType = MessageType.PHOTO,
                  mediaUrl = "gallery_photo_stream"
                )
              )
            }
            "video" -> {
              onSendMessage(
                ChatMessage(
                  id = "m_video_${System.currentTimeMillis()}",
                  matchId = profile.id,
                  senderName = "Mia",
                  text = "Rain in Lahore gardens (0:24)",
                  isMine = true,
                  isRead = true,
                  messageType = MessageType.VIDEO,
                  mediaUrl = "video_rain_stream"
                )
              )
            }
            "location" -> {
              onSendMessage(
                ChatMessage(
                  id = "m_loc_${System.currentTimeMillis()}",
                  matchId = profile.id,
                  senderName = "Mia",
                  text = "Approximate Area: Gulberg III, Lahore",
                  isMine = true,
                  isRead = true,
                  messageType = MessageType.LOCATION_PIN,
                  locationTitle = "Gulberg III Conservatory",
                  locationAddress = "Lahore, Punjab (~3 km away)",
                  locationDistance = "~3 km away"
                )
              )
            }
          }
        }
      )
    }
  }

  // Long-press Context Actions Modal
  if (selectedMessageForMenu != null) {
    val msg = selectedMessageForMenu!!
    AlertDialog(
      onDismissRequest = { selectedMessageForMenu = null },
      title = { Text("Message Actions", fontStyle = FontStyle.Italic) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          // Reaction strip
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
          ) {
            listOf("🌹", "💖", "😂", "🔥", "✨").forEach { emoji ->
              Text(
                emoji,
                fontSize = 24.sp,
                modifier = Modifier
                  .clickable {
                    onAddReaction(msg.id, emoji)
                    selectedMessageForMenu = null
                  }
                  .padding(4.dp)
              )
            }
          }
          HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
          TextButton(
            onClick = {
              replyToMessage = msg
              selectedMessageForMenu = null
            },
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("Reply to Message", color = BloomRose)
          }
          TextButton(
            onClick = {
              onDeleteMessage(msg.id, false)
              selectedMessageForMenu = null
            },
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("Delete for Me")
          }
          if (msg.isMine) {
            TextButton(
              onClick = {
                onDeleteMessage(msg.id, true)
                selectedMessageForMenu = null
              },
              modifier = Modifier.fillMaxWidth()
            ) {
              Text("Delete for Everyone", color = BloomDanger)
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { selectedMessageForMenu = null }) {
          Text("Cancel")
        }
      }
    )
  }

  // Milestone Petal Celebration Dialog
  if (showMilestoneCelebration) {
    AlertDialog(
      onDismissRequest = { showMilestoneCelebration = false },
      title = { Text("🌸 ${streak.currentStreakDays}-Day Bloom Milestone!", fontStyle = FontStyle.Italic, color = BloomDeepRose) },
      text = {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text("✨ ✦ 🌸 ✦ ✨", fontSize = 28.sp)
          Text(
            "You and ${profile.name} have maintained daily respectful conversation for ${streak.currentStreakDays} days! Intentional consistency builds lifelong trust.",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
          )
        }
      },
      confirmButton = {
        Button(
          onClick = { showMilestoneCelebration = false },
          colors = ButtonDefaults.buttonColors(containerColor = BloomRose)
        ) {
          Text("Celebrate Together 🌸")
        }
      }
    )
  }

  // Screenshot blocked notice
  if (showScreenshotNotice) {
    AlertDialog(
      onDismissRequest = { showScreenshotNotice = false },
      title = { Text("Disappearing Media Opened", fontStyle = FontStyle.Italic) },
      text = {
        Text("🔒 This media is View Once. Screenshots are strictly blocked via FLAG_SECURE to protect member dignity and privacy.")
      },
      confirmButton = {
        TextButton(onClick = { showScreenshotNotice = false }) {
          Text("Understood", color = BloomRose)
        }
      }
    )
  }
}

@Composable
private fun RichMessageBubble(
  message: ChatMessage,
  isCandleMode: Boolean,
  onLongPress: () -> Unit,
  onGrantConsent: () -> Unit,
  onViewDisappearing: () -> Unit
) {
  val isMine = message.isMine

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .pointerInput(Unit) {
        detectTapGestures(onLongPress = { onLongPress() })
      },
    horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
  ) {
    // Reply preview if quoted
    if (message.replyToText != null) {
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.Black.copy(alpha = 0.08f),
        modifier = Modifier.padding(bottom = 2.dp)
      ) {
        Text(
          "↩ ${message.replyToSenderName}: ${message.replyToText}",
          style = MaterialTheme.typography.labelSmall.copy(fontStyle = FontStyle.Italic),
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
      }
    }

    Surface(
      shape = RoundedCornerShape(
        topStart = 18.dp,
        topEnd = 18.dp,
        bottomStart = if (isMine) 18.dp else 4.dp,
        bottomEnd = if (isMine) 4.dp else 18.dp
      ),
      color = if (isMine) {
        if (isCandleMode) BloomDeepRose else BloomRose
      } else {
        if (isCandleMode) Color(0xFF3B1F42) else Color.White
      },
      shadowElevation = 2.dp,
      modifier = Modifier.widthIn(max = 290.dp)
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
        when (message.messageType) {
          MessageType.TEXT -> {
            Text(
              text = message.text,
              style = MaterialTheme.typography.bodyMedium.copy(
                color = if (isMine) Color.White else (if (isCandleMode) Color.White else BloomPlum)
              )
            )
          }

          MessageType.STICKER -> {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Text(message.stickerEmoji ?: "🌹", fontSize = 36.sp)
              Column {
                Text(
                  message.stickerName ?: "Bloom Rose",
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isMine) Color.White else BloomPlum
                  )
                )
                Text(
                  message.stickerPack ?: "Sticker Pack",
                  style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                )
              }
            }
          }

          MessageType.PHOTO, MessageType.SELFIE -> {
            if (message.mediaConsentRequired && !message.mediaConsentGranted && !isMine) {
              // Consent Blocked
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Text("🔒 Photo Consent Requested", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("Receiver consent required before revealing media.", style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
                Button(
                  onClick = onGrantConsent,
                  colors = ButtonDefaults.buttonColors(containerColor = BloomRose),
                  modifier = Modifier.height(32.dp)
                ) {
                  Text("Tap to View", fontSize = 11.sp)
                }
              }
            } else {
              // Photo / Selfie Canvas
              Box(
                modifier = Modifier
                  .size(240.dp, 160.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .background(Brush.linearGradient(listOf(BloomPeach, BloomLavender))),
                contentAlignment = Alignment.Center
              ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text(if (message.messageType == MessageType.SELFIE) "🤳" else "📸", fontSize = 42.sp)
                  Text(
                    if (message.messageType == MessageType.SELFIE) "Liveness Selfie Verified" else "Romantic Photo",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                  )
                }
              }
              if (message.text.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(message.text, style = MaterialTheme.typography.bodySmall.copy(color = if (isMine) Color.White else BloomPlum))
              }
            }
          }

          MessageType.VIDEO -> {
            Box(
              modifier = Modifier
                .size(240.dp, 150.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.7f)),
              contentAlignment = Alignment.Center
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.PlayCircle, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(42.dp))
                Text("Video (0:24)", style = MaterialTheme.typography.labelSmall.copy(color = Color.White))
              }
            }
          }

          MessageType.VOICE_NOTE -> {
            var isPlaying by remember { mutableStateOf(false) }
            var playbackSpeed by remember { mutableStateOf("1x") }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              IconButton(
                onClick = { isPlaying = !isPlaying },
                modifier = Modifier
                  .size(32.dp)
                  .background(if (isMine) Color.White.copy(alpha = 0.25f) else BloomRose, CircleShape)
              ) {
                Icon(
                  if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(18.dp)
                )
              }

              // Waveform representation
              Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
                message.voiceWaveform.forEach { heightRatio ->
                  Box(
                    modifier = Modifier
                      .width(3.dp)
                      .height((24 * heightRatio).dp)
                      .background(if (isMine) Color.White else BloomRose, RoundedCornerShape(99.dp))
                  )
                }
              }

              Text(
                "0:${message.voiceDurationSeconds}",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = if (isMine) Color.White else BloomPlum,
                  fontSize = 11.sp
                )
              )

              // Playback speed toggle (1x / 1.5x / 2x)
              Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.15f),
                modifier = Modifier.clickable {
                  playbackSpeed = when (playbackSpeed) {
                    "1x" -> "1.5x"
                    "1.5x" -> "2x"
                    else -> "1x"
                  }
                }
              ) {
                Text(
                  playbackSpeed,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isMine) Color.White else BloomPlum,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
          }

          MessageType.LOCATION_PIN -> {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = if (isMine) Color.White else BloomRose)
                Text(
                  message.locationTitle ?: "Approximate Location",
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = if (isMine) Color.White else BloomPlum)
                )
              }
              Text(
                message.locationAddress ?: "Lahore, Punjab (~3 km away)",
                style = MaterialTheme.typography.labelSmall.copy(color = if (isMine) Color.White.copy(alpha = 0.8f) else Color.Gray)
              )
            }
          }

          MessageType.DISAPPEARING_MEDIA -> {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.clickable { onViewDisappearing() }
            ) {
              Text("①", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = if (isMine) Color.White else BloomRose)
              Column {
                Text(
                  if (message.hasBeenViewed) "Viewed Photo" else "View Once Photo",
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isMine) Color.White else BloomPlum
                  )
                )
                Text(
                  if (message.hasBeenViewed) "Expired" else "Tap to view (Screenshot blocked)",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isMine) Color.White.copy(alpha = 0.7f) else Color.Gray,
                    fontSize = 10.sp
                  )
                )
              }
            }
          }

          else -> {}
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Time + Receipts
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = message.timestamp,
            style = MaterialTheme.typography.labelSmall.copy(
              fontSize = 10.sp,
              color = if (isMine) Color.White.copy(alpha = 0.75f) else (if (isCandleMode) BloomCandleDarkMuted else BloomPlumMuted)
            )
          )
          if (isMine) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (message.isRead) "✓✓" else "✓",
              fontSize = 10.sp,
              color = if (message.isRead) Color.White else Color.White.copy(alpha = 0.7f)
            )
          }
        }
      }
    }

    // Reaction badge pill
    if (message.reaction != null) {
      Surface(
        shape = CircleShape,
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.offset(y = (-6).dp, x = if (isMine) (-8).dp else 8.dp)
      ) {
        Text(message.reaction, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
      }
    }

    // Safety Alert Card if flagged
    if (message.safetyAlert != null) {
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = BloomDanger.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, BloomDanger.copy(alpha = 0.3f)),
        modifier = Modifier.padding(top = 4.dp).widthIn(max = 280.dp)
      ) {
        Text(
          text = message.safetyAlert,
          style = MaterialTheme.typography.labelSmall.copy(color = BloomDanger, fontSize = 11.sp),
          modifier = Modifier.padding(8.dp)
        )
      }
    }
  }
}

@Composable
private fun StickerKeyboardDrawer(
  stickerPacks: List<StickerPack>,
  isCandleMode: Boolean,
  onSelectSticker: (StickerPack, StickerItem) -> Unit
) {
  var selectedPackIndex by remember { mutableIntStateOf(0) }
  var searchQuery by remember { mutableStateOf("") }

  Surface(
    color = if (isCandleMode) Color(0xFF28152E) else BloomBlush,
    modifier = Modifier.fillMaxWidth().height(220.dp).padding(top = 8.dp)
  ) {
    Column {
      // Pack Selector Tabs
      ScrollableTabRow(
        selectedTabIndex = selectedPackIndex,
        edgePadding = 8.dp,
        containerColor = Color.Transparent
      ) {
        stickerPacks.forEachIndexed { index, pack ->
          Tab(
            selected = selectedPackIndex == index,
            onClick = { selectedPackIndex = index },
            text = { Text("${pack.coverEmoji} ${pack.name}", fontSize = 12.sp) }
          )
        }
      }

      val currentPack = stickerPacks.getOrNull(selectedPackIndex) ?: stickerPacks.first()

      LazyRow(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        items(currentPack.stickers) { sticker ->
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable { onSelectSticker(currentPack, sticker) }
          ) {
            Text(sticker.iconEmoji, fontSize = 42.sp)
            Text(sticker.label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            if (sticker.urduMeaning != null) {
              Text(sticker.urduMeaning, fontSize = 10.sp, color = BloomRose)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun AttachmentSheetContent(
  isCandleMode: Boolean,
  onActionSelected: (String) -> Unit
) {
  Column(
    modifier = Modifier.fillMaxWidth().padding(24.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Text(
      "Share with Respect",
      style = MaterialTheme.typography.titleMedium.copy(
        fontWeight = FontWeight.Bold,
        fontStyle = FontStyle.Italic,
        color = if (isCandleMode) BloomWarmGold else BloomDeepRose
      )
    )

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
      AttachmentOption(Icons.Default.CameraAlt, "Selfie", "selfie", onActionSelected)
      AttachmentOption(Icons.Default.Photo, "Gallery", "photo", onActionSelected)
      AttachmentOption(Icons.Default.Videocam, "Video (60s)", "video", onActionSelected)
      AttachmentOption(Icons.Default.LocationOn, "Location", "location", onActionSelected)
    }
  }
}

@Composable
private fun AttachmentOption(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  actionKey: String,
  onClick: (String) -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.clickable { onClick(actionKey) },
    verticalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    Box(
      modifier = Modifier
        .size(50.dp)
        .shadow(4.dp, CircleShape)
        .background(BloomRose, CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(icon, contentDescription = title, tint = Color.White)
    }
    Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold)
  }
}

@Composable
private fun EmptyChatPlaceholder(isCandleMode: Boolean) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(40.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Text("🌸", fontSize = 48.sp)
    Text(
      "No Active Chats Yet",
      style = MaterialTheme.typography.titleLarge.copy(
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.Bold,
        color = if (isCandleMode) BloomWarmGold else BloomDeepRose
      )
    )
    Text(
      "When both you and someone else bloom into mutual admiration, your safe conversation begins here.",
      style = MaterialTheme.typography.bodySmall.copy(
        color = if (isCandleMode) BloomCandleDarkMuted else BloomPlum,
        textAlign = TextAlign.Center
      )
    )
  }
}
