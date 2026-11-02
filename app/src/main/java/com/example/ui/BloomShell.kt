package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.*

@Composable
fun BloomShell(
  viewModel: BloomViewModel
) {
  val uiState by viewModel.uiState.collectAsState()
  val profiles by viewModel.profiles.collectAsState()
  val messages by viewModel.messages.collectAsState()
  val scheduledDates by viewModel.scheduledDates.collectAsState()
  val quizQuestions by viewModel.quizQuestions.collectAsState()
  val safetySettings by viewModel.safetySettings.collectAsState()
  val dailyBlooms by viewModel.dailyBlooms.collectAsState()
  val boostStatus by viewModel.boostStatus.collectAsState()
  val isPremium by viewModel.isPremium.collectAsState()

  // Social Network State
  val feedPosts by viewModel.feedPosts.collectAsState()
  val trendingHashtags by viewModel.trendingHashtags.collectAsState()
  val notifications by viewModel.notifications.collectAsState()
  val socialProfile by viewModel.userSocialProfile.collectAsState()
  val hiddenPostIds by viewModel.hiddenPostIds.collectAsState()

  // New features
  val stories by viewModel.stories.collectAsState()
  val nearbyPeople by viewModel.nearbyPeople.collectAsState()
  val liveLocationState by viewModel.liveLocation.collectAsState()
  val friendSuggestions by viewModel.friendSuggestions.collectAsState()
  val communities by viewModel.communities.collectAsState()
  val groupChats by viewModel.groupChats.collectAsState()

  val haptic = LocalHapticFeedback.current
  val configuration = LocalConfiguration.current
  val isWideScreen = configuration.screenWidthDp >= 720

  val layoutDirection = if (uiState.isRtlUrdu) LayoutDirection.Rtl else LayoutDirection.Ltr

  val isPreAuthScreen = uiState.currentScreen == ScreenNav.SPLASH ||
    uiState.currentScreen == ScreenNav.ONBOARDING ||
    uiState.currentScreen == ScreenNav.PHONE_AUTH ||
    uiState.currentScreen == ScreenNav.AGE_GATE

  CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
    BloomTheme(
      themeMode = uiState.appThemeMode,
      candleMode = uiState.isCandleMode
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            if (uiState.isCandleMode) {
              Brush.verticalGradient(
                listOf(
                  BloomCandleDarkBg,
                  Color(0xFF231229),
                  BloomCandleDarkBg
                )
              )
            } else {
              Brush.verticalGradient(
                listOf(
                  BloomBlush,
                  BloomBlushMid,
                  BloomLavender.copy(alpha = 0.5f)
                )
              )
            }
          )
      ) {
        // GPU-friendly Ambient Petals layer
        AmbientPetalsField(
          enabled = uiState.ambientPetalsEnabled,
          isCandleMode = uiState.isCandleMode
        )

        if (isPreAuthScreen) {
          when (uiState.currentScreen) {
            ScreenNav.SPLASH -> SplashScreen(
              isCandleMode = uiState.isCandleMode,
              onSplashFinished = { viewModel.onSplashFinished() }
            )
            ScreenNav.ONBOARDING -> OnboardingScreen(
              isCandleMode = uiState.isCandleMode,
              onFinishOnboarding = { viewModel.onOnboardingFinished() }
            )
            ScreenNav.PHONE_AUTH, ScreenNav.AGE_GATE -> PhoneAuthScreen(
              isCandleMode = uiState.isCandleMode,
              onVerificationSuccessful = { viewModel.onPhoneAuthFinished() }
            )
            else -> {}
          }
        } else {
          Row(modifier = Modifier.fillMaxSize()) {
            if (isWideScreen) {
              BloomSidebar(
                currentScreen = uiState.currentScreen,
                unreadLikes = uiState.unreadLikesCount,
                unreadMessages = uiState.unreadMessagesCount,
                isCandleMode = uiState.isCandleMode,
                isPremium = isPremium,
                onNavigate = { viewModel.navigateTo(it) }
              )
            }

            Scaffold(
              containerColor = Color.Transparent,
              topBar = {
                if (uiState.currentScreen != ScreenNav.FULL_PROFILE) {
                  BloomTopBar(
                    title = uiState.currentScreen.title,
                    searchQuery = uiState.searchQuery,
                    unreadNotifications = uiState.unreadNotificationsCount,
                    isCandleMode = uiState.isCandleMode,
                    ambientPetalsEnabled = uiState.ambientPetalsEnabled,
                    isPremium = isPremium,
                    onSearchChange = { viewModel.updateSearchQuery(it) },
                    onNotificationsClick = { viewModel.navigateTo(ScreenNav.NOTIFICATIONS) },
                    onToggleCandleMode = { viewModel.toggleCandleMode() },
                    onTogglePetals = { viewModel.toggleAmbientPetals() },
                    onProfileClick = { viewModel.navigateTo(ScreenNav.PROFILE) },
                    onPremiumClick = { viewModel.navigateTo(ScreenNav.PREMIUM) }
                  )
                }
              },
              bottomBar = {
                if (!isWideScreen && uiState.currentScreen != ScreenNav.FULL_PROFILE && uiState.currentScreen != ScreenNav.PREMIUM) {
                  UpgradedBottomDock(
                    currentScreen = uiState.currentScreen,
                    unreadMessages = uiState.unreadMessagesCount,
                    isCandleMode = uiState.isCandleMode,
                    onNavigate = { viewModel.navigateTo(it) },
                    onCenterAction = {
                      haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                      viewModel.openQuickCreate(true)
                    }
                  )
                }
              }
            ) { innerPadding ->
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .padding(innerPadding)
              ) {
                AnimatedContent(
                  targetState = uiState.currentScreen,
                  transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
                  },
                  label = "screen_nav_transition"
                ) { targetScreen ->
                  when (targetScreen) {
                    ScreenNav.HOME -> HomeScreen(
                      posts = feedPosts,
                      stories = stories,
                      hiddenPostIds = hiddenPostIds,
                      isCandleMode = uiState.isCandleMode,
                      isVerifiedOnly = uiState.isVerifiedOnlyFeed,
                      isRefreshing = uiState.isRefreshingFeed,
                      onRefresh = { viewModel.refreshFeed() },
                      onToggleVerifiedOnly = { viewModel.toggleVerifiedOnlyFeed() },
                      onLikePost = { viewModel.likePost(it) },
                      onSavePost = { viewModel.toggleSavePost(it) },
                      onAddComment = { postId, parentId, text -> viewModel.addComment(postId, parentId, text) },
                      onLikeComment = { postId, cId -> viewModel.likeComment(postId, cId) },
                      onVotePoll = { postId, optIdx -> viewModel.votePostPoll(postId, optIdx) },
                      onReportPost = { postId, reason -> viewModel.reportPost(postId, reason) },
                      onHidePost = { viewModel.hidePost(it) },
                      onOpenStory = { viewModel.openStory(it) },
                      onAddStoryClick = { viewModel.openQuickCreate(true) },
                      onOpenComposer = { viewModel.openPostComposer(true) }
                    )
                    ScreenNav.EXPLORE -> ExploreScreen(
                      searchQuery = uiState.searchQuery,
                      trendingHashtags = trendingHashtags,
                      suggestedPeople = friendSuggestions,
                      communities = communities,
                      feedPosts = feedPosts,
                      isCandleMode = uiState.isCandleMode,
                      onSearchChange = { viewModel.updateSearchQuery(it) },
                      onHashtagClick = { tag ->
                        viewModel.updateSearchQuery(tag)
                      },
                      onConnectPerson = { id -> viewModel.sendFriendRequest(id) },
                      onSelectCommunity = { comm ->
                        viewModel.selectCommunity(comm)
                        viewModel.navigateTo(ScreenNav.COMMUNITIES)
                      },
                      onPostClick = { /* open post */ }
                    )
                    ScreenNav.NOTIFICATIONS -> NotificationsScreen(
                      notifications = notifications,
                      isCandleMode = uiState.isCandleMode,
                      onNotificationClick = { notif ->
                        viewModel.markNotificationRead(notif.id)
                        if (notif.type == SocialNotificationType.MATCH) {
                          viewModel.navigateTo(ScreenNav.MESSAGES)
                        } else if (notif.targetPostId != null) {
                          viewModel.navigateTo(ScreenNav.HOME)
                        }
                      },
                      onMarkAllRead = { viewModel.markAllNotificationsRead() },
                      onBack = { viewModel.navigateTo(ScreenNav.HOME) }
                    )
                    ScreenNav.DISCOVER -> DiscoverScreen(
                      profiles = profiles,
                      searchQuery = uiState.searchQuery,
                      dailyBlooms = dailyBlooms,
                      boostStatus = boostStatus,
                      isCandleMode = uiState.isCandleMode,
                      onLikeProfile = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.likeProfile(it)
                      },
                      onPassProfile = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.passProfile(it)
                      },
                      onMessageProfile = { viewModel.openChatWith(it.id) },
                      onSaveProfile = { viewModel.toggleSaveProfile(it) },
                      onOpenFullProfile = { viewModel.openFullProfile(it) },
                      onBoostProfile = { viewModel.boostProfile() },
                      onViewMatches = { viewModel.navigateTo(ScreenNav.MATCHES) }
                    )
                    ScreenNav.MATCHES -> MatchesScreen(
                      profiles = profiles,
                      searchQuery = uiState.searchQuery,
                      isCandleMode = uiState.isCandleMode,
                      onOpenChat = { viewModel.openChatWith(it.id) },
                      onToggleSave = { viewModel.toggleSaveProfile(it) },
                      onOpenFullProfile = { viewModel.openFullProfile(it) }
                    )
                    ScreenNav.LIKES_YOU -> LikesYouScreen(
                      admirers = profiles.filter { it.id == "p_ayaan" || it.id == "p_sofia" || it.isLiked },
                      isPremium = isPremium,
                      isCandleMode = uiState.isCandleMode,
                      onOpenProfile = { viewModel.openFullProfile(it) },
                      onOpenPremium = { viewModel.navigateTo(ScreenNav.PREMIUM) }
                    )
                    ScreenNav.MESSAGES -> MessagesScreen(
                      profiles = profiles,
                      messagesMap = messages,
                      selectedProfileId = uiState.activeChatProfileId,
                      stories = stories,
                      stickerPacks = viewModel.stickerPacks,
                      liveLocationState = liveLocationState,
                      isCandleMode = uiState.isCandleMode,
                      onSelectProfile = { viewModel.openChatWith(it) },
                      onSendRichMessage = { matchId, msg ->
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.sendRichMessage(matchId, msg)
                      },
                      onAddReaction = { matchId, msgId, emoji -> viewModel.addReaction(matchId, msgId, emoji) },
                      onDeleteMessage = { matchId, msgId, forEveryone -> viewModel.deleteMessage(matchId, msgId, forEveryone) },
                      onGrantMediaConsent = { matchId, msgId -> viewModel.grantMediaConsent(matchId, msgId) },
                      onMarkDisappearingViewed = { matchId, msgId -> viewModel.markDisappearingMediaViewed(matchId, msgId) },
                      onFreezeStreak = { viewModel.freezeStreak(it) },
                      onStartLiveLocation = { viewModel.startLiveLocation(it, 15, false) },
                      onStopLiveLocation = { viewModel.stopLiveLocation() },
                      onBlockUser = { viewModel.reportAndBlockUser(it, "Inappropriate Behavior") },
                      onAddStoryClick = { viewModel.openQuickCreate(true) },
                      onStoryClick = { viewModel.openStory(it) }
                    )
                    ScreenNav.COMMUNITIES -> CommunitiesScreen(
                      communities = communities,
                      groupChats = groupChats,
                      selectedCommunity = uiState.selectedCommunity,
                      selectedGroupChat = uiState.selectedGroupChat,
                      isCandleMode = uiState.isCandleMode,
                      onSelectCommunity = { viewModel.selectCommunity(it) },
                      onSelectGroupChat = { viewModel.selectGroupChat(it) },
                      onToggleJoinCommunity = { viewModel.toggleJoinCommunity(it) },
                      onCreateCommunityPost = { commId, content -> viewModel.createCommunityPost(commId, content) },
                      onLikeCommunityPost = { commId, pId -> viewModel.likeCommunityPost(commId, pId) },
                      onReportCommunityPost = { cId, pId, reason -> viewModel.reportCommunityPost(cId, pId, reason) },
                      onCreateGroupChat = { n, d, e, m -> viewModel.createGroupChat(n, d, e, m) },
                      onVoteGroupPoll = { gId, optIdx -> viewModel.voteGroupPoll(gId, optIdx) }
                    )
                    ScreenNav.NEARBY -> NearbyScreen(
                      nearbyPeople = nearbyPeople,
                      friendSuggestions = friendSuggestions,
                      liveLocationState = liveLocationState,
                      privacySettings = safetySettings.privacy,
                      isCandleMode = uiState.isCandleMode,
                      onStartLiveLocation = { match, dur, trusted -> viewModel.startLiveLocation(match, dur, trusted) },
                      onStopLiveLocation = { viewModel.stopLiveLocation() },
                      onSendFriendRequest = { viewModel.sendFriendRequest(it) },
                      onAcceptFriendRequest = { viewModel.acceptFriendRequest(it) },
                      onDeclineFriendRequest = { viewModel.declineFriendRequest(it) },
                      onRemoveFriendSuggestion = { viewModel.removeFriendSuggestion(it) },
                      onUpdatePrivacy = { viewModel.updatePrivacy(it) },
                      onOpenPermissionExplainer = { viewModel.openLocationPermissionDialog(true) }
                    )
                    ScreenNav.PROFILE -> ProfileScreen(
                      socialProfile = socialProfile,
                      isCandleMode = uiState.isCandleMode,
                      onFollowToggle = { viewModel.toggleFollowUser(socialProfile.userId) },
                      onFriendToggle = { viewModel.toggleSocialFriend(socialProfile.userId) },
                      onMessageClick = { viewModel.openChatWith("p_ayaan") },
                      onRomanticBloom = {
                        val firstMatch = profiles.firstOrNull()
                        if (firstMatch != null) viewModel.likeProfile(firstMatch)
                      },
                      onOpenSettings = { viewModel.navigateTo(ScreenNav.SETTINGS) },
                      onPostClick = { /* open post detail */ }
                    )
                    ScreenNav.SETTINGS -> ProfileSettingsScreen(
                      themeMode = uiState.appThemeMode,
                      isCandleMode = uiState.isCandleMode,
                      ambientPetalsEnabled = uiState.ambientPetalsEnabled,
                      isRtlUrdu = uiState.isRtlUrdu,
                      isPremium = isPremium,
                      privacySettings = safetySettings.privacy,
                      onSelectThemeMode = { viewModel.setThemeMode(it) },
                      onToggleCandleMode = { viewModel.toggleCandleMode() },
                      onToggleAmbientPetals = { viewModel.toggleAmbientPetals() },
                      onToggleLanguage = { viewModel.toggleLanguage() },
                      onOpenPremium = { viewModel.navigateTo(ScreenNav.PREMIUM) },
                      onOpenSelfieVerification = { viewModel.openSelfieDialog(true) },
                      onUpdatePrivacy = { viewModel.updatePrivacy(it) },
                      onExportData = { viewModel.exportUserData() },
                      onDeleteAccount = { viewModel.deleteUserAccount() }
                    )
                    ScreenNav.FULL_PROFILE -> {
                      val target = uiState.fullProfileTarget ?: profiles.first()
                      FullProfileScreen(
                        profile = target,
                        isCandleMode = uiState.isCandleMode,
                        onBack = { viewModel.navigateTo(ScreenNav.DISCOVER) },
                        onLike = {
                          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                          viewModel.likeProfile(target)
                        },
                        onPass = {
                          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                          viewModel.passProfile(target)
                          viewModel.navigateTo(ScreenNav.DISCOVER)
                        },
                        onMessage = { viewModel.openChatWith(target.id) },
                        onReport = { reason, details ->
                          viewModel.reportAndBlockUser(target.name, reason, details)
                          viewModel.navigateTo(ScreenNav.DISCOVER)
                        },
                        onBlock = {
                          viewModel.reportAndBlockUser(target.name, "Blocked by member")
                          viewModel.navigateTo(ScreenNav.DISCOVER)
                        }
                      )
                    }
                    ScreenNav.PREMIUM -> PremiumScreen(
                      isPremium = isPremium,
                      isCandleMode = uiState.isCandleMode,
                      onActivatePremium = {
                        viewModel.activatePremium()
                        viewModel.navigateTo(ScreenNav.DISCOVER)
                      },
                      onBack = { viewModel.navigateTo(ScreenNav.DISCOVER) }
                    )
                    else -> {}
                  }
                }
              }
            }
          }
        }

        // Mutual Match Celebration Dialog
        val matchedProfile = uiState.matchedCelebrationProfile
        if (matchedProfile != null) {
          MatchCelebrationDialog(
            matchedProfile = matchedProfile,
            isCandleMode = uiState.isCandleMode,
            onDismiss = { viewModel.dismissMatchCelebration() },
            onStartChat = { profile ->
              viewModel.dismissMatchCelebration()
              viewModel.openChatWith(profile.id)
            }
          )
        }

        // Selfie Verification Dialog
        if (uiState.showSelfieDialog) {
          SelfieVerificationDialog(
            isCandleMode = uiState.isCandleMode,
            onDismiss = { viewModel.openSelfieDialog(false) },
            onSelfieCaptured = {
              viewModel.captureSelfie()
              viewModel.openSelfieDialog(false)
            }
          )
        }

        // Quick Create Sheet (Center ✿ Action)
        if (uiState.showQuickCreateSheet) {
          QuickCreateSheet(
            isCandleMode = uiState.isCandleMode,
            onDismiss = { viewModel.openQuickCreate(false) },
            onOpenPostComposer = { viewModel.openPostComposer(true) },
            onCreateStory = { viewModel.addStory(it) },
            onOpenSelfie = { viewModel.openSelfieDialog(true) },
            onCreateGroup = { viewModel.navigateTo(ScreenNav.COMMUNITIES) }
          )
        }

        // Post Composer Dialog
        if (uiState.showPostComposer) {
          PostComposerDialog(
            isCandleMode = uiState.isCandleMode,
            onDismiss = { viewModel.openPostComposer(false) },
            onPublishPost = { caption, photoGradients, hashtags, mentions, audience, locationCity, isStory, poll ->
              viewModel.createFeedPost(caption, photoGradients, hashtags, mentions, audience, locationCity, isStory, poll)
            }
          )
        }

        // Story Viewer Overlay
        val activeStory = uiState.activeStory
        if (activeStory != null) {
          StoryViewerDialog(
            story = activeStory,
            isCandleMode = uiState.isCandleMode,
            onDismiss = { viewModel.closeStory() },
            onReplyInChat = { replyText ->
              viewModel.sendMessage(activeStory.userId, replyText)
              viewModel.openChatWith(activeStory.userId)
            },
            onReportStory = { reason -> viewModel.reportStory(activeStory.id, reason) }
          )
        }
      }
    }
  }
}

@Composable
private fun BloomTopBar(
  title: String,
  searchQuery: String,
  unreadNotifications: Int,
  isCandleMode: Boolean,
  ambientPetalsEnabled: Boolean,
  isPremium: Boolean,
  onSearchChange: (String) -> Unit,
  onNotificationsClick: () -> Unit,
  onToggleCandleMode: () -> Unit,
  onTogglePetals: () -> Unit,
  onProfileClick: () -> Unit,
  onPremiumClick: () -> Unit
) {
  Surface(
    color = Color.Transparent,
    modifier = Modifier
      .fillMaxWidth()
      .statusBarsPadding()
      .padding(horizontal = 16.dp, vertical = 6.dp)
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
        Text(
          text = title,
          style = MaterialTheme.typography.titleLarge.copy(
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Bold,
            color = if (isCandleMode) BloomWarmGold else BloomDeepRose
          )
        )
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        // Notification bell with unread badge
        Box {
          IconButton(
            onClick = onNotificationsClick,
            modifier = Modifier.size(36.dp).testTag("top_bar_notifications_button")
          ) {
            Icon(
              Icons.Outlined.Notifications,
              contentDescription = "Notifications",
              tint = if (isCandleMode) BloomWarmGold else BloomDeepRose,
              modifier = Modifier.size(20.dp)
            )
          }
          if (unreadNotifications > 0) {
            Box(
              modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-3).dp, y = 5.dp)
                .size(8.dp)
                .background(BloomRose, CircleShape)
            )
          }
        }

        IconButton(
          onClick = onTogglePetals,
          modifier = Modifier.size(36.dp)
        ) {
          Text(if (ambientPetalsEnabled) "🌸" else "🥀", fontSize = 16.sp)
        }

        IconButton(
          onClick = onToggleCandleMode,
          modifier = Modifier.size(36.dp)
        ) {
          Text(if (isCandleMode) "🕯️" else "☀️", fontSize = 16.sp)
        }

        IconButton(
          onClick = onProfileClick,
          modifier = Modifier
            .size(34.dp)
            .shadow(2.dp, CircleShape)
            .clip(CircleShape)
            .background(BloomRose)
            .testTag("top_bar_profile_avatar")
        ) {
          Text("M", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

/**
 * Upgraded Bottom Dock per Section 8:
 * Home, Discover, Center ✿ Button, Chats, Communities, Profile
 */
@Composable
private fun UpgradedBottomDock(
  currentScreen: ScreenNav,
  unreadMessages: Int,
  isCandleMode: Boolean,
  onNavigate: (ScreenNav) -> Unit,
  onCenterAction: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .navigationBarsPadding()
      .padding(horizontal = 16.dp, vertical = 8.dp),
    contentAlignment = Alignment.Center
  ) {
    Surface(
      shape = RoundedCornerShape(9999.dp),
      color = if (isCandleMode) Color(0xFF28152E).copy(alpha = 0.95f) else Color.White.copy(alpha = 0.92f),
      shadowElevation = 12.dp,
      border = BorderStroke(
        1.dp,
        if (isCandleMode) BloomWarmGold.copy(alpha = 0.35f) else BloomRose.copy(alpha = 0.3f)
      ),
      modifier = Modifier.fillMaxWidth().height(62.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
      ) {
        DockNavItem(
          icon = Icons.Outlined.Home,
          label = "Home",
          isActive = currentScreen == ScreenNav.HOME,
          onClick = { onNavigate(ScreenNav.HOME) }
        )

        DockNavItem(
          icon = Icons.Outlined.Explore,
          label = "Discover",
          isActive = currentScreen == ScreenNav.EXPLORE || currentScreen == ScreenNav.DISCOVER,
          onClick = { onNavigate(ScreenNav.EXPLORE) }
        )

        // Center Round "✿" Quick-Create Action Button (Post, Story, Reel, Poll, Group)
        Box(
          modifier = Modifier
            .offset(y = (-10).dp)
            .size(54.dp)
            .shadow(8.dp, CircleShape)
            .background(
              Brush.linearGradient(listOf(BloomRose, BloomCandleGold)),
              CircleShape
            )
            .border(2.dp, Color.White, CircleShape)
            .clickable { onCenterAction() }
            .testTag("center_bloom_button"),
          contentAlignment = Alignment.Center
        ) {
          Text("✿", fontSize = 26.sp, color = Color.White)
        }

        DockNavItem(
          icon = Icons.Outlined.ChatBubbleOutline,
          label = "Chats",
          badge = unreadMessages,
          isActive = currentScreen == ScreenNav.MESSAGES,
          onClick = { onNavigate(ScreenNav.MESSAGES) }
        )

        DockNavItem(
          icon = Icons.Outlined.Groups,
          label = "Circles",
          isActive = currentScreen == ScreenNav.COMMUNITIES,
          onClick = { onNavigate(ScreenNav.COMMUNITIES) }
        )

        DockNavItem(
          icon = Icons.Outlined.Person,
          label = "Profile",
          isActive = currentScreen == ScreenNav.PROFILE,
          onClick = { onNavigate(ScreenNav.PROFILE) }
        )
      }
    }
  }
}

@Composable
private fun DockNavItem(
  icon: ImageVector,
  label: String,
  isActive: Boolean,
  badge: Int = 0,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .clickable { onClick() }
      .padding(horizontal = 4.dp, vertical = 2.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
      Box {
        Icon(
          imageVector = icon,
          contentDescription = label,
          tint = if (isActive) BloomRose else Color.Gray.copy(alpha = 0.7f),
          modifier = Modifier.size(20.dp)
        )
        if (badge > 0) {
          Box(
            modifier = Modifier
              .offset(x = 10.dp, y = (-4).dp)
              .size(8.dp)
              .background(BloomRose, CircleShape)
          )
        }
      }
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(
          fontSize = 9.sp,
          color = if (isActive) BloomRose else Color.Gray.copy(alpha = 0.7f),
          fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
        )
      )
    }
  }
}

@Composable
private fun BloomSidebar(
  currentScreen: ScreenNav,
  unreadLikes: Int,
  unreadMessages: Int,
  isCandleMode: Boolean,
  isPremium: Boolean,
  onNavigate: (ScreenNav) -> Unit
) {
  val sidebarBrush = Brush.verticalGradient(
    colors = if (isCandleMode) listOf(Color(0xFF3B1F42), Color(0xFF28152E))
             else listOf(Color(0xFFF28DB0), Color(0xFFB79CF0))
  )

  Surface(
    modifier = Modifier
      .width(260.dp)
      .fillMaxHeight()
      .padding(12.dp)
      .shadow(16.dp, RoundedCornerShape(36.dp)),
    shape = RoundedCornerShape(36.dp),
    color = Color.Transparent
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(sidebarBrush)
        .padding(18.dp)
    ) {
      Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Box {
              Box(
                modifier = Modifier
                  .size(54.dp)
                  .shadow(6.dp, CircleShape)
                  .border(2.5.dp, Brush.sweepGradient(listOf(BloomWarmGold, BloomRose, BloomWarmGold)), CircleShape)
                  .clip(CircleShape)
                  .background(Color.White.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
              ) {
                Text("M", style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold))
              }
              Box(
                modifier = Modifier
                  .size(14.dp)
                  .background(BloomOnline, CircleShape)
                  .border(2.dp, Color.White, CircleShape)
                  .align(Alignment.BottomEnd)
              )
            }

            Column {
              Text(
                text = "Hi, Mia 🌸",
                style = MaterialTheme.typography.headlineSmall.copy(
                  fontStyle = FontStyle.Italic,
                  color = Color.White,
                  fontWeight = FontWeight.Bold
                )
              )
              Text("Verified Member 🛡️", style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.85f), fontSize = 10.sp))
            }
          }

          HorizontalDivider(color = Color.White.copy(alpha = 0.25f), thickness = 1.dp)

          val navItems = listOf(
            Triple(ScreenNav.HOME, Icons.Outlined.Home, 0),
            Triple(ScreenNav.EXPLORE, Icons.Outlined.Explore, 0),
            Triple(ScreenNav.NOTIFICATIONS, Icons.Outlined.Notifications, 0),
            Triple(ScreenNav.MESSAGES, Icons.Outlined.ChatBubbleOutline, unreadMessages),
            Triple(ScreenNav.COMMUNITIES, Icons.Outlined.Groups, 0),
            Triple(ScreenNav.PROFILE, Icons.Outlined.Person, 0),
            Triple(ScreenNav.DISCOVER, Icons.Outlined.FavoriteBorder, unreadLikes),
            Triple(ScreenNav.NEARBY, Icons.Outlined.LocationOn, 0),
            Triple(ScreenNav.SETTINGS, Icons.Outlined.Settings, 0)
          )

          navItems.forEach { (screen, icon, badge) ->
            val isActive = currentScreen == screen
            SidebarNavItem(
              label = screen.title,
              icon = icon,
              badge = badge,
              isActive = isActive,
              onClick = { onNavigate(screen) }
            )
          }
        }

        DecorativeCandle(candleHeight = 36.dp, candleWidth = 18.dp, isCandleMode = isCandleMode)
      }
    }
  }
}

@Composable
private fun SidebarNavItem(
  label: String,
  icon: ImageVector,
  badge: Int = 0,
  isActive: Boolean,
  onClick: () -> Unit
) {
  val background = if (isActive) Color.White.copy(alpha = 0.95f) else Color.Transparent
  val textColor = if (isActive) BloomRose else Color.White

  Surface(
    shape = RoundedCornerShape(9999.dp),
    color = background,
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("sidebar_${label.lowercase().replace(" ", "_")}")
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Icon(
          imageVector = icon,
          contentDescription = label,
          tint = textColor,
          modifier = Modifier.size(19.dp)
        )
        Text(
          text = label,
          style = MaterialTheme.typography.bodyMedium.copy(
            color = textColor,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
            fontSize = 13.sp
          )
        )
      }

      if (badge > 0) {
        Surface(
          shape = CircleShape,
          color = if (isActive) BloomRose else Color.White
        ) {
          Text(
            text = badge.toString(),
            color = if (isActive) Color.White else BloomRose,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }
      }
    }
  }
}
