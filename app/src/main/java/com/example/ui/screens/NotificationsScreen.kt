package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SocialNotification
import com.example.model.SocialNotificationType
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
  notifications: List<SocialNotification>,
  isCandleMode: Boolean,
  onNotificationClick: (SocialNotification) -> Unit,
  onMarkAllRead: () -> Unit,
  onBack: () -> Unit
) {
  var selectedTab by remember { mutableStateOf("All") }

  val filteredList = remember(notifications, selectedTab) {
    when (selectedTab) {
      "Likes" -> notifications.filter { it.type == SocialNotificationType.LIKE }
      "Comments" -> notifications.filter { it.type == SocialNotificationType.COMMENT }
      "Matches" -> notifications.filter { it.type == SocialNotificationType.MATCH }
      "Follows" -> notifications.filter { it.type == SocialNotificationType.FOLLOW }
      else -> notifications
    }
  }

  val grouped = remember(filteredList) {
    filteredList.groupBy { it.dateGroup }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .testTag("notifications_screen")
      .padding(horizontal = 16.dp, vertical = 8.dp)
  ) {
    // Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        IconButton(onClick = onBack) {
          Icon(
            Icons.Default.ArrowBack,
            contentDescription = "Back",
            tint = if (isCandleMode) BloomWarmGold else BloomDeepRose
          )
        }
        Text(
          text = "Notifications 🔔",
          style = MaterialTheme.typography.titleLarge.copy(
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Bold,
            color = if (isCandleMode) BloomWarmGold else BloomDeepRose
          )
        )
      }

      TextButton(onClick = onMarkAllRead) {
        Text("Mark all read", color = BloomRose, fontSize = 12.sp, fontWeight = FontWeight.Bold)
      }
    }

    // Filter Chips
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      listOf("All", "Likes", "Comments", "Matches", "Follows").forEach { tab ->
        FilterChip(
          selected = selectedTab == tab,
          onClick = { selectedTab = tab },
          label = { Text(tab, fontSize = 11.sp) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = BloomRose,
            selectedLabelColor = Color.White
          )
        )
      }
    }

    LazyColumn(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(14.dp),
      contentPadding = PaddingValues(bottom = 80.dp)
    ) {
      grouped.forEach { (dateGroup, items) ->
        item {
          Text(
            text = dateGroup,
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.Bold,
              color = if (isCandleMode) BloomWarmGold else BloomDeepRose
            ),
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
          )
        }

        items(items, key = { it.id }) { notif ->
          NotificationRow(
            notification = notif,
            isCandleMode = isCandleMode,
            onClick = { onNotificationClick(notif) }
          )
        }
      }

      if (filteredList.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(40.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("🕊️", fontSize = 36.sp)
              Spacer(modifier = Modifier.height(8.dp))
              Text("All caught up!", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
              Text("No new notifications in this category.", style = MaterialTheme.typography.bodySmall.copy(color = BloomPlumMuted))
            }
          }
        }
      }
    }
  }
}

@Composable
fun NotificationRow(
  notification: SocialNotification,
  isCandleMode: Boolean,
  onClick: () -> Unit
) {
  val typeEmoji = when (notification.type) {
    SocialNotificationType.LIKE -> "❤️"
    SocialNotificationType.COMMENT -> "💬"
    SocialNotificationType.MATCH -> "🌸"
    SocialNotificationType.FOLLOW -> "👤"
    SocialNotificationType.STORY_VIEW -> "☕"
    SocialNotificationType.FRIEND_REQUEST -> "🤝"
  }

  Surface(
    shape = RoundedCornerShape(18.dp),
    color = if (!notification.isRead) {
      if (isCandleMode) Color(0xFF381B3E) else BloomBlush.copy(alpha = 0.85f)
    } else {
      if (isCandleMode) Color(0xFF28132B).copy(alpha = 0.7f) else Color.White.copy(alpha = 0.8f)
    },
    border = BorderStroke(
      1.dp,
      if (!notification.isRead) BloomRose.copy(alpha = 0.4f) else Color.Transparent
    ),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
  ) {
    Row(
      modifier = Modifier.padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Box {
          Box(
            modifier = Modifier
              .size(46.dp)
              .clip(CircleShape)
              .background(Brush.linearGradient(listOf(Color(notification.actorAvatarColors.first), Color(notification.actorAvatarColors.second)))),
            contentAlignment = Alignment.Center
          ) {
            Text(notification.actorName.take(1), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
          }

          // Type emoji badge
          Surface(
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier
              .size(18.dp)
              .align(Alignment.BottomEnd)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text(typeEmoji, fontSize = 10.sp)
            }
          }
        }

        Column {
          Text(
            text = "${notification.actorName} ${notification.message}",
            style = MaterialTheme.typography.bodySmall.copy(
              color = if (isCandleMode) Color.White else BloomPlum,
              fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.Normal
            )
          )
          Text(
            text = notification.timeAgo,
            style = MaterialTheme.typography.labelSmall.copy(
              color = BloomPlumMuted,
              fontSize = 10.sp
            )
          )
        }
      }

      if (!notification.isRead) {
        Box(
          modifier = Modifier
            .size(8.dp)
            .background(BloomRose, CircleShape)
        )
      }
    }
  }
}
