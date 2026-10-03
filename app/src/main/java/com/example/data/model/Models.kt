package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class User(
    val id: String,
    @Json(name = "first_name") val firstName: String,
    @Json(name = "last_name") val lastName: String,
    @Json(name = "email_or_phone") val emailOrPhone: String,
    val dob: String? = null,
    val gender: String? = null,
    @Json(name = "profile_pic") val profilePic: String? = null,
    @Json(name = "cover_pic") val coverPic: String? = null,
    @Json(name = "is_verified") val isVerified: Boolean = false,
    val bio: String? = "Drop a thought...",
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val postsCount: Int = 0,
    val joinedDate: String = "Joined September 2026"
) {
    val fullName: String get() = "$firstName $lastName".trim()
}

@JsonClass(generateAdapter = true)
data class Reaction(
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val emoji: String // "👍", "❤️", "😆", "😮", "😢", "😡"
)

@JsonClass(generateAdapter = true)
data class Comment(
    val id: String,
    val postId: String,
    val authorName: String,
    val authorAvatar: String,
    val text: String,
    val timestamp: String,
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val replies: List<Comment> = emptyList()
)

@JsonClass(generateAdapter = true)
data class Post(
    val id: String = "",
    @Json(name = "user_id") val userId: String = "",
    @Json(name = "author_name") val authorName: String = "",
    @Json(name = "author_avatar") val authorAvatar: String = "",
    @Json(name = "is_verified") val isVerified: Boolean = false,
    val timestamp: String = "",
    val content: String = "",
    @Json(name = "background_style") val backgroundStyle: String? = null,
    @Json(name = "image_url") val imageUrl: String? = null,
    @Json(name = "video_url") val videoUrl: String? = null,
    @Json(name = "is_video") val isVideo: Boolean = false,
    @Json(name = "likes_count") val likesCount: Int = 0,
    @Json(name = "comments_count") val commentsCount: Int = 0,
    @Json(name = "shares_count") val sharesCount: Int = 0,
    @Json(name = "is_liked") val isLiked: Boolean = false,
    val userReaction: String? = null,
    val reactions: List<Reaction> = emptyList(),
    val commentsList: List<Comment> = emptyList(),
    val privacy: String = "Public"
)

@JsonClass(generateAdapter = true)
data class Story(
    val id: String = "",
    @Json(name = "author_name") val authorName: String = "",
    @Json(name = "author_avatar") val authorAvatar: String = "",
    @Json(name = "story_image") val storyImage: String = "",
    @Json(name = "is_user_story") val isUserStory: Boolean = false,
    @Json(name = "text_content") val textContent: String? = null,
    @Json(name = "background_color") val backgroundColor: String? = null
)

@JsonClass(generateAdapter = true)
data class ChatMessage(
    val id: String,
    @Json(name = "thread_id") val threadId: String,
    @Json(name = "sender_id") val senderId: String,
    @Json(name = "sender_name") val senderName: String,
    @Json(name = "sender_avatar") val senderAvatar: String,
    @Json(name = "is_verified") val isVerified: Boolean = false,
    val message: String,
    val timestamp: String,
    @Json(name = "is_voice_message") val isVoiceMessage: Boolean = false,
    @Json(name = "is_missed_call") val isMissedCall: Boolean = false,
    @Json(name = "unread_count") val unreadCount: Int = 0
)

@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    val status: String, // "success" or "error"
    val message: String? = null,
    val data: T? = null
)

@JsonClass(generateAdapter = true)
data class AuthResponse(
    val status: String,
    val message: String? = null,
    val user: User? = null,
    val token: String? = null
)

@JsonClass(generateAdapter = true)
data class FlikataNotification(
    val id: String,
    val title: String,
    val message: String,
    val type: String = "post", // "post", "like", "friend", "system"
    val timestamp: String = "Just now",
    val isRead: Boolean = false,
    val avatarUrl: String? = null,
    val postThumbnail: String? = null
)

data class PostUploadState(
    val isUploading: Boolean = false,
    val progress: Float = 0f, // 0.0 to 1.0 (0% to 100%)
    val postSnippet: String = "",
    val postImage: String? = null,
    val postVideo: String? = null,
    val isVideo: Boolean = false,
    val isCompleted: Boolean = false
)

@JsonClass(generateAdapter = true)
data class ReelCommentItem(
    val id: String = "",
    val authorName: String = "",
    val authorAvatar: String = "",
    val text: String = "",
    val timestamp: String = "",
    var isLiked: Boolean = false,
    val reactionEmoji: String? = null,
    val isVerified: Boolean = false
)

@JsonClass(generateAdapter = true)
data class ReelModel(
    val id: String = "",
    val userId: String = "",
    val authorName: String = "",
    val authorAvatar: String = "",
    val isVerified: Boolean = false,
    val mediaUrl: String = "",
    val caption: String = "",
    val audioTitle: String = "Original Audio",
    var likesCount: Int = 0,
    var commentsCount: Int = 0,
    var sharesCount: Int = 0,
    var viewsCount: String = "1",
    var isLiked: Boolean = false,
    var userReaction: String? = null,
    var isFollowing: Boolean = false,
    val comments: MutableList<ReelCommentItem> = mutableListOf()
)

@JsonClass(generateAdapter = true)
data class ReelSettingsData(
    @Json(name = "max_duration_seconds") val maxDurationSeconds: Int = 60
)

@JsonClass(generateAdapter = true)
data class UploadData(
    val url: String = "",
    @Json(name = "bucket_id") val bucketId: Int? = null,
    @Json(name = "bucket_name") val bucketName: String? = null,
    val size: Long? = null,
    val mime: String? = null
)

@JsonClass(generateAdapter = true)
data class UploadResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: UploadData? = null
)

@JsonClass(generateAdapter = true)
data class GenericResponse(
    val success: Boolean = false,
    val message: String? = null
)

@JsonClass(generateAdapter = true)
data class CommentResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: Comment? = null
)

@JsonClass(generateAdapter = true)
data class StoriesResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<Story>? = null
)

