package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DecorativeCandle
import com.example.ui.components.NeumorphicGlassCard
import com.example.ui.theme.*

@Composable
fun PhoneAuthScreen(
  isCandleMode: Boolean,
  onVerificationSuccessful: () -> Unit,
  modifier: Modifier = Modifier
) {
  var ageConfirmed by remember { mutableStateOf(true) }
  var phoneNumber by remember { mutableStateOf("2065550192") }
  var otpCode by remember { mutableStateOf("") }
  var otpSent by remember { mutableStateOf(false) }
  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        if (isCandleMode) {
          Brush.verticalGradient(listOf(BloomCandleDarkBg, Color(0xFF231229), BloomCandleDarkBg))
        } else {
          Brush.verticalGradient(listOf(BloomBlush, BloomBlushMid, BloomLavender.copy(alpha = 0.5f)))
        }
      )
      .systemBarsPadding()
      .padding(20.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 480.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
      DecorativeCandle(candleHeight = 44.dp, candleWidth = 20.dp, isCandleMode = isCandleMode)

      // 18+ Age Gate & Phone Verification Card
      NeumorphicGlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        isCandleMode = isCandleMode
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Surface(
            shape = CircleShape,
            color = BloomRose.copy(alpha = 0.15f),
            modifier = Modifier.size(56.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(Icons.Default.Security, contentDescription = null, tint = BloomRose, modifier = Modifier.size(28.dp))
            }
          }

          Text(
            text = if (!otpSent) "18+ Age Gate & Phone Sign-In" else "Verify 6-Digit OTP",
            style = MaterialTheme.typography.headlineMedium.copy(
              fontStyle = FontStyle.Italic,
              fontWeight = FontWeight.Bold,
              color = if (isCandleMode) BloomWarmGold else BloomDeepRose
            ),
            textAlign = TextAlign.Center
          )

          // 18+ Mandatory Confirmation Checkbox
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isCandleMode) BloomCandleDarkSurface else Color(0xFFFFF2F4),
            border = BorderStroke(1.dp, BloomRose.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Checkbox(
                checked = ageConfirmed,
                onCheckedChange = { ageConfirmed = it },
                colors = CheckboxDefaults.colors(checkedColor = BloomRose),
                modifier = Modifier.testTag("age_confirm_checkbox")
              )
              Text(
                text = "I solemnly confirm I am 18 years of age or older and am seeking an intentional, marriage-minded life partner.",
                style = MaterialTheme.typography.bodySmall.copy(
                  fontSize = 11.sp,
                  color = if (isCandleMode) BloomCandleDarkText else BloomPlum,
                  lineHeight = 16.sp
                )
              )
            }
          }

          if (!otpSent) {
            // Step 1: Phone Number Input
            OutlinedTextField(
              value = phoneNumber,
              onValueChange = { phoneNumber = it },
              label = { Text("Phone Number (+1)") },
              leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = BloomRose) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
              shape = RoundedCornerShape(18.dp),
              modifier = Modifier.fillMaxWidth().testTag("phone_input_field"),
              singleLine = true
            )

            Text(
              text = "We will send an encrypted SMS verification code. Your number is never shown to prospective matches.",
              style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.sp,
                color = if (isCandleMode) BloomCandleDarkMuted else BloomMuted,
                textAlign = TextAlign.Center
              )
            )

            Button(
              onClick = {
                if (ageConfirmed && phoneNumber.isNotBlank()) {
                  isLoading = true
                  otpSent = true
                  isLoading = false
                }
              },
              enabled = ageConfirmed && phoneNumber.isNotBlank() && !isLoading,
              modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("send_otp_button"),
              shape = RoundedCornerShape(9999.dp),
              colors = ButtonDefaults.buttonColors(containerColor = BloomRose)
            ) {
              Text("Send Verification Code (OTP) 📲", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            }
          } else {
            // Step 2: OTP Entry
            OutlinedTextField(
              value = otpCode,
              onValueChange = { if (it.length <= 6) otpCode = it },
              label = { Text("Enter 6-Digit Code") },
              placeholder = { Text("123456") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              shape = RoundedCornerShape(18.dp),
              modifier = Modifier.fillMaxWidth().testTag("otp_input_field"),
              singleLine = true
            )

            Text(
              text = "Code sent to +1 $phoneNumber • Auto-testing demo code: 123456",
              style = MaterialTheme.typography.labelSmall.copy(
                color = BloomOnline,
                fontWeight = FontWeight.Medium
              )
            )

            Button(
              onClick = {
                // Accepts real code or demo 123456
                onVerificationSuccessful()
              },
              modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("verify_otp_button"),
              shape = RoundedCornerShape(9999.dp),
              colors = ButtonDefaults.buttonColors(containerColor = BloomRose)
            ) {
              Text("Verify & Enter Sanctuary ✨", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            }

            TextButton(onClick = { otpSent = false }) {
              Text("Change phone number", color = BloomRose)
            }
          }
        }
      }
    }
  }
}
