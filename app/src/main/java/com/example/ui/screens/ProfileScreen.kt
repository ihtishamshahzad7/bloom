package com.example.ui.screens

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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FeedPost
import com.example.model.ProfileHighlight
import com.example.model.UserSocialProfile
import com.example.ui.theme.*

@Composable
fun ProfileScreen(
  socialProfile: UserSocialProfile,
  isCandleMode: Boolean,
  onFollowToggle: () -> Unit,
  onFriendToggle: () -> Unit,
  onMessageClick: () -> Unit,
  onRomanticBloom: () -> Unit,
  onOpenSettings: () -> Unit,
  onPostClick: (FeedPost) -> Unit
) {
  val haptic = LocalHapticFeedback.current
  var selectedTab by remember { mutableStateOf(0) } // 0: Grid, 1: Saved/Badges

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("instagram_style_profile_screen")
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 10.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Top Bar: Handle & Settings Icon
    item {
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
            text = socialProfile.handle,
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              fontStyle = FontStyle.Italic,
              color = if (isCandleMode) BloomWarmGold else BloomDeepRose
            )
          )
          Text("🛡️", fontSize = 14.sp)
        }

        IconButton(onClick = onOpenSettings, modifier = Modifier.testTag("profile_settings_gear")) {
          Icon(
            imageVector = Icons.Outlined.Settings,
            contentDescription = "Settings",
            tint = if (isCandleMode) BloomWarmGold else BloomPlum
          )
        }
      }
    }

    // Avatar + Counts Row
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Avatar with romantic gradient border
        Box(
          modifier = Modifier
            .size(86.dp)
            .shadow(6.dp, CircleShape)
            .border(
              3.dp,
              Brush.sweepGradient(listOf(BloomWarmGold, BloomRose, BloomPeach, BloomWarmGold)),
              CircleShape
            )
            .padding(4.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(Color(socialProfile.avatarColors.first), Color(socialProfile.avatarColors.second)))),
          contentAlignment = Alignment.Center
        ) {
          Text(
            socialProfile.name.take(1),
            style = MaterialTheme.typography.headlineMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
          )
        }

        // 3 Counts: Posts, Followers, Following, Friends
        Row(
          horizontalArrangement = Arrangement.spacedBy(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          StatColumn(count = socialProfile.postsCount.toString(), label = "Posts", isCandleMode = isCandleMode)
          StatColumn(count = socialProfile.followersCount.toString(), label = "Followers", isCandleMode = isCandleMode)
          StatColumn(count = socialProfile.followingCount.toString(), label = "Following", isCandleMode = isCandleMode)
          StatColumn(count = socialProfile.friendsCount.toString(), label = "Friends", isCandleMode = isCandleMode)
        }
      }
    }

    // Bio, Nickname, Badges, City
    item {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          text = "${socialProfile.name} (${socialProfile.nickname})",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = if (isCandleMode) Color.White else BloomPlum
          )
        )

        // Badges Row
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.padding(vertical = 2.dp)
        ) {
          socialProfile.badges.forEach { badge ->
            Surface(
              shape = RoundedCornerShape(9999.dp),
              color = if (isCandleMode) Color(0xFF3B1F42) else BloomBlush,
              border = BorderStroke(1.dp, BloomRose.copy(alpha = 0.35f))
            ) {
              Text(
                text = badge,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.SemiBold,
                  color = if (isCandleMode) BloomWarmGold else BloomDeepRose,
                  fontSize = 10.sp
                ),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
              )
            }
          }
        }

        Text(
          text = socialProfile.bio,
          style = MaterialTheme.typography.bodySmall.copy(
            color = if (isCandleMode) Color.White.copy(alpha = 0.9f) else BloomPlum,
            lineHeight = 18.sp
          )
        )

        Text(
          text = "📍 ${socialProfile.city}",
          style = MaterialTheme.typography.labelSmall.copy(
            color = BloomRose,
            fontWeight = FontWeight.Medium
          )
        )
      }
    }

    // Action Button Row: Follow / Add Friend / Message / Like (Romantic Bloom)
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Follow Button
        Button(
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onFollowToggle()
          },
          modifier = Modifier.weight(1f),
          colors = ButtonDefaults.buttonColors(
            containerColor = if (socialProfile.isFollowing) Color.Gray.copy(alpha = 0.4f) else BloomRose
          ),
          shape = RoundedCornerShape(12.dp),
          contentPadding = PaddingValues(vertical = 4.dp)
        ) {
          Text(
            text = if (socialProfile.isFollowing) "Following ✓" else "Follow",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
        }

        // Add Friend Button
        Button(
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onFriendToggle()
          },
          modifier = Modifier.weight(1f),
          colors = ButtonDefaults.buttonColors(
            containerColor = if (socialProfile.isFriend) Color.Gray.copy(alpha = 0.4f) else BloomPeach
          ),
          shape = RoundedCornerShape(12.dp),
          contentPadding = PaddingValues(vertical = 4.dp)
        ) {
          Text(
            text = if (socialProfile.isFriend) "Friends 🤝" else "Add Friend",
            fontSize = 11.sp,
            color = BloomPlum,
            fontWeight = FontWeight.Bold
          )
        }

        // Message Button
        IconButton(
          onClick = onMessageClick,
          modifier = Modifier
            .size(38.dp)
            .shadow(2.dp, RoundedCornerShape(10.dp))
            .background(if (isCandleMode) Color(0xFF3B1F42) else Color.White, RoundedCornerShape(10.dp))
            .border(1.dp, BloomRose.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
        ) {
          Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Message", tint = BloomRose, modifier = Modifier.size(18.dp))
        }

        // Romantic Bloom Like Button (Heart/Flower)
        IconButton(
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onRomanticBloom()
          },
          modifier = Modifier
            .size(38.dp)
            .shadow(4.dp, RoundedCornerShape(10.dp))
            .background(Brush.linearGradient(listOf(BloomRose, BloomWarmGold)), RoundedCornerShape(10.dp))
            .testTag("romantic_bloom_button")
        ) {
          Text("🌸", fontSize = 16.sp)
        }
      }
    }

    // Highlights Carousel
    item {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "Story Highlights ✦",
          style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            color = if (isCandleMode) BloomWarmGold else BloomDeepRose
          )
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
          items(socialProfile.highlights) { hl ->
            HighlightCircle(highlight = hl, isCandleMode = isCandleMode)
          }

          item {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(58.dp)
                  .clip(CircleShape)
                  .background(if (isCandleMode) Color(0xFF331838) else BloomBlush)
                  .border(1.dp, BloomRose.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.Add, contentDescription = "New Highlight", tint = BloomRose)
              }
              Text("New", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
            }
          }
        }
      }
    }

    // Grid vs Saved Tabs
    item {
      TabRow(
        selectedTabIndex = selectedTab,
        containerColor = Color.Transparent,
        contentColor = BloomRose,
        divider = { HorizontalDivider(color = BloomRose.copy(alpha = 0.2f)) }
      ) {
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          icon = { Icon(Icons.Default.GridOn, contentDescription = "Posts Grid") }
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          icon = { Icon(Icons.Outlined.BookmarkBorder, contentDescription = "Saved Moments") }
        )
      }
    }

    // 3-Column Posts Grid
    item {
      val postsList = socialProfile.posts
      if (postsList.isNotEmpty()) {
        val chunked = postsList.chunked(3)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          chunked.forEach { rowPosts ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              rowPosts.forEach { post ->
                val primaryGradient = post.photoGradients.firstOrNull() ?: Pair(0xFFF28DB0, 0xFF9E77ED)
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(Color(primaryGradient.first), Color(primaryGradient.second))))
                    .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .clickable { onPostClick(post) },
                  contentAlignment = Alignment.BottomStart
                ) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))))
                      .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text("❤️ ${post.likesCount}", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    if (post.photoGradients.size > 1) {
                      Text("📷 ${post.photoGradients.size}", color = Color.White, fontSize = 9.sp)
                    }
                  }
                }
              }
              repeat(3 - rowPosts.size) {
                Spacer(modifier = Modifier.weight(1f))
              }
            }
          }
        }
      } else {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Text("No posts shared yet 🌸", style = MaterialTheme.typography.bodySmall.copy(color = BloomPlumMuted))
        }
      }
    }
  }
}

@Composable
private fun StatColumn(count: String, label: String, isCandleMode: Boolean) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      text = count,
      style = MaterialTheme.typography.titleMedium.copy(
        fontWeight = FontWeight.Bold,
        color = if (isCandleMode) Color.White else BloomPlum
      )
    )
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall.copy(
        color = if (isCandleMode) BloomCandleDarkMuted else BloomPlumMuted,
        fontSize = 11.sp
      )
    )
  }
}

@Composable
private fun HighlightCircle(highlight: ProfileHighlight, isCandleMode: Boolean) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    Box(
      modifier = Modifier
        .size(58.dp)
        .shadow(4.dp, CircleShape)
        .border(2.dp, BloomRose.copy(alpha = 0.5f), CircleShape)
        .padding(3.dp)
        .clip(CircleShape)
        .background(Brush.linearGradient(listOf(Color(highlight.coverColors.first), Color(highlight.coverColors.second)))),
      contentAlignment = Alignment.Center
    ) {
      Text(highlight.emoji, fontSize = 22.sp)
    }

    Text(
      text = highlight.title,
      style = MaterialTheme.typography.labelSmall.copy(
        fontSize = 10.sp,
        color = if (isCandleMode) Color.White else BloomPlum
      )
    )
  }
}
