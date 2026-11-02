package com.example.ui.screens

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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.StoriesRow
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  posts: List<FeedPost>,
  stories: List<StoryItem>,
  hiddenPostIds: Set<String>,
  isCandleMode: Boolean,
  isVerifiedOnly: Boolean,
  isRefreshing: Boolean,
  onRefresh: () -> Unit,
  onToggleVerifiedOnly: () -> Unit,
  onLikePost: (String) -> Unit,
  onSavePost: (String) -> Unit,
  onAddComment: (postId: String, parentId: String?, text: String) -> String?,
  onLikeComment: (postId: String, commentId: String) -> Unit,
  onVotePoll: (postId: String, optIdx: Int) -> Unit,
  onReportPost: (postId: String, reason: String) -> Unit,
  onHidePost: (String) -> Unit,
  onOpenStory: (StoryItem) -> Unit,
  onAddStoryClick: () -> Unit,
  onOpenComposer: () -> Unit
) {
  val haptic = LocalHapticFeedback.current
  var activeCommentsPost by remember { mutableStateOf<FeedPost?>(null) }
  var reportDialogPost by remember { mutableStateOf<FeedPost?>(null) }

  val filteredPosts = remember(posts, hiddenPostIds, isVerifiedOnly) {
    posts
      .filterNot { it.id in hiddenPostIds }
      .filter { !isVerifiedOnly || it.authorVerified }
  }

  Box(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .testTag("home_feed_list"),
      contentPadding = PaddingValues(bottom = 96.dp)
    ) {
      // 1. Stories Row at the Top
      item {
        StoriesRow(
          stories = stories,
          isCandleMode = isCandleMode,
          onStoryClick = onOpenStory,
          onAddStoryClick = onAddStoryClick
        )
      }

      // 2. Feed Filter Bar & Pull to refresh indicator
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            FilterChip(
              selected = isVerifiedOnly,
              onClick = onToggleVerifiedOnly,
              label = {
                Text(
                  "🛡️ Verified Only",
                  fontSize = 11.sp,
                  fontWeight = if (isVerifiedOnly) FontWeight.Bold else FontWeight.Normal
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = BloomRose,
                selectedLabelColor = Color.White
              ),
              modifier = Modifier.testTag("verified_only_toggle")
            )

            IconButton(
              onClick = onRefresh,
              modifier = Modifier.size(32.dp)
            ) {
              Icon(
                Icons.Default.Refresh,
                contentDescription = "Refresh",
                tint = if (isCandleMode) BloomWarmGold else BloomRose,
                modifier = Modifier.size(18.dp)
              )
            }
          }

          Button(
            onClick = onOpenComposer,
            colors = ButtonDefaults.buttonColors(containerColor = BloomRose),
            shape = RoundedCornerShape(9999.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
            modifier = Modifier.testTag("new_post_button")
          ) {
            Icon(Icons.Default.Add, contentDescription = "New Post", modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Share", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }
      }

      // Refreshing shimmer / skeleton state
      if (isRefreshing) {
        item {
          ShimmerFeedSkeleton(isCandleMode = isCandleMode)
        }
      }

      // 3. Posts Stream
      items(filteredPosts, key = { it.id }) { post ->
        FeedPostCard(
          post = post,
          isCandleMode = isCandleMode,
          onLike = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onLikePost(post.id)
          },
          onSave = { onSavePost(post.id) },
          onCommentClick = { activeCommentsPost = post },
          onVotePoll = { optIdx -> onVotePoll(post.id, optIdx) },
          onReport = { reportDialogPost = post },
          onHide = { onHidePost(post.id) }
        )
        Spacer(modifier = Modifier.height(14.dp))
      }

      if (filteredPosts.isEmpty() && !isRefreshing) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(40.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("🌸", fontSize = 36.sp)
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                "Feed is peaceful and quiet",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                "Be the first to share an inspiring reflection or photo.",
                style = MaterialTheme.typography.bodySmall.copy(color = BloomPlumMuted)
              )
            }
          }
        }
      }
    }

    // Threaded Comments Sheet
    val currentCommentPost = activeCommentsPost
    if (currentCommentPost != null) {
      ThreadedCommentsSheet(
        post = currentCommentPost,
        isCandleMode = isCandleMode,
        onDismiss = { activeCommentsPost = null },
        onAddComment = { parentId, text -> onAddComment(currentCommentPost.id, parentId, text) },
        onLikeComment = { commentId -> onLikeComment(currentCommentPost.id, commentId) }
      )
    }

    // Report Post Dialog
    val reportPostTarget = reportDialogPost
    if (reportPostTarget != null) {
      ReportPostDialog(
        post = reportPostTarget,
        isCandleMode = isCandleMode,
        onDismiss = { reportDialogPost = null },
        onSubmit = { reason ->
          onReportPost(reportPostTarget.id, reason)
          reportDialogPost = null
        }
      )
    }
  }
}

@Composable
fun FeedPostCard(
  post: FeedPost,
  isCandleMode: Boolean,
  onLike: () -> Unit,
  onSave: () -> Unit,
  onCommentClick: () -> Unit,
  onVotePoll: (Int) -> Unit,
  onReport: () -> Unit,
  onHide: () -> Unit
) {
  var showMenu by remember { mutableStateOf(false) }

  Surface(
    shape = RoundedCornerShape(26.dp),
    color = if (isCandleMode) Color(0xFF2E1734).copy(alpha = 0.95f) else Color.White.copy(alpha = 0.92f),
    border = BorderStroke(
      1.dp,
      if (isCandleMode) BloomWarmGold.copy(alpha = 0.25f) else BloomRose.copy(alpha = 0.2f)
    ),
    shadowElevation = 4.dp,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp)
      .testTag("feed_post_${post.id}")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      // Header: Avatar, Name, Handle, Badges, Time, Menu
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Box {
            Box(
              modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(post.authorAvatarColors.first), Color(post.authorAvatarColors.second)))),
              contentAlignment = Alignment.Center
            ) {
              Text(
                post.authorName.take(1),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
              )
            }
            if (post.isOnline) {
              Box(
                modifier = Modifier
                  .size(12.dp)
                  .background(BloomOnline, CircleShape)
                  .border(2.dp, Color.White, CircleShape)
                  .align(Alignment.BottomEnd)
              )
            }
          }

          Column {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Text(
                text = post.authorName,
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = if (isCandleMode) BloomWarmGold else BloomDeepRose
                )
              )
              if (post.authorVerified) {
                Text("🛡️", fontSize = 12.sp)
              }
              if (post.authorStreakDays > 0) {
                Text("🌸${post.authorStreakDays}🔥", fontSize = 11.sp)
              }
            }

            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                text = "${post.authorHandle} • ${post.timestamp}",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = if (isCandleMode) BloomCandleDarkMuted else BloomPlumMuted,
                  fontSize = 11.sp
                )
              )
              if (post.locationCity != null) {
                Text(
                  text = "📍 ${post.locationCity}",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = BloomRose,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                  )
                )
              }
            }
          }
        }

        Box {
          IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = if (isCandleMode) Color.White else BloomPlum)
          }
          DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
          ) {
            DropdownMenuItem(
              text = { Text("Hide Post") },
              leadingIcon = { Icon(Icons.Outlined.VisibilityOff, contentDescription = "Hide") },
              onClick = {
                showMenu = false
                onHide()
              }
            )
            DropdownMenuItem(
              text = { Text("Report Post", color = BloomDanger) },
              leadingIcon = { Icon(Icons.Outlined.Report, contentDescription = "Report", tint = BloomDanger) },
              onClick = {
                showMenu = false
                onReport()
              }
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Caption with stylized hashtags and mentions
      val annotatedCaption = remember(post.textCaption) {
        buildAnnotatedString {
          val words = post.textCaption.split(" ")
          words.forEachIndexed { idx, word ->
            if (word.startsWith("#") || word.startsWith("@")) {
              withStyle(SpanStyle(color = BloomRose, fontWeight = FontWeight.Bold)) {
                append(word)
              }
            } else {
              append(word)
            }
            if (idx < words.size - 1) append(" ")
          }
        }
      }

      Text(
        text = annotatedCaption,
        style = MaterialTheme.typography.bodyMedium.copy(
          color = if (isCandleMode) Color.White.copy(alpha = 0.95f) else BloomPlum,
          lineHeight = 20.sp
        )
      )

      // Photo Carousel (up to 6 photos)
      if (post.photoGradients.isNotEmpty()) {
        Spacer(modifier = Modifier.height(12.dp))
        var currentPhotoIdx by remember { mutableStateOf(0) }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(230.dp)
              .clip(RoundedCornerShape(20.dp))
              .background(
                Brush.linearGradient(
                  listOf(
                    Color(post.photoGradients[currentPhotoIdx].first),
                    Color(post.photoGradients[currentPhotoIdx].second)
                  )
                )
              )
              .shadow(6.dp, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.BottomEnd
          ) {
            // Photo counter badge if multiple photos
            if (post.photoGradients.size > 1) {
              Surface(
                shape = RoundedCornerShape(9999.dp),
                color = Color.Black.copy(alpha = 0.5f),
                modifier = Modifier.padding(10.dp)
              ) {
                Text(
                  text = "${currentPhotoIdx + 1}/${post.photoGradients.size}",
                  color = Color.White,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
              }
            }
          }

          // Indicator dots if more than 1 photo
          if (post.photoGradients.size > 1) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              post.photoGradients.indices.forEach { idx ->
                Box(
                  modifier = Modifier
                    .size(if (currentPhotoIdx == idx) 8.dp else 6.dp)
                    .clip(CircleShape)
                    .background(if (currentPhotoIdx == idx) BloomRose else Color.Gray.copy(alpha = 0.4f))
                    .clickable { currentPhotoIdx = idx }
                )
              }
            }
          }
        }
      }

      // Interactive Poll (if present)
      val poll = post.poll
      if (poll != null) {
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = if (isCandleMode) Color(0xFF381B3E) else BloomPeach.copy(alpha = 0.45f),
          border = BorderStroke(1.dp, BloomRose.copy(alpha = 0.3f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text(
              text = "📊 ${poll.question}",
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = if (isCandleMode) BloomWarmGold else BloomDeepRose
              )
            )
            Spacer(modifier = Modifier.height(8.dp))

            poll.options.forEachIndexed { idx, opt ->
              val percent = if (poll.totalVotes > 0) ((opt.voteCount.toFloat() / poll.totalVotes) * 100).toInt() else 0
              val isVotedByMe = poll.hasVoted && poll.userVotedIndex == idx

              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp)
                  .height(36.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(if (isCandleMode) Color(0xFF28132B) else Color.White)
                  .border(
                    width = if (isVotedByMe) 2.dp else 1.dp,
                    color = if (isVotedByMe) BloomRose else Color.Transparent,
                    shape = RoundedCornerShape(10.dp)
                  )
                  .clickable {
                    if (!poll.hasVoted) onVotePoll(idx)
                  }
              ) {
                // Background percentage fill
                if (poll.hasVoted) {
                  Box(
                    modifier = Modifier
                      .fillMaxHeight()
                      .fillMaxWidth(percent / 100f)
                      .background(BloomRose.copy(alpha = 0.25f))
                  )
                }

                Row(
                  modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    if (isVotedByMe) {
                      Icon(Icons.Default.Check, contentDescription = "Voted", tint = BloomRose, modifier = Modifier.size(16.dp))
                    }
                    Text(
                      opt.text,
                      style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = if (isVotedByMe) FontWeight.Bold else FontWeight.Normal,
                        color = if (isCandleMode) Color.White else BloomPlum
                      )
                    )
                  }

                  if (poll.hasVoted) {
                    Text(
                      "$percent%",
                      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = BloomRose)
                    )
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
              "${poll.totalVotes} votes total",
              style = MaterialTheme.typography.labelSmall.copy(color = BloomPlumMuted, fontSize = 10.sp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Action Row: Like, Comment, Share, Save
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          // Like button
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .clickable { onLike() }
              .testTag("like_post_button_${post.id}")
          ) {
            Icon(
              imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
              contentDescription = "Like",
              tint = if (post.isLiked) BloomRose else if (isCandleMode) Color.White.copy(alpha = 0.7f) else BloomPlumMuted,
              modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
              "${post.likesCount}",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = if (post.isLiked) BloomRose else if (isCandleMode) Color.White else BloomPlum
              )
            )
          }

          // Comment button
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onCommentClick() }
          ) {
            Icon(
              imageVector = Icons.Outlined.ChatBubbleOutline,
              contentDescription = "Comment",
              tint = if (isCandleMode) Color.White.copy(alpha = 0.7f) else BloomPlumMuted,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
              "${post.commentsCount}",
              style = MaterialTheme.typography.labelMedium.copy(
                color = if (isCandleMode) Color.White else BloomPlum
              )
            )
          }

          // Share button
          Icon(
            imageVector = Icons.Outlined.Share,
            contentDescription = "Share",
            tint = if (isCandleMode) Color.White.copy(alpha = 0.7f) else BloomPlumMuted,
            modifier = Modifier
              .size(20.dp)
              .clickable { /* share feedback */ }
          )
        }

        // Bookmark Save
        Icon(
          imageVector = if (post.isSaved) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
          contentDescription = "Save",
          tint = if (post.isSaved) BloomWarmGold else if (isCandleMode) Color.White.copy(alpha = 0.7f) else BloomPlumMuted,
          modifier = Modifier
            .size(22.dp)
            .clickable { onSave() }
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreadedCommentsSheet(
  post: FeedPost,
  isCandleMode: Boolean,
  onDismiss: () -> Unit,
  onAddComment: (parentId: String?, text: String) -> String?,
  onLikeComment: (String) -> Unit
) {
  var commentInput by remember { mutableStateOf("") }
  var replyingToComment by remember { mutableStateOf<PostComment?>(null) }
  var errorText by remember { mutableStateOf<String?>(null) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = if (isCandleMode) BloomCandleDarkBg else Color(0xFFFFF7F9),
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(0.85f)
        .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
      Text(
        text = "Comments (${post.comments.size + post.comments.sumOf { it.replies.size }}) 🌸",
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.Bold,
          color = if (isCandleMode) BloomWarmGold else BloomDeepRose
        ),
        modifier = Modifier.padding(bottom = 8.dp)
      )

      HorizontalDivider(color = BloomRose.copy(alpha = 0.2f))

      LazyColumn(
        modifier = Modifier
          .weight(1f)
          .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(post.comments) { comment ->
          CommentItem(
            comment = comment,
            isCandleMode = isCandleMode,
            onReply = { replyingToComment = comment },
            onLike = { onLikeComment(comment.id) }
          )
        }

        if (post.comments.isEmpty()) {
          item {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                "No comments yet. Share your sincere thoughts 🌸",
                style = MaterialTheme.typography.bodySmall.copy(color = BloomPlumMuted)
              )
            }
          }
        }
      }

      // Replying badge
      if (replyingToComment != null) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(BloomRose.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
          .padding(horizontal = 10.dp, vertical = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            "Replying to ${replyingToComment?.authorName}",
            style = MaterialTheme.typography.labelSmall.copy(color = BloomRose, fontWeight = FontWeight.Bold)
          )
          IconButton(
            onClick = { replyingToComment = null },
            modifier = Modifier.size(20.dp)
          ) {
            Icon(Icons.Default.Close, contentDescription = "Cancel", tint = BloomRose, modifier = Modifier.size(14.dp))
          }
        }
        Spacer(modifier = Modifier.height(4.dp))
      }

      if (errorText != null) {
        Text(errorText ?: "", color = BloomDanger, fontSize = 11.sp, modifier = Modifier.padding(bottom = 4.dp))
      }

      // Comment input bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .navigationBarsPadding()
          .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        OutlinedTextField(
          value = commentInput,
          onValueChange = {
            commentInput = it
            errorText = null
          },
          placeholder = { Text("Write a thoughtful comment...") },
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(20.dp),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BloomRose)
        )

        IconButton(
          onClick = {
            if (commentInput.isNotBlank()) {
              val err = onAddComment(replyingToComment?.id, commentInput.trim())
              if (err != null) {
                errorText = err
              } else {
                commentInput = ""
                replyingToComment = null
              }
            }
          },
          modifier = Modifier
            .size(44.dp)
            .background(BloomRose, CircleShape)
        ) {
          Icon(Icons.Default.Send, contentDescription = "Post", tint = Color.White, modifier = Modifier.size(20.dp))
        }
      }
    }
  }
}

@Composable
fun CommentItem(
  comment: PostComment,
  isCandleMode: Boolean,
  onReply: () -> Unit,
  onLike: () -> Unit
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Top
    ) {
      Row(
        modifier = Modifier.weight(1f),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Box(
          modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(Color(comment.authorAvatarColors.first), Color(comment.authorAvatarColors.second)))),
          contentAlignment = Alignment.Center
        ) {
          Text(comment.authorName.take(1), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        Column {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Text(
              comment.authorName,
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = if (isCandleMode) BloomWarmGold else BloomDeepRose
              )
            )
            Text(comment.timestamp, style = MaterialTheme.typography.labelSmall.copy(color = BloomPlumMuted, fontSize = 10.sp))
          }
          Text(
            comment.text,
            style = MaterialTheme.typography.bodySmall.copy(
              color = if (isCandleMode) Color.White else BloomPlum
            )
          )

          Text(
            "Reply",
            style = MaterialTheme.typography.labelSmall.copy(
              color = BloomRose,
              fontWeight = FontWeight.Bold
            ),
            modifier = Modifier
              .clickable { onReply() }
              .padding(top = 2.dp)
          )
        }
      }

      IconButton(onClick = onLike, modifier = Modifier.size(28.dp)) {
        Icon(
          imageVector = if (comment.isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
          contentDescription = "Like Comment",
          tint = if (comment.isLiked) BloomRose else BloomPlumMuted,
          modifier = Modifier.size(16.dp)
        )
      }
    }

    // Threaded nested replies
    if (comment.replies.isNotEmpty()) {
      Column(
        modifier = Modifier
          .padding(start = 38.dp, top = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        comment.replies.forEach { reply ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Box(
              modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(reply.authorAvatarColors.first), Color(reply.authorAvatarColors.second)))),
              contentAlignment = Alignment.Center
            ) {
              Text(reply.authorName.take(1), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            }
            Column {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Text(reply.authorName, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                Text(reply.timestamp, style = MaterialTheme.typography.labelSmall.copy(color = BloomPlumMuted, fontSize = 9.sp))
              }
              Text(reply.text, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp))
            }
          }
        }
      }
    }
  }
}

@Composable
fun ShimmerFeedSkeleton(isCandleMode: Boolean) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = if (isCandleMode) Color(0xFF381B3E).copy(alpha = 0.5f) else BloomBlush.copy(alpha = 0.7f),
      modifier = Modifier
        .fillMaxWidth()
        .height(180.dp)
    ) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = BloomRose, strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
      }
    }
  }
}

@Composable
fun ReportPostDialog(
  post: FeedPost,
  isCandleMode: Boolean,
  onDismiss: () -> Unit,
  onSubmit: (String) -> Unit
) {
  var selectedReason by remember { mutableStateOf("Inappropriate Content") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Report Post", fontWeight = FontWeight.Bold) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Help us maintain a safe, halal, and respectful environment.")
        listOf("Inappropriate Content", "Spam / Commercial", "Harassment / Unkind", "Scam / Financial").forEach { reason ->
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .fillMaxWidth()
              .clickable { selectedReason = reason }
          ) {
            RadioButton(selected = selectedReason == reason, onClick = { selectedReason = reason })
            Text(reason, style = MaterialTheme.typography.bodyMedium)
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { onSubmit(selectedReason) },
        colors = ButtonDefaults.buttonColors(containerColor = BloomDanger)
      ) {
        Text("Report & Hide", color = Color.White)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
