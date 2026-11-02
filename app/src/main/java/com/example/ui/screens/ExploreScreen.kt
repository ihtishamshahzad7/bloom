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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
  searchQuery: String,
  trendingHashtags: List<TrendingHashtag>,
  suggestedPeople: List<FriendSuggestion>,
  communities: List<Community>,
  feedPosts: List<FeedPost>,
  isCandleMode: Boolean,
  onSearchChange: (String) -> Unit,
  onHashtagClick: (String) -> Unit,
  onConnectPerson: (String) -> Unit,
  onSelectCommunity: (Community) -> Unit,
  onPostClick: (FeedPost) -> Unit
) {
  var selectedFilterCategory by remember { mutableStateOf("All") }

  val filteredHashtags = remember(trendingHashtags, searchQuery) {
    if (searchQuery.isBlank()) trendingHashtags
    else trendingHashtags.filter { it.tag.contains(searchQuery, ignoreCase = true) || it.category.contains(searchQuery, ignoreCase = true) }
  }

  val filteredPeople = remember(suggestedPeople, searchQuery) {
    if (searchQuery.isBlank()) suggestedPeople
    else suggestedPeople.filter { it.name.contains(searchQuery, ignoreCase = true) || it.city.contains(searchQuery, ignoreCase = true) || it.mutualInterests.any { i -> i.contains(searchQuery, ignoreCase = true) } }
  }

  val filteredCommunities = remember(communities, searchQuery) {
    if (searchQuery.isBlank()) communities
    else communities.filter { it.name.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true) }
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("explore_screen"),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(18.dp)
  ) {
    // 1. Search Bar
    item {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchChange,
        placeholder = { Text("Search people, #hashtags, circles, or city...") },
        leadingIcon = {
          Icon(Icons.Default.Search, contentDescription = "Search", tint = BloomRose)
        },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { onSearchChange("") }) {
              Icon(Icons.Default.Clear, contentDescription = "Clear", tint = BloomPlumMuted)
            }
          }
        },
        shape = RoundedCornerShape(9999.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = BloomRose,
          unfocusedBorderColor = if (isCandleMode) Color.White.copy(alpha = 0.2f) else BloomBlushMid
        ),
        modifier = Modifier
          .fillMaxWidth()
          .shadow(4.dp, RoundedCornerShape(9999.dp))
          .testTag("explore_search_input")
      )
    }

    // Category Filter Chips
    item {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("All", "Literature", "Architecture", "Chai", "Travel").forEach { cat ->
          FilterChip(
            selected = selectedFilterCategory == cat,
            onClick = { selectedFilterCategory = cat },
            label = { Text(cat, fontSize = 11.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = BloomRose,
              selectedLabelColor = Color.White
            )
          )
        }
      }
    }

    // 2. Trending Hashtags (X-Style)
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Trending in Circle 🔥",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              fontStyle = FontStyle.Italic,
              color = if (isCandleMode) BloomWarmGold else BloomDeepRose
            )
          )
          Text(
            "Curated for you",
            style = MaterialTheme.typography.labelSmall.copy(color = BloomPlumMuted)
          )
        }

        Surface(
          shape = RoundedCornerShape(22.dp),
          color = if (isCandleMode) Color(0xFF2E1734) else Color.White.copy(alpha = 0.92f),
          border = BorderStroke(1.dp, if (isCandleMode) BloomWarmGold.copy(alpha = 0.25f) else BloomRose.copy(alpha = 0.2f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            filteredHashtags.forEach { item ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { onHashtagClick(item.tag) },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = item.category,
                    style = MaterialTheme.typography.labelSmall.copy(color = BloomPlumMuted, fontSize = 10.sp)
                  )
                  Text(
                    text = item.tag,
                    style = MaterialTheme.typography.bodyMedium.copy(
                      fontWeight = FontWeight.Bold,
                      color = if (isCandleMode) Color.White else BloomPlum
                    )
                  )
                }

                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  if (item.isHot) {
                    Text("🔥", fontSize = 12.sp)
                  }
                  Text(
                    text = item.postsCountFormatted,
                    style = MaterialTheme.typography.labelSmall.copy(color = BloomRose, fontWeight = FontWeight.SemiBold)
                  )
                }
              }
              if (item != filteredHashtags.last()) {
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.15f))
              }
            }
          }
        }
      }
    }

    // 3. Suggested People Carousel
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Suggested Souls 🌸",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              fontStyle = FontStyle.Italic,
              color = if (isCandleMode) BloomWarmGold else BloomDeepRose
            )
          )
          Text(
            "Mutual values",
            style = MaterialTheme.typography.labelSmall.copy(color = BloomPlumMuted)
          )
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
          items(filteredPeople) { person ->
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = if (isCandleMode) Color(0xFF2E1734) else Color.White,
              border = BorderStroke(1.dp, if (isCandleMode) BloomWarmGold.copy(alpha = 0.25f) else BloomRose.copy(alpha = 0.2f)),
              shadowElevation = 3.dp,
              modifier = Modifier
                .width(170.dp)
                .padding(vertical = 4.dp)
            ) {
              Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(person.avatarColorStart), Color(person.avatarColorEnd)))),
                  contentAlignment = Alignment.Center
                ) {
                  Text(person.initials, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }

                Text(
                  text = person.name,
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isCandleMode) Color.White else BloomPlum
                  ),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )

                Text(
                  text = "${person.city} • ${person.compatibilityScore}% match",
                  style = MaterialTheme.typography.labelSmall.copy(color = BloomRose, fontSize = 10.sp)
                )

                Button(
                  onClick = { onConnectPerson(person.id) },
                  colors = ButtonDefaults.buttonColors(
                    containerColor = if (person.status == FriendRequestStatus.SENT) Color.Gray else BloomRose
                  ),
                  shape = RoundedCornerShape(9999.dp),
                  contentPadding = PaddingValues(horizontal = 14.dp, vertical = 2.dp),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text(
                    text = if (person.status == FriendRequestStatus.SENT) "Sent ✓" else "Follow",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }
          }
        }
      }
    }

    // 4. Popular Communities
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = "Vibrant Circles 🕊️",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic,
            color = if (isCandleMode) BloomWarmGold else BloomDeepRose
          )
        )

        filteredCommunities.take(3).forEach { comm ->
          Surface(
            shape = RoundedCornerShape(18.dp),
            color = if (isCandleMode) Color(0xFF2E1734) else Color.White,
            border = BorderStroke(1.dp, if (isCandleMode) BloomWarmGold.copy(alpha = 0.25f) else BloomRose.copy(alpha = 0.2f)),
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onSelectCommunity(comm) }
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                Text(comm.emoji, fontSize = 26.sp)
                Column {
                  Text(
                    comm.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                      fontWeight = FontWeight.Bold,
                      color = if (isCandleMode) BloomWarmGold else BloomDeepRose
                    )
                  )
                  Text(
                    "${comm.memberCount} members • ${comm.category}",
                    style = MaterialTheme.typography.labelSmall.copy(color = BloomPlumMuted, fontSize = 10.sp)
                  )
                }
              }

              Icon(Icons.Default.ChevronRight, contentDescription = "Open", tint = BloomRose)
            }
          }
        }
      }
    }

    // 5. Explore Moments Grid
    item {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "Community Moments 🎨",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic,
            color = if (isCandleMode) BloomWarmGold else BloomDeepRose
          )
        )

        // 3-Column Aesthetic Grid
        val allPhotoGradients = feedPosts.flatMap { it.photoGradients }.take(9)
        val chunked = allPhotoGradients.chunked(3)
        chunked.forEach { rowGradients ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            rowGradients.forEach { pair ->
              Box(
                modifier = Modifier
                  .weight(1f)
                  .aspectRatio(1f)
                  .clip(RoundedCornerShape(14.dp))
                  .background(Brush.linearGradient(listOf(Color(pair.first), Color(pair.second))))
                  .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
              )
            }
            // Fill empty slots if last row has less than 3
            repeat(3 - rowGradients.size) {
              Spacer(modifier = Modifier.weight(1f))
            }
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(72.dp))
    }
  }
}
