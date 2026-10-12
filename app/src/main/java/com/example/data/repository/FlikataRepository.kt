package com.example.data.repository

import com.example.data.local.ChatDao
import com.example.data.local.ChatEntity
import com.example.data.local.PostDao
import com.example.data.local.PostEntity
import com.example.data.local.UserDao
import com.example.data.local.UserEntity
import com.example.data.model.ChatMessage
import com.example.data.model.Comment
import com.example.data.model.Post
import com.example.data.model.Reaction
import com.example.data.model.ReelModel
import com.example.data.model.ReelSettingsData
import com.example.data.model.Story
import com.example.data.model.User
import com.example.data.model.UserProfileData
import com.example.data.remote.ApiClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.Buffer
import okio.BufferedSink
import okio.ForwardingSink
import okio.buffer
import java.util.UUID

private class ChunkedProgressRequestBody(
    private val bytes: ByteArray,
    private val mediaType: MediaType?,
    private val onProgress: (Float) -> Unit
) : RequestBody() {
    override fun contentType() = mediaType
    override fun contentLength() = bytes.size.toLong()

    override fun writeTo(sink: BufferedSink) {
        val total = bytes.size.toLong()
        if (total <= 0L) {
            onProgress(1f)
            return
        }
        var written = 0L
        val chunkSize = 32 * 1024 // 32 KB chunk for smooth network streaming
        var offset = 0
        while (offset < bytes.size) {
            val len = minOf(chunkSize, bytes.size - offset)
            sink.write(bytes, offset, len)
            sink.flush()
            offset += len
            written += len
            val progress = (written.toFloat() / total.toFloat()).coerceIn(0f, 1f)
            onProgress(progress)
        }
    }
}

class FlikataRepository(
    private val userDao: UserDao,
    private val postDao: PostDao,
    private val chatDao: ChatDao
) {
    suspend fun getAllPostsDirect(): List<Post> {
        return postDao.getAllPostsList().map { it.toModel() }
    }

    suspend fun getVideoPostsDirect(): List<Post> {
        return postDao.getVideoPosts().map { it.toModel() }
    }
    val loggedInUser: Flow<User?> = userDao.getLoggedInUserFlow().map { entity ->
        val model = entity?.toModel()
        if (model != null) {
            ApiClient.currentAuthToken = model.authToken
            ApiClient.currentUserId = model.id
        }
        model
    }

    val allPosts: Flow<List<Post>> = postDao.getAllPosts().map { list ->
        list.map { it.toModel() }
    }

    val allChats: Flow<List<ChatMessage>> = chatDao.getAllChats().map { list ->
        list.map { it.toModel() }
    }

    suspend fun updateUser(user: User) {
        userDao.insertUser(user.toEntity(isLoggedIn = true))
        try {
            ApiClient.apiService.updateProfile(
                userId = user.id,
                fullName = user.fullName,
                bio = user.bio,
                occupation = null,
                location = null,
                profilePic = user.profilePic,
                coverPic = user.coverPic
            )
        } catch (e: Exception) {
            android.util.Log.e("FlikataProfile", "Failed to sync profile to server: ${e.message}")
        }
    }

    suspend fun fetchUserProfile(userId: String): Result<UserProfileData> {
        val cleanId = userId.removePrefix("user_").removePrefix("partner_").trim()
        if (cleanId.isBlank()) return Result.failure(Exception("Invalid user ID"))
        return try {
            val resp = ApiClient.apiService.getProfile(cleanId)
            if (resp.isSuccessful && resp.body()?.success == true && resp.body()?.data != null) {
                Result.success(resp.body()!!.data!!)
            } else {
                Result.failure(Exception(resp.body()?.message ?: "Failed to load user profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(emailOrPhone: String, password: String): Result<User> {
        return try {
            val response = ApiClient.apiService.login(emailOrPhone, password)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == "success" && body.user != null) {
                    val userWithToken = if (!body.token.isNullOrBlank()) {
                        body.user.copy(authToken = body.token)
                    } else {
                        body.user
                    }
                    saveUserSession(userWithToken)
                    Result.success(userWithToken)
                } else {
                    val errMsg = body?.message ?: "Invalid email/phone or password."
                    Result.failure(Exception(errMsg))
                }
            } else {
                Result.failure(Exception("Server returned error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to connect to server. Please check internet connection."))
        }
    }

    suspend fun register(
        firstName: String,
        lastName: String,
        accountType: String,
        emailOrPhone: String,
        dob: String,
        gender: String,
        password: String
    ): Result<User> {
        return try {
            val response = ApiClient.apiService.register(
                firstName = firstName,
                lastName = lastName,
                accountType = accountType,
                emailOrPhone = emailOrPhone,
                dob = dob,
                gender = gender,
                password = password
            )
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == "success" && body.user != null) {
                    val userWithToken = if (!body.token.isNullOrBlank()) {
                        body.user.copy(authToken = body.token)
                    } else {
                        body.user
                    }
                    saveUserSession(userWithToken)
                    Result.success(userWithToken)
                } else {
                    val errMsg = body?.message ?: "Registration failed."
                    Result.failure(Exception(errMsg))
                }
            } else {
                Result.failure(Exception("Server returned error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to connect to server. Please check internet connection."))
        }
    }

    private suspend fun saveUserSession(user: User) {
        ApiClient.currentAuthToken = user.authToken
        ApiClient.currentUserId = user.id
        userDao.logoutAll()
        userDao.insertUser(user.toEntity(isLoggedIn = true))
    }

    suspend fun logout() {
        ApiClient.currentAuthToken = null
        ApiClient.currentUserId = null
        userDao.logoutAll()
    }

    suspend fun getLoggedInUserDirect(): User? {
        val entity = userDao.getLoggedInUser() ?: return null
        val model = entity.toModel()
        ApiClient.currentAuthToken = model.authToken
        ApiClient.currentUserId = model.id
        return model
    }

    suspend fun syncPostsFromServer(): Result<List<Post>> {
        return try {
            val user = userDao.getLoggedInUser()
            val response = ApiClient.apiService.getPosts(userId = user?.id)
            if (response.isSuccessful) {
                val posts = response.body() ?: emptyList()
                if (posts.isNotEmpty()) {
                    postDao.syncAllPosts(posts.map { it.toEntity() })
                }
                Result.success(posts)
            } else {
                Result.failure(Exception("Failed to fetch posts from server"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun editPost(postId: String, content: String, privacy: String): Result<Boolean> {
        val user = userDao.getLoggedInUser() ?: return Result.failure(Exception("Please log in first"))
        return try {
            val resp = ApiClient.apiService.editPost(
                postId = postId,
                userId = user.id,
                content = content,
                privacy = privacy
            )
            if (resp.isSuccessful && resp.body()?.success == true) {
                postDao.updatePost(postId, content, privacy)
                syncPostsFromServer()
                Result.success(true)
            } else {
                Result.failure(Exception(resp.body()?.message ?: "Failed to edit post"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncChatsFromServer(userId: String): Result<List<ChatMessage>> {
        return try {
            val user = userDao.getLoggedInUser()?.toModel()
            val token = user?.authToken ?: ApiClient.currentAuthToken
            val myId = user?.id ?: userId
            val response = ApiClient.apiService.getChats(userId = myId, authToken = token)
            if (response.isSuccessful) {
                val chats = response.body() ?: emptyList()
                if (chats.isNotEmpty()) {
                    chatDao.syncAllChats(chats.map { it.toEntity() })
                }
                Result.success(chats)
            } else {
                Result.failure(Exception("Failed to fetch chats from server: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchConversationMessages(partnerId: String): Result<List<com.example.data.model.ServerMessageItem>> {
        return try {
            val cleanPartnerId = partnerId.removePrefix("partner_").removePrefix("user_").trim()
            val user = userDao.getLoggedInUser()?.toModel()
            val token = user?.authToken ?: ApiClient.currentAuthToken
            val myId = user?.id ?: ApiClient.currentUserId
            val authHeader = if (!token.isNullOrBlank()) "Bearer $token" else null

            val response = ApiClient.apiService.getMessages(
                partnerId = cleanPartnerId,
                userId = myId,
                authToken = token,
                authHeader = authHeader
            )
            if (response.isSuccessful && response.body()?.success == true) {
                val msgList = response.body()?.data?.messages ?: emptyList()
                Result.success(msgList)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendChatMessage(
        partnerId: String,
        partnerName: String,
        partnerAvatar: String,
        message: String
    ): Result<Boolean> {
        val cleanPartnerId = partnerId.removePrefix("partner_").removePrefix("user_").trim()
        val currentUser = userDao.getLoggedInUser()?.toModel()
        val timestamp = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()).format(java.util.Date())
        val token = currentUser?.authToken ?: ApiClient.currentAuthToken
        val myId = currentUser?.id ?: ApiClient.currentUserId
        val authHeader = if (!token.isNullOrBlank()) "Bearer $token" else null

        val chatEntity = com.example.data.local.ChatEntity(
            id = cleanPartnerId,
            threadId = "t_${cleanPartnerId.hashCode()}",
            senderId = cleanPartnerId,
            senderName = partnerName,
            senderAvatar = partnerAvatar,
            isVerified = false,
            lastMessage = message,
            timestamp = timestamp,
            isVoiceMessage = false,
            isMissedCall = false,
            unreadCount = 0
        )
        chatDao.insertChats(listOf(chatEntity))

        try {
            val resp = ApiClient.apiService.sendMessage(
                partnerId = cleanPartnerId,
                text = message,
                message = message,
                senderId = myId,
                userId = myId,
                receiverId = cleanPartnerId,
                senderName = currentUser?.fullName,
                senderAvatar = currentUser?.profilePic ?: "",
                authToken = token,
                authHeader = authHeader
            )
            if (resp.isSuccessful) {
                android.util.Log.d("FlikataChat", "Message sent successfully to server!")
            } else {
                android.util.Log.w("FlikataChat", "Server message response code: ${resp.code()}")
            }
            if (myId != null) {
                syncChatsFromServer(myId)
            }
        } catch (e: Exception) {
            android.util.Log.e("FlikataChat", "Failed to send message: ${e.message}", e)
        }
        return Result.success(true)
    }


    suspend fun createPost(
        content: String,
        backgroundStyle: String? = null,
        privacy: String = "Public",
        imageUrl: String? = null,
        videoUrl: String? = null,
        isVideo: Boolean = false
    ): Result<Post> {
        val currentUser = userDao.getLoggedInUser()?.toModel()
            ?: return Result.failure(Exception("Please log in first"))
        val tempId = UUID.randomUUID().toString()
        val newPost = Post(
            id = tempId,
            userId = currentUser.id,
            authorName = currentUser.fullName,
            authorAvatar = currentUser.profilePic ?: "",
            isVerified = currentUser.isVerified,
            timestamp = "Just now",
            content = content,
            backgroundStyle = backgroundStyle,
            imageUrl = imageUrl,
            videoUrl = videoUrl,
            isVideo = isVideo,
            likesCount = 0,
            commentsCount = 0,
            sharesCount = 0,
            isLiked = false,
            privacy = privacy
        )

        postDao.insertPost(newPost.toEntity())

        try {
            val resp = ApiClient.apiService.createPost(
                userId = currentUser.id,
                authorName = currentUser.fullName,
                content = content,
                backgroundStyle = backgroundStyle,
                privacy = privacy,
                imageUrl = imageUrl,
                videoUrl = videoUrl
            )
            if (resp.isSuccessful && resp.body() != null) {
                val serverPost = resp.body()!!
                postDao.deletePost(tempId)
                postDao.insertPost(serverPost.toEntity())
                return Result.success(serverPost)
            } else {
                android.util.Log.e("FlikataPost", "Server createPost error: ${resp.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            android.util.Log.e("FlikataPost", "Error creating post on server: ${e.message}", e)
        }

        return Result.success(newPost)
    }

    suspend fun uploadMediaFile(
        bytes: ByteArray, 
        fileName: String, 
        mimeType: String,
        folder: String? = null,
        onProgress: (Float) -> Unit = {}
    ): Result<String> {
        val targetFolder = folder ?: if (mimeType.startsWith("video")) "reels" else "images"

        // 1. Try Direct Presigned Cloudflare R2 Upload first (bypasses any PHP upload size limits!)
        try {
            val presignedResp = ApiClient.apiService.getUploadUrl(
                folder = targetFolder,
                filename = fileName,
                mime = mimeType
            )
            if (presignedResp.isSuccessful && presignedResp.body()?.success == true) {
                val uploadData = presignedResp.body()?.data
                if (uploadData != null && uploadData.uploadUrl.isNotBlank()) {
                    val client = okhttp3.OkHttpClient.Builder()
                        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                        .writeTimeout(180, java.util.concurrent.TimeUnit.SECONDS)
                        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                        .build()

                    val reqBody = ChunkedProgressRequestBody(bytes, mimeType.toMediaTypeOrNull(), onProgress)
                    val request = okhttp3.Request.Builder()
                        .url(uploadData.uploadUrl)
                        .put(reqBody)
                        .addHeader("Content-Type", mimeType)
                        .build()

                    val directResp = client.newCall(request).execute()
                    if (directResp.isSuccessful) {
                        onProgress(1f)
                        android.util.Log.d("FlikataUpload", "Direct Cloudflare R2 upload SUCCESS: ${uploadData.fileUrl}")
                        return Result.success(uploadData.fileUrl)
                    } else {
                        android.util.Log.w("FlikataUpload", "Direct R2 upload returned ${directResp.code}, falling back to upload.php")
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("FlikataUpload", "Direct R2 presigned attempt failed: ${e.message}, falling back to upload.php")
        }

        // 2. Fallback to upload.php
        return try {
            val countingBody = ChunkedProgressRequestBody(bytes, mimeType.toMediaTypeOrNull(), onProgress)
            val part = MultipartBody.Part.createFormData("file", fileName, countingBody)
            val folderPart = targetFolder.toRequestBody("text/plain".toMediaTypeOrNull())
            val response = ApiClient.apiService.uploadMedia(part, folderPart)
            if (response.isSuccessful && response.body()?.success == true) {
                onProgress(1f)
                val url = response.body()?.data?.url
                if (!url.isNullOrBlank()) {
                    Result.success(url)
                } else {
                    Result.failure(Exception("Cloudflare R2 returned empty URL"))
                }
            } else {
                val err = response.body()?.message ?: "Upload to Cloudflare R2 failed: ${response.code()}"
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleLike(postId: String) {
        postDao.toggleLike(postId)
        val user = userDao.getLoggedInUser()
        if (user != null) {
            try {
                ApiClient.apiService.toggleLike(postId, user.id)
            } catch (_: Exception) {}
        }
    }

    suspend fun deletePost(postId: String): Result<Boolean> {
        postDao.deletePost(postId)
        val user = userDao.getLoggedInUser()
        if (user != null) {
            try {
                ApiClient.apiService.deletePost(postId, user.id)
            } catch (e: Exception) {
                android.util.Log.e("FlikataDelete", "Error deleting post from server: ${e.message}", e)
            }
        }
        return Result.success(true)
    }

    suspend fun syncReelsFromServer(): Result<List<ReelModel>> {
        return try {
            val user = userDao.getLoggedInUser()
            val resp = ApiClient.apiService.getReels(user?.id)
            if (resp.isSuccessful && resp.body()?.success == true && resp.body()?.data != null) {
                val uniqueReels = resp.body()!!.data!!.distinctBy { it.mediaUrl }
                Result.success(uniqueReels)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addReel(mediaUrl: String, caption: String?, audioTitle: String? = null): Result<Boolean> {
        val user = userDao.getLoggedInUser() ?: return Result.failure(Exception("Please log in first"))
        return try {
            val resp = ApiClient.apiService.addReel(
                userId = user.id,
                mediaUrl = mediaUrl,
                caption = caption,
                audioTitle = audioTitle
            )
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success(true)
            } else {
                Result.failure(Exception(resp.body()?.message ?: "Failed to upload reel"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteReel(reelId: String): Result<Boolean> {
        val user = userDao.getLoggedInUser() ?: return Result.failure(Exception("Please log in first"))
        return try {
            val resp = ApiClient.apiService.deleteReel(
                reelId = reelId,
                userId = user.id
            )
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success(true)
            } else {
                Result.failure(Exception(resp.body()?.message ?: "Failed to delete reel"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleReelLike(reelId: String, reaction: String = "Like"): Result<Boolean> {
        val user = userDao.getLoggedInUser() ?: return Result.failure(Exception("Please log in first"))
        return try {
            val resp = ApiClient.apiService.toggleReelLike(reelId, user.id, reaction)
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success(true)
            } else {
                Result.failure(Exception(resp.body()?.message ?: "Failed to like reel"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setReelReaction(reelId: String, reaction: String): Result<Boolean> {
        val user = userDao.getLoggedInUser() ?: return Result.failure(Exception("Please log in first"))
        return try {
            val resp = ApiClient.apiService.setReelReaction(reelId, user.id, reaction)
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success(true)
            } else {
                Result.failure(Exception(resp.body()?.message ?: "Failed to set reaction"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun recordReelView(reelId: String): Result<Boolean> {
        val user = userDao.getLoggedInUser()
        return try {
            val resp = ApiClient.apiService.recordReelView(reelId, user?.id)
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to record view"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addReelComment(reelId: String, text: String): Result<Comment?> {
        val user = userDao.getLoggedInUser()
        val userId = user?.id ?: "1"
        return try {
            val targetPostId = if (reelId.startsWith("post_")) reelId.removePrefix("post_") else null
            val targetReelId = if (reelId.startsWith("post_")) null else reelId
            val resp = ApiClient.apiService.addComment(
                postId = targetPostId,
                reelId = targetReelId,
                userId = userId,
                text = text
            )
            if (targetPostId != null) {
                try {
                    postDao.incrementCommentsCount(targetPostId)
                } catch (_: Exception) {}
            }
            if (resp.isSuccessful && resp.body()?.success == true) {
                Result.success(resp.body()?.data)
            } else {
                Result.failure(Exception(resp.body()?.message ?: "Failed to add comment"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getReelMaxDurationSeconds(): Int {
        return try {
            val resp = ApiClient.apiService.getReelSettings()
            if (resp.isSuccessful && resp.body()?.success == true && resp.body()?.data != null) {
                resp.body()!!.data!!.maxDurationSeconds
            } else {
                60
            }
        } catch (_: Exception) {
            60
        }
    }

    suspend fun setReaction(postId: String, emoji: String?) {
        postDao.updateReaction(postId, emoji)
        val user = userDao.getLoggedInUser()
        if (user != null) {
            try {
                ApiClient.apiService.setReaction(postId, user.id, emoji ?: "")
            } catch (_: Exception) {}
        }
    }

    suspend fun getPostComments(postId: String): Result<List<Comment>> {
        return try {
            val resp = ApiClient.apiService.getPostComments(postId)
            if (resp.isSuccessful && resp.body()?.success == true && resp.body()?.data != null) {
                Result.success(resp.body()!!.data!!)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getReelComments(reelId: String): Result<List<Comment>> {
        return try {
            val resp = ApiClient.apiService.getReelComments(reelId)
            if (resp.isSuccessful && resp.body()?.success == true && resp.body()?.data != null) {
                Result.success(resp.body()!!.data!!)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addComment(postId: String, text: String): Result<Comment> {
        val user = userDao.getLoggedInUser() ?: return Result.failure(Exception("Please log in first"))
        return try {
            val resp = ApiClient.apiService.addComment(
                postId = postId,
                reelId = null,
                userId = user.id,
                text = text
            )
            if (resp.isSuccessful && resp.body()?.success == true && resp.body()?.data != null) {
                syncPostsFromServer()
                Result.success(resp.body()!!.data!!)
            } else {
                Result.failure(Exception(resp.body()?.message ?: "Failed to add comment"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearAllPosts() {
        postDao.clearPosts()
    }

    suspend fun seedInitialDataIfEmpty() {
        // Do not seed any dummy user - require real registration/login!
        // Start fresh with clean database
    }

    fun getSampleStories(): List<Story> {
        return listOf(
            Story(
                id = "story_create",
                authorName = "Create story",
                authorAvatar = "",
                storyImage = "",
                isUserStory = true
            )
        )
    }

    suspend fun syncStoriesFromServer(): Result<List<Story>> {
        return try {
            val user = userDao.getLoggedInUser()
            val response = ApiClient.apiService.getStories(user?.id)
            if (response.isSuccessful && response.body()?.success == true) {
                val remoteStories = response.body()?.data ?: emptyList()
                val list = mutableListOf<Story>()
                list.add(
                    Story(
                        id = "story_create",
                        authorName = "Create story",
                        authorAvatar = user?.profilePic ?: "",
                        storyImage = "",
                        isUserStory = true
                    )
                )
                list.addAll(remoteStories)
                Result.success(list)
            } else {
                Result.failure(Exception("Failed to fetch stories: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addStory(
        storyImage: String?,
        textContent: String?,
        backgroundColor: String?
    ): Result<Boolean> {
        return try {
            val user = userDao.getLoggedInUser()
            if (user == null) {
                return Result.failure(Exception("User not logged in"))
            }
            val response = ApiClient.apiService.addStory(
                userId = user.id,
                storyImage = storyImage,
                textContent = textContent,
                backgroundColor = backgroundColor
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to add story"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getNotifications(): List<com.example.data.model.FlikataNotification> {
        val user = userDao.getLoggedInUser()
        val uid = user?.id ?: ApiClient.currentUserId ?: ""
        return try {
            val response = ApiClient.apiService.getNotifications(userId = uid)
            if (response.isSuccessful && response.body()?.data != null) {
                response.body()!!.data!!
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun markNotificationsRead(id: String? = null) {
        val user = userDao.getLoggedInUser()
        val uid = user?.id ?: ApiClient.currentUserId ?: ""
        try {
            ApiClient.apiService.markNotificationsRead(userId = uid, notificationId = id)
        } catch (e: Exception) {
            // ignore
        }
    }

    suspend fun deleteNotification(id: String? = null) {
        val user = userDao.getLoggedInUser()
        val uid = user?.id ?: ApiClient.currentUserId ?: ""
        try {
            ApiClient.apiService.deleteNotification(userId = uid, notificationId = id)
        } catch (e: Exception) {
            // ignore
        }
    }

    suspend fun updateUserLocal(user: User) {
        userDao.insertUser(user.toEntity(isLoggedIn = true))
    }
}

private fun UserEntity.toModel() = User(
    id = id,
    firstName = firstName,
    lastName = lastName,
    emailOrPhone = emailOrPhone,
    dob = dob,
    gender = gender,
    profilePic = profilePic,
    coverPic = coverPic,
    isVerified = isVerified,
    bio = bio,
    followersCount = followersCount,
    followingCount = followingCount,
    postsCount = postsCount,
    joinedDate = joinedDate,
    authToken = authToken
)

private fun User.toEntity(isLoggedIn: Boolean = true) = UserEntity(
    id = id,
    firstName = firstName,
    lastName = lastName,
    emailOrPhone = emailOrPhone,
    dob = dob,
    gender = gender,
    profilePic = profilePic,
    coverPic = coverPic,
    isVerified = isVerified,
    bio = bio,
    followersCount = followersCount,
    followingCount = followingCount,
    postsCount = postsCount,
    joinedDate = joinedDate,
    authToken = authToken,
    isLoggedIn = isLoggedIn
)

private fun PostEntity.toModel(): Post {
    return Post(
        id = id,
        userId = userId,
        authorName = authorName,
        authorAvatar = authorAvatar,
        isVerified = isVerified,
        timestamp = timestamp,
        content = content,
        backgroundStyle = backgroundStyle,
        imageUrl = imageUrl,
        videoUrl = videoUrl,
        isVideo = isVideo,
        likesCount = likesCount,
        commentsCount = commentsCount,
        sharesCount = sharesCount,
        isLiked = isLiked,
        userReaction = userReaction,
        reactions = emptyList(),
        commentsList = emptyList(),
        privacy = privacy
    )
}

private fun Post.toEntity() = PostEntity(
    id = id,
    userId = userId,
    authorName = authorName,
    authorAvatar = authorAvatar,
    isVerified = isVerified,
    timestamp = timestamp,
    content = content,
    backgroundStyle = backgroundStyle,
    imageUrl = imageUrl,
    videoUrl = videoUrl,
    isVideo = isVideo,
    likesCount = likesCount,
    commentsCount = commentsCount,
    sharesCount = sharesCount,
    isLiked = isLiked,
    userReaction = userReaction,
    privacy = privacy
)

private fun ChatEntity.toModel() = ChatMessage(
    id = id,
    threadIdRaw = threadId,
    senderIdRaw = if (senderId.isNotBlank()) senderId else id,
    senderNameRaw = senderName,
    senderAvatarRaw = senderAvatar,
    isVerifiedRaw = isVerified,
    message = lastMessage,
    timestamp = timestamp,
    isVoiceMessage = isVoiceMessage,
    isMissedCall = isMissedCall,
    unreadCountRaw = unreadCount
)

private fun ChatMessage.toEntity(): ChatEntity {
    val cleanSenderId = senderId.removePrefix("partner_").removePrefix("user_").trim()
    val partnerKey = if (cleanSenderId.isNotBlank() && cleanSenderId != "partner") cleanSenderId else id
    return ChatEntity(
        id = partnerKey,
        threadId = threadId,
        senderId = partnerKey,
        senderName = senderName,
        senderAvatar = senderAvatar,
        isVerified = isVerified,
        lastMessage = message,
        timestamp = timestamp,
        isVoiceMessage = isVoiceMessage,
        isMissedCall = isMissedCall,
        unreadCount = unreadCount
    )
}
