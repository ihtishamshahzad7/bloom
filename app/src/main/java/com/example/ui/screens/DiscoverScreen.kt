package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DailyBloomsCounter
import com.example.model.ProfileBoostStatus
import com.example.model.UserProfile
import com.example.ui.components.CompatibilityRing
import com.example.ui.components.DecorativeCandle
import com.example.ui.components.NeumorphicGlassCard
import com.example.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun DiscoverScreen(
  profiles: List<UserProfile>,
  searchQuery: String = "",
  dailyBlooms: DailyBloomsCounter = DailyBloomsCounter(),
  boostStatus: ProfileBoostStatus = ProfileBoostStatus(),
  isCandleMode: Boolean,
  onLikeProfile: (UserProfile) -> Unit,
  onPassProfile: (UserProfile) -> Unit,
  onMessageProfile: (UserProfile) -> Unit,
  onSaveProfile: (UserProfile) -> Unit,
  onOpenFullProfile: (UserProfile) -> Unit,
  onBoostProfile: () -> Unit,
  onViewMatches: () -> Unit,
  modifier: Modifier = Modifier
) {
  val haptic = LocalHapticFeedback.current
  var activeCardIndex by remember { mutableIntStateOf(0) }
  var offsetX by remember { mutableFloatStateOf(0f) }
  var showIcebreakerInput by remember { mutableStateOf(false) }
  var icebreakerAnswer by remember { mutableStateOf("") }
  var icebreakerSaved by remember { mutableStateOf(false) }

  // Filter profiles by search query (interest or city or name)
  val visibleProfiles = remember(profiles, searchQuery) {
    if (searchQuery.isBlank()) profiles
    else {
      profiles.filter { p ->
        p.name.contains(searchQuery, ignoreCase = true) ||
        p.city.contains(searchQuery, ignoreCase = true) ||
        p.occupation.contains(searchQuery, ignoreCase = true) ||
        p.interestChips.any { it.contains(searchQuery, ignoreCase = true) }
      }
    }
  }

  val currentProfile = visibleProfiles.getOrNull(activeCardIndex.coerceAtMost(visibleProfiles.size - 1))

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(18.dp)
  ) {
    // 1. HERO BANNER: Romantic rose-to-lavender gradient card
    item {
      HeroBanner(
        isCandleMode = isCandleMode,
        onViewMatches = onViewMatches
      )
    }

    // 2. DAILY BLOOM LIMIT & BOOST BAR
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Daily Blooms Left Pill
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = if (isCandleMode) BloomCandleDarkCard else Color.White,
          border = BorderStroke(1.dp, BloomRose.copy(alpha = 0.3f)),
          modifier = Modifier.weight(1f)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Text("🌷", fontSize = 16.sp)
            Column {
              Text(
                text = "${dailyBlooms.remainingBlooms} of ${dailyBlooms.maxDailyBlooms} Blooms Left",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = BloomRose)
              )
              Text(
                text = "Resets in ${dailyBlooms.resetHoursRemaining}h at dusk",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, color = BloomMuted)
              )
            }
          }
        }

        // Profile Boost Button
        Button(
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onBoostProfile()
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = if (boostStatus.isBoosted) BloomOnline else BloomLavenderDeep
          ),
          shape = RoundedCornerShape(18.dp),
          contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
          modifier = Modifier.testTag("boost_profile_btn")
        ) {
          Icon(Icons.Outlined.RocketLaunch, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (boostStatus.isBoosted) "Boosted 3x (${boostStatus.remainingMinutes}m)" else "Boost (1h)",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
          )
        }
      }
    }

    // 3. DAILY BLOOM CURATED PROFILE CARD
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
              text = "Daily Bloom",
              style = MaterialTheme.typography.displaySmall.copy(
                fontStyle = FontStyle.Italic,
                color = if (isCandleMode) BloomWarmGold else BloomDeepRose
              )
            )
            Surface(
              shape = RoundedCornerShape(9999.dp),
              color = if (isCandleMode) BloomCandleDarkCard else BloomBlush,
              border = BorderStroke(1.dp, BloomRose.copy(alpha = 0.3f))
            ) {
              Text(
                text = "${(activeCardIndex + 1).coerceAtMost(visibleProfiles.size)} of ${visibleProfiles.size} curated",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = BloomRose,
                  fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }
          }

          Text(
            text = "Quality First 🌷",
            style = MaterialTheme.typography.labelSmall.copy(
              color = if (isCandleMode) BloomCandleDarkMuted else BloomMuted
            )
          )
        }

        if (currentProfile != null) {
          // Swipeable Profile Card Deck
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .offset { IntOffset(offsetX.roundToInt(), 0) }
              .rotate(offsetX * 0.04f)
              .pointerInput(currentProfile.id) {
                detectDragGestures(
                  onDragEnd = {
                    if (offsetX > 260f) {
                      haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                      onLikeProfile(currentProfile)
                      if (activeCardIndex < visibleProfiles.size - 1) activeCardIndex++
                    } else if (offsetX < -260f) {
                      haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                      onPassProfile(currentProfile)
                      if (activeCardIndex < visibleProfiles.size - 1) activeCardIndex++
                    }
                    offsetX = 0f
                  },
                  onDrag = { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                  }
                )
              }
          ) {
            CuratedProfileCard(
              profile = currentProfile,
              isCandleMode = isCandleMode,
              onCardClick = { onOpenFullProfile(currentProfile) },
              onPass = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onPassProfile(currentProfile)
                if (activeCardIndex < visibleProfiles.size - 1) activeCardIndex++
              },
              onMessage = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onMessageProfile(currentProfile)
              },
              onLike = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onLikeProfile(currentProfile)
                if (activeCardIndex < visibleProfiles.size - 1) activeCardIndex++
              },
              onSave = { onSaveProfile(currentProfile) }
            )
          }
        } else {
          // All viewed empty state
          NeumorphicGlassCard(
            modifier = Modifier.fillMaxWidth(),
            isCandleMode = isCandleMode
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              DecorativeCandle(candleHeight = 44.dp, isCandleMode = isCandleMode)
              Text(
                text = "You've Seen Today's Blooms 🌷",
                style = MaterialTheme.typography.headlineSmall.copy(
                  fontStyle = FontStyle.Italic,
                  color = if (isCandleMode) BloomWarmGold else BloomDeepRose
                )
              )
              Text(
                text = "No new profiles right now — good things bloom slowly. 5 fresh, intentional partner recommendations arrive tomorrow at dusk.",
                style = MaterialTheme.typography.bodyMedium.copy(
                  color = if (isCandleMode) BloomCandleDarkMuted else BloomPlum
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
              Button(
                onClick = { activeCardIndex = 0 },
                colors = ButtonDefaults.buttonColors(containerColor = BloomRose),
                shape = RoundedCornerShape(9999.dp)
              ) {
                Text("Review Curated Profiles Again")
              }
            }
          }
        }
      }
    }

    // 4. STAT CARDS: Matches 12, Likes received 34, Profile views 128, Response rate 92%
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = "Your Romance Pulse",
          style = MaterialTheme.typography.headlineSmall.copy(
            fontStyle = FontStyle.Italic,
            color = if (isCandleMode) BloomWarmGold else BloomDeepRose
          )
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          StatCard(
            title = "Matches",
            value = "12",
            subtitle = "+4 new",
            color = BloomRose,
            icon = Icons.Default.Favorite,
            isCandleMode = isCandleMode,
            modifier = Modifier.weight(1f)
          )
          StatCard(
            title = "Likes",
            value = "34",
            subtitle = "Admirers",
            color = BloomPeach,
            icon = Icons.Default.VolunteerActivism,
            isCandleMode = isCandleMode,
            modifier = Modifier.weight(1f)
          )
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          StatCard(
            title = "Views",
            value = "128",
            subtitle = "This week",
            color = BloomLavenderDeep,
            icon = Icons.Default.Visibility,
            isCandleMode = isCandleMode,
            modifier = Modifier.weight(1f)
          )
          StatCard(
            title = "Response",
            value = "92%",
            subtitle = "Thoughtful",
            color = BloomCandleGold,
            icon = Icons.Default.Bolt,
            isCandleMode = isCandleMode,
            modifier = Modifier.weight(1f)
          )
        }
      }
    }

    // 5. ICEBREAKER OF THE DAY CARD
    item {
      NeumorphicGlassCard(
        modifier = Modifier.fillMaxWidth(),
        isCandleMode = isCandleMode
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Surface(
              shape = CircleShape,
              color = BloomCandleGold.copy(alpha = 0.2f),
              modifier = Modifier.size(32.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Text("✨", fontSize = 16.sp)
              }
            }
            Column {
              Text(
                text = "Icebreaker of the Day",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                text = "Shared with your potential matches",
                style = MaterialTheme.typography.bodySmall.copy(
                  color = if (isCandleMode) BloomCandleDarkMuted else BloomMuted
                )
              )
            }
          }

          Text(
            text = "“What's a small, quiet moment that always makes your heart feel grateful?”",
            style = MaterialTheme.typography.bodyLarge.copy(
              fontStyle = FontStyle.Italic,
              color = if (isCandleMode) BloomCandleDarkText else BloomPlum,
              fontWeight = FontWeight.Medium
            )
          )

          if (!showIcebreakerInput && !icebreakerSaved) {
            Button(
              onClick = { showIcebreakerInput = true },
              colors = ButtonDefaults.buttonColors(containerColor = BloomRose),
              shape = RoundedCornerShape(9999.dp),
              modifier = Modifier.align(Alignment.End).testTag("answer_icebreaker_button")
            ) {
              Text("Answer & Share")
            }
          } else if (showIcebreakerInput && !icebreakerSaved) {
            OutlinedTextField(
              value = icebreakerAnswer,
              onValueChange = { icebreakerAnswer = it },
              placeholder = { Text("E.g. Hearing morning rain while brewing cardamom tea...") },
              modifier = Modifier.fillMaxWidth().testTag("icebreaker_input"),
              shape = RoundedCornerShape(16.dp),
              maxLines = 3
            )
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.End,
              verticalAlignment = Alignment.CenterVertically
            ) {
              TextButton(onClick = { showIcebreakerInput = false }) {
                Text("Cancel")
              }
              Spacer(modifier = Modifier.width(8.dp))
              Button(
                onClick = {
                  if (icebreakerAnswer.isNotBlank()) {
                    icebreakerSaved = true
                    showIcebreakerInput = false
                  }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BloomRose),
                shape = RoundedCornerShape(9999.dp)
              ) {
                Text("Save to Profile")
              }
            }
          } else {
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = BloomOnline.copy(alpha = 0.15f),
              border = BorderStroke(1.dp, BloomOnline.copy(alpha = 0.4f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BloomOnline, modifier = Modifier.size(18.dp))
                Text(
                  text = "Your response is visible to prospective matches!",
                  style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun HeroBanner(
  isCandleMode: Boolean,
  onViewMatches: () -> Unit
) {
  val bannerGradient = if (isCandleMode) {
    Brush.horizontalGradient(
      listOf(Color(0xFF4A1F52), Color(0xFF381B42), Color(0xFF2C1434))
    )
  } else {
    Brush.horizontalGradient(
      listOf(Color(0xFFF28DB0), Color(0xFFE594C6), Color(0xFFB79CF0))
    )
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .shadow(12.dp, RoundedCornerShape(28.dp))
      .testTag("hero_banner"),
    shape = RoundedCornerShape(28.dp),
    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(bannerGradient)
        .padding(20.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(9999.dp),
            color = Color.White.copy(alpha = 0.25f)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Text("✦", fontSize = 11.sp, color = BloomCandleGold)
              Text(
                text = "New match · Ayaan 💕",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = Color.White,
                  fontWeight = FontWeight.Bold
                )
              )
            }
          }

          Text(
            text = "Good Evening, Mia 🌙",
            style = MaterialTheme.typography.displaySmall.copy(
              fontStyle = FontStyle.Italic,
              color = Color.White
            )
          )

          Text(
            text = "You have 4 new matches and 2 admirers who liked you today.",
            style = MaterialTheme.typography.bodyMedium.copy(
              color = Color.White.copy(alpha = 0.92f)
            )
          )

          Spacer(modifier = Modifier.height(4.dp))

          Button(
            onClick = onViewMatches,
            colors = ButtonDefaults.buttonColors(
              containerColor = Color.White,
              contentColor = BloomDeepRose
            ),
            shape = RoundedCornerShape(9999.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            modifier = Modifier.testTag("hero_view_matches_button")
          ) {
            Text(
              text = "View Matches 💞",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
          }
        }

        // Candle with flickering flame at top right corner
        DecorativeCandle(
          candleHeight = 52.dp,
          candleWidth = 24.dp,
          isCandleMode = isCandleMode,
          modifier = Modifier.padding(start = 12.dp)
        )
      }
    }
  }
}

@Composable
private fun CuratedProfileCard(
  profile: UserProfile,
  isCandleMode: Boolean,
  onCardClick: () -> Unit,
  onPass: () -> Unit,
  onMessage: () -> Unit,
  onLike: () -> Unit,
  onSave: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .shadow(16.dp, RoundedCornerShape(32.dp))
      .clickable { onCardClick() }
      .testTag("profile_card_${profile.id}"),
    shape = RoundedCornerShape(32.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isCandleMode) BloomCandleDarkCard else Color.White
    )
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // Photo-style visual header
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(240.dp)
          .background(
            Brush.verticalGradient(
              listOf(Color(profile.avatarColorStart), Color(profile.avatarColorEnd))
            )
          )
      ) {
        // Initials / portrait silhouette
        Box(
          modifier = Modifier
            .size(110.dp)
            .align(Alignment.Center)
            .shadow(12.dp, CircleShape)
            .border(3.dp, Color.White.copy(alpha = 0.8f), CircleShape)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.25f)),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = profile.initials,
            style = MaterialTheme.typography.displayMedium.copy(
              color = Color.White,
              fontWeight = FontWeight.Bold
            )
          )
        }

        // Top Left: Intentions Badge
        Surface(
          shape = RoundedCornerShape(9999.dp),
          color = Color(0xCC2A1630),
          modifier = Modifier
            .padding(16.dp)
            .align(Alignment.TopStart)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Text("💍", fontSize = 12.sp)
            Text(
              text = profile.intentions,
              style = MaterialTheme.typography.labelSmall.copy(
                color = BloomWarmGold,
                fontWeight = FontWeight.Medium
              )
            )
          }
        }

        // Top Right: Save Bookmark button
        IconButton(
          onClick = onSave,
          modifier = Modifier
            .padding(12.dp)
            .align(Alignment.TopEnd)
            .background(Color.White.copy(alpha = 0.35f), CircleShape)
        ) {
          Icon(
            imageVector = if (profile.isSaved) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
            contentDescription = "Save profile",
            tint = if (profile.isSaved) BloomCandleGold else Color.White
          )
        }

        // Bottom Right: Compatibility Ring Badge
        Box(
          modifier = Modifier
            .padding(16.dp)
            .align(Alignment.BottomEnd)
            .background(Color.White.copy(alpha = 0.9f), CircleShape)
            .padding(4.dp)
        ) {
          CompatibilityRing(
            percentage = profile.compatibilityPercent,
            size = 46.dp,
            strokeWidth = 3.5.dp
          )
        }

        // Tap hint
        Surface(
          shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
          color = Color(0x77000000),
          modifier = Modifier.align(Alignment.BottomCenter)
        ) {
          Text(
            text = "Tap to view full profile & photo album",
            style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontSize = 9.sp),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
          )
        }
      }

      // Profile details
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Name, Age, City, Verified
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                text = "${profile.name}, ${profile.age}",
                style = MaterialTheme.typography.headlineLarge.copy(
                  color = if (isCandleMode) BloomWarmGold else BloomDeepRose,
                  fontWeight = FontWeight.Bold
                )
              )
              if (profile.isVerified) {
                Icon(
                  imageVector = Icons.Default.Verified,
                  contentDescription = "Verified Identity",
                  tint = BloomOnline,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
            Text(
              text = "📍 ${profile.city} • ${profile.distanceMiles} miles away",
              style = MaterialTheme.typography.bodySmall.copy(
                color = if (isCandleMode) BloomCandleDarkMuted else BloomMuted
              )
            )
            Text(
              text = "🎓 ${profile.education} • ${profile.occupation}",
              style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium,
                color = if (isCandleMode) BloomCandleDarkText else BloomPlum
              )
            )
          }
        }

        // Bio
        Text(
          text = profile.bio,
          style = MaterialTheme.typography.bodyMedium.copy(
            color = if (isCandleMode) BloomCandleDarkText else BloomPlum,
            lineHeight = 21.sp
          )
        )

        // Interest chips
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          profile.interestChips.take(3).forEach { chip ->
            Surface(
              shape = RoundedCornerShape(9999.dp),
              color = if (isCandleMode) BloomCandleDarkSurface else BloomBlush,
              border = BorderStroke(1.dp, BloomRose.copy(alpha = 0.25f))
            ) {
              Text(
                text = chip,
                style = MaterialTheme.typography.labelSmall.copy(
                  color = if (isCandleMode) BloomCandleDarkText else BloomDeepRose,
                  fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
              )
            }
          }
        }

        // Action Buttons: ✕ Pass, 💬 Message, ❤️ Like
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Pass Button
          IconButton(
            onClick = onPass,
            modifier = Modifier
              .size(56.dp)
              .shadow(6.dp, CircleShape)
              .background(if (isCandleMode) BloomCandleDarkSurface else Color.White, CircleShape)
              .border(1.dp, Color.Gray.copy(alpha = 0.3f), CircleShape)
              .testTag("pass_button")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Pass",
              tint = Color(0xFF9E7A9E),
              modifier = Modifier.size(26.dp)
            )
          }

          // Message Button
          IconButton(
            onClick = onMessage,
            modifier = Modifier
              .size(52.dp)
              .shadow(6.dp, CircleShape)
              .background(if (isCandleMode) BloomCandleDarkSurface else Color.White, CircleShape)
              .border(1.dp, BloomLavenderDeep.copy(alpha = 0.5f), CircleShape)
              .testTag("message_button")
          ) {
            Icon(
              imageVector = Icons.Outlined.ChatBubbleOutline,
              contentDescription = "Send message",
              tint = BloomLavenderDeep,
              modifier = Modifier.size(24.dp)
            )
          }

          // Like Button with Petal Bloom
          IconButton(
            onClick = onLike,
            modifier = Modifier
              .size(62.dp)
              .shadow(10.dp, CircleShape)
              .background(
                Brush.linearGradient(listOf(BloomRose, BloomDeepRose)),
                CircleShape
              )
              .testTag("like_button")
          ) {
            Icon(
              imageVector = Icons.Default.Favorite,
              contentDescription = "Like profile",
              tint = Color.White,
              modifier = Modifier.size(32.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun StatCard(
  title: String,
  value: String,
  subtitle: String,
  color: Color,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  isCandleMode: Boolean,
  modifier: Modifier = Modifier
) {
  NeumorphicGlassCard(
    modifier = modifier,
    shape = RoundedCornerShape(22.dp),
    isCandleMode = isCandleMode
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Box(
        modifier = Modifier
          .size(38.dp)
          .background(color.copy(alpha = 0.2f), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = color,
          modifier = Modifier.size(20.dp)
        )
      }
      Column {
        Text(
          text = value,
          style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            color = if (isCandleMode) BloomCandleDarkText else BloomPlum
          )
        )
        Text(
          text = "$title • $subtitle",
          style = MaterialTheme.typography.labelSmall.copy(
            color = if (isCandleMode) BloomCandleDarkMuted else BloomMuted
          )
        )
      }
    }
  }
}
