# Bloom — Safe Social Network & Romantic Matchmaking (Dashboard V50)

> **"A safe, intentional social network and romantic matchmaking sanctuary for young adults."**

**Bloom** is a native Android application (Kotlin, Jetpack Compose, Firebase) combining thoughtful social networking with safe matchmaking. The design language embraces **soft neumorphism and glassmorphism** with a romantic floral-and-candlelight mood: dreamy blush gradients, warm candle glow, falling rose petals, and Playfair Display + Poppins typography.

---

## ✨ Social Network & Community Features

### 1. Home Feed
* **Multi-Format Posts:**
  * Multi-photo carousels (up to 6 photos) with smooth swipe indicators and photo count badges.
  * Deeply formatted captions with colored, interactive hashtags (`#UrduPoetry`, `#LahoreHeritage`) and mentions (`@ayaan_malik`).
  * Interactive community polls with real-time percentage progress bars and instant voting.
* **Rich Interactions:**
  * Rose heart like button (`❤️ / 🌹`) with animated count.
  * Threaded comments bottom sheet supporting nested replies (`Reply to Ayaan`), comment liking, and live safety check.
  * Share and bookmark save icons.
* **Feed Controls & Polish:**
  * **Verified-Only Filter:** `🛡️ Verified Only` toggle in the header to view strictly identity-verified members.
  * **Pull-to-Refresh & Shimmer:** Smooth skeleton loading state during refresh.
  * **Ephemeral Stories Row:** 24h photo and text status row positioned gracefully at the top of the feed.
  * **Safety Moderation:** Every post features an overflow menu with `Hide Post` and `Report Post` (auto-hides and records report).

### 2. Post Composer
* **Multi-Photo Picker:** Pick up to 6 photos from high-aesthetic floral and golden palettes.
* **Caption & Tags:** Live caption input with hashtag (`#`) and mention (`@`) recognition.
* **Audience Selector:** Choose who can view your thoughts:
  * *Everyone* (Public Feed)
  * *Friends Only*
  * *Matches Only*
* **Location Tagging:** City only (e.g., *Lahore*, *Islamabad*, *Karachi*); strictly zero coordinates for privacy protection.
* **"Post as 24h Story" Toggle:** Simultaneously broadcast the post to the ephemeral 24h stories strip.
* **Poll Builder:** Add interactive poll questions with custom options directly inside the post.

### 3. Profile (Instagram-Style)
* **Statistics Bar:** Real-time counters for Posts, Followers, Following, and Platonic Friends.
* **Profile Header:** Custom avatar with gradient story ring, active online presence dot, nickname (`Mia 🌸`), full name, bio, and city.
* **Badges:** Verified Identity 🛡️, Bloom Streak 🌸🔥, Early Bloomer 🌱✨.
* **Action Button Row:**
  * `Follow` / `Following ✓`
  * `Add Friend 🤝` / `Friends ✓`
  * `Message 💬` (Opens direct chat thread)
  * `Like 🌸` (Romantic Bloom action with tactile haptic feedback)
* **Highlights Carousel:** Permanent story highlights (*"Travel ✈️"*, *"Books 📚"*, *"Chai ☕"*, *"Art 🎨"*, *"Courtyards 🏛️"*).
* **3-Column Posts Grid:** Instagram-style thumbnail grid with like counts and multi-photo indicators.

### 4. Explore & Discovery
* **Trending Hashtags (X-Style):** Real-time trending topics with post volume (`#UrduPoetry 4.8K posts 🔥`, `#LahoreHeritage 3.2K posts`, `#ChaiMoments`).
* **Suggested People Carousel:** Matches recommended by shared interests and values with quick follow buttons.
* **Vibrant Circles:** Popular communities preview with member counts and one-tap join.
* **Unified Search:** Real-time search across people, hashtags, circles, and cities.
* **Community Moments Grid:** Aesthetic masonry grid of community reflections.

### 5. Notifications Center
* **Grouped Timeframes:** Categorized under *"Today"* and *"This Week"*.
* **Filter Tabs:** `All`, `Likes`, `Comments`, `Matches`, `Follows`.
* **Badges & Unread Indicators:** Rose indicator dot and top-bar bell count badge.
* **Quick Actions:** One-tap *"Mark all read"* and direct navigation to the associated post or chat.

### 6. Rich Chat, Streaks & Ephemeral Stories
* **Message Types:** Text, emojis, 4 sticker packs (*Bloom Roses*, *Cute Couples*, *Urdu Phrases*, *Candle Love*), photos, voice notes (waveform audio with 1x/1.5x/2x speed), location pins, and disappearing "View Once" media (with `FLAG_SECURE` screenshot protection).
* **Daily Bloom Streaks (🔥 / 🌸):** Snapchat-style streak counter with ⏳ expiry warning (last 4 hours) and Streak Freeze (❄️) protection.
* **24h Ephemeral Stories:** Photo and text status posts with mood emojis, gradient backdrops, and viewer inspector.

### 7. Bottom Dock Layout (Section 8)
1. **Home** (`ScreenNav.HOME`)
2. **Discover** (`ScreenNav.EXPLORE`)
3. **✿ Center Quick-Create Action Button** (Post, Story, Reel, Poll, Group)
4. **Chats** (`ScreenNav.MESSAGES` with unread badge)
5. **Communities** (`ScreenNav.COMMUNITIES`)
6. **Profile** (`ScreenNav.PROFILE`)

---

## 🛡️ Safety & Privacy Architecture

* **Strict 18+ Verification:** Prohibits underage accounts at the auth, app shell, and Firestore rules layers.
* **Automated Keyword Filter:** Prevents scam and financial solicitations (*crypto, wire, paypal, cashapp*).
* **Rate Limiting:** Prevents spam floods on posts, comments, and messages.
* **Zero Location Exposure:** No coordinates or phone numbers are ever stored or rendered; only rounded steps and city names.
* **Data Sovereignty:** One-tap JSON archive export and permanent account wipe.

---

## 🔧 Firebase Configuration & Deployment Guide

### Step 1: Register App in Firebase Console
* **Package Name:** `com.aistudio.bloom.vmkplr`
* **App Nickname:** `Bloom`
* **Debug SHA-1:** `81:C8:0A:39:AB:F4:5B:7A:1E:CA:14:A7:89:85:C0:60:4B:BA:0E:6A`
* **Debug SHA-256:** `8D:1E:4A:D5:52:76:02:76:2F:FA:6A:5E:71:B4:C3:62:63:19:8E:82:82:51:F8:40:75:25:9B:7B:44:93:D5:44`

Place the downloaded `google-services.json` into `/app/google-services.json`.

### Step 2: Deploy Firestore Security Rules
```bash
firebase deploy --only firestore:rules
```

### Step 3: Deploy Firestore Composite Indexes
Deploy the compound queries configuration from `firestore.indexes.json`:
```bash
firebase deploy --only firestore:indexes
```

### Step 4: Deploy Cloud Storage Rules
```bash
firebase deploy --only storage:rules
```

### Step 5: Deploy Cloud Functions
```bash
cd functions
npm install
firebase deploy --only functions
```
**Cloud Functions Included:**
1. `onPostCreated`: Aggregates trending hashtags, executes keyword moderation, and computes initial feed ranking.
2. `onCommentCreated`: Dispatches real-time in-app notifications to post authors.
3. `checkStreaksDaily`: Midnight UTC cron auditing chat activity and maintaining streaks/freezes.
4. `cleanupExpiredStories`: Hourly cron purging 24h expired stories and temporary storage assets.
5. `onStoryFlagged`: Auto-moderation trigger that blurs and quarantines flagged content.
