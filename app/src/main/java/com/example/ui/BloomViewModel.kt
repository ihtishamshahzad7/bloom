package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BloomRepository
import com.example.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class BloomUiState(
  val currentScreen: ScreenNav = ScreenNav.SPLASH,
  val appThemeMode: AppThemeMode = AppThemeMode.SYSTEM,
  val isCandleMode: Boolean = false,
  val ambientPetalsEnabled: Boolean = true,
  val isRtlUrdu: Boolean = false,
  val searchQuery: String = "",
  val activeChatProfileId: String? = null,
  val fullProfileTarget: UserProfile? = null,
  val matchedCelebrationProfile: UserProfile? = null,
  val showSelfieDialog: Boolean = false,
  val showQuickCreateSheet: Boolean = false,
  val showPostComposer: Boolean = false,
  val isVerifiedOnlyFeed: Boolean = false,
  val isRefreshingFeed: Boolean = false,
  val activeStory: StoryItem? = null,
  val selectedCommunity: Community? = null,
  val selectedGroupChat: GroupChat? = null,
  val showLocationPermissionDialog: Boolean = false,
  val viewOnceMediaNotice: String? = null,
  val unreadLikesCount: Int = 2,
  val unreadMessagesCount: Int = 1,
  val unreadNotificationsCount: Int = 2,
  val hasCompletedOnboarding: Boolean = false,
  val hasCompletedPhoneAuth: Boolean = false,
  val isSecureScreenActive: Boolean = false
)

class BloomViewModel(
  private val repository: BloomRepository = BloomRepository()
) : ViewModel() {

  private val _uiState = MutableStateFlow(BloomUiState())
  val uiState: StateFlow<BloomUiState> = _uiState.asStateFlow()

  val profiles: StateFlow<List<UserProfile>> = repository.profiles
  val messages: StateFlow<Map<String, List<ChatMessage>>> = repository.messages
  val scheduledDates: StateFlow<List<ScheduledDate>> = repository.scheduledDates
  val quizQuestions: StateFlow<List<QuizQuestion>> = repository.quizQuestions
  val safetySettings: StateFlow<SafetySettings> = repository.safetySettings
  val dailyBlooms: StateFlow<DailyBloomsCounter> = repository.dailyBlooms
  val boostStatus: StateFlow<ProfileBoostStatus> = repository.boostStatus
  val isPremium: StateFlow<Boolean> = repository.isPremium
  val dateIdeas: List<DateIdea> = repository.dateIdeas

  // Social Network Features
  val feedPosts: StateFlow<List<FeedPost>> = repository.feedPosts
  val trendingHashtags: StateFlow<List<TrendingHashtag>> = repository.trendingHashtags
  val notifications: StateFlow<List<SocialNotification>> = repository.notifications
  val userSocialProfile: StateFlow<UserSocialProfile> = repository.userSocialProfile
  val hiddenPostIds: StateFlow<Set<String>> = repository.hiddenPostIds

  // Enhanced features
  val stories: StateFlow<List<StoryItem>> = repository.stories
  val nearbyPeople: StateFlow<List<NearbyPerson>> = repository.nearbyPeople
  val liveLocation: StateFlow<LiveLocationState> = repository.liveLocation
  val friendSuggestions: StateFlow<List<FriendSuggestion>> = repository.friendSuggestions
  val communities: StateFlow<List<Community>> = repository.communities
  val groupChats: StateFlow<List<GroupChat>> = repository.groupChats
  val stickerPacks: List<StickerPack> = repository.stickerPacks
  val typingStatus: StateFlow<Map<String, Boolean>> = repository.typingStatus
  val recordingStatus: StateFlow<Map<String, Boolean>> = repository.recordingStatus

  fun onSplashFinished() {
    _uiState.update {
      it.copy(
        currentScreen = if (!it.hasCompletedOnboarding) ScreenNav.ONBOARDING
                        else if (!it.hasCompletedPhoneAuth) ScreenNav.PHONE_AUTH
                        else ScreenNav.HOME
      )
    }
  }

  fun onOnboardingFinished() {
    _uiState.update {
      it.copy(
        hasCompletedOnboarding = true,
        currentScreen = ScreenNav.PHONE_AUTH
      )
    }
  }

  fun onPhoneAuthFinished() {
    _uiState.update {
      it.copy(
        hasCompletedPhoneAuth = true,
        currentScreen = ScreenNav.HOME
      )
    }
  }

  fun navigateTo(screen: ScreenNav) {
    _uiState.update {
      it.copy(
        currentScreen = screen,
        selectedCommunity = null,
        selectedGroupChat = null
      )
    }
  }

  fun openFullProfile(profile: UserProfile) {
    _uiState.update {
      it.copy(
        fullProfileTarget = profile,
        currentScreen = ScreenNav.FULL_PROFILE
      )
    }
  }

  fun setThemeMode(mode: AppThemeMode) {
    _uiState.update { it.copy(appThemeMode = mode) }
  }

  fun toggleCandleMode() {
    _uiState.update { it.copy(isCandleMode = !it.isCandleMode) }
  }

  fun toggleAmbientPetals() {
    _uiState.update { it.copy(ambientPetalsEnabled = !it.ambientPetalsEnabled) }
  }

  fun toggleLanguage() {
    _uiState.update { it.copy(isRtlUrdu = !it.isRtlUrdu) }
  }

  fun updateSearchQuery(query: String) {
    _uiState.update { it.copy(searchQuery = query) }
  }

  fun openChatWith(profileId: String?) {
    _uiState.update {
      it.copy(
        currentScreen = ScreenNav.MESSAGES,
        activeChatProfileId = profileId
      )
    }
  }

  fun likeProfile(profile: UserProfile) {
    val isMutual = repository.likeProfile(profile.id)
    if (isMutual) {
      _uiState.update { it.copy(matchedCelebrationProfile = profile) }
    }
  }

  fun dismissMatchCelebration() {
    _uiState.update { it.copy(matchedCelebrationProfile = null) }
  }

  fun passProfile(profile: UserProfile) {
    repository.passProfile(profile.id)
  }

  fun toggleSaveProfile(profile: UserProfile) {
    repository.toggleSaveProfile(profile.id)
  }

  fun sendMessage(matchId: String, text: String): String? {
    return repository.sendMessage(matchId, text)
  }

  fun sendRichMessage(matchId: String, message: ChatMessage): String? {
    return repository.sendRichMessage(matchId, message)
  }

  fun addReaction(matchId: String, messageId: String, emoji: String) {
    repository.addReaction(matchId, messageId, emoji)
  }

  fun deleteMessage(matchId: String, messageId: String, deleteForEveryone: Boolean) {
    repository.deleteMessage(matchId, messageId, deleteForEveryone)
  }

  fun grantMediaConsent(matchId: String, messageId: String) {
    repository.grantMediaConsent(matchId, messageId)
  }

  fun markDisappearingMediaViewed(matchId: String, messageId: String) {
    repository.markDisappearingMediaViewed(matchId, messageId)
  }

  fun setSecureScreen(active: Boolean) {
    _uiState.update { it.copy(isSecureScreenActive = active) }
  }

  fun freezeStreak(matchId: String) {
    repository.freezeStreak(matchId)
  }

  // Stories
  fun openStory(story: StoryItem) {
    _uiState.update { it.copy(activeStory = story) }
    repository.recordStoryView(story.id)
  }

  fun closeStory() {
    _uiState.update { it.copy(activeStory = null) }
  }

  fun addStory(story: StoryItem) {
    repository.addStory(story)
  }

  fun reportStory(storyId: String, reason: String) {
    repository.reportStory(storyId, reason)
    closeStory()
  }

  // Location
  fun openLocationPermissionDialog(show: Boolean) {
    _uiState.update { it.copy(showLocationPermissionDialog = show) }
  }

  fun startLiveLocation(matchName: String, durationMinutes: Int, withTrustedContact: Boolean) {
    repository.startLiveLocationSharing(matchName, durationMinutes, withTrustedContact)
  }

  fun stopLiveLocation() {
    repository.stopLiveLocationSharing()
  }

  // Friend Suggestions
  fun sendFriendRequest(id: String) {
    repository.sendFriendRequest(id)
  }

  fun acceptFriendRequest(id: String) {
    repository.acceptFriendRequest(id)
  }

  fun declineFriendRequest(id: String) {
    repository.declineFriendRequest(id)
  }

  fun removeFriendSuggestion(id: String) {
    repository.removeFriendSuggestion(id)
  }

  // Communities & Groups
  fun selectCommunity(community: Community?) {
    _uiState.update { it.copy(selectedCommunity = community) }
  }

  fun selectGroupChat(group: GroupChat?) {
    _uiState.update { it.copy(selectedGroupChat = group) }
  }

  fun toggleJoinCommunity(communityId: String) {
    repository.toggleJoinCommunity(communityId)
    // Update selected community if open
    val updated = repository.communities.value.find { it.id == communityId }
    if (updated != null && _uiState.value.selectedCommunity?.id == communityId) {
      _uiState.update { it.copy(selectedCommunity = updated) }
    }
  }

  fun createCommunityPost(communityId: String, content: String) {
    repository.createCommunityPost(communityId, content)
    val updated = repository.communities.value.find { it.id == communityId }
    if (updated != null && _uiState.value.selectedCommunity?.id == communityId) {
      _uiState.update { it.copy(selectedCommunity = updated) }
    }
  }

  fun likeCommunityPost(communityId: String, postId: String) {
    repository.likeCommunityPost(communityId, postId)
    val updated = repository.communities.value.find { it.id == communityId }
    if (updated != null && _uiState.value.selectedCommunity?.id == communityId) {
      _uiState.update { it.copy(selectedCommunity = updated) }
    }
  }

  fun reportCommunityPost(communityId: String, postId: String, reason: String) {
    repository.reportCommunityPost(communityId, postId, reason)
  }

  fun createGroupChat(name: String, description: String, iconEmoji: String, members: List<String>) {
    repository.createGroupChat(name, description, iconEmoji, members)
  }

  fun voteGroupPoll(groupId: String, optionIndex: Int) {
    repository.voteGroupPoll(groupId, optionIndex)
    val updated = repository.groupChats.value.find { it.id == groupId }
    if (updated != null && _uiState.value.selectedGroupChat?.id == groupId) {
      _uiState.update { it.copy(selectedGroupChat = updated) }
    }
  }

  fun openQuickCreate(show: Boolean) {
    _uiState.update { it.copy(showQuickCreateSheet = show) }
  }

  fun togglePhotoConsent(profileId: String) {
    repository.togglePhotoConsent(profileId)
  }

  fun proposeDate(match: UserProfile, idea: DateIdea, dateStr: String, timeStr: String) {
    repository.proposeDate(match, idea, dateStr, timeStr)
    _uiState.update { it.copy(currentScreen = ScreenNav.DATES) }
  }

  fun answerQuizQuestion(questionId: Int, optionIndex: Int) {
    repository.answerQuizQuestion(questionId, optionIndex)
  }

  fun updateSafety(transform: (SafetySettings) -> SafetySettings) {
    repository.updateSafety(transform)
  }

  fun updatePrivacy(transform: (PrivacySettings) -> PrivacySettings) {
    repository.updatePrivacy(transform)
  }

  fun reportAndBlockUser(userName: String, reason: String, details: String = "") {
    repository.reportAndBlockUser(userName, reason, details)
  }

  fun exportUserData(): String {
    return repository.exportUserDataJson()
  }

  fun deleteUserAccount() {
    repository.deleteUserAccountAndData()
    _uiState.update {
      it.copy(
        hasCompletedPhoneAuth = false,
        hasCompletedOnboarding = false,
        currentScreen = ScreenNav.ONBOARDING
      )
    }
  }

  fun boostProfile() {
    repository.boostProfile()
  }

  fun activatePremium() {
    repository.setPremium(true)
  }

  fun openSelfieDialog(show: Boolean) {
    _uiState.update { it.copy(showSelfieDialog = show) }
  }

  fun captureSelfie() {
    repository.submitSelfieVerification()
  }

  // Social Network actions
  fun toggleVerifiedOnlyFeed() {
    _uiState.update { it.copy(isVerifiedOnlyFeed = !it.isVerifiedOnlyFeed) }
  }

  fun refreshFeed() {
    viewModelScope.launch {
      _uiState.update { it.copy(isRefreshingFeed = true) }
      kotlinx.coroutines.delay(650)
      _uiState.update { it.copy(isRefreshingFeed = false) }
    }
  }

  fun openPostComposer(show: Boolean) {
    _uiState.update { it.copy(showPostComposer = show) }
  }

  fun likePost(postId: String) {
    repository.likePost(postId)
  }

  fun toggleSavePost(postId: String) {
    repository.toggleSavePost(postId)
  }

  fun addComment(postId: String, parentCommentId: String?, text: String): String? {
    return repository.addComment(postId, parentCommentId, text)
  }

  fun likeComment(postId: String, commentId: String) {
    repository.likeComment(postId, commentId)
  }

  fun votePostPoll(postId: String, optionIndex: Int) {
    repository.votePostPoll(postId, optionIndex)
  }

  fun createFeedPost(
    caption: String,
    photoGradients: List<Pair<Long, Long>>,
    hashtags: List<String>,
    mentions: List<String>,
    audience: PostAudience,
    locationCity: String?,
    isStory: Boolean,
    poll: FeedPostPoll?
  ): String? {
    val error = repository.createFeedPost(caption, photoGradients, hashtags, mentions, audience, locationCity, isStory, poll)
    if (error == null) {
      _uiState.update { it.copy(showPostComposer = false) }
    }
    return error
  }

  fun reportPost(postId: String, reason: String) {
    repository.reportPost(postId, reason)
  }

  fun hidePost(postId: String) {
    repository.hidePost(postId)
  }

  fun toggleFollowUser(userId: String) {
    repository.toggleFollowUser(userId)
  }

  fun toggleSocialFriend(userId: String) {
    repository.toggleSocialFriend(userId)
  }

  fun markNotificationRead(id: String) {
    repository.markNotificationRead(id)
    val remaining = repository.notifications.value.count { !it.isRead && it.id != id }
    _uiState.update { it.copy(unreadNotificationsCount = remaining) }
  }

  fun markAllNotificationsRead() {
    repository.markAllNotificationsRead()
    _uiState.update { it.copy(unreadNotificationsCount = 0) }
  }
}

