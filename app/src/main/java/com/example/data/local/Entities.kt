package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val firstName: String,
    val lastName: String,
    val emailOrPhone: String,
    val dob: String?,
    val gender: String?,
    val profilePic: String?,
    val coverPic: String?,
    val isVerified: Boolean,
    val bio: String?,
    val followersCount: Int,
    val followingCount: Int,
    val postsCount: Int,
    val joinedDate: String,
    val isLoggedIn: Boolean = true
)

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val authorName: String,
    val authorAvatar: String,
    val isVerified: Boolean,
    val timestamp: String,
    val content: String,
    val backgroundStyle: String?,
    val imageUrl: String?,
    val videoUrl: String? = null,
    val isVideo: Boolean = false,
    val likesCount: Int,
    val commentsCount: Int,
    val sharesCount: Int,
    val isLiked: Boolean,
    val userReaction: String? = null,
    val privacy: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val id: String,
    val threadId: String,
    val senderName: String,
    val senderAvatar: String,
    val isVerified: Boolean,
    val lastMessage: String,
    val timestamp: String,
    val isVoiceMessage: Boolean,
    val isMissedCall: Boolean,
    val unreadCount: Int
)
