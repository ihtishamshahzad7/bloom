package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.NeumorphicGlassCard
import com.example.ui.theme.*

@Composable
fun CommunitiesScreen(
  communities: List<Community>,
  groupChats: List<GroupChat>,
  selectedCommunity: Community?,
  selectedGroupChat: GroupChat?,
  isCandleMode: Boolean,
  onSelectCommunity: (Community?) -> Unit,
  onSelectGroupChat: (GroupChat?) -> Unit,
  onToggleJoinCommunity: (String) -> Unit,
  onCreateCommunityPost: (String, String) -> Unit,
  onLikeCommunityPost: (String, String) -> Unit,
  onReportCommunityPost: (String, String, String) -> Unit,
  onCreateGroupChat: (String, String, String, List<String>) -> Unit,
  onVoteGroupPoll: (String, Int) -> Unit,
  modifier: Modifier = Modifier
) {
  var activeTab by remember { mutableIntStateOf(0) } // 0: Public Communities, 1: Private Group Chats
  var showCreateGroupDialog by remember { mutableStateOf(false) }

  if (selectedCommunity != null) {
    BackHandler { onSelectCommunity(null) }
    CommunityDetailView(
      community = selectedCommunity,
      isCandleMode = isCandleMode,
      onBack = { onSelectCommunity(null) },
      onToggleJoin = { onToggleJoinCommunity(selectedCommunity.id) },
      onCreatePost = { text -> onCreateCommunityPost(selectedCommunity.id, text) },
      onLikePost = { postId -> onLikeCommunityPost(selectedCommunity.id, postId) },
      onReportPost = { postId, reason -> onReportCommunityPost(selectedCommunity.id, postId, reason) }
    )
  } else if (selectedGroupChat != null) {
    BackHandler { onSelectGroupChat(null) }
    GroupChatDetailView(
      groupChat = selectedGroupChat,
      isCandleMode = isCandleMode,
      onBack = { onSelectGroupChat(null) },
      onVotePoll = { optIdx -> onVoteGroupPoll(selectedGroupChat.id, optIdx) }
    )
  } else {
    Column(
      modifier = modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp)
    ) {
      Spacer(modifier = Modifier.height(16.dp))

      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Communities & Circles 🌿",
            style = MaterialTheme.typography.headlineMedium.copy(
              fontStyle = FontStyle.Italic,
              fontWeight = FontWeight.Bold,
              color = if (isCandleMode) BloomWarmGold else BloomDeepRose
            )
          )
          Text(
            text = "Public interest spaces & safe matched groups (18+ only)",
            style = MaterialTheme.typography.bodySmall.copy(
              color = if (isCandleMode) BloomCandleDarkMuted else BloomPlum
            )
          )
        }

        if (activeTab == 1) {
          IconButton(
            onClick = { showCreateGroupDialog = true },
            modifier = Modifier
              .size(40.dp)
              .background(BloomRose, CircleShape)
          ) {
            Icon(Icons.Default.Add, contentDescription = "Create Group", tint = Color.White)
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Tab Row
      TabRow(
        selectedTabIndex = activeTab,
        containerColor = Color.Transparent,
        contentColor = BloomRose
      ) {
        Tab(
          selected = activeTab == 0,
          onClick = { activeTab = 0 },
          text = { Text("Public Communities", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
        )
        Tab(
          selected = activeTab == 1,
          onClick = { activeTab = 1 },
          text = { Text("Group Circles (${groupChats.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      if (activeTab == 0) {
        // Public Communities List
        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(14.dp),
          contentPadding = PaddingValues(bottom = 96.dp)
        ) {
          item {
            // Safety Guideline Banner
            Surface(
              shape = RoundedCornerShape(16.dp),
              color = if (isCandleMode) Color(0xFF3B1F42) else BloomPeach.copy(alpha = 0.4f),
              border = BorderStroke(1.dp, BloomWarmGold.copy(alpha = 0.4f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Icon(Icons.Default.Security, contentDescription = null, tint = BloomRose)
                Text(
                  "Public Community Policy: Exact home locations, wire transfers, and contact exchanges are filtered to safeguard all members.",
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)
                )
              }
            }
          }

          items(communities, key = { it.id }) { comm ->
            NeumorphicGlassCard(
              isCandleMode = isCandleMode,
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelectCommunity(comm) }
                .testTag("community_item_${comm.id}")
            ) {
              Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                  ) {
                    Box(
                      modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                          Brush.linearGradient(listOf(Color(comm.gradientStart), Color(comm.gradientEnd)))
                        ),
                      contentAlignment = Alignment.Center
                    ) {
                      Text(comm.emoji, fontSize = 24.sp)
                    }
                    Column {
                      Text(
                        comm.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                          fontWeight = FontWeight.Bold,
                          color = if (isCandleMode) Color.White else BloomPlum
                        )
                      )
                      Text(
                        "${comm.memberCount} members · ${comm.category}",
                        style = MaterialTheme.typography.labelSmall.copy(
                          color = if (isCandleMode) BloomCandleDarkMuted else BloomPlumMuted
                        )
                      )
                    }
                  }

                  Button(
                    onClick = { onToggleJoinCommunity(comm.id) },
                    colors = ButtonDefaults.buttonColors(
                      containerColor = if (comm.isJoined) Color.Gray.copy(alpha = 0.2f) else BloomRose,
                      contentColor = if (comm.isJoined) (if (isCandleMode) Color.White else BloomPlum) else Color.White
                    ),
                    shape = RoundedCornerShape(9999.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                  ) {
                    Text(if (comm.isJoined) "Joined ✓" else "Join 🌿", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                  }
                }

                Text(
                  text = comm.description,
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = if (isCandleMode) BloomCandleDarkMuted else BloomPlum
                  )
                )

                if (comm.pinnedPost != null) {
                  Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.05f),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Row(
                      modifier = Modifier.padding(10.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                      Text("📌", fontSize = 14.sp)
                      Text(
                        text = "${comm.pinnedPost.authorName}: ${comm.pinnedPost.content}",
                        style = MaterialTheme.typography.labelSmall.copy(fontStyle = FontStyle.Italic),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                      )
                    }
                  }
                }
              }
            }
          }
        }
      } else {
        // Private Group Chats List
        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(14.dp),
          contentPadding = PaddingValues(bottom = 96.dp)
        ) {
          items(groupChats, key = { it.id }) { group ->
            NeumorphicGlassCard(
              isCandleMode = isCandleMode,
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelectGroupChat(group) }
                .testTag("group_chat_${group.id}")
            ) {
              Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(group.gradientStart), Color(group.gradientEnd)))),
                  contentAlignment = Alignment.Center
                ) {
                  Text(group.iconEmoji, fontSize = 24.sp)
                }

                Column(modifier = Modifier.weight(1f)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      group.name,
                      style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isCandleMode) Color.White else BloomPlum
                      )
                    )
                    Text(
                      group.lastMessageTime,
                      style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 10.sp)
                    )
                  }

                  Text(
                    "${group.membersCount} members · ${group.description}",
                    style = MaterialTheme.typography.labelSmall.copy(color = BloomRose, fontSize = 11.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )

                  Spacer(modifier = Modifier.height(2.dp))

                  Text(
                    group.lastMessageText,
                    style = MaterialTheme.typography.bodySmall.copy(
                      color = if (isCandleMode) BloomCandleDarkMuted else BloomPlumMuted
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
              }
            }
          }
        }
      }
    }
  }

  // Create Group Dialog
  if (showCreateGroupDialog) {
    var groupName by remember { mutableStateOf("") }
    var groupDescription by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("📚") }

    AlertDialog(
      onDismissRequest = { showCreateGroupDialog = false },
      title = { Text("Create Matched Circle", fontStyle = FontStyle.Italic) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            "Start a safe group (2–20 members) for family-introduced or matched friends:",
            style = MaterialTheme.typography.bodySmall
          )
          OutlinedTextField(
            value = groupName,
            onValueChange = { groupName = it },
            placeholder = { Text("Group Name (e.g. Chai & Poetry Society)") },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = groupDescription,
            onValueChange = { groupDescription = it },
            placeholder = { Text("Purpose & shared interests") },
            modifier = Modifier.fillMaxWidth()
          )
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("📚", "☕", "🎨", "🏃", "✈️", "🌱").forEach { emoji ->
              Surface(
                shape = CircleShape,
                color = if (selectedEmoji == emoji) BloomRose.copy(alpha = 0.3f) else Color.Transparent,
                border = BorderStroke(1.dp, if (selectedEmoji == emoji) BloomRose else Color.LightGray),
                modifier = Modifier
                  .size(36.dp)
                  .clickable { selectedEmoji = emoji }
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Text(emoji, fontSize = 16.sp)
                }
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (groupName.isNotBlank()) {
              onCreateGroupChat(
                groupName,
                groupDescription.ifEmpty { "A safe space for mutual enrichment" },
                selectedEmoji,
                listOf("Ayaan Malik", "Sofia Zahra")
              )
              showCreateGroupDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = BloomRose)
        ) {
          Text("Create Group")
        }
      },
      dismissButton = {
        TextButton(onClick = { showCreateGroupDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
private fun CommunityDetailView(
  community: Community,
  isCandleMode: Boolean,
  onBack: () -> Unit,
  onToggleJoin: () -> Unit,
  onCreatePost: (String) -> Unit,
  onLikePost: (String) -> Unit,
  onReportPost: (String, String) -> Unit
) {
  var newPostText by remember { mutableStateOf("") }
  var showRulesDialog by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        IconButton(onClick = onBack) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Text(
          community.name,
          style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic,
            color = if (isCandleMode) BloomWarmGold else BloomDeepRose
          )
        )
      }
    }

    // Community Header Card
    item {
      NeumorphicGlassCard(isCandleMode = isCandleMode, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Text(community.emoji, fontSize = 32.sp)
              Column {
                Text(community.category, style = MaterialTheme.typography.labelSmall.copy(color = BloomRose))
                Text("${community.memberCount} committed members", style = MaterialTheme.typography.labelSmall)
              }
            }

            Button(
              onClick = onToggleJoin,
              colors = ButtonDefaults.buttonColors(
                containerColor = if (community.isJoined) Color.Gray.copy(alpha = 0.2f) else BloomRose
              )
            ) {
              Text(if (community.isJoined) "Joined ✓" else "Join 🌿")
            }
          }

          Text(community.description, style = MaterialTheme.typography.bodySmall)

          Row(
            modifier = Modifier
              .clickable { showRulesDialog = true }
              .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(Icons.Default.Gavel, contentDescription = null, tint = BloomRose, modifier = Modifier.size(16.dp))
            Text("Community Rules & Moderation Policy", style = MaterialTheme.typography.labelSmall.copy(color = BloomRose, fontWeight = FontWeight.Bold))
          }
        }
      }
    }

    // Create Post Input
    item {
      Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (isCandleMode) Color(0xFF3B1F42) else Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = newPostText,
            onValueChange = { newPostText = it },
            placeholder = { Text("Share an inspiring quote, book reflection, or safe question...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
          ) {
            Button(
              onClick = {
                if (newPostText.isNotBlank()) {
                  onCreatePost(newPostText)
                  newPostText = ""
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = BloomRose),
              enabled = newPostText.isNotBlank()
            ) {
              Text("Post Reflection")
            }
          }
        }
      }
    }

    // Community Posts Stream
    items(community.posts, key = { it.id }) { post ->
      NeumorphicGlassCard(isCandleMode = isCandleMode, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(BloomRose),
                contentAlignment = Alignment.Center
              ) {
                Text(post.authorName.take(1), color = Color.White, fontWeight = FontWeight.Bold)
              }
              Column {
                Text(post.authorName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                if (post.authorBadge != null) {
                  Text(post.authorBadge, style = MaterialTheme.typography.labelSmall.copy(color = BloomRose, fontSize = 10.sp))
                }
              }
            }

            Text(post.timestamp, style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
          }

          Text(post.content, style = MaterialTheme.typography.bodyMedium)

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
              Row(
                modifier = Modifier.clickable { onLikePost(post.id) },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Icon(
                  if (post.isLikedByMe) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                  contentDescription = "Like",
                  tint = if (post.isLikedByMe) BloomRose else Color.Gray,
                  modifier = Modifier.size(18.dp)
                )
                Text("${post.likesCount}", style = MaterialTheme.typography.labelSmall)
              }

              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Comments", tint = Color.Gray, modifier = Modifier.size(18.dp))
                Text("${post.commentsCount}", style = MaterialTheme.typography.labelSmall)
              }
            }

            IconButton(onClick = { onReportPost(post.id, "Inappropriate community post") }) {
              Icon(Icons.Outlined.Flag, contentDescription = "Report", tint = Color.Gray, modifier = Modifier.size(16.dp))
            }
          }
        }
      }
    }
  }

  if (showRulesDialog) {
    AlertDialog(
      onDismissRequest = { showRulesDialog = false },
      title = { Text("Community Moderation Rules", fontStyle = FontStyle.Italic) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          community.rules.forEach { rule ->
            Text("• $rule", style = MaterialTheme.typography.bodySmall)
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showRulesDialog = false }) {
          Text("Understood", color = BloomRose)
        }
      }
    )
  }
}

@Composable
private fun GroupChatDetailView(
  groupChat: GroupChat,
  isCandleMode: Boolean,
  onBack: () -> Unit,
  onVotePoll: (Int) -> Unit
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        IconButton(onClick = onBack) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Text(
          groupChat.name,
          style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic,
            color = if (isCandleMode) BloomWarmGold else BloomDeepRose
          )
        )
      }
    }

    // Members Card
    item {
      NeumorphicGlassCard(isCandleMode = isCandleMode, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Circle Members (${groupChat.membersCount}/20 max)", fontWeight = FontWeight.Bold)
          Text(groupChat.memberNames.joinToString(", "), style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
        }
      }
    }

    // Interactive Group Poll Card
    if (groupChat.activePoll != null) {
      val poll = groupChat.activePoll
      item {
        NeumorphicGlassCard(isCandleMode = isCandleMode, modifier = Modifier.fillMaxWidth()) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Text("📊", fontSize = 20.sp)
              Text("Group Poll · ${poll.totalVotes} votes", fontWeight = FontWeight.Bold)
            }

            Text(poll.question, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))

            poll.options.forEachIndexed { index, option ->
              val isSelected = poll.userVotedOptionIndex == index
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) BloomRose.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.05f),
                border = BorderStroke(1.dp, if (isSelected) BloomRose else Color.Transparent),
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { onVotePoll(index) }
              ) {
                Row(
                  modifier = Modifier.padding(12.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(option.text, style = MaterialTheme.typography.bodySmall)
                  Text("${option.voteCount} votes", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
              }
            }
          }
        }
      }
    }
  }
}
