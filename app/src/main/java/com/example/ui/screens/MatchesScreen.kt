package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Share
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
import com.example.model.UserProfile
import com.example.ui.components.CompatibilityRing
import com.example.ui.components.NeumorphicGlassCard
import com.example.ui.theme.*

enum class MatchSegment {
  NEW, MUTUAL, SAVED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchesScreen(
  profiles: List<UserProfile>,
  searchQuery: String = "",
  isCandleMode: Boolean,
  onOpenChat: (UserProfile) -> Unit,
  onToggleSave: (UserProfile) -> Unit,
  onOpenFullProfile: (UserProfile) -> Unit = {},
  modifier: Modifier = Modifier
) {
  var selectedSegment by remember { mutableStateOf(MatchSegment.MUTUAL) }
  var isFamilyModeActive by remember { mutableStateOf(false) }
  var showFiltersSheet by remember { mutableStateOf(false) }
  var showFamilyShareDialog by remember { mutableStateOf<UserProfile?>(null) }

  // Filter state
  var minAge by remember { mutableFloatStateOf(21f) }
  var maxAge by remember { mutableFloatStateOf(30f) }
  var verifiedOnly by remember { mutableStateOf(false) }
  var maxDistance by remember { mutableFloatStateOf(35f) }

  val filteredProfiles = remember(profiles, selectedSegment, verifiedOnly, minAge, maxAge, maxDistance, searchQuery) {
    profiles.filter { profile ->
      val segmentMatch = when (selectedSegment) {
        MatchSegment.NEW -> !profile.isMutualMatch && !profile.isSaved
        MatchSegment.MUTUAL -> profile.isMutualMatch
        MatchSegment.SAVED -> profile.isSaved
      }
      val ageMatch = profile.age in minAge.toInt()..maxAge.toInt()
      val distMatch = profile.distanceMiles <= maxDistance
      val verMatch = !verifiedOnly || profile.isVerified
      val queryMatch = searchQuery.isBlank() ||
        profile.name.contains(searchQuery, ignoreCase = true) ||
        profile.city.contains(searchQuery, ignoreCase = true) ||
        profile.interestChips.any { it.contains(searchQuery, ignoreCase = true) }
      segmentMatch && ageMatch && distMatch && verMatch && queryMatch
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
  ) {
    Spacer(modifier = Modifier.height(16.dp))

    // Header & Filter Button
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "Your Matches",
          style = MaterialTheme.typography.displaySmall.copy(
            fontStyle = FontStyle.Italic,
            color = if (isCandleMode) BloomWarmGold else BloomDeepRose
          )
        )
        Text(
          text = "Meaningful connections blooming together",
          style = MaterialTheme.typography.bodySmall.copy(
            color = if (isCandleMode) BloomCandleDarkMuted else BloomMuted
          )
        )
      }

      OutlinedButton(
        onClick = { showFiltersSheet = true },
        shape = RoundedCornerShape(9999.dp),
        colors = ButtonDefaults.outlinedButtonColors(
          containerColor = if (isCandleMode) BloomCandleDarkSurface else Color.White
        ),
        border = BorderStroke(1.dp, BloomRose.copy(alpha = 0.4f)),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
        modifier = Modifier.testTag("filters_button")
      ) {
        Icon(Icons.Outlined.FilterList, contentDescription = null, tint = BloomRose, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Filters", style = MaterialTheme.typography.labelMedium.copy(color = BloomRose))
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Segmented Toggle (New | Mutual | Saved)
    Surface(
      shape = RoundedCornerShape(9999.dp),
      color = if (isCandleMode) BloomCandleDarkCard else BloomBlush.copy(alpha = 0.7f),
      border = BorderStroke(1.dp, BloomRose.copy(alpha = 0.2f)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier.padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
      ) {
        MatchSegment.values().forEach { seg ->
          val isSelected = selectedSegment == seg
          val label = when (seg) {
            MatchSegment.NEW -> "New (2)"
            MatchSegment.MUTUAL -> "Mutual (4)"
            MatchSegment.SAVED -> "Saved (2)"
          }
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(9999.dp))
              .background(if (isSelected) BloomRose else Color.Transparent)
              .clickable { selectedSegment = seg }
              .padding(vertical = 8.dp)
              .testTag("segment_${seg.name.lowercase()}"),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = label,
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else if (isCandleMode) BloomCandleDarkMuted else BloomPlum
              )
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Family-Involved Mode Banner & Toggle
    NeumorphicGlassCard(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(20.dp),
      isCandleMode = isCandleMode
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text("🏡", fontSize = 18.sp)
          Column {
            Text(
              text = "Family-Involved Mode",
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = if (isCandleMode) BloomCandleDarkText else BloomPlum
              )
            )
            Text(
              text = "Share read-only profiles with parents & mentors",
              style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.sp,
                color = if (isCandleMode) BloomCandleDarkMuted else BloomMuted
              )
            )
          }
        }
        Switch(
          checked = isFamilyModeActive,
          onCheckedChange = { isFamilyModeActive = it },
          colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = BloomRose
          ),
          modifier = Modifier.testTag("family_mode_switch")
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Matches List
    LazyColumn(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(14.dp),
      contentPadding = PaddingValues(bottom = 96.dp)
    ) {
      if (filteredProfiles.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 40.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Text("🌷", fontSize = 36.sp)
              Text(
                text = "No matches in this view",
                style = MaterialTheme.typography.headlineSmall.copy(
                  color = if (isCandleMode) BloomWarmGold else BloomDeepRose,
                  fontStyle = FontStyle.Italic
                )
              )
              Text(
                text = "Try adjusting your filters or checking back as new blooms arrive.",
                style = MaterialTheme.typography.bodySmall.copy(
                  color = if (isCandleMode) BloomCandleDarkMuted else BloomMuted
                )
              )
            }
          }
        }
      } else {
        items(filteredProfiles, key = { it.id }) { match ->
          MatchCardItem(
            profile = match,
            isCandleMode = isCandleMode,
            isFamilyModeActive = isFamilyModeActive,
            onCardClick = { onOpenFullProfile(match) },
            onSayHello = { onOpenChat(match) },
            onToggleSave = { onToggleSave(match) },
            onShareWithFamily = { showFamilyShareDialog = match }
          )
        }
      }
    }
  }

  // Filters Modal Bottom Sheet
  if (showFiltersSheet) {
    ModalBottomSheet(
      onDismissRequest = { showFiltersSheet = false },
      containerColor = if (isCandleMode) BloomCandleDarkCard else Color(0xFFFFF9FA),
      shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Match Preferences",
            style = MaterialTheme.typography.headlineMedium.copy(
              fontStyle = FontStyle.Italic,
              color = if (isCandleMode) BloomWarmGold else BloomDeepRose
            )
          )
          TextButton(onClick = {
            minAge = 20f
            maxAge = 32f
            maxDistance = 50f
            verifiedOnly = false
          }) {
            Text("Reset", color = BloomRose)
          }
        }

        // Age Range Slider
        Text(
          text = "Age Range: ${minAge.toInt()} – ${maxAge.toInt()} years",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
        )
        RangeSlider(
          value = minAge..maxAge,
          onValueChange = { range ->
            minAge = range.start
            maxAge = range.endInclusive
          },
          valueRange = 18f..35f,
          colors = SliderDefaults.colors(
            thumbColor = BloomRose,
            activeTrackColor = BloomRose
          )
        )

        // Max Distance
        Text(
          text = "Maximum Distance: ${maxDistance.toInt()} miles",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
        )
        Slider(
          value = maxDistance,
          onValueChange = { maxDistance = it },
          valueRange = 5f..100f,
          colors = SliderDefaults.colors(
            thumbColor = BloomLavenderDeep,
            activeTrackColor = BloomLavenderDeep
          )
        )

        // Verified Only Toggle
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text("Verified Profiles Only", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
            Text("Phone OTP & Selfie identity confirmed", style = MaterialTheme.typography.bodySmall.copy(color = BloomMuted))
          }
          Switch(
            checked = verifiedOnly,
            onCheckedChange = { verifiedOnly = it },
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = BloomRose)
          )
        }

        Button(
          onClick = { showFiltersSheet = false },
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("apply_filters_button"),
          shape = RoundedCornerShape(9999.dp),
          colors = ButtonDefaults.buttonColors(containerColor = BloomRose)
        ) {
          Text("Apply Filters (${filteredProfiles.size} results)", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(20.dp))
      }
    }
  }

  // Family Share Dialog
  if (showFamilyShareDialog != null) {
    val target = showFamilyShareDialog!!
    AlertDialog(
      onDismissRequest = { showFamilyShareDialog = null },
      containerColor = if (isCandleMode) BloomCandleDarkCard else Color(0xFFFFF7F9),
      title = {
        Text(
          text = "Share with Family 🏡",
          style = MaterialTheme.typography.headlineSmall.copy(fontStyle = FontStyle.Italic, color = BloomRose)
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "Generate a secure, read-only profile overview for ${target.name} to share with parents or guardians.",
            style = MaterialTheme.typography.bodyMedium
          )
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isCandleMode) BloomCandleDarkSurface else Color.White,
            border = BorderStroke(1.dp, BloomLavenderDeep.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Text("Included in family brief:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = BloomLavenderDeep))
              Text("• Occupation: ${target.occupation}", style = MaterialTheme.typography.bodySmall)
              Text("• Education: ${target.education}", style = MaterialTheme.typography.bodySmall)
              Text("• Values: ${target.values.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
              Text("• Intentions: ${target.intentions}", style = MaterialTheme.typography.bodySmall)
              Text("• Note: Contact handles remain protected.", style = MaterialTheme.typography.labelSmall.copy(color = BloomOnline))
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = { showFamilyShareDialog = null },
          colors = ButtonDefaults.buttonColors(containerColor = BloomRose),
          shape = RoundedCornerShape(9999.dp)
        ) {
          Text("Copy Secure Link")
        }
      },
      dismissButton = {
        TextButton(onClick = { showFamilyShareDialog = null }) {
          Text("Close")
        }
      }
    )
  }
}

@Composable
private fun MatchCardItem(
  profile: UserProfile,
  isCandleMode: Boolean,
  isFamilyModeActive: Boolean,
  onCardClick: () -> Unit = {},
  onSayHello: () -> Unit,
  onToggleSave: () -> Unit,
  onShareWithFamily: () -> Unit
) {
  NeumorphicGlassCard(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onCardClick() },
    shape = RoundedCornerShape(26.dp),
    isCandleMode = isCandleMode
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // Circular Avatar with rose gradient
          Box(
            modifier = Modifier
              .size(56.dp)
              .clip(CircleShape)
              .background(
                Brush.linearGradient(
                  listOf(Color(profile.avatarColorStart), Color(profile.avatarColorEnd))
                )
              ),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = profile.initials,
              style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
            )
          }

          Column {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Text(
                text = "${profile.name}, ${profile.age}",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = if (isCandleMode) BloomWarmGold else BloomPlum
                )
              )
              if (profile.isVerified) {
                Icon(Icons.Default.Verified, contentDescription = "Verified", tint = BloomOnline, modifier = Modifier.size(16.dp))
              }
            }
            Text(
              text = "📍 ${profile.city} • ${profile.occupation}",
              style = MaterialTheme.typography.bodySmall.copy(
                color = if (isCandleMode) BloomCandleDarkMuted else BloomMuted
              )
            )
            if (profile.isMutualMatch) {
              Text(
                text = "Matched 💞",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = BloomRose,
                  fontWeight = FontWeight.Bold
                )
              )
            }
          }
        }

        CompatibilityRing(percentage = profile.compatibilityPercent, size = 44.dp, strokeWidth = 3.dp)
      }

      // Shared interests chips
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        profile.interestChips.take(2).forEach { chip ->
          Surface(
            shape = RoundedCornerShape(9999.dp),
            color = if (isCandleMode) BloomCandleDarkSurface else BloomBlushMid.copy(alpha = 0.5f)
          ) {
            Text(
              text = chip,
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }
      }

      // Action row: Say Hello + Family Share if active
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (isFamilyModeActive) {
          OutlinedButton(
            onClick = onShareWithFamily,
            shape = RoundedCornerShape(9999.dp),
            border = BorderStroke(1.dp, BloomLavenderDeep.copy(alpha = 0.5f)),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
          ) {
            Icon(Icons.Outlined.Share, contentDescription = null, tint = BloomLavenderDeep, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Family Preview", style = MaterialTheme.typography.labelSmall.copy(color = BloomLavenderDeep))
          }
        } else {
          Spacer(modifier = Modifier.width(4.dp))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          IconButton(
            onClick = onToggleSave,
            modifier = Modifier.size(38.dp)
          ) {
            Icon(
              imageVector = if (profile.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
              contentDescription = "Save",
              tint = if (profile.isSaved) BloomCandleGold else Color.Gray,
              modifier = Modifier.size(20.dp)
            )
          }

          Button(
            onClick = onSayHello,
            shape = RoundedCornerShape(9999.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BloomRose),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            modifier = Modifier.testTag("say_hello_${profile.id}")
          ) {
            Text("Say Hello 💬", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
          }
        }
      }
    }
  }
}
