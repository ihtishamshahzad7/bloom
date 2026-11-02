const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();
const db = admin.firestore();
const storage = admin.storage();

/**
 * 1. Post Creation & Feed Ranking / Fan-out Trigger
 *
 * When a member publishes a new post:
 * - Aggregates trending hashtags in 'trending_hashtags' collection.
 * - Computes feed ranking metadata: following + friends + mutual interests + recency score.
 * - Performs automated safety & keyword moderation check.
 */
exports.onPostCreated = functions.firestore
  .document("posts/{postId}")
  .onCreate(async (snap, context) => {
    const postData = snap.data();
    const postId = context.params.postId;
    const now = Date.now();

    console.log(`Processing new feed post ${postId} by ${postData.authorName}...`);

    try {
      // Safety keyword filter & auto-flagging
      const caption = postData.textCaption || "";
      const scamKeywords = ["wire money", "send money", "crypto", "paypal", "cashapp", "free gift card"];
      const containsScam = scamKeywords.some((kw) => caption.toLowerCase().includes(kw));

      if (containsScam) {
        console.warn(`Post ${postId} contains scam keywords. Auto-flagging for moderation review.`);
        await snap.ref.update({
          isFlagged: true,
          moderationReason: "Automated keyword safety filter triggered",
          isAutoBlurred: true
        });
        return;
      }

      // Extract & aggregate hashtags
      const hashtags = caption.match(/#\w+/g) || [];
      const batch = db.batch();

      for (const tag of hashtags) {
        const cleanTag = tag.trim();
        const tagRef = db.collection("trending_hashtags").doc(cleanTag.toLowerCase());
        batch.set(
          tagRef,
          {
            tag: cleanTag,
            postsCount: admin.firestore.FieldValue.increment(1),
            lastUsedAt: now,
            category: "Community Reflections",
            isHot: true
          },
          { merge: true }
        );
      }
      await batch.commit();

      // Compute initial feed ranking score
      const verifiedBonus = postData.authorVerified ? 30 : 0;
      const initialRankingScore = 100 + verifiedBonus;

      await snap.ref.update({
        rankingScore: initialRankingScore,
        createdAt: now
      });

      console.log(`Post ${postId} ranked and hashtags aggregated successfully.`);
    } catch (error) {
      console.error(`Error processing post ${postId}:`, error);
    }
  });

/**
 * 2. Notification Trigger for Comments
 *
 * Dispatches an in-app notification to the post's author when a comment is posted.
 */
exports.onCommentCreated = functions.firestore
  .document("posts/{postId}/comments/{commentId}")
  .onCreate(async (snap, context) => {
    const comment = snap.data();
    const postId = context.params.postId;

    try {
      const postSnap = await db.collection("posts").doc(postId).get();
      if (!postSnap.exists) return;

      const postData = postSnap.data();
      const postAuthorId = postData.authorId;

      // Don't notify if commenting on own post
      if (comment.authorId === postAuthorId) return;

      await db
        .collection("notifications")
        .doc(postAuthorId)
        .collection("items")
        .add({
          type: "COMMENT",
          actorName: comment.authorName || "Someone",
          actorHandle: comment.authorHandle || "@member",
          message: `commented: "${(comment.text || "").slice(0, 32)}"`,
          targetPostId: postId,
          timestamp: Date.now(),
          isRead: false,
          dateGroup: "Today"
        });

      console.log(`Notification dispatched to ${postAuthorId} for comment on ${postId}.`);
    } catch (error) {
      console.error("Error creating comment notification:", error);
    }
  });

/**
 * 3. Daily Streak Check (Cron running every 24 hours at midnight UTC)
 *
 * Inspects all active match conversations.
 * - If both users messaged within 24 hours: increments streak days.
 * - If one/both missed: checks for active 'streakFreeze'. If active, consumes freeze;
 *   otherwise resets streak to 0.
 */
exports.checkStreaksDaily = functions.pubsub
  .schedule("0 0 * * *")
  .timeZone("UTC")
  .onRun(async (context) => {
    const now = Date.now();
    const oneDayAgo = now - 24 * 60 * 60 * 1000;

    console.log("Starting daily Bloom Streak audit...");

    try {
      const matchesSnapshot = await db.collection("matches").get();

      for (const matchDoc of matchesSnapshot.docs) {
        const matchData = matchDoc.data();
        const users = matchData.users || [];
        if (users.length < 2) continue;

        const userA = users[0];
        const userB = users[1];

        // Retrieve messages from last 24h
        const messagesSnapshot = await matchDoc.ref
          .collection("messages")
          .where("timestamp", ">=", oneDayAgo)
          .get();

        const senders = new Set();
        messagesSnapshot.forEach((doc) => {
          senders.add(doc.data().senderId);
        });

        const bothSpoke = senders.has(userA) && senders.has(userB);
        const streakDocRef = db.collection("streaks").doc(matchDoc.id);
        const streakSnap = await streakDocRef.get();
        const streakData = streakSnap.exists ? streakSnap.data() : { currentStreakDays: 0, streakFreezeActive: false };

        if (bothSpoke) {
          const newStreak = (streakData.currentStreakDays || 0) + 1;
          await streakDocRef.set(
            {
              users: users,
              currentStreakDays: newStreak,
              lastIncrementedAt: now,
              hoursRemaining: 24,
              isExpiringSoon: false
            },
            { merge: true }
          );
          console.log(`Match ${matchDoc.id} streak incremented to ${newStreak} 🔥`);
        } else {
          if (streakData.streakFreezeActive) {
            await streakDocRef.update({
              streakFreezeActive: false,
              hoursRemaining: 24,
              notes: "Protected by Streak Freeze ❄️"
            });
            console.log(`Match ${matchDoc.id} protected by Streak Freeze.`);
          } else {
            await streakDocRef.update({
              currentStreakDays: 0,
              isExpiringSoon: false
            });
            console.log(`Match ${matchDoc.id} streak ended.`);
          }
        }
      }
    } catch (error) {
      console.error("Error running daily streak audit:", error);
    }
  });

/**
 * 4. 24-Hour Story Expiry Cleanup (Runs every 1 hour)
 *
 * Removes stories older than 24 hours (where expiresAt <= Date.now()).
 */
exports.cleanupExpiredStories = functions.pubsub
  .schedule("every 1 hours")
  .onRun(async (context) => {
    const now = Date.now();
    console.log("Running 24h Story Expiry cleanup...");

    try {
      const expiredSnapshot = await db
        .collection("stories")
        .where("expiresAt", "<=", now)
        .get();

      const batch = db.batch();
      expiredSnapshot.forEach((doc) => {
        batch.delete(doc.ref);
      });

      await batch.commit();
      console.log(`Cleaned up ${expiredSnapshot.size} expired stories.`);
    } catch (error) {
      console.error("Error cleaning expired stories:", error);
    }
  });

/**
 * 5. Story Moderation Trigger
 */
exports.onStoryFlagged = functions.firestore
  .document("stories/{storyId}")
  .onUpdate(async (change, context) => {
    const before = change.before.data();
    const after = change.after.data();

    if (!before.isFlagged && after.isFlagged) {
      console.log(`Story ${context.params.storyId} flagged for review. Auto-blurring...`);

      return change.after.ref.update({
        isAutoBlurred: true,
        visibility: "MODERATION_REVIEW",
        flaggedAt: Date.now()
      });
    }
    return null;
  });
