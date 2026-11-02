package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.CompatibilityRing
import com.example.ui.components.NeumorphicGlassCard
import com.example.ui.theme.*

@Composable
fun NearbyScreen(
  nearbyPeople: List<NearbyPerson>,
  friendSuggestions: List<FriendSuggestion>,
  liveLocationState: LiveLocationState,
  privacySettings: PrivacySettings,
  isCandleMode: Boolean,
  onStartLiveLocation: (String, Int, Boolean) -> Unit,
  onStopLiveLocation: () -> Unit,
  onSendFriendRequest: (String) -> Unit,
  onAcceptFriendRequest: (String) -> Unit,
  onDeclineFriendRequest: (String) -> Unit,
  onRemoveFriendSuggestion: (String) -> Unit,
  onUpdatePrivacy: ((PrivacySettings) -> PrivacySettings) -> Unit,
  onOpenPermissionExplainer: () -> Unit,
  modifier: Modifier = Modifier
) {
  var activeTab by remember { mutableIntStateOf(0) } // 0: Nearby People, 1: Friend Suggestions
  var maxDistanceKm by remember { mutableFloatStateOf(10f) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Nearby & Suggestions 📍",
            style = MaterialTheme.typography.headlineMedium.copy(
              fontStyle = FontStyle.Italic,
              fontWeight = FontWeight.Bold,
              color = if (isCandleMode) BloomWarmGold else BloomDeepRose
            )
          )
          Text(
            text = "Privacy-first location · Rounded distance (~3 km)",
            style = MaterialTheme.typography.bodySmall.copy(
              color = if (isCandleMode) BloomCandleDarkMuted else BloomPlum
            )
          )
        }
      }
    }

    // Tab Row: Nearby vs Suggestions
    item {
      TabRow(
        selectedTabIndex = activeTab,
        containerColor = Color.Transparent,
        contentColor = BloomRose
      ) {
        Tab(
          selected = activeTab == 0,
          onClick = { activeTab = 0 },
          text = { Text("Nearby People", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
        )
        Tab(
          selected = activeTab == 1,
          onClick = { activeTab = 1 },
          text = { Text("Friend Suggestions (${friendSuggestions.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
        )
      }
    }

    if (activeTab == 0) {
      // Privacy & Ghost Mode Controls
      item {
        NeumorphicGlassCard(isCandleMode = isCandleMode, modifier = Modifier.fillMaxWidth()) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text("Ghost Mode (Invisible)", fontWeight = FontWeight.Bold)
                Text(
                  "Hides your approximate pin from nearby radar.",
                  style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                )
              }
              Switch(
                checked = privacySettings.ghostModeEnabled,
                onCheckedChange = { checked ->
                  onUpdatePrivacy { it.copy(ghostModeEnabled = checked) }
                },
                colors = SwitchDefaults.colors(checkedThumbColor = BloomRose)
              )
            }

            HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f), thickness = 0.5.dp)

            // Distance Radius Slider
            Column {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text("Distance Radius Filter", style = MaterialTheme.typography.labelMedium)
                Text("Within ${maxDistanceKm.toInt()} km", fontWeight = FontWeight.Bold, color = BloomRose)
              }
              Slider(
                value = maxDistanceKm,
                onValueChange = { maxDistanceKm = it },
                valueRange = 2f..50f,
                steps = 6,
                colors = SliderDefaults.colors(thumbColor = BloomRose, activeTrackColor = BloomRose)
              )
            }

            // Live Location Guardian for Meetups
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (isCandleMode) Color(0xFF3B1F42) else BloomBlush,
              border = BorderStroke(1.dp, BloomWarmGold.copy(alpha = 0.3f))
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = BloomRose)
                Column(modifier = Modifier.weight(1f)) {
                  Text("Date Safety Live Location", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                  Text("Share live location with a trusted parent or friend during a meetup.", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                }
                if (liveLocationState.isSharing) {
                  Button(
                    onClick = onStopLiveLocation,
                    colors = ButtonDefaults.buttonColors(containerColor = BloomDanger),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                  ) {
                    Text("Stop", fontSize = 11.sp)
                  }
                } else {
                  Button(
                    onClick = { onStartLiveLocation("Ayaan Malik", 60, true) },
                    colors = ButtonDefaults.buttonColors(containerColor = BloomRose),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                  ) {
                    Text("Share 1h", fontSize = 11.sp)
                  }
                }
              }
            }
          }
        }
      }

      // Nearby List
      val filteredNearby = nearbyPeople.filter { it.distanceKm <= maxDistanceKm }

      items(filteredNearby, key = { it.id }) { person ->
        NeumorphicGlassCard(isCandleMode = isCandleMode, modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            Box(
              modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(person.avatarColorStart), Color(person.avatarColorEnd)))),
              contentAlignment = Alignment.Center
            ) {
              Text(person.initials, color = Color.White, fontWeight = FontWeight.Bold)
            }

            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(person.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                if (person.isVerified) {
                  Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = BloomOnline, modifier = Modifier.size(15.dp))
                }
              }

              Text(
                "${person.age} · ${person.approximateArea}",
                style = MaterialTheme.typography.labelSmall.copy(color = BloomRose, fontWeight = FontWeight.Bold)
              )

              Spacer(modifier = Modifier.height(4.dp))

              Text(
                person.interests.joinToString(" · "),
                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                maxLines = 1
              )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
              CompatibilityRing(percentage = person.compatibilityPercent, size = 38.dp, strokeWidth = 3.dp)
              Text("Match", fontSize = 9.sp, color = Color.Gray)
            }
          }
        }
      }
    } else {
      // Friend Suggestions Tab (Separate from Romantic Like)
      item {
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = if (isCandleMode) Color(0xFF3B1F42) else BloomPeach.copy(alpha = 0.4f),
          border = BorderStroke(1.dp, BloomRose.copy(alpha = 0.3f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Text("🤝", fontSize = 20.sp)
            Text(
              "Friend Requests are non-romantic connections for platonic community engagement, book discussions, and mutual circle events.",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)
            )
          }
        }
      }

      items(friendSuggestions, key = { it.id }) { friend ->
        NeumorphicGlassCard(isCandleMode = isCandleMode, modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            Box(
              modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(friend.avatarColorStart), Color(friend.avatarColorEnd)))),
              contentAlignment = Alignment.Center
            ) {
              Text(friend.initials, color = Color.White, fontWeight = FontWeight.Bold)
            }

            Column(modifier = Modifier.weight(1f)) {
              Text(friend.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
              Text(
                "${friend.age} · ${friend.city} · ${friend.compatibilityScore}% Compatibility",
                style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
              )
              Text(
                "Shared: ${friend.mutualInterests.joinToString(", ")}",
                style = MaterialTheme.typography.bodySmall.copy(color = BloomRose)
              )
            }

            when (friend.status) {
              FriendRequestStatus.SUGGESTED -> {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  IconButton(
                    onClick = { onRemoveFriendSuggestion(friend.id) },
                    modifier = Modifier.size(34.dp)
                  ) {
                    Icon(Icons.Default.Close, contentDescription = "Not Interested", tint = Color.Gray)
                  }
                  Button(
                    onClick = { onSendFriendRequest(friend.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = BloomRose),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(34.dp)
                  ) {
                    Text("Add 🤝", fontSize = 11.sp)
                  }
                }
              }

              FriendRequestStatus.SENT -> {
                Surface(
                  shape = RoundedCornerShape(9999.dp),
                  color = BloomOnline.copy(alpha = 0.2f),
                  border = BorderStroke(1.dp, BloomOnline)
                ) {
                  Text(
                    "Request Sent ✓",
                    fontSize = 11.sp,
                    color = BloomOnline,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }
              }

              FriendRequestStatus.ACCEPTED -> {
                Surface(
                  shape = RoundedCornerShape(9999.dp),
                  color = BloomRose.copy(alpha = 0.2f)
                ) {
                  Text(
                    "Friends 🌸",
                    fontSize = 11.sp,
                    color = BloomRose,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }
              }

              else -> {}
            }
          }
        }
      }
    }
  }
}
