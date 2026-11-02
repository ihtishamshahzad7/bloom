package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SafetySettings
import com.example.ui.components.NeumorphicGlassCard
import com.example.ui.theme.*

@Composable
fun SafetyCenterScreen(
  safetySettings: SafetySettings,
  isCandleMode: Boolean,
  onUpdateSafety: ((SafetySettings) -> SafetySettings) -> Unit,
  onOpenSelfieVerification: () -> Unit = {},
  onDeleteAccount: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  var showExportDialog by remember { mutableStateOf(false) }
  var showDeleteDialog by remember { mutableStateOf(false) }
  var showReportDialog by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(18.dp)
  ) {
    // Header
    item {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          text = "Safety & Trust Sanctuary",
          style = MaterialTheme.typography.displaySmall.copy(
            fontStyle = FontStyle.Italic,
            color = if (isCandleMode) BloomWarmGold else BloomDeepRose
          )
        )
        Text(
          text = "Zero tolerance for harm. Built for safe, respectful courtship.",
          style = MaterialTheme.typography.bodySmall.copy(
            color = if (isCandleMode) BloomCandleDarkMuted else BloomMuted
          )
        )
      }
    }

    // 1. 18+ AGE GATE CONFIRMATION CARD
    item {
      Surface(
        shape = RoundedCornerShape(22.dp),
        color = if (isCandleMode) BloomCandleDarkCard else Color(0xFFFFF2F4),
        border = BorderStroke(1.5.dp, BloomRose.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .background(BloomRose, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Text("18+", style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
          }
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Strict 18+ Young Adult Verification",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = "Bloom is exclusively for marriage-minded adults aged 18–30. Profiles of minors are strictly prohibited and immediately purged.",
              style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.sp,
                color = if (isCandleMode) BloomCandleDarkMuted else BloomPlum.copy(alpha = 0.8f)
              )
            )
          }
        }
      }
    }

    // 2. VERIFICATION STATUS CHECKLIST
    item {
      NeumorphicGlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        isCandleMode = isCandleMode
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Identity Verification Steps",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Surface(
              shape = RoundedCornerShape(9999.dp),
              color = BloomOnline.copy(alpha = 0.15f)
            ) {
              Text(
                text = "Verified Badge ✓",
                style = MaterialTheme.typography.labelSmall.copy(color = BloomOnline, fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }
          }

          VerificationRow("Phone Number OTP", "Confirmed on registration", isVerified = true, isCandleMode = isCandleMode)
          VerificationRow(
            title = "Real-Time Selfie Liveness",
            subtitle = if (safetySettings.selfieCaptured) "Selfie submitted • Pending Admin Review" else "Tap to capture liveness selfie",
            isVerified = safetySettings.selfieCaptured,
            isCandleMode = isCandleMode,
            onClick = onOpenSelfieVerification
          )
          VerificationRow("Government ID Check", "Optional additional trust layer", isVerified = safetySettings.idVerified, isCandleMode = isCandleMode) {
            onUpdateSafety { it.copy(idVerified = !it.idVerified) }
          }
        }
      }
    }

    // 3. PRIVACY & VISIBILITY CONTROLS
    item {
      NeumorphicGlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        isCandleMode = isCandleMode
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          Text(
            text = "Privacy & Profile Visibility",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )

          // Incognito
          ControlToggleRow(
            title = "Hide Profile (Incognito Mode)",
            subtitle = "Only members you have explicitly liked will be able to discover you",
            checked = safetySettings.isIncognitoHidden,
            onCheckedChange = { chk -> onUpdateSafety { it.copy(isIncognitoHidden = chk) } },
            isCandleMode = isCandleMode,
            tag = "incognito_toggle"
          )

          // Photo Privacy (Blur before mutual)
          ControlToggleRow(
            title = "Photo Privacy Guard",
            subtitle = "Keep your photos softly blurred until mutual interest is established",
            checked = safetySettings.blurPhotosUntilMatched,
            onCheckedChange = { chk -> onUpdateSafety { it.copy(blurPhotosUntilMatched = chk) } },
            isCandleMode = isCandleMode,
            tag = "blur_photos_toggle"
          )

          // Consent before photos in chat
          ControlToggleRow(
            title = "Ask Before Sharing In-Chat Photos",
            subtitle = "Direct photos in messages require explicit acceptance before unblurring",
            checked = safetySettings.askConsentBeforePhotos,
            onCheckedChange = { chk -> onUpdateSafety { it.copy(askConsentBeforePhotos = chk) } },
            isCandleMode = isCandleMode,
            tag = "photo_consent_toggle"
          )

          // Pause Account
          ControlToggleRow(
            title = "Pause My Account",
            subtitle = "Temporarily freeze discovery while keeping your existing matches safe",
            checked = safetySettings.isAccountPaused,
            onCheckedChange = { chk -> onUpdateSafety { it.copy(isAccountPaused = chk) } },
            isCandleMode = isCandleMode,
            tag = "pause_account_toggle"
          )
        }
      }
    }

    // 4. FRAUD & ROMANCE SCAM PREVENTION TIPS
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = "Romance & Safety Guidelines",
          style = MaterialTheme.typography.headlineSmall.copy(
            fontStyle = FontStyle.Italic,
            color = if (isCandleMode) BloomWarmGold else BloomDeepRose
          )
        )

        ScamTipCard(
          icon = "💸",
          title = "Financial Red Flags",
          description = "Anyone asking for money, travel funds, investment tips, or crypto is committing fraud. Bloom members never solicit funds.",
          isCandleMode = isCandleMode
        )
        ScamTipCard(
          icon = "☕",
          title = "Meet Exclusively in Public",
          description = "Always schedule first meetings in verified public cafés, parks, or galleries. Inform a family member of your plan.",
          isCandleMode = isCandleMode
        )
        ScamTipCard(
          icon = "🔒",
          title = "Protect Private Credentials",
          description = "Bloom staff will never request your account password, email OTP, or banking verification numbers.",
          isCandleMode = isCandleMode
        )
      }
    }

    // 5. ACCOUNT ACTIONS & PRIVACY STATEMENT
    item {
      NeumorphicGlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        isCandleMode = isCandleMode
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Text(
            text = "Data Stewardship & Account Rights",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedButton(
              onClick = { showExportDialog = true },
              shape = RoundedCornerShape(9999.dp),
              modifier = Modifier.weight(1f).testTag("export_data_button")
            ) {
              Text("Export My Data", fontSize = 12.sp)
            }

            Button(
              onClick = { showDeleteDialog = true },
              shape = RoundedCornerShape(9999.dp),
              colors = ButtonDefaults.buttonColors(containerColor = BloomDanger.copy(alpha = 0.9f)),
              modifier = Modifier.weight(1f).testTag("delete_account_button")
            ) {
              Text("Delete Account", fontSize = 12.sp)
            }
          }

          Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isCandleMode) BloomCandleDarkSurface else Color(0xFFF7F4FA),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(Icons.Outlined.Lock, contentDescription = null, tint = BloomLavenderDeep, modifier = Modifier.size(16.dp))
              Text(
                text = "Privacy Statement: Your personal data is end-to-end encrypted and never sold or shared with advertisers.",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = BloomMuted)
              )
            }
          }
        }
      }
    }
  }

  // Export Data Dialog
  if (showExportDialog) {
    AlertDialog(
      onDismissRequest = { showExportDialog = false },
      title = { Text("Export Your Data 📦") },
      text = { Text("A secure archive of your profile details, quiz responses, and conversation transcripts has been prepared for local download.") },
      confirmButton = {
        Button(
          onClick = { showExportDialog = false },
          colors = ButtonDefaults.buttonColors(containerColor = BloomRose)
        ) {
          Text("Download Archive (.json)")
        }
      },
      dismissButton = {
        TextButton(onClick = { showExportDialog = false }) { Text("Close") }
      }
    )
  }

  // Delete Account Dialog
  if (showDeleteDialog) {
    AlertDialog(
      onDismissRequest = { showDeleteDialog = false },
      title = { Text("Delete Account Permanently?", color = BloomDanger) },
      text = { Text("This will permanently remove your profile, match history, and photos from Bloom servers. This action is irreversible.") },
      confirmButton = {
        Button(
          onClick = {
            showDeleteDialog = false
            onDeleteAccount()
          },
          colors = ButtonDefaults.buttonColors(containerColor = BloomDanger)
        ) {
          Text("Confirm Permanent Deletion")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
      }
    )
  }
}

@Composable
private fun VerificationRow(
  title: String,
  subtitle: String,
  isVerified: Boolean,
  isCandleMode: Boolean,
  onClick: (() -> Unit)? = null
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(enabled = onClick != null) { onClick?.invoke() }
      .padding(vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Box(
        modifier = Modifier
          .size(28.dp)
          .background(if (isVerified) BloomOnline else Color.Gray.copy(alpha = 0.3f), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = if (isVerified) Icons.Default.Check else Icons.Default.Add,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(16.dp)
        )
      }
      Column {
        Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
        Text(subtitle, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = BloomMuted))
      }
    }

    if (onClick != null) {
      Text(
        text = if (isVerified) "Verified ✓" else "Verify",
        style = MaterialTheme.typography.labelSmall.copy(
          color = if (isVerified) BloomOnline else BloomRose,
          fontWeight = FontWeight.Bold
        )
      )
    }
  }
}

@Composable
private fun ControlToggleRow(
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  isCandleMode: Boolean,
  tag: String
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
      Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
      Text(subtitle, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = BloomMuted))
    }
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors = SwitchDefaults.colors(
        checkedThumbColor = Color.White,
        checkedTrackColor = BloomRose
      ),
      modifier = Modifier.testTag(tag)
    )
  }
}

@Composable
private fun ScamTipCard(
  icon: String,
  title: String,
  description: String,
  isCandleMode: Boolean
) {
  Surface(
    shape = RoundedCornerShape(18.dp),
    color = if (isCandleMode) BloomCandleDarkCard else Color.White,
    border = BorderStroke(1.dp, BloomRose.copy(alpha = 0.15f)),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier.padding(14.dp),
      verticalAlignment = Alignment.Top,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Text(icon, fontSize = 20.sp)
      Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
        Text(description, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = BloomMuted, lineHeight = 16.sp))
      }
    }
  }
}
