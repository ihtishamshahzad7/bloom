package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CompatibilityScoreSummary
import com.example.model.QuizQuestion
import com.example.ui.components.CompatibilityRing
import com.example.ui.components.DecorativeCandle
import com.example.ui.components.NeumorphicGlassCard
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CompatibilityScreen(
  questions: List<QuizQuestion>,
  isCandleMode: Boolean,
  onAnswerQuestion: (Int, Int) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Results & Profile, 1: Quiz Questions
  val summary = remember { CompatibilityScoreSummary() }

  val answeredCount = questions.count { it.selectedIndex != null }
  val progressFraction = answeredCount.toFloat() / questions.size.coerceAtLeast(1)

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
          text = "Compatibility Harmony",
          style = MaterialTheme.typography.displaySmall.copy(
            fontStyle = FontStyle.Italic,
            color = if (isCandleMode) BloomWarmGold else BloomDeepRose
          )
        )
        Text(
          text = "Deep values, communication styles, and lifelong marriage resonance",
          style = MaterialTheme.typography.bodySmall.copy(
            color = if (isCandleMode) BloomCandleDarkMuted else BloomMuted
          )
        )
      }
    }

    // Toggle: "Harmony Profile" vs "Values Quiz (8 Questions)"
    item {
      Surface(
        shape = RoundedCornerShape(9999.dp),
        color = if (isCandleMode) BloomCandleDarkCard else BloomBlush.copy(alpha = 0.7f),
        border = BorderStroke(1.dp, BloomRose.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(modifier = Modifier.padding(4.dp)) {
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(9999.dp))
              .background(if (selectedTab == 0) BloomRose else Color.Transparent)
              .clickable { selectedTab = 0 }
              .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "Harmony Profile",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                color = if (selectedTab == 0) Color.White else if (isCandleMode) BloomCandleDarkMuted else BloomPlum
              )
            )
          }

          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(9999.dp))
              .background(if (selectedTab == 1) BloomRose else Color.Transparent)
              .clickable { selectedTab = 1 }
              .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "Values Quiz ($answeredCount/${questions.size})",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                color = if (selectedTab == 1) Color.White else if (isCandleMode) BloomCandleDarkMuted else BloomPlum
              )
            )
          }
        }
      }
    }

    if (selectedTab == 0) {
      // TAB 0: RESULTS & HARMONY PROFILE
      item {
        // Overall Resonance Hero
        NeumorphicGlassCard(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(28.dp),
          isCandleMode = isCandleMode
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Text(
                text = "Overall Alignment: 94%",
                style = MaterialTheme.typography.headlineMedium.copy(
                  fontStyle = FontStyle.Italic,
                  color = if (isCandleMode) BloomWarmGold else BloomDeepRose,
                  fontWeight = FontWeight.Bold
                )
              )
              Text(
                text = "Strongest synergy in Core Values, Family Closeness, and Compassionate Communication.",
                style = MaterialTheme.typography.bodySmall.copy(
                  color = if (isCandleMode) BloomCandleDarkMuted else BloomPlum
                )
              )
            }
            CompatibilityRing(percentage = 94, size = 68.dp, strokeWidth = 5.dp)
          }
        }
      }

      // Radar / 5-Axis Attribute Bars
      item {
        NeumorphicGlassCard(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(26.dp),
          isCandleMode = isCandleMode
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            Text(
              text = "5-Dimension Compatibility Radar",
              style = MaterialTheme.typography.titleMedium.copy(
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold,
                color = if (isCandleMode) BloomWarmGold else BloomPlum
              )
            )

            CompatibilityBarItem("Core Moral Values & Faith", summary.valuesMatch, BloomRose, isCandleMode)
            CompatibilityBarItem("Empathic Communication", summary.communicationMatch, BloomLavenderDeep, isCandleMode)
            CompatibilityBarItem("Family Traditions & Elders", summary.familyLifeMatch, BloomPeach, isCandleMode)
            CompatibilityBarItem("Romantic & Emotional Gentleness", summary.romanceVibeMatch, BloomCandleGold, isCandleMode)
            CompatibilityBarItem("Life Stewardship & Purpose", summary.lifeGoalsMatch, BloomOnline, isCandleMode)
          }
        }
      }

      // Love Language Ring & Text Card
      item {
        NeumorphicGlassCard(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(26.dp),
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
              Text("💖", fontSize = 20.sp)
              Text(
                text = "Primary Love Language",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
            }
            Text(
              text = summary.topLoveLanguage,
              style = MaterialTheme.typography.headlineSmall.copy(
                color = BloomRose,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic
              )
            )
            Text(
              text = "You feel most cherished when emotions are voiced with tenderness and when shared moments are completely undisturbed by modern distractions.",
              style = MaterialTheme.typography.bodySmall.copy(
                color = if (isCandleMode) BloomCandleDarkMuted else BloomPlum
              )
            )
          }
        }
      }

      // "Your Ideal Partner" Poetic Text Card
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(28.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (isCandleMode) BloomCandleDarkCard else Color(0xFFFFF6F8)
          ),
          border = BorderStroke(1.dp, BloomRose.copy(alpha = 0.3f))
        ) {
          Box(modifier = Modifier.fillMaxWidth().padding(22.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Text("🕊️", fontSize = 20.sp)
                Text(
                  text = "Your Ideal Partner Bloom",
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Bold,
                    color = if (isCandleMode) BloomWarmGold else BloomDeepRose
                  )
                )
              }

              Text(
                text = "“${summary.idealPartnerSummary}”",
                style = MaterialTheme.typography.bodyLarge.copy(
                  fontStyle = FontStyle.Italic,
                  color = if (isCandleMode) BloomCandleDarkText else BloomPlum,
                  lineHeight = 24.sp
                )
              )

              Button(
                onClick = { selectedTab = 1 },
                colors = ButtonDefaults.buttonColors(containerColor = BloomRose),
                shape = RoundedCornerShape(9999.dp),
                modifier = Modifier.align(Alignment.End).testTag("retake_quiz_button")
              ) {
                Text("Refine Quiz Answers")
              }
            }
          }
        }
      }
    } else {
      // TAB 1: 8 VALUES QUIZ QUESTIONS WITH FLOWER-BLOOM SELECTION
      item {
        LinearProgressIndicator(
          progress = { progressFraction },
          modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(9999.dp)),
          color = BloomRose,
          trackColor = BloomBlush
        )
      }

      itemsIndexed(questions, key = { _, q -> q.id }) { index, question ->
        QuizQuestionItem(
          index = index + 1,
          question = question,
          isCandleMode = isCandleMode,
          onSelectOption = { optIdx -> onAnswerQuestion(question.id, optIdx) }
        )
      }
    }
  }
}

@Composable
private fun CompatibilityBarItem(
  label: String,
  percent: Int,
  barColor: Color,
  isCandleMode: Boolean
) {
  Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
      )
      Text(
        text = "$percent%",
        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = barColor)
      )
    }

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(8.dp)
        .clip(RoundedCornerShape(9999.dp))
        .background(if (isCandleMode) Color(0x33FFFFFF) else Color(0x22000000))
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth(percent / 100f)
          .fillMaxHeight()
          .clip(RoundedCornerShape(9999.dp))
          .background(Brush.horizontalGradient(listOf(barColor.copy(alpha = 0.7f), barColor)))
      )
    }
  }
}

@Composable
private fun QuizQuestionItem(
  index: Int,
  question: QuizQuestion,
  isCandleMode: Boolean,
  onSelectOption: (Int) -> Unit
) {
  NeumorphicGlassCard(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(24.dp),
    isCandleMode = isCandleMode
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Surface(
          shape = RoundedCornerShape(9999.dp),
          color = BloomRose.copy(alpha = 0.15f)
        ) {
          Text(
            text = "Question $index • ${question.category}",
            style = MaterialTheme.typography.labelSmall.copy(color = BloomRose, fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }
      }

      Text(
        text = question.question,
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.Bold,
          color = if (isCandleMode) BloomWarmGold else BloomPlum
        )
      )

      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        question.options.forEachIndexed { optIndex, option ->
          val isSelected = question.selectedIndex == optIndex
          val cardBorder = if (isSelected) BloomRose else if (isCandleMode) BloomCandleDarkBorder else BloomRose.copy(alpha = 0.18f)
          val cardBg = if (isSelected) {
            if (isCandleMode) BloomCandleDarkSurface else Color(0xFFFFF2F5)
          } else {
            if (isCandleMode) Color.Transparent else Color.White
          }

          Surface(
            shape = RoundedCornerShape(16.dp),
            color = cardBg,
            border = BorderStroke(if (isSelected) 2.dp else 1.dp, cardBorder),
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onSelectOption(optIndex) }
              .testTag("quiz_${question.id}_opt_$optIndex")
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              // Flower Bloom Radio Icon
              Box(
                modifier = Modifier
                  .size(24.dp)
                  .clip(CircleShape)
                  .background(if (isSelected) BloomRose else Color.Transparent)
                  .border(2.dp, if (isSelected) BloomRose else Color.Gray.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
              ) {
                if (isSelected) {
                  Icon(
                    imageVector = Icons.Default.Spa,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                  )
                }
              }

              Column {
                Text(
                  text = option.label,
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) BloomRose else if (isCandleMode) BloomCandleDarkText else BloomPlum
                  )
                )
                Text(
                  text = option.explanation,
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    color = if (isCandleMode) BloomCandleDarkMuted else BloomMuted
                  )
                )
              }
            }
          }
        }
      }
    }
  }
}
