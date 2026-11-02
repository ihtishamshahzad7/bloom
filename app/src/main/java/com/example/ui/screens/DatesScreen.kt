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
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DateIdea
import com.example.model.ScheduledDate
import com.example.model.UserProfile
import com.example.ui.components.DecorativeCandle
import com.example.ui.components.NeumorphicGlassCard
import com.example.ui.theme.*

@Composable
fun DatesScreen(
  scheduledDates: List<ScheduledDate>,
  dateIdeas: List<DateIdea>,
  profiles: List<UserProfile>,
  isCandleMode: Boolean,
  onProposeDate: (UserProfile, DateIdea, String, String) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedIdeaForProposal by remember { mutableStateOf<DateIdea?>(null) }
  var selectedMatchForProposal by remember { mutableStateOf(profiles.firstOrNull()) }
  var proposedDateText by remember { mutableStateOf("Saturday, Nov 15") }
  var proposedTimeText by remember { mutableStateOf("3:00 PM") }
  var showSosAlert by remember { mutableStateOf(false) }
  var checkInTimerHours by remember { mutableIntStateOf(2) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(20.dp)
  ) {
    // Header
    item {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          text = "Plan a Date",
          style = MaterialTheme.typography.displaySmall.copy(
            fontStyle = FontStyle.Italic,
            color = if (isCandleMode) BloomWarmGold else BloomDeepRose
          )
        )
        Text(
          text = "Safe, thoughtful, public-first courtship experiences",
          style = MaterialTheme.typography.bodySmall.copy(
            color = if (isCandleMode) BloomCandleDarkMuted else BloomMuted
          )
        )
      }
    }

    // 1. DATE SAFETY CARD (Trusted contact, timer, SOS)
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
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(Icons.Outlined.Security, contentDescription = null, tint = BloomRose, modifier = Modifier.size(24.dp))
              Text(
                text = "Courtship Safety Guardian",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
            }

            Surface(
              shape = RoundedCornerShape(9999.dp),
              color = BloomOnline.copy(alpha = 0.15f),
              border = BorderStroke(1.dp, BloomOnline.copy(alpha = 0.4f))
            ) {
              Text(
                text = "Active Protection",
                style = MaterialTheme.typography.labelSmall.copy(color = BloomOnline, fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }
          }

          Text(
            text = "Every Bloom date is verified to be in a public setting. Share live location links with a parent or trusted friend with a single tap.",
            style = MaterialTheme.typography.bodySmall.copy(
              color = if (isCandleMode) BloomCandleDarkMuted else BloomPlum.copy(alpha = 0.85f),
              lineHeight = 18.sp
            )
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (isCandleMode) BloomCandleDarkSurface else Color.White,
              border = BorderStroke(1.dp, BloomRose.copy(alpha = 0.2f)),
              modifier = Modifier.weight(1f)
            ) {
              Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Text("⏱️", fontSize = 16.sp)
                Column {
                  Text("Check-In Timer", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                  Text("${checkInTimerHours}h auto reminder", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, color = BloomMuted))
                }
              }
            }

            // SOS Emergency Button
            Button(
              onClick = { showSosAlert = true },
              colors = ButtonDefaults.buttonColors(containerColor = BloomDanger),
              shape = RoundedCornerShape(9999.dp),
              contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
              modifier = Modifier.testTag("sos_button")
            ) {
              Icon(Icons.Default.Emergency, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("SOS Assist", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            }
          }
        }
      }
    }

    // 2. ROMANTIC DATE IDEAS CAROUSEL (5 Ideas with Propose)
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = "Curated Date Inspirations",
          style = MaterialTheme.typography.headlineSmall.copy(
            fontStyle = FontStyle.Italic,
            color = if (isCandleMode) BloomWarmGold else BloomDeepRose
          )
        )
        Text(
          text = "Safe, elegant settings designed for genuine, pressure-free connection",
          style = MaterialTheme.typography.bodySmall.copy(
            color = if (isCandleMode) BloomCandleDarkMuted else BloomMuted
          )
        )

        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          contentPadding = PaddingValues(vertical = 4.dp)
        ) {
          items(dateIdeas) { idea ->
            DateIdeaCard(
              idea = idea,
              isCandleMode = isCandleMode,
              onPropose = { selectedIdeaForProposal = idea }
            )
          }
        }
      }
    }

    // 3. MONTHLY CALENDAR GRID & SCHEDULED DATES
    item {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Upcoming Gatherings",
            style = MaterialTheme.typography.headlineSmall.copy(
              fontStyle = FontStyle.Italic,
              color = if (isCandleMode) BloomWarmGold else BloomDeepRose
            )
          )

          Surface(
            shape = RoundedCornerShape(9999.dp),
            color = if (isCandleMode) BloomCandleDarkCard else BloomBlush
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = BloomRose, modifier = Modifier.size(14.dp))
              Text("November 2026", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = BloomRose))
            }
          }
        }

        // Mini Month Calendar Chips Row
        MiniCalendarStrip(isCandleMode = isCandleMode)

        // Scheduled Dates list
        if (scheduledDates.isEmpty()) {
          NeumorphicGlassCard(
            modifier = Modifier.fillMaxWidth(),
            isCandleMode = isCandleMode
          ) {
            Column(
              modifier = Modifier.fillMaxWidth().padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Text("🕊️", fontSize = 28.sp)
              Text("No dates planned yet", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
              Text("Select a curated idea above to propose to one of your mutual matches.", style = MaterialTheme.typography.bodySmall.copy(color = BloomMuted), textAlign = TextAlign.Center)
            }
          }
        } else {
          scheduledDates.forEach { date ->
            ScheduledDateCard(date = date, isCandleMode = isCandleMode)
          }
        }
      }
    }
  }

  // Date Proposal Modal Dialog
  if (selectedIdeaForProposal != null) {
    val idea = selectedIdeaForProposal!!
    AlertDialog(
      onDismissRequest = { selectedIdeaForProposal = null },
      containerColor = if (isCandleMode) BloomCandleDarkCard else Color(0xFFFFF7F9),
      title = {
        Text(
          text = "Propose Date: ${idea.title}",
          style = MaterialTheme.typography.headlineSmall.copy(
            fontStyle = FontStyle.Italic,
            color = BloomRose
          )
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text(
            text = idea.description,
            style = MaterialTheme.typography.bodySmall.copy(
              color = if (isCandleMode) BloomCandleDarkMuted else BloomPlum
            )
          )

          // Select Match dropdown or radio
          Text("Select Match:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            profiles.take(3).forEach { p ->
              val isSel = selectedMatchForProposal?.id == p.id
              Surface(
                shape = RoundedCornerShape(9999.dp),
                color = if (isSel) BloomRose else if (isCandleMode) BloomCandleDarkSurface else Color.White,
                border = BorderStroke(1.dp, BloomRose.copy(alpha = 0.4f)),
                modifier = Modifier.clickable { selectedMatchForProposal = p }
              ) {
                Text(
                  text = p.name,
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isSel) Color.White else if (isCandleMode) BloomCandleDarkText else BloomPlum,
                    fontWeight = FontWeight.Bold
                  ),
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
              }
            }
          }

          OutlinedTextField(
            value = proposedDateText,
            onValueChange = { proposedDateText = it },
            label = { Text("Proposed Date") },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
          )

          OutlinedTextField(
            value = proposedTimeText,
            onValueChange = { proposedTimeText = it },
            label = { Text("Proposed Time") },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (selectedMatchForProposal != null) {
              onProposeDate(selectedMatchForProposal!!, idea, proposedDateText, proposedTimeText)
              selectedIdeaForProposal = null
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = BloomRose),
          shape = RoundedCornerShape(9999.dp),
          modifier = Modifier.testTag("confirm_propose_date_button")
        ) {
          Text("Send Proposal 💞")
        }
      },
      dismissButton = {
        TextButton(onClick = { selectedIdeaForProposal = null }) {
          Text("Cancel")
        }
      }
    )
  }

  // SOS Simulation Alert Dialog
  if (showSosAlert) {
    AlertDialog(
      onDismissRequest = { showSosAlert = false },
      containerColor = if (isCandleMode) BloomCandleDarkCard else Color.White,
      title = {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Icon(Icons.Default.Emergency, contentDescription = null, tint = BloomDanger)
          Text("SOS Safety Protocol", color = BloomDanger, fontWeight = FontWeight.Bold)
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Bloom Safety Guardian will instantly:", style = MaterialTheme.typography.bodyMedium)
          Text("1. Send your live GPS location to your designated trusted family contact.")
          Text("2. Dispatch a discrete fake phone call to provide an immediate exit excuse.")
          Text("3. Provide one-tap calling to local public emergency services.")
        }
      },
      confirmButton = {
        Button(
          onClick = { showSosAlert = false },
          colors = ButtonDefaults.buttonColors(containerColor = BloomDanger),
          shape = RoundedCornerShape(9999.dp)
        ) {
          Text("Simulate Safety Alert")
        }
      },
      dismissButton = {
        TextButton(onClick = { showSosAlert = false }) {
          Text("Dismiss")
        }
      }
    )
  }
}

@Composable
private fun DateIdeaCard(
  idea: DateIdea,
  isCandleMode: Boolean,
  onPropose: () -> Unit
) {
  NeumorphicGlassCard(
    modifier = Modifier.width(220.dp),
    shape = RoundedCornerShape(24.dp),
    isCandleMode = isCandleMode
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = CircleShape,
          color = BloomCandleGold.copy(alpha = 0.2f),
          modifier = Modifier.size(38.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Text(idea.emoji, fontSize = 20.sp)
          }
        }

        Surface(
          shape = RoundedCornerShape(9999.dp),
          color = if (isCandleMode) BloomCandleDarkSurface else BloomBlush
        ) {
          Text(
            text = idea.estimatedTime,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = BloomDeepRose),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }
      }

      Text(
        text = idea.title,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        maxLines = 2
      )

      Text(
        text = idea.description,
        style = MaterialTheme.typography.bodySmall.copy(
          fontSize = 11.sp,
          color = if (isCandleMode) BloomCandleDarkMuted else BloomPlum.copy(alpha = 0.8f),
          lineHeight = 16.sp
        ),
        maxLines = 3
      )

      Button(
        onClick = onPropose,
        shape = RoundedCornerShape(9999.dp),
        colors = ButtonDefaults.buttonColors(containerColor = BloomRose),
        modifier = Modifier.fillMaxWidth().testTag("propose_date_${idea.id}"),
        contentPadding = PaddingValues(vertical = 6.dp)
      ) {
        Text("Propose 💌", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
      }
    }
  }
}

@Composable
private fun MiniCalendarStrip(isCandleMode: Boolean) {
  val days = listOf("Fri 6", "Sat 7", "Sun 8", "Mon 9", "Tue 10", "Wed 11", "Thu 12")
  LazyRow(
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    items(days) { day ->
      val isToday = day == "Sat 7"
      val hasEvent = day == "Sat 8"
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isToday) BloomRose else if (isCandleMode) BloomCandleDarkCard else Color.White,
        border = BorderStroke(1.dp, if (hasEvent) BloomCandleGold else BloomRose.copy(alpha = 0.2f))
      ) {
        Column(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
          Text(
            text = day.split(" ")[0],
            style = MaterialTheme.typography.labelSmall.copy(
              color = if (isToday) Color.White.copy(alpha = 0.8f) else BloomMuted,
              fontSize = 10.sp
            )
          )
          Text(
            text = day.split(" ")[1],
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = if (isToday) Color.White else if (isCandleMode) BloomCandleDarkText else BloomPlum
            )
          )
          if (hasEvent) {
            Box(modifier = Modifier.size(5.dp).background(BloomCandleGold, CircleShape))
          }
        }
      }
    }
  }
}

@Composable
private fun ScheduledDateCard(
  date: ScheduledDate,
  isCandleMode: Boolean
) {
  NeumorphicGlassCard(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(22.dp),
    isCandleMode = isCandleMode
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(Brush.linearGradient(listOf(Color(date.matchColorStart), Color(date.matchColorEnd)))),
            contentAlignment = Alignment.Center
          ) {
            Text(date.matchInitials, style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
          }
          Column {
            Text(
              text = "Date with ${date.matchName}",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = "${date.dateFormatted} • ${date.timeFormatted}",
              style = MaterialTheme.typography.bodySmall.copy(color = BloomRose, fontWeight = FontWeight.Medium)
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(9999.dp),
          color = BloomOnline.copy(alpha = 0.15f)
        ) {
          Text(
            text = "Confirmed",
            style = MaterialTheme.typography.labelSmall.copy(color = BloomOnline, fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }
      }

      Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isCandleMode) BloomCandleDarkSurface else Color(0xFFFFF9FA),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text("📍 ${date.location}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = BloomOnline, modifier = Modifier.size(14.dp))
            Text("Shared with trusted guardian contact", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, color = BloomMuted))
          }
        }
      }
    }
  }
}
