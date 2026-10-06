# Flikata Complete PHP + MySQL + Cloudflare R2 Backend

This is the complete, production-ready backend engine for the **Flikata Social Android Application**.

---

## 📁 Directory Structure

```text
backend/
├── database.sql                  # Complete MySQL schema & tables
├── storage.php                   # Cloudflare R2 Multi-Bucket Web Manager (Open in browser)
├── r2_helper.php                 # Pure PHP AWS SigV4 Cloudflare R2 integration & auto-failover
├── upload.php                    # Direct Media Upload API (Auto bucket selector & quota manager)
├── config/
│   └── db.php                    # PDO MySQL database connection & response helpers
├── auth/
│   ├── register.php              # User registration
│   └── login.php                 # User login & token generation
├── posts/
│   ├── get_posts.php             # Feed posts with author, comments, likes, reactions
│   ├── create_post.php           # Post creation (supports text, images, videos)
│   ├── toggle_like.php           # Like / unlike posts
│   ├── set_reaction.php          # Emoji reaction (Like, Love, Haha, Wow, Sad, Angry)
│   └── delete_post.php           # Delete post
├── comments/
│   ├── get_comments.php          # Get post or reel comments
│   └── add_comment.php           # Add comment
├── reels/
│   ├── get_reels.php             # Full reels video feed
│   └── add_reel.php              # Upload & publish reel
├── stories/
│   ├── get_stories.php           # Active 24h stories
│   └── add_story.php             # Post story with background/image
├── chats/
│   ├── get_conversations.php     # Message threads & active online contacts
│   ├── get_messages.php          # Conversation history with replies
│   ├── send_message.php          # Send text/image/video/audio call notice
│   ├── edit_message.php          # Edit sent message
│   └── delete_message.php        # Delete message
├── profile/
│   ├── get_profile.php           # User profile & stats (posts, reels, photos count)
│   └── update_profile.php        # Update bio, avatar, banner, details
└── notifications/
    ├── get_notifications.php     # User notifications list
    └── mark_read.php             # Mark as read
```

---

## 🚀 Setup & Installation (3 Simple Steps)

### Step 1: Import Database
1. Open **phpMyAdmin** or your MySQL command line.
2. Select database `flikata.bd` with `utf8mb4_unicode_ci` collation.
3. Import the file `database.sql`.

### Step 2: Configure Database Connection
Already pre-configured in `backend/config/db.php`:

```php
define('DB_HOST', 'localhost');
define('DB_NAME', 'flikata.bd');
define('DB_USER', 'flikata.bd');
define('DB_PASS', 'flikata.bd');
```

---

## ☁️ Step 3: Cloudflare R2 Multi-Bucket Setup (`storage.php`)

Open `storage.php` in your web browser:
```text
https://yourdomain.com/backend/storage.php
```

### Features:
1. **Add Unlimited Cloudflare R2 Buckets**: Click the blue `+` button at the bottom right. Fill in:
   - **Account Label**: e.g., `Primary Media R2`
   - **Bucket Name**: e.g., `flikata-media`
   - **Cloudflare Account ID**: (Found on your Cloudflare Dashboard -> R2 Overview)
   - **Access Key ID**: (Generated in Cloudflare R2 API Tokens)
   - **Secret Access Key**: (Generated in Cloudflare R2 API Tokens)
   - **Public URL Endpoint (CDN/R2)**: e.g., `https://pub-xxxxxxxx.r2.dev` or your custom domain `https://media.yourdomain.com`
   - **Account Limit (GB)**: e.g., `10.0` GB
2. **Quota & Automatic Rollover**:
   - Each bucket has a progress bar tracking `Used GB / Limit GB`.
   - When an active bucket reaches its GB limit, the system **automatically rolls over and uploads to the next active bucket** without any disruption!
3. **Active / Inactive Toggle**:
   - Easily enable or disable any bucket with a single click.
4. **Test Connection Button**:
   - Click the **"Test"** button on any card. It sends a live S3 SigV4 ping to Cloudflare. If credentials are correct, a green **"Connection Successful"** modal appears. If there is any error, it displays the exact error message.
5. **Edit & Delete**:
   - Modify keys, URLs, or storage limits anytime.

---

## 📱 Connecting Android App to the Backend

In the Android app, define your backend URL:
```kotlin
object ApiConstants {
    const val BASE_URL = "https://yourdomain.com/backend/"
}
```
All endpoints output standard, high-speed JSON matching the app's models (`Post`, `Comment`, `ChatMessage`, `Story`, `ReelModel`, `User`).
