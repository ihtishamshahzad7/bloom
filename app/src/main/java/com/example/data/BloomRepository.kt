package com.example.data

import android.util.Log
import com.example.model.*
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BloomRepository(
  private val firestore: FirebaseFirestore? = try { Firebase.firestore } catch (e: Throwable) { null },
  private val auth: FirebaseAuth? = try { Firebase.auth } catch (e: Throwable) { null }
) {

  private val coroutineScope = CoroutineScope(Dispatchers.IO)

  // In-memory state and fallback cache
  private val _profiles = MutableStateFlow(createInitialProfiles())
  val profiles: StateFlow<List<UserProfile>> = _profiles.asStateFlow()

  private val _messages = MutableStateFlow(createInitialMessages())
  val messages: StateFlow<Map<String, List<ChatMessage>>> = _messages.asStateFlow()

  private val _scheduledDates = MutableStateFlow(createInitialDates())
  val scheduledDates: StateFlow<List<ScheduledDate>> = _scheduledDates.asStateFlow()

  private val _quizQuestions = MutableStateFlow(createInitialQuestions())
  val quizQuestions: StateFlow<List<QuizQuestion>> = _quizQuestions.asStateFlow()

  private val _safetySettings = MutableStateFlow(SafetySettings())
  val safetySettings: StateFlow<SafetySettings> = _safetySettings.asStateFlow()

  private val _dailyBlooms = MutableStateFlow(DailyBloomsCounter())
  val dailyBlooms: StateFlow<DailyBloomsCounter> = _dailyBlooms.asStateFlow()

  private val _boostStatus = MutableStateFlow(ProfileBoostStatus())
  val boostStatus: StateFlow<ProfileBoostStatus> = _boostStatus.asStateFlow()

  private val _isPremium = MutableStateFlow(false)
  val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

  // Stories & Status state
  private val _stories = MutableStateFlow(createInitialStories())
  val stories: StateFlow<List<StoryItem>> = _stories.asStateFlow()

  // Nearby & Live Location state
  private val _nearbyPeople = MutableStateFlow(createInitialNearbyPeople())
  val nearbyPeople: StateFlow<List<NearbyPerson>> = _nearbyPeople.asStateFlow()

  private val _liveLocation = MutableStateFlow(LiveLocationState())
  val liveLocation: StateFlow<LiveLocationState> = _liveLocation.asStateFlow()

  // Friend Suggestions state
  private val _friendSuggestions = MutableStateFlow(createInitialFriendSuggestions())
  val friendSuggestions: StateFlow<List<FriendSuggestion>> = _friendSuggestions.asStateFlow()

  // Communities & Groups state
  private val _communities = MutableStateFlow(createInitialCommunities())
  val communities: StateFlow<List<Community>> = _communities.asStateFlow()

  private val _groupChats = MutableStateFlow(createInitialGroupChats())
  val groupChats: StateFlow<List<GroupChat>> = _groupChats.asStateFlow()

  // Live typing / recording indicators per chat
  private val _typingStatus = MutableStateFlow<Map<String, Boolean>>(emptyMap())
  val typingStatus: StateFlow<Map<String, Boolean>> = _typingStatus.asStateFlow()

  private val _recordingStatus = MutableStateFlow<Map<String, Boolean>>(emptyMap())
  val recordingStatus: StateFlow<Map<String, Boolean>> = _recordingStatus.asStateFlow()

  // Social Network state: Feed, Notifications, Trending, User Profile
  private val _feedPosts = MutableStateFlow(createInitialFeedPosts())
  val feedPosts: StateFlow<List<FeedPost>> = _feedPosts.asStateFlow()

  private val _trendingHashtags = MutableStateFlow(createInitialTrendingHashtags())
  val trendingHashtags: StateFlow<List<TrendingHashtag>> = _trendingHashtags.asStateFlow()

  private val _notifications = MutableStateFlow(createInitialNotifications())
  val notifications: StateFlow<List<SocialNotification>> = _notifications.asStateFlow()

  private val _userSocialProfile = MutableStateFlow(createInitialSocialProfile())
  val userSocialProfile: StateFlow<UserSocialProfile> = _userSocialProfile.asStateFlow()

  private val _hiddenPostIds = MutableStateFlow<Set<String>>(emptySet())
  val hiddenPostIds: StateFlow<Set<String>> = _hiddenPostIds.asStateFlow()

  // Rate Limiting tracker: timestamps of recently sent messages
  private val recentMessageTimestamps = mutableListOf<Long>()

  init {
    initRealtimeFirestoreListeners()
  }

  private fun initRealtimeFirestoreListeners() {
    val db = firestore ?: return
    try {
      db.collection("users")
        .whereGreaterThanOrEqualTo("age", 18)
        .addSnapshotListener { snapshot, error ->
          if (error != null) {
            Log.w("BloomRepo", "Profiles listener error, local fallback active: ${error.message}")
            return@addSnapshotListener
          }
          if (snapshot != null && !snapshot.isEmpty) {
            val remoteProfiles = snapshot.documents.mapNotNull { doc ->
              val name = doc.getString("name") ?: return@mapNotNull null
              UserProfile(
                id = doc.id,
                name = name,
                nickname = doc.getString("nickname") ?: name,
                age = (doc.getLong("age") ?: 25).toInt(),
                city = doc.getString("city") ?: "Lahore",
                distanceMiles = (doc.getLong("distanceMiles") ?: 5).toInt(),
                education = doc.getString("education") ?: "University Graduate",
                occupation = doc.getString("occupation") ?: "Professional",
                religionCommunity = doc.getString("religionCommunity") ?: "Family-Oriented",
                intentions = doc.getString("intentions") ?: "Serious relationship / Marriage-minded",
                bio = doc.getString("bio") ?: "",
                statusLine = doc.getString("statusLine") ?: "Finding joy in the quiet moments 🌸",
                compatibilityPercent = (doc.getLong("compatibilityPercent") ?: 90).toInt(),
                isVerified = doc.getBoolean("verified") ?: true,
                adminApproved = doc.getBoolean("adminApproved") ?: true,
                interestChips = (doc.get("interests") as? List<*>)?.mapNotNull { it?.toString() } ?: listOf("☕ Chai", "📚 Reading"),
                values = (doc.get("values") as? List<*>)?.mapNotNull { it?.toString() } ?: listOf("Faith", "Family"),
                loveLanguage = doc.getString("loveLanguage") ?: "Quality Time",
                initials = name.take(2).uppercase()
              )
            }
            if (remoteProfiles.isNotEmpty()) {
              _profiles.update { current ->
                val blocked = _safetySettings.value.blockedUserNames
                (remoteProfiles + current).distinctBy { it.id }.filterNot { it.name in blocked }
              }
            }
          }
        }
    } catch (e: Throwable) {
      Log.d("BloomRepo", "Firestore running local fallback: ${e.message}")
    }
  }

  val dateIdeas: List<DateIdea> = listOf(
    DateIdea("1", "Artisan Coffee & Botanical Walk", "☕", "Casual & Warm", "Meet at an airy glasshouse café followed by an afternoon stroll surrounded by orchids.", "1.5 hours", "Public & Relaxed"),
    DateIdea("2", "Historic Bookstore Browse", "📚", "Quiet & Thoughtful", "Explore cozy aisles of classic literature, exchange book recommendations, and discuss favorite authors.", "2 hours", "Public & Cozy"),
    DateIdea("3", "Golden Hour Lake Picnic", "🌅", "Romantic Nature", "Soft blanket, seasonal fruits, warm cardamom tea, and scenic sunset views by the marina water.", "2.5 hours", "Scenic Public Park"),
    DateIdea("4", "Contemporary Art Gallery", "🎨", "Cultural & Inspiring", "Stroll through quiet exhibition halls, appreciating modern sculptures and discussing life perspectives.", "2 hours", "Museum / Gallery"),
    DateIdea("5", "Candlelit Garden Dinner", "🕯️", "Intimate & Elegant", "A quiet corner table under fairy lights with acoustic strings and Mediterranean cuisine.", "3 hours", "Reserved Restaurant")
  )

  val stickerPacks: List<StickerPack> = listOf(
    StickerPack(
      id = "bloom_roses",
      name = "Bloom Roses",
      coverEmoji = "🌹",
      stickers = listOf(
        StickerItem("s1", "Blooming Rose", "🌹", "کھلتا گلاب"),
        StickerItem("s2", "Warm Bouquet", "💐", "خوبصورت گلدستہ"),
        StickerItem("s3", "Cherry Blossom", "🌸", "بہار کا پھول"),
        StickerItem("s4", "Gentle Hibiscus", "🌺", "محبت کا اظہار"),
        StickerItem("s5", "Petal Flutter", "🥀", "شائستہ جذبات")
      )
    ),
    StickerPack(
      id = "cute_couples",
      name = "Cute Couples",
      coverEmoji = "💑",
      stickers = listOf(
        StickerItem("c1", "Stargazing", "✨", "تاروں کی چھاؤں"),
        StickerItem("c2", "Chai for Two", "☕", "شام کی گرم چائے"),
        StickerItem("c3", "Peace & Love", "🕊️", "امن و سکون"),
        StickerItem("c4", "Forever Vow", "💍", "ہمیشہ کا ساتھ")
      )
    ),
    StickerPack(
      id = "urdu_phrases",
      name = "Urdu Phrases",
      coverEmoji = "📜",
      stickers = listOf(
        StickerItem("u1", "Salam 🌸", "🤲", "السلام علیکم"),
        StickerItem("u2", "Shukriya 💕", "🙏", "بہت شکریہ"),
        StickerItem("u3", "MashaAllah ✨", "🌟", "ماشاءاللہ"),
        StickerItem("u4", "Jaan 🕊️", "💖", "میری جان"),
        StickerItem("u5", "Dil Se ☕", "🫀", "تہہ دل سے")
      )
    ),
    StickerPack(
      id = "candle_love",
      name = "Candle Love",
      coverEmoji = "🕯️",
      stickers = listOf(
        StickerItem("l1", "Warm Hearth", "🕯️", "شمع کی گرمی"),
        StickerItem("l2", "Evening Light", "🕯️❤️", "محبت کی شمع"),
        StickerItem("l3", "Pure Radiance", "🕯️✨", "پرنور محفل")
      )
    )
  )

  fun likeProfile(profileId: String): Boolean {
    if (_dailyBlooms.value.remainingBlooms > 0) {
      _dailyBlooms.update { it.copy(remainingBlooms = it.remainingBlooms - 1) }
    }

    var wasMutual = false
    _profiles.update { list ->
      list.map { profile ->
        if (profile.id == profileId) {
          val becomeMutual = profile.id == "p_ayaan" || profile.id == "p_sofia" || !profile.isMutualMatch
          wasMutual = becomeMutual
          profile.copy(isLiked = true, isMutualMatch = becomeMutual)
        } else profile
      }
    }

    val db = firestore
    val currentUserId = auth?.currentUser?.uid ?: "current_user_mia"
    if (db != null) {
      try {
        val likeData = hashMapOf(
          "fromUserId" to currentUserId,
          "toUserId" to profileId,
          "timestamp" to System.currentTimeMillis()
        )
        db.collection("likes").add(likeData)

        if (wasMutual) {
          val matchData = hashMapOf(
            "users" to listOf(currentUserId, profileId),
            "createdAt" to System.currentTimeMillis(),
            "compatibilityScore" to 94
          )
          db.collection("matches").document("match_${currentUserId}_$profileId").set(matchData)
        }
      } catch (e: Throwable) {
        Log.d("BloomRepo", "Offline like stored locally: ${e.message}")
      }
    }

    return wasMutual
  }

  fun passProfile(profileId: String) {
    if (_dailyBlooms.value.remainingBlooms > 0) {
      _dailyBlooms.update { it.copy(remainingBlooms = it.remainingBlooms - 1) }
    }
    _profiles.update { list ->
      list.filterNot { it.id == profileId }
    }
  }

  fun toggleSaveProfile(profileId: String) {
    _profiles.update { list ->
      list.map { if (it.id == profileId) it.copy(isSaved = !it.isSaved) else it }
    }
  }

  fun sendMessage(matchId: String, text: String, senderName: String = "Mia"): String? {
    val newMsg = ChatMessage(
      id = "m_${System.currentTimeMillis()}",
      matchId = matchId,
      senderName = senderName,
      text = text,
      timestamp = "Just now",
      isMine = true,
      isRead = false,
      isDelivered = true,
      messageType = MessageType.TEXT
    )
    return sendRichMessage(matchId, newMsg)
  }

  fun sendRichMessage(matchId: String, message: ChatMessage): String? {
    val now = System.currentTimeMillis()
    synchronized(recentMessageTimestamps) {
      recentMessageTimestamps.removeAll { now - it > 10000L }
      if (recentMessageTimestamps.size >= 5) {
        return "⚠️ Rate limit reached: Please slow down to maintain thoughtful conversation."
      }
      recentMessageTimestamps.add(now)
    }

    // Auto-moderation and safety keywords check
    val text = message.text
    val warning = when {
      text.contains("send money", ignoreCase = true) || text.contains("wire cash", ignoreCase = true) ||
      text.contains("crypto", ignoreCase = true) || text.contains("paypal", ignoreCase = true) ||
      text.contains("cashapp", ignoreCase = true) ->
        "⚠️ Safety Alert: Never transfer funds or share payment handles with matches."
      text.contains("password", ignoreCase = true) || text.contains("otp", ignoreCase = true) ->
        "⚠️ Privacy Alert: Never reveal security codes or passwords."
      Regex("\\b\\d{10,}\\b").containsMatchIn(text) ->
        "💡 Note: For your protection, keep discussions within Bloom during early courtship."
      else -> null
    }

    val finalMsg = message.copy(
      safetyAlert = warning,
      timestamp = if (message.timestamp.isEmpty()) "Just now" else message.timestamp
    )

    _messages.update { current ->
      val thread = current[matchId]?.toMutableList() ?: mutableListOf()
      thread.add(finalMsg)
      current + (matchId to thread)
    }

    // Update streak activity for this match
    _profiles.update { list ->
      list.map { profile ->
        if (profile.id == matchId) {
          val s = profile.bloomStreak
          profile.copy(
            bloomStreak = s.copy(
              currentStreakDays = if (s.currentStreakDays == 0) 1 else s.currentStreakDays,
              lastMessageTimestamp = now,
              hoursRemaining = 24,
              isExpiringSoon = false
            )
          )
        } else profile
      }
    }

    return null
  }

  fun addReaction(matchId: String, messageId: String, emoji: String) {
    _messages.update { current ->
      val thread = current[matchId]?.map { msg ->
        if (msg.id == messageId) msg.copy(reaction = if (msg.reaction == emoji) null else emoji) else msg
      } ?: emptyList()
      current + (matchId to thread)
    }
  }

  fun deleteMessage(matchId: String, messageId: String, deleteForEveryone: Boolean) {
    _messages.update { current ->
      val thread = current[matchId] ?: emptyList()
      val updated = if (deleteForEveryone) {
        thread.map { msg ->
          if (msg.id == messageId) {
            msg.copy(
              isDeletedForEveryone = true,
              text = "This message was deleted",
              messageType = MessageType.TEXT,
              mediaUrl = null
            )
          } else msg
        }
      } else {
        thread.filterNot { it.id == messageId }
      }
      current + (matchId to updated)
    }
  }

  fun markDisappearingMediaViewed(matchId: String, messageId: String) {
    _messages.update { current ->
      val thread = current[matchId]?.map { msg ->
        if (msg.id == messageId) msg.copy(hasBeenViewed = true) else msg
      } ?: emptyList()
      current + (matchId to thread)
    }
  }

  fun grantMediaConsent(matchId: String, messageId: String) {
    _messages.update { current ->
      val thread = current[matchId]?.map { msg ->
        if (msg.id == messageId) msg.copy(mediaConsentGranted = true) else msg
      } ?: emptyList()
      current + (matchId to thread)
    }
  }

  fun setTyping(matchId: String, isTyping: Boolean) {
    _typingStatus.update { it + (matchId to isTyping) }
  }

  fun setRecording(matchId: String, isRecording: Boolean) {
    _recordingStatus.update { it + (matchId to isRecording) }
  }

  fun freezeStreak(matchId: String) {
    _profiles.update { list ->
      list.map { profile ->
        if (profile.id == matchId) {
          profile.copy(
            bloomStreak = profile.bloomStreak.copy(
              streakFreezeActive = true,
              hoursRemaining = 48,
              isExpiringSoon = false
            )
          )
        } else profile
      }
    }
  }

  // Stories & Status actions
  fun addStory(item: StoryItem) {
    _stories.update { listOf(item) + it }
  }

  fun recordStoryView(storyId: String, viewerName: String = "Mia") {
    _stories.update { list ->
      list.map { story ->
        if (story.id == storyId) {
          val alreadyIn = story.viewersList.any { it.userName == viewerName }
          if (alreadyIn) {
            story.copy(isViewedByMe = true)
          } else {
            val viewer = StoryViewer(
              userId = "u_current",
              userName = viewerName,
              userInitials = viewerName.take(2).uppercase(),
              avatarColorStart = 0xFFF28DB0,
              avatarColorEnd = 0xFFB79CF0,
              viewedAt = "Just now"
            )
            story.copy(
              isViewedByMe = true,
              viewsCount = story.viewsCount + 1,
              viewersList = listOf(viewer) + story.viewersList
            )
          }
        } else story
      }
    }
  }

  fun reportStory(storyId: String, reason: String) {
    _stories.update { list ->
      list.map { if (it.id == storyId) it.copy(isFlagged = true) else it }
    }
    reportAndBlockUser("Story_$storyId", reason, "Reported story content flagged by user.")
  }

  // Live Location
  fun startLiveLocationSharing(matchName: String, durationMinutes: Int, withTrustedContact: Boolean) {
    _liveLocation.value = LiveLocationState(
      isSharing = true,
      sharedWithMatchName = matchName,
      isSharedWithTrustedContact = withTrustedContact,
      remainingMinutes = durationMinutes,
      approximateArea = "Gulberg III, Lahore (~3 km radius)"
    )
  }

  fun stopLiveLocationSharing() {
    _liveLocation.value = LiveLocationState(isSharing = false)
  }

  // Friend Suggestions
  fun sendFriendRequest(id: String) {
    _friendSuggestions.update { list ->
      list.map { if (it.id == id) it.copy(status = FriendRequestStatus.SENT) else it }
    }
  }

  fun acceptFriendRequest(id: String) {
    _friendSuggestions.update { list ->
      list.map { if (it.id == id) it.copy(status = FriendRequestStatus.ACCEPTED) else it }
    }
  }

  fun declineFriendRequest(id: String) {
    _friendSuggestions.update { list ->
      list.map { if (it.id == id) it.copy(status = FriendRequestStatus.DECLINED) else it }
    }
  }

  fun removeFriendSuggestion(id: String) {
    _friendSuggestions.update { list ->
      list.filterNot { it.id == id }
    }
  }

  // Communities & Groups
  fun toggleJoinCommunity(communityId: String) {
    _communities.update { list ->
      list.map { if (it.id == communityId) it.copy(isJoined = !it.isJoined) else it }
    }
  }

  fun createCommunityPost(communityId: String, content: String) {
    val newPost = CommunityPost(
      id = "post_${System.currentTimeMillis()}",
      communityId = communityId,
      authorId = "u_mia",
      authorName = "Mia",
      authorBadge = "Member 🌸",
      content = content,
      timestamp = "Just now",
      likesCount = 1,
      commentsCount = 0,
      isLikedByMe = true
    )
    _communities.update { list ->
      list.map { comm ->
        if (comm.id == communityId) {
          comm.copy(posts = listOf(newPost) + comm.posts)
        } else comm
      }
    }
  }

  fun likeCommunityPost(communityId: String, postId: String) {
    _communities.update { list ->
      list.map { comm ->
        if (comm.id == communityId) {
          val updatedPosts = comm.posts.map { post ->
            if (post.id == postId) {
              val newLiked = !post.isLikedByMe
              post.copy(
                isLikedByMe = newLiked,
                likesCount = if (newLiked) post.likesCount + 1 else post.likesCount - 1
              )
            } else post
          }
          comm.copy(posts = updatedPosts)
        } else comm
      }
    }
  }

  fun reportCommunityPost(communityId: String, postId: String, reason: String) {
    _communities.update { list ->
      list.map { comm ->
        if (comm.id == communityId) {
          val updatedPosts = comm.posts.map { if (it.id == postId) it.copy(isFlagged = true) else it }
          comm.copy(posts = updatedPosts)
        } else comm
      }
    }
    reportAndBlockUser("Post_$postId", reason, "Reported community post in $communityId")
  }

  fun createGroupChat(name: String, description: String, iconEmoji: String, members: List<String>) {
    val newGroup = GroupChat(
      id = "group_${System.currentTimeMillis()}",
      name = name,
      description = description,
      membersCount = members.size + 1,
      adminIds = listOf("u_mia"),
      memberNames = listOf("Mia (You)") + members,
      iconEmoji = iconEmoji,
      gradientStart = 0xFFF28DB0,
      gradientEnd = 0xFF9E77ED,
      lastMessageText = "Group created! Welcome to our safe space 🌸",
      lastMessageTime = "Just now",
      unreadCount = 0,
      activePoll = GroupPoll(
        id = "poll_welcome",
        question = "What should our first meetup activity be?",
        options = listOf(
          PollOption(1, "Artisan Chai & Poetry", 3),
          PollOption(2, "Botanical Garden Walk", 5),
          PollOption(3, "Virtual Book Discussion", 2)
        ),
        totalVotes = 10
      )
    )
    _groupChats.update { listOf(newGroup) + it }
  }

  fun voteGroupPoll(groupId: String, optionIndex: Int) {
    _groupChats.update { list ->
      list.map { group ->
        if (group.id == groupId && group.activePoll != null) {
          val poll = group.activePoll
          val updatedOptions = poll.options.mapIndexed { idx, opt ->
            if (idx == optionIndex) opt.copy(voteCount = opt.voteCount + 1) else opt
          }
          group.copy(
            activePoll = poll.copy(
              options = updatedOptions,
              totalVotes = poll.totalVotes + 1,
              hasVoted = true,
              userVotedOptionIndex = optionIndex
            )
          )
        } else group
      }
    }
  }

  fun togglePhotoConsent(profileId: String) {
    _profiles.update { list ->
      list.map { if (it.id == profileId) it.copy(photoConsentApproved = !it.photoConsentApproved) else it }
    }
  }

  fun proposeDate(match: UserProfile, idea: DateIdea, dateStr: String, timeStr: String) {
    val newDate = ScheduledDate(
      id = "d_${System.currentTimeMillis()}",
      matchName = match.name,
      matchInitials = match.initials,
      matchColorStart = match.avatarColorStart,
      matchColorEnd = match.avatarColorEnd,
      title = idea.title,
      emoji = idea.emoji,
      dateFormatted = dateStr,
      timeFormatted = timeStr,
      location = idea.description.take(30) + " (Public)",
      isPublicPlaceConfirmed = true,
      trustedContactShared = true
    )
    _scheduledDates.update { listOf(newDate) + it }
  }

  fun answerQuizQuestion(questionId: Int, optionIndex: Int) {
    _quizQuestions.update { list ->
      list.map { if (it.id == questionId) it.copy(selectedIndex = optionIndex) else it }
    }
  }

  fun updateSafety(transform: (SafetySettings) -> SafetySettings) {
    _safetySettings.update { transform(it) }
  }

  fun updatePrivacy(transform: (PrivacySettings) -> PrivacySettings) {
    _safetySettings.update { current ->
      current.copy(privacy = transform(current.privacy))
    }
  }

  fun submitSelfieVerification() {
    _safetySettings.update {
      it.copy(
        selfieCaptured = true,
        selfieLivenessVerified = true
      )
    }
    val currentUserId = auth?.currentUser?.uid ?: "current_user_mia"
    firestore?.collection("verification_requests")?.document(currentUserId)?.set(
      hashMapOf(
        "userId" to currentUserId,
        "selfieCaptured" to true,
        "adminApproved" to false,
        "submittedAt" to System.currentTimeMillis()
      )
    )
  }

  fun reportAndBlockUser(userName: String, reason: String, details: String = "") {
    _safetySettings.update {
      it.copy(blockedUserNames = it.blockedUserNames + userName)
    }
    _profiles.update { list ->
      list.filterNot { it.name == userName }
    }
    val report = ReportSubmission(
      id = "rep_${System.currentTimeMillis()}",
      reportedUserName = userName,
      reason = reason,
      details = details,
      timestamp = System.currentTimeMillis()
    )
    firestore?.collection("reports")?.add(report)
  }

  fun exportUserDataJson(): String {
    val profile = _profiles.value.firstOrNull()
    return """
      {
        "app": "Bloom Matchmaking",
        "exportedAt": "${System.currentTimeMillis()}",
        "profile": {
          "name": "Mia",
          "nickname": "Mia 🌸",
          "intentions": "Marriage-minded & safe",
          "verified": true,
          "privacy": {
            "hideLastSeen": ${_safetySettings.value.privacy.hideLastSeen},
            "hideOnlineStatus": ${_safetySettings.value.privacy.hideOnlineStatus},
            "ghostMode": ${_safetySettings.value.privacy.ghostModeEnabled}
          }
        },
        "chatThreads": ${_messages.value.size},
        "activeStreaks": ${_profiles.value.count { it.bloomStreak.currentStreakDays > 0 }},
        "communitiesJoined": ${_communities.value.count { it.isJoined }}
      }
    """.trimIndent()
  }

  fun deleteUserAccountAndData() {
    _profiles.value = emptyList()
    _messages.value = emptyMap()
    _scheduledDates.value = emptyList()
    _stories.value = emptyList()
    _groupChats.value = emptyList()
    _safetySettings.value = SafetySettings(phoneVerified = false, selfieLivenessVerified = false)

    val currentUserId = auth?.currentUser?.uid
    if (currentUserId != null) {
      firestore?.collection("users")?.document(currentUserId)?.delete()
      auth?.currentUser?.delete()
    }
  }

  fun boostProfile() {
    _boostStatus.value = ProfileBoostStatus(
      isBoosted = true,
      remainingMinutes = 60,
      multiplier = "3x"
    )
  }

  fun setPremium(premium: Boolean) {
    _isPremium.value = premium
  }

  private fun createInitialProfiles(): List<UserProfile> = listOf(
    UserProfile(
      id = "p_ayaan",
      name = "Ayaan Malik",
      nickname = "Ayaan 🌸",
      age = 26,
      city = "Lahore, Punjab",
      distanceMiles = 3,
      education = "M.S. Software Engineering, LUMS",
      occupation = "Product Architect & Writer",
      religionCommunity = "Sunni / Traditional & Progressive Harmony",
      intentions = "Marriage within 12–18 months",
      bio = "Balancing architectural design with quiet Sunday mornings, classical Urdu poetry, and family Sunday lunches. Seeking someone who finds beauty in sincere conversations, shares deep spiritual grounding, and laughs easily.",
      statusLine = "Reading Faiz Ahmed Faiz over afternoon chai ☕",
      compatibilityPercent = 94,
      isVerified = true,
      adminApproved = true,
      isOnline = true,
      lastSeen = "Active now",
      badges = listOf("Verified 🛡️", "Streak 14🔥", "Early Bloomer 🌱✨"),
      interestChips = listOf("☕ Specialty Coffee", "📚 Classic Literature", "✈️ Mountain Treks", "🎨 Calligraphy"),
      values = listOf("Faith & Integrity", "Devotion to Family", "Mutual Respect", "Gentle Communication"),
      loveLanguage = "Words of Affirmation & Quality Time",
      avatarColorStart = 0xFFF28DB0,
      avatarColorEnd = 0xFF9E77ED,
      initials = "AM",
      promptQuestion = "What a peaceful lifelong home feels like...",
      promptAnswer = "Where both voices soften in disagreement, where morning prayers bring tranquility, and where chai is always shared with attentive presence.",
      isLiked = true,
      isMutualMatch = true,
      bloomStreak = BloomStreak("p_ayaan", currentStreakDays = 14, hoursRemaining = 6, isExpiringSoon = false, milestoneCelebrated = 14)
    ),
    UserProfile(
      id = "p_sofia",
      name = "Sofia Zahra",
      nickname = "Sofia 🕊️",
      age = 24,
      city = "Islamabad, Federal",
      distanceMiles = 8,
      education = "B.Arch Architecture, NUST",
      occupation = "Urban Heritage Designer",
      religionCommunity = "Spiritual & Values-Driven",
      intentions = "Lifelong partnership built on empathy and mutual support",
      bio = "Passionate about preserving historic courtyards, sketching heritage botanical arches, and baking sourdough bread for loved ones on rainy days.",
      statusLine = "Sketching the arches of old Havelis 🎨",
      compatibilityPercent = 91,
      isVerified = true,
      adminApproved = true,
      isOnline = true,
      lastSeen = "Active now",
      badges = listOf("Verified 🛡️", "Streak 7🔥"),
      interestChips = listOf("🏛️ Heritage Art", "🌱 Indoor Plants", "📖 History", "☕ Masala Chai"),
      values = listOf("Kindness as Strength", "Intellectual Curiosity", "Spiritual Balance"),
      loveLanguage = "Acts of Service",
      avatarColorStart = 0xFFFF9BB3,
      avatarColorEnd = 0xFFFFD3C2,
      initials = "SZ",
      promptQuestion = "The gesture that touches my heart most...",
      promptAnswer = "Remembering how I take my morning tea, and giving full attention when we sit together.",
      isLiked = true,
      isMutualMatch = true,
      bloomStreak = BloomStreak("p_sofia", currentStreakDays = 7, hoursRemaining = 3, isExpiringSoon = true, milestoneCelebrated = 7)
    ),
    UserProfile(
      id = "p_danyal",
      name = "Danyal Khan",
      nickname = "Danyal ✈️",
      age = 27,
      city = "Karachi, Sindh",
      distanceMiles = 12,
      education = "B.S. Biomedical Sciences, AKU",
      occupation = "Medical Researcher",
      religionCommunity = "Traditional Family Values",
      intentions = "Serious commitment leading to marriage",
      bio = "Invested in healthcare equity and pediatric oncology. In free hours, I train for distance running and enjoy ocean walks at sunset with fresh coconut water.",
      statusLine = "Sunset walk by the Arabian Sea 🌅",
      compatibilityPercent = 88,
      isVerified = true,
      adminApproved = true,
      isOnline = false,
      lastSeen = "Last seen 20m ago",
      badges = listOf("Verified 🛡️"),
      interestChips = listOf("🏃 Distance Running", "🌊 Sea Breeze", "🔬 Science", "🥗 Healthy Cooking"),
      values = listOf("Patience", "Empathy", "Family Prioritization"),
      loveLanguage = "Quality Time",
      avatarColorStart = 0xFF9E77ED,
      avatarColorEnd = 0xFF6172F3,
      initials = "DK",
      promptQuestion = "Together, our ideal weekend is...",
      promptAnswer = "Volunteering together on Saturday morning, followed by trying a new cozy bookstore café.",
      bloomStreak = BloomStreak("p_danyal", currentStreakDays = 3, hoursRemaining = 18)
    ),
    UserProfile(
      id = "p_zainab",
      name = "Zainab Tariq",
      nickname = "Zainab 🌸",
      age = 25,
      city = "Lahore, Punjab",
      distanceMiles = 5,
      education = "M.Ed Education & Child Development",
      occupation = "Early Education Curriculum Specialist",
      religionCommunity = "Values-Centered",
      intentions = "Marriage-minded partnership",
      bio = "Creating inspiring learning environments for children. Loving poetry recitations, watercolor landscapes, and home-cooked family feasts.",
      statusLine = "Baking cardamom biscuits for the weekend 🍪",
      compatibilityPercent = 86,
      isVerified = true,
      adminApproved = true,
      isOnline = true,
      lastSeen = "Active now",
      badges = listOf("Verified 🛡️", "Early Bloomer 🌱✨"),
      interestChips = listOf("🎨 Watercolors", "👩‍🏫 Child Development", "🌸 Gardening", "🍰 Baking"),
      values = listOf("Gentleness", "Compassion", "Sincerity"),
      loveLanguage = "Receiving Gifts of Meaning",
      avatarColorStart = 0xFFFF8FA3,
      avatarColorEnd = 0xFFCDB8F0,
      initials = "ZT",
      promptQuestion = "What makes love endure across decades...",
      promptAnswer = "Choosing each other every single morning, through mundane days and celebrated milestones alike."
    )
  )

  private fun createInitialMessages(): Map<String, List<ChatMessage>> = mapOf(
    "p_ayaan" to listOf(
      ChatMessage(
        id = "m1",
        matchId = "p_ayaan",
        senderName = "Ayaan Malik",
        text = "Assalam-o-Alaikum Mia! It was so heartwarming to see your profile bloom. Your note about peaceful evenings resonated so deeply with me.",
        timestamp = "6:15 PM",
        isMine = false,
        isRead = true,
        isDelivered = true,
        messageType = MessageType.TEXT
      ),
      ChatMessage(
        id = "m2",
        matchId = "p_ayaan",
        senderName = "Mia",
        text = "Walaikum Assalam Ayaan! Thank you so much. I noticed your love for Faiz’s poetry and LUMS architecture — that must keep your days inspired! 🌸",
        timestamp = "6:22 PM",
        isMine = true,
        isRead = true,
        isDelivered = true,
        messageType = MessageType.TEXT
      ),
      ChatMessage(
        id = "m3",
        matchId = "p_ayaan",
        senderName = "Ayaan Malik",
        text = "It truly does. Here is a picture from my desk during today’s golden hour reading session:",
        timestamp = "6:28 PM",
        isMine = false,
        isRead = true,
        isDelivered = true,
        messageType = MessageType.PHOTO,
        mediaConsentRequired = true,
        mediaConsentGranted = true,
        mediaUrl = "photo_golden_desk"
      ),
      ChatMessage(
        id = "m4",
        matchId = "p_ayaan",
        senderName = "Ayaan Malik",
        text = "Would you enjoy a quiet botanical garden walk this Saturday? There's a lovely tea lounge nearby.",
        timestamp = "6:32 PM",
        isMine = false,
        isRead = true,
        isDelivered = true,
        messageType = MessageType.TEXT,
        reaction = "🌹"
      )
    ),
    "p_sofia" to listOf(
      ChatMessage(
        id = "m_s1",
        matchId = "p_sofia",
        senderName = "Sofia Zahra",
        text = "Hello Mia! Your values on family dinners reminded me so much of our weekend traditions.",
        timestamp = "Yesterday",
        isMine = false,
        isRead = true,
        isDelivered = true,
        messageType = MessageType.TEXT
      ),
      ChatMessage(
        id = "m_s2",
        matchId = "p_sofia",
        senderName = "Mia",
        text = "Sofia, that means the world! There is nothing more grounding than sharing bread with people you cherish.",
        timestamp = "Yesterday",
        isMine = true,
        isRead = true,
        isDelivered = true,
        messageType = MessageType.TEXT,
        reaction = "✨"
      )
    )
  )

  private fun createInitialDates(): List<ScheduledDate> = listOf(
    ScheduledDate(
      id = "d1",
      matchName = "Ayaan Malik",
      matchInitials = "AM",
      matchColorStart = 0xFFF28DB0,
      matchColorEnd = 0xFF9E77ED,
      title = "Artisan Coffee & Botanical Walk",
      emoji = "☕",
      dateFormatted = "Saturday, Nov 15",
      timeFormatted = "3:30 PM",
      location = "Gulberg Glasshouse Café & Orchid Conservatory (Public)",
      isPublicPlaceConfirmed = true,
      trustedContactShared = true
    )
  )

  private fun createInitialQuestions(): List<QuizQuestion> = listOf(
    QuizQuestion(
      id = 1,
      category = "Spiritual & Moral Anchors",
      question = "How do you cultivate peace, moral grounding, and spirituality in daily family life?",
      options = listOf(
        QuizOption("Daily communal prayers and mindful gratitude", "Shared reflection brings lifelong serenity.", mapOf("values" to 25)),
        QuizOption("Personal devotion, quiet reflection and ethical deed", "Faith expressed through gentle actions and kindness.", mapOf("values" to 22)),
        QuizOption("Living through compassionate community service", "Serving others together as our moral purpose.", mapOf("values" to 20))
      ),
      selectedIndex = 0
    ),
    QuizQuestion(
      id = 2,
      category = "Conflict & Gentleness",
      question = "When disagreements or stressful moments arise, what is your instinct?",
      options = listOf(
        QuizOption("Pause, cool down with gentle patience, and speak softly", "Preserving emotional dignity is paramount.", mapOf("communication" to 25)),
        QuizOption("Address directly with warmth, honesty, and empathy", "Clear communication with compassionate reassurance.", mapOf("communication" to 23)),
        QuizOption("Pray together and seek wisdom before responding", "Inviting divine calm into the conversation.", mapOf("communication" to 24))
      ),
      selectedIndex = 0
    ),
    QuizQuestion(
      id = 3,
      category = "Family & Extended Bonds",
      question = "How do you envision extended family involvement in your married life?",
      options = listOf(
        QuizOption("Frequent Sunday lunches, mutual warmth, and open-door hospitality", "Cherishing grandparents, siblings, and extended kin.", mapOf("family" to 25)),
        QuizOption("Loving respect with strong healthy emotional boundaries", "A balanced harmony between our home and extended family.", mapOf("family" to 23)),
        QuizOption("Joint family harmony with clear mutual consideration", "Embracing multi-generational wisdom with grace.", mapOf("family" to 21))
      ),
      selectedIndex = 1
    ),
    QuizQuestion(
      id = 4,
      category = "Courtship & Romance",
      question = "What represents intentional romance in a modern marriage?",
      options = listOf(
        QuizOption("Thoughtful daily gestures: brewing chai, love notes, sincere praise", "Small consistent tenderness nurtures everlasting affection.", mapOf("romance" to 25)),
        QuizOption("Deep conversations over long evening walks and books", "Intellectual and heart-to-heart companionship.", mapOf("romance" to 24)),
        QuizOption("Adventurous trips, shared milestones and celebratory dinners", "Building vibrant shared memories across the world.", mapOf("romance" to 20))
      ),
      selectedIndex = 0
    )
  )

  private fun createInitialStories(): List<StoryItem> = listOf(
    StoryItem(
      id = "s_ayaan_1",
      userId = "p_ayaan",
      userName = "Ayaan Malik",
      userInitials = "AM",
      avatarColorStart = 0xFFF28DB0,
      avatarColorEnd = 0xFF9E77ED,
      storyType = StoryType.PHOTO,
      textCaption = "Golden hour library sanctuary 📚✨",
      bgGradientStart = 0xFFF28DB0,
      bgGradientEnd = 0xFF9E77ED,
      viewsCount = 24,
      visibility = StoryVisibility.EVERYONE
    ),
    StoryItem(
      id = "s_sofia_1",
      userId = "p_sofia",
      userName = "Sofia Zahra",
      userInitials = "SZ",
      avatarColorStart = 0xFFFF9BB3,
      avatarColorEnd = 0xFFFFD3C2,
      storyType = StoryType.TEXT_STATUS,
      textCaption = "Feeling grateful today ☕ Fresh rain over Margalla Hills.",
      moodEmoji = "☕",
      bgGradientStart = 0xFF9E77ED,
      bgGradientEnd = 0xFF6172F3,
      viewsCount = 31,
      visibility = StoryVisibility.MATCHES_ONLY
    ),
    StoryItem(
      id = "s_zainab_1",
      userId = "p_zainab",
      userName = "Zainab Tariq",
      userInitials = "ZT",
      avatarColorStart = 0xFFFF8FA3,
      avatarColorEnd = 0xFFCDB8F0,
      storyType = StoryType.PHOTO,
      textCaption = "Fresh roses harvested from our courtyard garden 🌸",
      bgGradientStart = 0xFFE85D8A,
      bgGradientEnd = 0xFFF6C177,
      viewsCount = 19,
      visibility = StoryVisibility.EVERYONE
    )
  )

  private fun createInitialNearbyPeople(): List<NearbyPerson> = listOf(
    NearbyPerson(
      id = "nb_1",
      name = "Hamza Farooq",
      age = 27,
      city = "Lahore",
      approximateArea = "Gulberg III (~2 km away)",
      distanceKm = 2.1,
      interests = listOf("☕ Specialty Coffee", "📚 Urdu Literature", "🚲 Cycling"),
      compatibilityPercent = 92,
      avatarColorStart = 0xFFF28DB0,
      avatarColorEnd = 0xFF9E77ED,
      initials = "HF"
    ),
    NearbyPerson(
      id = "nb_2",
      name = "Mariam Saeed",
      age = 25,
      city = "Lahore",
      approximateArea = "DHA Phase 5 (~3.5 km away)",
      distanceKm = 3.5,
      interests = listOf("🎨 Oil Painting", "🌱 Terrariums", "☕ Kashmiri Chai"),
      compatibilityPercent = 89,
      avatarColorStart = 0xFFFF9BB3,
      avatarColorEnd = 0xFFFFD3C2,
      initials = "MS"
    ),
    NearbyPerson(
      id = "nb_3",
      name = "Bilal Qureshi",
      age = 28,
      city = "Lahore",
      approximateArea = "Model Town (~5 km away)",
      distanceKm = 4.8,
      interests = listOf("🏃 Marathons", "📖 Islamic History", "✈️ Travel"),
      compatibilityPercent = 87,
      avatarColorStart = 0xFF9E77ED,
      avatarColorEnd = 0xFF6172F3,
      initials = "BQ"
    )
  )

  private fun createInitialFriendSuggestions(): List<FriendSuggestion> = listOf(
    FriendSuggestion(
      id = "fs_1",
      name = "Zara Rizvi",
      age = 25,
      city = "Lahore",
      mutualInterests = listOf("📚 Book Lovers", "☕ Chai Culture"),
      compatibilityScore = 93,
      avatarColorStart = 0xFFFF8FA3,
      avatarColorEnd = 0xFFCDB8F0,
      initials = "ZR"
    ),
    FriendSuggestion(
      id = "fs_2",
      name = "Usman Tariq",
      age = 27,
      city = "Lahore",
      mutualInterests = listOf("🎨 Calligraphy", "✈️ Mountain Treks"),
      compatibilityScore = 90,
      avatarColorStart = 0xFFF28DB0,
      avatarColorEnd = 0xFF9E77ED,
      initials = "UT"
    ),
    FriendSuggestion(
      id = "fs_3",
      name = "Ayesha Siddiqui",
      age = 24,
      city = "Islamabad",
      mutualInterests = listOf("🌱 Botany", "📖 History"),
      compatibilityScore = 87,
      avatarColorStart = 0xFF9E77ED,
      avatarColorEnd = 0xFF6172F3,
      initials = "AS"
    )
  )

  private fun createInitialCommunities(): List<Community> = listOf(
    Community(
      id = "comm_books",
      name = "Book Lovers & Poetry",
      category = "Intellectual & Soulful",
      emoji = "📚",
      description = "A peaceful sanctuary discussing classical Urdu verse, Iqbal, Faiz, Rumi, and uplifting fiction.",
      memberCount = 1240,
      isJoined = true,
      rules = listOf("18+ respectful tone only", "No spam or unsolicited contact", "Celebrate thoughtful perspectives"),
      pinnedPost = CommunityPost(
        id = "p_pin_1",
        communityId = "comm_books",
        authorId = "u_mod",
        authorName = "Community Moderator 🛡️",
        authorBadge = "Moderator",
        content = "Welcome members! Share the quote that brought you peace this week.",
        timestamp = "Pinned",
        likesCount = 58,
        commentsCount = 14,
        isPinned = true
      ),
      gradientStart = 0xFFF28DB0,
      gradientEnd = 0xFF9E77ED,
      posts = listOf(
        CommunityPost(
          id = "p_post_1",
          communityId = "comm_books",
          authorId = "u_ayaan",
          authorName = "Ayaan Malik",
          authorBadge = "Top Contributor 🌸",
          content = "Currently revisiting Faiz's 'Nuskha-ha-e-Wafa'. The gentle longing in his metaphors continues to inspire my creative mornings.",
          timestamp = "2h ago",
          likesCount = 34,
          commentsCount = 9,
          isLikedByMe = true
        ),
        CommunityPost(
          id = "p_post_2",
          communityId = "comm_books",
          authorId = "u_sofia",
          authorName = "Sofia Zahra",
          content = "Any recommendations for historical fiction set in Mughal Lahore? Looking for immersive heritage stories.",
          timestamp = "5h ago",
          likesCount = 18,
          commentsCount = 12
        )
      )
    ),
    Community(
      id = "comm_chai",
      name = "Chai Culture & Conversations",
      category = "Warm & Social",
      emoji = "☕",
      description = "Dedicated to the art of brewing cardamom chai, Kashmiri chai, and wholesome community warmth.",
      memberCount = 980,
      isJoined = true,
      rules = listOf("Be kind and welcoming", "Share authentic recipes and tea stories"),
      gradientStart = 0xFFFF9BB3,
      gradientEnd = 0xFFFFD3C2,
      posts = listOf(
        CommunityPost(
          id = "p_chai_1",
          communityId = "comm_chai",
          authorId = "u_zainab",
          authorName = "Zainab Tariq",
          content = "Secret to perfect pink tea: slow boiling with star anise and crushed pistachios on top!",
          timestamp = "1d ago",
          likesCount = 42,
          commentsCount = 16
        )
      )
    ),
    Community(
      id = "comm_travel",
      name = "Hunza & Northern Treks",
      category = "Adventure & Nature",
      emoji = "✈️",
      description = "Scenic routes, autumn foliage in Hunza, Skardu lakes, and ethical nature travel.",
      memberCount = 1560,
      isJoined = false,
      rules = listOf("Respect local culture", "Leave no trace on treks"),
      gradientStart = 0xFF9E77ED,
      gradientEnd = 0xFF6172F3
    ),
    Community(
      id = "comm_lahore",
      name = "Lahore Heritage & Culture",
      category = "City Community",
      emoji = "🏰",
      description = "Walled City walks, Badshahi Mosque serenity, food street history, and arts in Lahore.",
      memberCount = 2100,
      isJoined = true,
      rules = listOf("Only Lahore-related art, events & discussion", "No exact locations or contact exchange"),
      gradientStart = 0xFFE85D8A,
      gradientEnd = 0xFFF6C177,
      posts = listOf(
        CommunityPost(
          id = "p_lahore_1",
          communityId = "comm_lahore",
          authorId = "u_hamza",
          authorName = "Hamza Farooq",
          content = "The courtyard of Wazir Khan Mosque at sunset is unparalleled in serenity. The glazed kashi-kari tiles catch the light beautifully.",
          timestamp = "3h ago",
          likesCount = 27,
          commentsCount = 6
        )
      )
    )
  )

  private fun createInitialGroupChats(): List<GroupChat> = listOf(
    GroupChat(
      id = "g_bookclub",
      name = "Lahore Book & Chai Circle",
      description = "A gentle group for 18+ matched members to organize monthly bookstore visits and chai discussions.",
      membersCount = 6,
      adminIds = listOf("u_mia", "p_ayaan"),
      memberNames = listOf("Mia (You)", "Ayaan Malik", "Sofia Zahra", "Hamza Farooq", "Zainab Tariq", "Zara Rizvi"),
      iconEmoji = "📚",
      gradientStart = 0xFFF28DB0,
      gradientEnd = 0xFF9E77ED,
      lastMessageText = "Ayaan: Botanical Garden terrace looks lovely for Saturday!",
      lastMessageTime = "10:14 AM",
      unreadCount = 2,
      activePoll = GroupPoll(
        id = "poll_meetup",
        question = "Where should our weekend book discussion take place?",
        options = listOf(
          PollOption(1, "Gulberg Glasshouse Café", 4),
          PollOption(2, "Bagh-e-Jinnah Library Lawn", 2),
          PollOption(3, "Historic Bookstore Lounge", 5)
        ),
        totalVotes = 11,
        hasVoted = false
      )
    )
  )

  // Social Network Operations
  fun likePost(postId: String) {
    _feedPosts.update { list ->
      list.map { post ->
        if (post.id == postId) {
          val newLiked = !post.isLiked
          post.copy(
            isLiked = newLiked,
            likesCount = if (newLiked) post.likesCount + 1 else (post.likesCount - 1).coerceAtLeast(0)
          )
        } else post
      }
    }
    val targetPost = _feedPosts.value.find { it.id == postId }
    if (targetPost != null && targetPost.isLiked && targetPost.authorId != "u_mia") {
      addNotification(
        SocialNotification(
          id = "notif_${System.currentTimeMillis()}",
          type = SocialNotificationType.LIKE,
          actorName = "Mia",
          actorHandle = "@mia_blossom",
          actorAvatarColors = Pair(0xFFF28DB0, 0xFF9E77ED),
          message = "liked your post: \"${targetPost.textCaption.take(24)}...\"",
          timeAgo = "Just now",
          targetPostId = postId,
          isRead = false,
          dateGroup = "Today"
        )
      )
    }
  }

  fun toggleSavePost(postId: String) {
    _feedPosts.update { list ->
      list.map { if (it.id == postId) it.copy(isSaved = !it.isSaved) else it }
    }
  }

  fun addComment(postId: String, parentCommentId: String?, text: String, authorName: String = "Mia"): String? {
    if (text.contains("send money", ignoreCase = true) || text.contains("crypto", ignoreCase = true)) {
      return "⚠️ Comment flagged: prohibited keywords."
    }
    val newComment = PostComment(
      id = "c_${System.currentTimeMillis()}",
      postId = postId,
      authorName = authorName,
      authorHandle = "@${authorName.lowercase().replace(" ", "_")}",
      authorAvatarColors = Pair(0xFFF28DB0, 0xFF9E77ED),
      text = text,
      timestamp = "Just now",
      likesCount = 0,
      parentCommentId = parentCommentId
    )
    _feedPosts.update { list ->
      list.map { post ->
        if (post.id == postId) {
          val updatedComments = if (parentCommentId != null) {
            post.comments.map { c ->
              if (c.id == parentCommentId) {
                c.copy(replies = c.replies + newComment)
              } else c
            }
          } else {
            post.comments + newComment
          }
          post.copy(
            comments = updatedComments,
            commentsCount = post.commentsCount + 1
          )
        } else post
      }
    }
    val targetPost = _feedPosts.value.find { it.id == postId }
    if (targetPost != null && targetPost.authorId != "u_mia") {
      addNotification(
        SocialNotification(
          id = "notif_${System.currentTimeMillis()}",
          type = SocialNotificationType.COMMENT,
          actorName = authorName,
          actorHandle = "@${authorName.lowercase().replace(" ", "_")}",
          actorAvatarColors = Pair(0xFFF28DB0, 0xFF9E77ED),
          message = "commented: \"${text.take(28)}\"",
          timeAgo = "Just now",
          targetPostId = postId,
          isRead = false,
          dateGroup = "Today"
        )
      )
    }
    return null
  }

  fun likeComment(postId: String, commentId: String) {
    _feedPosts.update { list ->
      list.map { post ->
        if (post.id == postId) {
          val updated = post.comments.map { c ->
            if (c.id == commentId) {
              val newLiked = !c.isLiked
              c.copy(isLiked = newLiked, likesCount = if (newLiked) c.likesCount + 1 else (c.likesCount - 1).coerceAtLeast(0))
            } else {
              val updatedReplies = c.replies.map { r ->
                if (r.id == commentId) {
                  val newLiked = !r.isLiked
                  r.copy(isLiked = newLiked, likesCount = if (newLiked) r.likesCount + 1 else (r.likesCount - 1).coerceAtLeast(0))
                } else r
              }
              c.copy(replies = updatedReplies)
            }
          }
          post.copy(comments = updated)
        } else post
      }
    }
  }

  fun votePostPoll(postId: String, optionIndex: Int) {
    _feedPosts.update { list ->
      list.map { post ->
        if (post.id == postId && post.poll != null && !post.poll.hasVoted) {
          val p = post.poll
          val updatedOptions = p.options.mapIndexed { idx, opt ->
            if (idx == optionIndex) opt.copy(voteCount = opt.voteCount + 1) else opt
          }
          post.copy(
            poll = p.copy(
              options = updatedOptions,
              totalVotes = p.totalVotes + 1,
              hasVoted = true,
              userVotedIndex = optionIndex
            )
          )
        } else post
      }
    }
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
    if (caption.isBlank() && photoGradients.isEmpty() && poll == null) {
      return "Please write a caption or add media/poll."
    }
    val newPost = FeedPost(
      id = "post_${System.currentTimeMillis()}",
      authorId = "u_mia",
      authorName = "Mia",
      authorNickname = "Mia 🌸",
      authorHandle = "@mia_blossom",
      authorAvatarColors = Pair(0xFFF28DB0, 0xFF9E77ED),
      authorVerified = true,
      authorStreakDays = 7,
      isOnline = true,
      timestamp = "Just now",
      textCaption = caption,
      photoGradients = photoGradients.take(6),
      hashtags = hashtags,
      mentions = mentions,
      locationCity = locationCity,
      audience = audience,
      likesCount = 0,
      commentsCount = 0,
      sharesCount = 0,
      isLiked = false,
      isSaved = false,
      isStory = isStory,
      poll = poll
    )
    _feedPosts.update { listOf(newPost) + it }
    _userSocialProfile.update { current ->
      current.copy(
        postsCount = current.postsCount + 1,
        posts = listOf(newPost) + current.posts
      )
    }
    if (isStory) {
      addStory(
        StoryItem(
          id = "story_post_${newPost.id}",
          userId = "u_mia",
          userName = "Mia",
          userInitials = "M",
          avatarColorStart = 0xFFF28DB0,
          avatarColorEnd = 0xFF9E77ED,
          textCaption = caption,
          moodEmoji = "🌸"
        )
      )
    }
    return null
  }

  fun reportPost(postId: String, reason: String) {
    _feedPosts.update { list ->
      list.map { if (it.id == postId) it.copy(isFlagged = true) else it }
    }
    _hiddenPostIds.update { it + postId }
    reportAndBlockUser("Post_$postId", reason, "Reported feed post flagged by member.")
  }

  fun hidePost(postId: String) {
    _hiddenPostIds.update { it + postId }
  }

  fun toggleFollowUser(userId: String) {
    _userSocialProfile.update { current ->
      val newFollowing = !current.isFollowing
      current.copy(
        isFollowing = newFollowing,
        followingCount = if (newFollowing) current.followingCount + 1 else (current.followingCount - 1).coerceAtLeast(0)
      )
    }
  }

  fun toggleSocialFriend(userId: String) {
    _userSocialProfile.update { current ->
      val newFriend = !current.isFriend
      current.copy(
        isFriend = newFriend,
        friendsCount = if (newFriend) current.friendsCount + 1 else (current.friendsCount - 1).coerceAtLeast(0)
      )
    }
  }

  fun addNotification(notification: SocialNotification) {
    _notifications.update { listOf(notification) + it }
  }

  fun markNotificationRead(id: String) {
    _notifications.update { list ->
      list.map { if (it.id == id) it.copy(isRead = true) else it }
    }
  }

  fun markAllNotificationsRead() {
    _notifications.update { list ->
      list.map { it.copy(isRead = true) }
    }
  }

  private fun createInitialFeedPosts(): List<FeedPost> = listOf(
    FeedPost(
      id = "fp_1",
      authorId = "p_ayaan",
      authorName = "Ayaan Malik",
      authorNickname = "Ayaan 🌸",
      authorHandle = "@ayaan_malik",
      authorAvatarColors = Pair(0xFFF28DB0, 0xFF9E77ED),
      authorVerified = true,
      authorStreakDays = 14,
      isOnline = true,
      timestamp = "2h ago",
      textCaption = "Sunday morning reflection: When Faiz wrote of gentle longing, he was describing the patience required to build a sanctuary of companionship with someone whose values echo your own. Wishing everyone a blessed, tranquil week 🌸 #UrduPoetry #ChaiMoments #FaithAndWarmth",
      photoGradients = listOf(
        Pair(0xFFF28DB0, 0xFF9E77ED),
        Pair(0xFFFF9BB3, 0xFFFFD3C2),
        Pair(0xFF9E77ED, 0xFF6172F3)
      ),
      hashtags = listOf("#UrduPoetry", "#ChaiMoments", "#FaithAndWarmth"),
      mentions = listOf("@sofia_zahra"),
      locationCity = "Lahore",
      audience = PostAudience.EVERYONE,
      likesCount = 48,
      commentsCount = 8,
      sharesCount = 5,
      isLiked = true,
      isSaved = true,
      comments = listOf(
        PostComment(
          id = "c1_1",
          postId = "fp_1",
          authorName = "Sofia Zahra",
          authorHandle = "@sofia_zahra",
          authorAvatarColors = Pair(0xFFFF9BB3, 0xFFFFD3C2),
          text = "Faiz’s cadence always brings calm to chaotic days. Beautifully put, Ayaan! 🕊️",
          timestamp = "1h ago",
          likesCount = 12,
          isLiked = true,
          replies = listOf(
            PostComment(
              id = "c1_1_1",
              postId = "fp_1",
              authorName = "Ayaan Malik",
              authorHandle = "@ayaan_malik",
              authorAvatarColors = Pair(0xFFF28DB0, 0xFF9E77ED),
              text = "Thank you Sofia! Looking forward to your architecture sketches this week.",
              timestamp = "45m ago",
              likesCount = 4,
              parentCommentId = "c1_1"
            )
          )
        ),
        PostComment(
          id = "c1_2",
          postId = "fp_1",
          authorName = "Zainab Tariq",
          authorHandle = "@zainab_tariq",
          authorAvatarColors = Pair(0xFFFF8FA3, 0xFFCDB8F0),
          text = "Chai and classic verse is the most grounding combination ✨",
          timestamp = "30m ago",
          likesCount = 6
        )
      )
    ),
    FeedPost(
      id = "fp_2",
      authorId = "p_sofia",
      authorName = "Sofia Zahra",
      authorNickname = "Sofia 🕊️",
      authorHandle = "@sofia_zahra",
      authorAvatarColors = Pair(0xFFFF9BB3, 0xFFFFD3C2),
      authorVerified = true,
      authorStreakDays = 7,
      isOnline = true,
      timestamp = "4h ago",
      textCaption = "Surveying the glazed kashi-kari tiles at Wazir Khan Mosque. Notice the symmetry: every floral motif speaks of timeless reverence. What heritage detail moves you most when exploring historic courtyards?",
      photoGradients = listOf(
        Pair(0xFFE85D8A, 0xFFF6C177),
        Pair(0xFF9E77ED, 0xFFB79CF0)
      ),
      hashtags = listOf("#LahoreHeritage", "#MughalArchitecture", "#Preservation"),
      mentions = emptyList(),
      locationCity = "Lahore",
      audience = PostAudience.EVERYONE,
      likesCount = 37,
      commentsCount = 5,
      poll = FeedPostPoll(
        question = "Which architectural element touches your soul most?",
        options = listOf(
          PollOption(1, "Floral Calligraphy & Tilework", 28),
          PollOption(2, "Sunlit Courtyard Arches", 34),
          PollOption(3, "Central Water Fountains", 16)
        ),
        totalVotes = 78,
        hasVoted = false
      )
    ),
    FeedPost(
      id = "fp_3",
      authorId = "p_danyal",
      authorName = "Danyal Khan",
      authorNickname = "Danyal ✈️",
      authorHandle = "@danyal_k",
      authorAvatarColors = Pair(0xFF9E77ED, 0xFF6172F3),
      authorVerified = true,
      authorStreakDays = 3,
      isOnline = false,
      timestamp = "6h ago",
      textCaption = "Post-shift distance run along Clifton Beach. There is profound stillness in listening to waves after a demanding week in pediatric research. Balance in mind, spirit, and service. 🏃🌊 #KarachiWalks #Mindfulness #HealthcareJoy",
      photoGradients = listOf(
        Pair(0xFF9E77ED, 0xFF6172F3)
      ),
      hashtags = listOf("#KarachiWalks", "#Mindfulness", "#HealthcareJoy"),
      locationCity = "Karachi",
      audience = PostAudience.EVERYONE,
      likesCount = 29,
      commentsCount = 3
    ),
    FeedPost(
      id = "fp_4",
      authorId = "p_zainab",
      authorName = "Zainab Tariq",
      authorNickname = "Zainab 🌸",
      authorHandle = "@zainab_tariq",
      authorAvatarColors = Pair(0xFFFF8FA3, 0xFFCDB8F0),
      authorVerified = true,
      authorStreakDays = 5,
      isOnline = true,
      timestamp = "8h ago",
      textCaption = "Freshly baked pistachio and saffron shortbread for our weekend family gathering. Cooking with love is my primary way of showing care and gratitude. 🍪🌸 #HomeCooking #FamilyTradition #SweetMoments",
      photoGradients = listOf(
        Pair(0xFFFF8FA3, 0xFFCDB8F0),
        Pair(0xFFFFD3C2, 0xFFFFE3EA)
      ),
      hashtags = listOf("#HomeCooking", "#FamilyTradition", "#SweetMoments"),
      locationCity = "Lahore",
      audience = PostAudience.EVERYONE,
      likesCount = 52,
      commentsCount = 11,
      isLiked = true
    )
  )

  private fun createInitialTrendingHashtags(): List<TrendingHashtag> = listOf(
    TrendingHashtag("#UrduPoetry", "Literature & Soul", "4.8K posts", isHot = true),
    TrendingHashtag("#LahoreHeritage", "Architecture & Culture", "3.2K posts", isHot = true),
    TrendingHashtag("#ChaiMoments", "Lifestyle & Warmth", "6.1K posts", isHot = true),
    TrendingHashtag("#HalalRomance", "Marriage Intentions", "2.9K posts", isHot = false),
    TrendingHashtag("#HunzaAutumn", "Northern Treks", "1.7K posts", isHot = false),
    TrendingHashtag("#MughalArt", "Cultural History", "1.4K posts", isHot = false),
    TrendingHashtag("#FamilyFirst", "Values & Bonding", "5.3K posts", isHot = false)
  )

  private fun createInitialNotifications(): List<SocialNotification> = listOf(
    SocialNotification(
      id = "n_1",
      type = SocialNotificationType.LIKE,
      actorName = "Ayaan Malik",
      actorHandle = "@ayaan_malik",
      actorAvatarColors = Pair(0xFFF28DB0, 0xFF9E77ED),
      message = "liked your photo in Botanical Garden",
      timeAgo = "15m ago",
      targetPostId = "fp_1",
      isRead = false,
      dateGroup = "Today"
    ),
    SocialNotification(
      id = "n_2",
      type = SocialNotificationType.MATCH,
      actorName = "Sofia Zahra",
      actorHandle = "@sofia_zahra",
      actorAvatarColors = Pair(0xFFFF9BB3, 0xFFFFD3C2),
      message = "bloomed with you! You have a mutual match 🌸",
      timeAgo = "1h ago",
      isRead = false,
      dateGroup = "Today"
    ),
    SocialNotification(
      id = "n_3",
      type = SocialNotificationType.COMMENT,
      actorName = "Zainab Tariq",
      actorHandle = "@zainab_tariq",
      actorAvatarColors = Pair(0xFFFF8FA3, 0xFFCDB8F0),
      message = "commented: \"Your reflection on peaceful homes was so gentle ✨\"",
      timeAgo = "3h ago",
      targetPostId = "fp_4",
      isRead = true,
      dateGroup = "Today"
    ),
    SocialNotification(
      id = "n_4",
      type = SocialNotificationType.FOLLOW,
      actorName = "Hamza Farooq",
      actorHandle = "@hamza_f",
      actorAvatarColors = Pair(0xFFF28DB0, 0xFF9E77ED),
      message = "started following your thoughtful journey",
      timeAgo = "Yesterday",
      isRead = true,
      dateGroup = "This Week"
    ),
    SocialNotification(
      id = "n_5",
      type = SocialNotificationType.STORY_VIEW,
      actorName = "Danyal Khan",
      actorHandle = "@danyal_k",
      actorAvatarColors = Pair(0xFF9E77ED, 0xFF6172F3),
      message = "viewed your 24h story reflection ☕",
      timeAgo = "2d ago",
      isRead = true,
      dateGroup = "This Week"
    )
  )

  private fun createInitialSocialProfile(): UserSocialProfile = UserSocialProfile(
    userId = "u_mia",
    name = "Mia Rizvi",
    nickname = "Mia 🌸",
    handle = "@mia_blossom",
    bio = "Cultivating patience, gentle aesthetics & intentional companionship. Architecture admirer, reader of Iqbal & lover of quiet afternoon tea. Serious relationship / marriage-minded ✨",
    city = "Lahore, Punjab",
    followersCount = 428,
    followingCount = 186,
    friendsCount = 34,
    postsCount = 18,
    badges = listOf("Verified 🛡️", "Streak 14🔥", "Early Bloomer 🌱✨"),
    isFollowing = false,
    isFriend = false,
    isLikedRomantic = false,
    avatarColors = Pair(0xFFF28DB0, 0xFF9E77ED),
    coverColors = Pair(0xFFF9D3E3, 0xFFCDB8F0),
    highlights = listOf(
      ProfileHighlight("hl_1", "Travel", "✈️", Pair(0xFFF28DB0, 0xFF9E77ED)),
      ProfileHighlight("hl_2", "Books", "📚", Pair(0xFF9E77ED, 0xFFB79CF0)),
      ProfileHighlight("hl_3", "Chai", "☕", Pair(0xFFFF9BB3, 0xFFFFD3C2)),
      ProfileHighlight("hl_4", "Art", "🎨", Pair(0xFFE85D8A, 0xFFF6C177)),
      ProfileHighlight("hl_5", "Courtyards", "🏛️", Pair(0xFF9E77ED, 0xFF6172F3))
    ),
    posts = listOf(
      FeedPost(
        id = "mp_1",
        authorId = "u_mia",
        authorName = "Mia Rizvi",
        authorNickname = "Mia 🌸",
        authorHandle = "@mia_blossom",
        authorAvatarColors = Pair(0xFFF28DB0, 0xFF9E77ED),
        timestamp = "3d ago",
        textCaption = "Botanical conservatory strolls in Lahore. Finding calm among orchids and century-old ficus trees 🌿",
        photoGradients = listOf(Pair(0xFFF28DB0, 0xFF9E77ED), Pair(0xFFFF9BB3, 0xFFFFD3C2)),
        hashtags = listOf("#NatureSanctuary", "#LahoreBotanic"),
        locationCity = "Lahore",
        likesCount = 64,
        commentsCount = 12
      ),
      FeedPost(
        id = "mp_2",
        authorId = "u_mia",
        authorName = "Mia Rizvi",
        authorNickname = "Mia 🌸",
        authorHandle = "@mia_blossom",
        authorAvatarColors = Pair(0xFFF28DB0, 0xFF9E77ED),
        timestamp = "5d ago",
        textCaption = "Cardamom pink chai brewed on a breezy rainy evening. The warmest hug in a cup ☕✨",
        photoGradients = listOf(Pair(0xFFFF9BB3, 0xFFFFD3C2)),
        hashtags = listOf("#KashmiriChai", "#RainyDays"),
        locationCity = "Lahore",
        likesCount = 89,
        commentsCount = 19
      ),
      FeedPost(
        id = "mp_3",
        authorId = "u_mia",
        authorName = "Mia Rizvi",
        authorNickname = "Mia 🌸",
        authorHandle = "@mia_blossom",
        authorAvatarColors = Pair(0xFFF28DB0, 0xFF9E77ED),
        timestamp = "1w ago",
        textCaption = "Visiting the historic Haveli courtyards in the Walled City. The woodwork carved by ancestral hands carries so much reverence 🏛️",
        photoGradients = listOf(Pair(0xFF9E77ED, 0xFF6172F3), Pair(0xFFE85D8A, 0xFFF6C177)),
        hashtags = listOf("#OldLahore", "#HeritageArch"),
        locationCity = "Lahore",
        likesCount = 112,
        commentsCount = 24
      )
    )
  )
}

