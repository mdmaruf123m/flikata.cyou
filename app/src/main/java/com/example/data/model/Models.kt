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
    val joinedDate: String = "Joined September 2026",
    @Json(name = "auth_token") val authToken: String? = null,
    @Json(name = "is_online") val isOnline: Boolean = true
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
    val id: String = "",
    val postId: String = "",
    val authorName: String = "",
    val authorAvatar: String = "",
    val isVerified: Boolean = false,
    val text: String = "",
    val timestamp: String = "",
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val replies: List<Comment> = emptyList()
)

@JsonClass(generateAdapter = true)
data class CommentsListResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<Comment>? = null
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
    @Json(name = "threadId") val threadIdRaw: String? = null,
    @Json(name = "thread_id") val threadIdAlt: String? = null,
    @Json(name = "senderId") val senderIdRaw: String? = null,
    @Json(name = "sender_id") val senderIdAlt: String? = null,
    @Json(name = "senderName") val senderNameRaw: String? = null,
    @Json(name = "sender_name") val senderNameAlt: String? = null,
    @Json(name = "senderAvatar") val senderAvatarRaw: String? = null,
    @Json(name = "sender_avatar") val senderAvatarAlt: String? = null,
    @Json(name = "is_verified") val isVerifiedAlt: Boolean = false,
    @Json(name = "isVerified") val isVerifiedRaw: Boolean = false,
    val message: String = "",
    val timestamp: String = "",
    @Json(name = "is_voice_message") val isVoiceMessage: Boolean = false,
    @Json(name = "is_missed_call") val isMissedCall: Boolean = false,
    @Json(name = "unread_count") val unreadCountAlt: Int = 0,
    @Json(name = "unreadCount") val unreadCountRaw: Int = 0
) {
    val threadId: String get() = threadIdRaw ?: threadIdAlt ?: id
    val senderId: String get() = senderIdRaw ?: senderIdAlt ?: id
    val senderName: String get() = senderNameRaw ?: senderNameAlt ?: "User"
    val senderAvatar: String get() = senderAvatarRaw ?: senderAvatarAlt ?: ""
    val isVerified: Boolean get() = isVerifiedRaw || isVerifiedAlt
    val unreadCount: Int get() = if (unreadCountRaw > 0) unreadCountRaw else unreadCountAlt
}

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
    var viewsCount: String = "0",
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

@JsonClass(generateAdapter = true)
data class ReelsResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<ReelModel>? = null
)

@JsonClass(generateAdapter = true)
data class ReelSettingsResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: ReelSettingsData? = null
)

@JsonClass(generateAdapter = true)
data class PresignedUploadData(
    @Json(name = "upload_url") val uploadUrl: String = "",
    @Json(name = "file_url") val fileUrl: String = "",
    @Json(name = "bucket_id") val bucketId: Int? = null,
    val key: String? = null
)

@JsonClass(generateAdapter = true)
data class PresignedUploadResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: PresignedUploadData? = null
)

@JsonClass(generateAdapter = true)
data class ServerMessageItem(
    val id: String = "",
    val text: String = "",
    @Json(name = "imageUrl") val imageUrl: String? = null,
    @Json(name = "videoUrl") val videoUrl: String? = null,
    val isMe: Boolean = false,
    val time: String = "Just now",
    val isEdited: Boolean = false,
    val isCallNotice: Boolean = false
)

@JsonClass(generateAdapter = true)
data class ServerMessagesData(
    @Json(name = "conversation_id") val conversationId: Int? = null,
    val messages: List<ServerMessageItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ServerMessagesResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: ServerMessagesData? = null
)

@JsonClass(generateAdapter = true)
data class UserProfileData(
    val id: Int? = null,
    @Json(name = "full_name") val fullName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    @Json(name = "profile_pic") val profilePic: String? = null,
    @Json(name = "cover_pic") val coverPic: String? = null,
    val bio: String? = null,
    val occupation: String? = null,
    val location: String? = null,
    val gender: String? = null,
    val dob: String? = null,
    @Json(name = "is_verified") val isVerified: Int? = null,
    @Json(name = "is_online") val isOnline: Int? = null,
    @Json(name = "posts_count") val postsCount: Int? = 0,
    @Json(name = "photos_count") val photosCount: Int? = 0,
    @Json(name = "reels_count") val reelsCount: Int? = 0,
    val joinedDate: String? = null
) {
    val idString: String get() = id?.toString() ?: ""
    val isVerifiedBool: Boolean get() = isVerified == 1
    val isOnlineBool: Boolean get() = isOnline == 1
}

@JsonClass(generateAdapter = true)
data class UserProfileResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: UserProfileData? = null
)

