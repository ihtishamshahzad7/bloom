

enum class CommunityRole { OWNER, MODERATOR, MEMBER }
enum class EventRsvpStatus { GOING, INTERESTED, DECLINED }
enum class ModerationStatus { PENDING, APPROVED, REJECTED, RESOLVED }
enum class SearchCategory { PEOPLE, HASHTAGS, COMMUNITIES, EVENTS }
enum class NotificationChannel { LIKES, COMMENTS, MATCHES, STREAKS, EVENTS, STORY_VIEWS, PUSH }
enum class PremiumPlanTier { MONTHLY, ANNUAL }

data class CommunityMembership(
    val communityId: String = "",
    val role: CommunityRole = CommunityRole.MEMBER,
    val isMuted: Boolean = false,
    val isBanned: Boolean = false,
    val joinedAt: Long = System.currentTimeMillis()
)

data class CommunityEvent(
    val id: String = "",
    val communityId: String = "",
    val title: String = "",
    val description: String = "",
    val venue: String = "",
    val startsAt: Long = System.currentTimeMillis(),
    val capacity: Int = 0,
    val rsvpCount: Int = 0,
    val rsvpStatus: EventRsvpStatus = EventRsvpStatus.INTERESTED,
    val verifiedOnly: Boolean = true,
    val remindersSent: Boolean = false,
    val isReported: Boolean = false,
    val createdBy: String = ""
)

data class RecentSearch(
    val query: String = "",
    val category: SearchCategory = SearchCategory.PEOPLE,
    val searchedAt: Long = System.currentTimeMillis()
)

data class PremiumPlan(
    val id: String = "monthly",
    val tier: PremiumPlanTier = PremiumPlanTier.MONTHLY,
    val priceLabel: String = "$9.99",
    val title: String = "Bloom Gold",
    val features: List<String> = listOf("Streak Freeze", "Boost", "Rose gifts")
)

data class RoseGift(
    val id: String = "",
    val recipientId: String = "",
    val senderId: String = "",
    val roseCount: Int = 1,
    val sentAt: Long = System.currentTimeMillis(),
    val message: String = "Blooming together"
)

data class ModerationQueueItem(
    val id: String = "",
    val targetType: String = "post",
    val targetId: String = "",
    val reportedBy: String = "",
    val reason: String = "",
    val details: String = "",
    val status: ModerationStatus = ModerationStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
)

data class AdminAuditLog(
    val id: String = "",
    val action: String = "",
    val actorUid: String = "",
    val targetType: String = "",
    val targetId: String = "",
    val details: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class NotificationPreference(
    val channel: NotificationChannel = NotificationChannel.PUSH,
    val enabled: Boolean = true,
    val quietHoursEnabled: Boolean = false,
    val quietHoursStart: String = "22:00",
    val quietHoursEnd: String = "08:00"
)

data class FeatureFlags(
    val dailyBloomLimit: Int = 5,
    val matchExpiryHours: Int = 48,
    val communityApprovalRequired: Boolean = false,
    val eventsEnabled: Boolean = true,
    val premiumGiftsEnabled: Boolean = true
)

data class VerificationStatus(
    val status: String = "unverified",
    val selfiePassed: Boolean = false,
    val idUploaded: Boolean = false,
    val adminReviewedBy: String? = null,
    val rejectedReason: String? = null
)

data class PurchaseRecord(
    val id: String = "",
    val planId: String = "monthly",
    val amount: String = "$9.99",
    val purchasedAt: Long = System.currentTimeMillis(),
    val entitlement: String = "gold_monthly"
)

