package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Security
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
import com.example.ui.theme.*

@Composable
fun SelfieVerificationDialog(
  isCandleMode: Boolean,
  onDismiss: () -> Unit,
  onSelfieCaptured: () -> Unit
) {
  var isCapturing by remember { mutableStateOf(false) }
  var isCompleted by remember { mutableStateOf(false) }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(28.dp),
      colors = CardDefaults.cardColors(
        containerColor = if (isCandleMode) BloomCandleDarkCard else Color.White
      ),
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
        .testTag("selfie_verification_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(Icons.Default.Security, contentDescription = null, tint = BloomRose)
          Text(
            text = "Selfie Liveness Verification",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = if (isCandleMode) BloomWarmGold else BloomDeepRose
            )
          )
        }

        Text(
          text = "Take a real-time selfie to prove liveness. Our admin team reviews every photo within 2 hours to safeguard member identities.",
          style = MaterialTheme.typography.bodySmall.copy(
            color = if (isCandleMode) BloomCandleDarkMuted else BloomPlum,
            textAlign = TextAlign.Center
          )
        )

        // Oval Face Guide Frame Placeholder (Simulating Camera Stream)
        Box(
          modifier = Modifier
            .size(190.dp, 240.dp)
            .shadow(6.dp, RoundedCornerShape(9999.dp))
            .clip(RoundedCornerShape(9999.dp))
            .background(
              Brush.verticalGradient(
                listOf(
                  Color(0xFF2C1632),
                  Color(0xFF45244E)
                )
              )
            )
            .border(3.dp, if (isCompleted) BloomOnline else BloomRose, RoundedCornerShape(9999.dp)),
          contentAlignment = Alignment.Center
        ) {
          if (!isCompleted) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(Icons.Default.Face, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(72.dp))
              Text("Position face inside oval", style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.8f)))
            }
          } else {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Box(
                modifier = Modifier.size(54.dp).background(BloomOnline, CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
              }
              Text("Selfie Captured!", style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
              Text("Status: Pending Admin Approval", style = MaterialTheme.typography.labelSmall.copy(color = BloomCandleGold))
            }
          }
        }

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = if (isCandleMode) BloomCandleDarkSurface else Color(0xFFFFF9FA),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "🛡️ Admin Approval Invariant: Your verified badge remains pending until human safety moderators verify photo authenticity.",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = BloomMuted, textAlign = TextAlign.Center),
            modifier = Modifier.padding(10.dp)
          )
        }

        if (!isCompleted) {
          Button(
            onClick = {
              isCompleted = true
              onSelfieCaptured()
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("capture_selfie_button"),
            colors = ButtonDefaults.buttonColors(containerColor = BloomRose),
            shape = RoundedCornerShape(9999.dp)
          ) {
            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Capture Liveness Selfie")
          }
        } else {
          Button(
            onClick = onDismiss,
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("done_selfie_button"),
            colors = ButtonDefaults.buttonColors(containerColor = BloomOnline),
            shape = RoundedCornerShape(9999.dp)
          ) {
            Text("Complete & Return")
          }
        }
      }
    }
  }
}
