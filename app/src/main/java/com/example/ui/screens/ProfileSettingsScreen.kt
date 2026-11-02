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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppThemeMode
import com.example.model.PrivacySettings
import com.example.ui.components.DecorativeCandle
import com.example.ui.components.NeumorphicGlassCard
import com.example.ui.theme.*

@Composable
fun ProfileSettingsScreen(
  themeMode: AppThemeMode,
  isCandleMode: Boolean,
  ambientPetalsEnabled: Boolean,
  isRtlUrdu: Boolean,
  isPremium: Boolean,
  privacySettings: PrivacySettings,
  onSelectThemeMode: (AppThemeMode) -> Unit,
  onToggleCandleMode: () -> Unit,
  onToggleAmbientPetals: () -> Unit,
  onToggleLanguage: () -> Unit,
  onOpenPremium: () -> Unit,
  onOpenSelfieVerification: () -> Unit,
  onUpdatePrivacy: ((PrivacySettings) -> PrivacySettings) -> Unit,
  onExportData: () -> String,
  onDeleteAccount: () -> Unit,
  modifier: Modifier = Modifier
) {
  var nicknameText by remember { mutableStateOf("Mia 🌸") }
  var statusLineText by remember { mutableStateOf("Finding joy in the quiet moments 🌸") }
  var bioText by remember {
    mutableStateOf(
      "Architectural researcher & lover of gentle evening walks. Cherish family dinners, quiet reading by candlelight, and building a peaceful sanctuary for two."
    )
  }
  var promptAnswerText by remember {
    mutableStateOf("A warm kitchen filled with the scent of fresh cardamom tea and listening with genuine presence.")
  }

  var showDeleteDialog by remember { mutableStateOf(false) }
  var showExportDialog by remember { mutableStateOf(false) }
  var exportedDataJson by remember { mutableStateOf("") }
  var savedSuccessSnackbar by remember { mutableStateOf(false) }

  val myInterests = listOf("☕ Specialty Tea", "📖 Romantic Realism", "🌿 Glasshouse Plants", "🎻 Acoustic Strings", "✈️ Historic Architecture")

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(18.dp)
  ) {
    // Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = if (isRtlUrdu) "میری پروفائل اور ترتیبات" else "Profile & Privacy",
            style = MaterialTheme.typography.headlineMedium.copy(
              fontStyle = FontStyle.Italic,
              fontWeight = FontWeight.Bold,
              color = if (isCandleMode) BloomWarmGold else BloomDeepRose
            )
          )
          Text(
            text = if (isRtlUrdu) "محبت اور وقار کے ساتھ محفوظ ترتیبات" else "Sacred, intentional presence & full data sovereignty",
            style = MaterialTheme.typography.bodySmall.copy(
              color = if (isCandleMode) BloomCandleDarkMuted else BloomPlum
            )
          )
        }

        DecorativeCandle(candleHeight = 38.dp, candleWidth = 18.dp, isCandleMode = isCandleMode)
      }
    }

    // Cover + Profile Avatar + Badges Card
    item {
      NeumorphicGlassCard(isCandleMode = isCandleMode, modifier = Modifier.fillMaxWidth()) {
        Column {
          // Cover Banner Gradient
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(90.dp)
              .background(Brush.horizontalGradient(listOf(BloomBlushMid, BloomLavender))),
            contentAlignment = Alignment.TopEnd
          ) {
            Surface(
              shape = RoundedCornerShape(9999.dp),
              color = Color.White.copy(alpha = 0.8f),
              modifier = Modifier.padding(8.dp)
            ) {
              Text("Cover Photo", fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
            }
          }

          Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // Profile Photo with Online Green Dot
            Box(modifier = Modifier.offset(y = (-45).dp)) {
              Box(
                modifier = Modifier
                  .size(86.dp)
                  .shadow(8.dp, CircleShape)
                  .clip(CircleShape)
                  .background(Brush.linearGradient(listOf(BloomRose, BloomLavender)))
                  .border(3.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Text("M", style = MaterialTheme.typography.headlineMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
              }

              if (!privacySettings.hideOnlineStatus) {
                Box(
                  modifier = Modifier
                    .size(18.dp)
                    .background(BloomOnline, CircleShape)
                    .border(2.dp, Color.White, CircleShape)
                    .align(Alignment.BottomEnd)
                )
              }
            }

            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.offset(y = (-35).dp)
            ) {
              Text(
                text = nicknameText,
                style = MaterialTheme.typography.headlineSmall.copy(
                  fontWeight = FontWeight.Bold,
                  fontStyle = FontStyle.Italic,
                  color = if (isCandleMode) Color.White else BloomPlum
                )
              )

              Text(
                text = statusLineText,
                style = MaterialTheme.typography.bodySmall.copy(
                  color = if (isCandleMode) BloomWarmGold else BloomDeepRose,
                  fontStyle = FontStyle.Italic
                )
              )

              Spacer(modifier = Modifier.height(8.dp))

              // Badges: Verified Identity 🛡️, Streak 🌸🔥, Early Bloomer 🌱✨
              Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(shape = RoundedCornerShape(9999.dp), color = BloomOnline.copy(alpha = 0.15f), border = BorderStroke(1.dp, BloomOnline)) {
                  Text("Verified 🛡️", fontSize = 11.sp, color = BloomOnline, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
                Surface(shape = RoundedCornerShape(9999.dp), color = BloomCandleGold.copy(alpha = 0.2f), border = BorderStroke(1.dp, BloomCandleGold)) {
                  Text("Streak 🌸🔥", fontSize = 11.sp, color = if (isCandleMode) BloomWarmGold else BloomDeepRose, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
                Surface(shape = RoundedCornerShape(9999.dp), color = BloomLavender.copy(alpha = 0.3f), border = BorderStroke(1.dp, BloomLavender)) {
                  Text("Early Bloomer 🌱✨", fontSize = 11.sp, color = if (isCandleMode) Color.White else BloomPlum, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
              }
            }
          }
        }
      }
    }

    // Shared Photos & Videos Gallery (with privacy lock)
    item {
      NeumorphicGlassCard(isCandleMode = isCandleMode, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("Shared Gallery (3 items)", fontWeight = FontWeight.Bold)
              Text("Visible only to approved mutual matches", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
            }
            Icon(Icons.Outlined.Lock, contentDescription = "Protected", tint = BloomRose)
          }

          LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(
              listOf(
                Triple("Sunset in Hunza", 0xFFE85D8A, 0xFFF6C177),
                Triple("Art Exhibit Visit", 0xFF9E77ED, 0xFFB79CF0),
                Triple("Chai Moments (Video)", 0xFFFF9BB3, 0xFFFFD3C2)
              )
            ) { (title, c1, c2) ->
              Box(
                modifier = Modifier
                  .size(110.dp, 80.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .background(Brush.linearGradient(listOf(Color(c1), Color(c2)))),
                contentAlignment = Alignment.Center
              ) {
                Text(title, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(4.dp))
              }
            }
          }
        }
      }
    }

    // Comprehensive Privacy & Visibility Settings
    item {
      NeumorphicGlassCard(isCandleMode = isCandleMode, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text(
            "Privacy Controls 🔒",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              fontStyle = FontStyle.Italic,
              color = if (isCandleMode) BloomWarmGold else BloomDeepRose
            )
          )

          // Hide Last Seen
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("Hide Last Seen", fontWeight = FontWeight.Bold)
              Text("Do not show when you were last active.", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
            }
            Switch(
              checked = privacySettings.hideLastSeen,
              onCheckedChange = { checked -> onUpdatePrivacy { it.copy(hideLastSeen = checked) } },
              colors = SwitchDefaults.colors(checkedThumbColor = BloomRose)
            )
          }

          HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f), thickness = 0.5.dp)

          // Hide Online Status
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("Hide Online Status", fontWeight = FontWeight.Bold)
              Text("Suppress the green presence indicator.", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
            }
            Switch(
              checked = privacySettings.hideOnlineStatus,
              onCheckedChange = { checked -> onUpdatePrivacy { it.copy(hideOnlineStatus = checked) } },
              colors = SwitchDefaults.colors(checkedThumbColor = BloomRose)
            )
          }

          HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f), thickness = 0.5.dp)

          // Ask consent before photos
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("Require Photo Consent", fontWeight = FontWeight.Bold)
              Text("Blur new match media until mutually agreed.", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
            }
            Switch(
              checked = privacySettings.askConsentBeforePhotos,
              onCheckedChange = { checked -> onUpdatePrivacy { it.copy(askConsentBeforePhotos = checked) } },
              colors = SwitchDefaults.colors(checkedThumbColor = BloomRose)
            )
          }
        }
      }
    }

    // Appearance & Language Controls
    item {
      NeumorphicGlassCard(isCandleMode = isCandleMode, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
          Text("Appearance & Language", fontWeight = FontWeight.Bold)

          // Theme Mode Selector
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf(
              Pair(AppThemeMode.SYSTEM, "System"),
              Pair(AppThemeMode.LIGHT, "Light"),
              Pair(AppThemeMode.DARK, "Dark")
            ).forEach { (mode, label) ->
              FilterChip(
                selected = themeMode == mode,
                onClick = { onSelectThemeMode(mode) },
                label = { Text(label, fontSize = 11.sp) }
              )
            }
          }

          // Candle Mode Toggle
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Candle Mode 🕯️ (Evening Dimming)")
            Switch(checked = isCandleMode, onCheckedChange = { onToggleCandleMode() })
          }

          // Urdu Language Switcher
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Language: English / اردو (RTL)")
            Button(
              onClick = onToggleLanguage,
              colors = ButtonDefaults.buttonColors(containerColor = BloomRose)
            ) {
              Text(if (isRtlUrdu) "English" else "اردو")
            }
          }
        }
      }
    }

    // Data Sovereignty & Account Deletion
    item {
      NeumorphicGlassCard(isCandleMode = isCandleMode, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text(
            "Data Sovereignty & Safety",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = BloomDanger)
          )

          // Export Data Button
          Button(
            onClick = {
              exportedDataJson = onExportData()
              showExportDialog = true
            },
            colors = ButtonDefaults.buttonColors(containerColor = BloomLavender),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("Download My Data (JSON)", color = BloomPlum, fontWeight = FontWeight.Bold)
          }

          // Delete Account Button
          Button(
            onClick = { showDeleteDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = BloomDanger),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("Permanently Delete Account & Media", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }

  // Export Data Dialog
  if (showExportDialog) {
    AlertDialog(
      onDismissRequest = { showExportDialog = false },
      title = { Text("Exported Member Archive", fontStyle = FontStyle.Italic) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Your complete activity and profile archive:", style = MaterialTheme.typography.bodySmall)
          Surface(
            color = Color.Black.copy(alpha = 0.05f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(180.dp)
          ) {
            Text(
              exportedDataJson,
              fontSize = 11.sp,
              modifier = Modifier.padding(8.dp)
            )
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showExportDialog = false }) {
          Text("Done", color = BloomRose)
        }
      }
    )
  }

  // Delete Account Confirmation Dialog
  if (showDeleteDialog) {
    AlertDialog(
      onDismissRequest = { showDeleteDialog = false },
      title = { Text("Confirm Account Deletion", color = BloomDanger) },
      text = {
        Text("This action is permanent and irreversible. All your mutual matches, stories, shared photos, and conversations will be deleted immediately.")
      },
      confirmButton = {
        Button(
          onClick = {
            onDeleteAccount()
            showDeleteDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = BloomDanger)
        ) {
          Text("Permanently Delete")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}
