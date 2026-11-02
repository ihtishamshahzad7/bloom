package com.example.model

enum class MessageType {
  TEXT, EMOJI, STICKER, PHOTO, SELFIE, VIDEO, VOICE_NOTE, LOCATION_PIN, DISAPPEARING_MEDIA
}

enum class StoryType {
  PHOTO, VIDEO, TEXT_STATUS
}

enum class StoryVisibility {
  EVERYONE, MATCHES_ONLY, CLOSE_FRIENDS, HIDDEN_FROM
}

enum class FriendRequestStatus {
  SUGGESTED, SENT, ACCEPTED, DECLINED, REMOVED
}

enum class AppThemeMode {
  SYSTEM, LIGHT, DARK
}

enum class ScreenNav(val title: String) {
  SPLASH("Splash"),
  ONBOARDING("Onboarding"),
  AGE_GATE("Age Verification"),
  PHONE_AUTH("Phone Verification"),
  HOME("Home Feed"),
  EXPLORE("Explore"),
  NOTIFICATIONS("Notifications"),
  DISCOVER("Discover"),
  MATCHES("Matches"),
  MESSAGES("Chats"),
  COMMUNITIES("Communities"),
  PROFILE("Profile"),
  LIKES_YOU("Likes You"),
  DATES("Dates"),
  COMPATIBILITY("Compatibility"),
  SAFETY("Safety Center"),
  SETTINGS("Settings & Privacy"),
  FULL_PROFILE("Profile Detail"),
  PREMIUM("Bloom Gold"),
  NEARBY("Nearby Friends"),
  FRIEND_SUGGESTIONS("Friend Suggestions")
}

enum class PostAudience(val label: String) {
  EVERYONE("Everyone"),
  FRIENDS("Friends Only"),
  MATCHES("Matches Only")
}

enum class SocialNotificationType {
  LIKE, COMMENT, FOLLOW, FRIEND_REQUEST, MATCH, STORY_VIEW
}

data class FeedPostPoll(
  val question: String,
  val options: List<PollOption>,
  val totalVotes: Int = 0,
  val hasVoted: Boolean = false,
  val userVotedIndex: Int? = null
)

data class PostComment(
  val id: String,
  val postId: String,
  val authorName: String,
  val authorHandle: String,
  val authorAvatarColors: Pair<Long, Long>,
  val text: String,
  val timestamp: String,
  val likesCount: Int = 0,
  val isLiked: Boolean = false,
  val parentCommentId: String? = null,
  val replies: List<PostComment> = emptyList()
)

data class FeedPost(
  val id: String,
  val authorId: String,
  val authorName: String,
  val authorNickname: String,
  val authorHandle: String,
  val authorAvatarColors: Pair<Long, Long>,
  val authorVerified: Boolean = true,
  val authorStreakDays: Int = 7,
  val isOnline: Boolean = true,
  val timestamp: String,
  val textCaption: String,
  val photoGradients: List<Pair<Long, Long>> = emptyList(), // up to 6 photos represented by gradient palettes
  val photoUrls: List<String> = emptyList(),
  val hashtags: List<String> = emptyList(),
  val mentions: List<String> = emptyList(),
  val locationCity: String? = null, // city only, zero exact coords
  val audience: PostAudience = PostAudience.EVERYONE,
  val likesCount: Int = 0,
  val commentsCount: Int = 0,
  val sharesCount: Int = 0,
  val isLiked: Boolean = false,
  val isSaved: Boolean = false,
  val isStory: Boolean = false,
  val poll: FeedPostPoll? = null,
  val isFlagged: Boolean = false,
  val comments: List<PostComment> = emptyList()
)

data class SocialNotification(
  val id: String,
  val type: SocialNotificationType,
  val actorName: String,
  val actorHandle: String,
  val actorAvatarColors: Pair<Long, Long>,
  val message: String,
  val timeAgo: String,
  val targetPostId: String? = null,
  val isRead: Boolean = false,
  val dateGroup: String = "Today"
)

data class ProfileHighlight(
  val id: String,
  val title: String,
  val emoji: String,
  val coverColors: Pair<Long, Long>
)

data class UserSocialProfile(
  val userId: String,
  val name: String,
  val nickname: String,
  val handle: String,
  val bio: String,
  val city: String,
  val followersCount: Int,
  val followingCount: Int,
  val friendsCount: Int,
  val postsCount: Int,
  val badges: List<String>,
  val isFollowing: Boolean = false,
  val isFriend: Boolean = false,
  val isLikedRomantic: Boolean = false,
  val avatarColors: Pair<Long, Long>,
  val coverColors: Pair<Long, Long>,
  val highlights: List<ProfileHighlight> = emptyList(),
  val posts: List<FeedPost> = emptyList()
)

data class TrendingHashtag(
  val tag: String,
  val category: String,
  val postsCountFormatted: String,
  val isHot: Boolean = false
)

data class SharedMediaItem(
  val id: String,
  val title: String,
  val colorStart: Long,
  val colorEnd: Long,
  val isVideo: Boolean = false,
  val duration: String? = null,
  val isLocked: Boolean = true
)

data class UserProfile(
  val id: String = "",
  val name: String = "",
  val nickname: String = "",
  val age: Int = 18,
  val city: String = "",
  val distanceMiles: Int = 5,
  val education: String = "",
  val occupation: String = "",
  val religionCommunity: String = "",
  val intentions: String = "Serious relationship / Marriage-minded",
  val bio: String = "",
  val statusLine: String = "Finding joy in the quiet moments 🌸",
  val compatibilityPercent: Int = 85,
  val isVerified: Boolean = true,
  val adminApproved: Boolean = true,
  val isOnline: Boolean = true,
  val lastSeen: String = "Active now",
  val badges: List<String> = listOf("Verified 🛡️", "Streak 🌸🔥", "Early Bloomer 🌱✨"),
  val interestChips: List<String> = emptyList(),
  val values: List<String> = emptyList(),
  val loveLanguage: String = "Quality Time",
  val avatarColorStart: Long = 0xFFF28DB0,
  val avatarColorEnd: Long = 0xFF9E77ED,
  val coverColorStart: Long = 0xFFF9D3E3,
  val coverColorEnd: Long = 0xFFCDB8F0,
  val initials: String = "B",
  val promptQuestion: String = "",
  val promptAnswer: String = "",
  val photoColorPairs: List<Pair<Long, Long>> = listOf(
    Pair(0xFFF28DB0, 0xFF9E77ED),
    Pair(0xFFFF9BB3, 0xFFFFD3C2),
    Pair(0xFF9E77ED, 0xFF6172F3)
  ),
  val sharedGallery: List<SharedMediaItem> = listOf(
    SharedMediaItem("1", "Sunset in Hunza", 0xFFE85D8A, 0xFFF6C177),
    SharedMediaItem("2", "Art Exhibit Visit", 0xFF9E77ED, 0xFFB79CF0),
    SharedMediaItem("3", "Chai Moments", 0xFFFF9BB3, 0xFFFFD3C2, isVideo = true, duration = "0:34")
  ),
  val isLiked: Boolean = false,
  val isMutualMatch: Boolean = false,
  val isSaved: Boolean = false,
  val photoConsentApproved: Boolean = false,
  val bloomStreak: BloomStreak = BloomStreak()
)

data class ChatMessage(
  val id: String = "",
  val matchId: String = "",
  val senderId: String = "",
  val senderName: String = "",
  val text: String = "",
  val timestamp: String = "",
  val isMine: Boolean = false,
  val isRead: Boolean = true,
  val isDelivered: Boolean = true,
  val messageType: MessageType = MessageType.TEXT,
  val mediaUrl: String? = null,
  val isViewOnce: Boolean = false,
  val hasBeenViewed: Boolean = false,
  val mediaConsentRequired: Boolean = false,
  val mediaConsentGranted: Boolean = false,
  val voiceDurationSeconds: Int = 0,
  val voiceWaveform: List<Float> = listOf(0.3f, 0.6f, 0.9f, 0.5f, 0.8f, 0.4f, 0.7f, 0.9f, 0.6f, 0.3f),
  val locationTitle: String? = null,
  val locationAddress: String? = null,
  val locationDistance: String? = null,
  val stickerPack: String? = null,
  val stickerName: String? = null,
  val stickerEmoji: String? = null,
  val reaction: String? = null,
  val replyToMessageId: String? = null,
  val replyToText: String? = null,
  val replyToSenderName: String? = null,
  val safetyAlert: String? = null,
  val isDeletedForEveryone: Boolean = false,
  val uploadProgress: Float? = null,
  val uploadFailed: Boolean = false
)

data class BloomStreak(
  val matchId: String = "",
  val currentStreakDays: Int = 7,
  val lastMessageTimestamp: Long = System.currentTimeMillis(),
  val hoursRemaining: Int = 6,
  val isExpiringSoon: Boolean = false,
  val streakFreezeActive: Boolean = false,
  val milestoneCelebrated: Int = 7
)

data class StoryViewer(
  val userId: String,
  val userName: String,
  val userInitials: String,
  val avatarColorStart: Long,
  val avatarColorEnd: Long,
  val viewedAt: String
)

data class StoryItem(
  val id: String,
  val userId: String,
  val userName: String,
  val userInitials: String,
  val avatarColorStart: Long,
  val avatarColorEnd: Long,
  val storyType: StoryType = StoryType.PHOTO,
  val textCaption: String = "",
  val moodEmoji: String? = null,
  val bgGradientStart: Long = 0xFFF28DB0,
  val bgGradientEnd: Long = 0xFFB79CF0,
  val createdAt: Long = System.currentTimeMillis(),
  val expiresAt: Long = System.currentTimeMillis() + 86400000L,
  val viewsCount: Int = 18,
  val viewersList: List<StoryViewer> = emptyList(),
  val visibility: StoryVisibility = StoryVisibility.EVERYONE,
  val isFlagged: Boolean = false,
  val isViewedByMe: Boolean = false
)

data class StickerItem(
  val id: String,
  val label: String,
  val iconEmoji: String,
  val urduMeaning: String? = null
)

data class StickerPack(
  val id: String,
  val name: String,
  val coverEmoji: String,
  val stickers: List<StickerItem>
)

data class NearbyPerson(
  val id: String,
  val name: String,
  val age: Int,
  val city: String,
  val approximateArea: String,
  val distanceKm: Double,
  val interests: List<String>,
  val compatibilityPercent: Int,
  val isVerified: Boolean = true,
  val avatarColorStart: Long,
  val avatarColorEnd: Long,
  val initials: String
)

data class FriendSuggestion(
  val id: String,
  val name: String,
  val age: Int,
  val city: String,
  val mutualInterests: List<String>,
  val compatibilityScore: Int,
  val avatarColorStart: Long,
  val avatarColorEnd: Long,
  val initials: String,
  val status: FriendRequestStatus = FriendRequestStatus.SUGGESTED
)

data class PollOption(
  val id: Int,
  val text: String,
  val voteCount: Int
)

data class GroupPoll(
  val id: String,
  val question: String,
  val options: List<PollOption>,
  val totalVotes: Int = 0,
  val hasVoted: Boolean = false,
  val userVotedOptionIndex: Int? = null
)

data class GroupChat(
  val id: String,
  val name: String,
  val description: String,
  val membersCount: Int,
  val adminIds: List<String>,
  val memberNames: List<String>,
  val iconEmoji: String,
  val gradientStart: Long,
  val gradientEnd: Long,
  val lastMessageText: String,
  val lastMessageTime: String,
  val unreadCount: Int = 0,
  val activePoll: GroupPoll? = null
)

data class CommunityPost(
  val id: String,
  val communityId: String,
  val authorId: String,
  val authorName: String,
  val authorBadge: String? = null,
  val content: String,
  val timestamp: String,
  val likesCount: Int,
  val commentsCount: Int,
  val isLikedByMe: Boolean = false,
  val isPinned: Boolean = false,
  val isFlagged: Boolean = false
)

data class Community(
  val id: String,
  val name: String,
  val category: String,
  val emoji: String,
  val description: String,
  val memberCount: Int,
  val isJoined: Boolean = false,
  val rules: List<String>,
  val pinnedPost: CommunityPost? = null,
  val gradientStart: Long,
  val gradientEnd: Long,
  val posts: List<CommunityPost> = emptyList()
)

data class LiveLocationState(
  val isSharing: Boolean = false,
  val sharedWithMatchName: String? = null,
  val isSharedWithTrustedContact: Boolean = false,
  val remainingMinutes: Int = 0,
  val approximateArea: String = "Gulberg III, Lahore"
)

data class PrivacySettings(
  val hideLastSeen: Boolean = false,
  val hideOnlineStatus: Boolean = false,
  val whoCanMessageMe: String = "Mutual Matches",
  val whoCanSeeStories: String = "Matches Only",
  val locationSharingOptIn: Boolean = true,
  val ghostModeEnabled: Boolean = false,
  val askConsentBeforePhotos: Boolean = true
)

data class DateIdea(
  val id: String,
  val title: String,
  val emoji: String,
  val category: String,
  val description: String,
  val estimatedTime: String,
  val vibe: String
)

data class ScheduledDate(
  val id: String,
  val matchName: String,
  val matchInitials: String,
  val matchColorStart: Long,
  val matchColorEnd: Long,
  val title: String,
  val emoji: String,
  val dateFormatted: String,
  val timeFormatted: String,
  val location: String,
  val isPublicPlaceConfirmed: Boolean = true,
  val trustedContactShared: Boolean = true
)

data class QuizOption(
  val label: String,
  val explanation: String,
  val scoreWeights: Map<String, Int>
)

data class QuizQuestion(
  val id: Int,
  val category: String,
  val question: String,
  val options: List<QuizOption>,
  val selectedIndex: Int? = null
)

data class CompatibilityScoreSummary(
  val valuesMatch: Int = 94,
  val communicationMatch: Int = 88,
  val familyLifeMatch: Int = 92,
  val romanceVibeMatch: Int = 96,
  val lifeGoalsMatch: Int = 90,
  val topLoveLanguage: String = "Words of Affirmation & Quality Time",
  val idealPartnerSummary: String = "Someone who balances ambition with emotional gentleness, values heartfelt family dinners, loves quiet conversations over tea, and approaches life as a lifelong companionship."
)

data class SafetySettings(
  val ageConfirmed18Plus: Boolean = true,
  val phoneVerified: Boolean = true,
  val selfieLivenessVerified: Boolean = true,
  val selfieCaptured: Boolean = false,
  val idVerified: Boolean = false,
  val isIncognitoHidden: Boolean = false,
  val blurPhotosUntilMatched: Boolean = true,
  val isAccountPaused: Boolean = false,
  val askConsentBeforePhotos: Boolean = true,
  val privacy: PrivacySettings = PrivacySettings(),
  val blockedUserNames: List<String> = listOf("Spam_Bot_92")
)

data class ReportSubmission(
  val id: String = "",
  val reportedUserName: String,
  val reason: String,
  val details: String = "",
  val timestamp: Long = System.currentTimeMillis()
)

data class DailyBloomsCounter(
  val maxDailyBlooms: Int = 5,
  val remainingBlooms: Int = 4,
  val resetHoursRemaining: Int = 8
)

data class ProfileBoostStatus(
  val isBoosted: Boolean = false,
  val remainingMinutes: Int = 0,
  val multiplier: String = "3x"
)
