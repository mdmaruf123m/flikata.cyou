package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.FlikataDatabase
import com.example.data.model.ChatMessage
import com.example.data.model.FlikataNotification
import com.example.data.model.Post
import com.example.data.model.PostUploadState
import com.example.data.model.ReelCommentItem
import com.example.data.model.ReelModel
import com.example.data.model.Story
import com.example.data.model.User
import com.example.data.repository.FlikataRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class Screen {
    SPLASH,
    LOGIN,
    REGISTER,
    HOME,
    REELS,
    CREATE_POST,
    CREATE_STORY,
    CHATS,
    CHAT_DETAIL,
    PROFILE,
    EDIT_PROFILE,
    SEARCH,
    STORY_VIEWER,
    FULL_IMAGE_VIEWER,
    NOTIFICATIONS,
    MENU,
    FRIENDS
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FlikataDatabase.getDatabase(application)
    val repository = FlikataRepository(db.userDao(), db.postDao(), db.chatDao())

    val isDarkMode = MutableStateFlow(false)
    val highlightedPostId = MutableStateFlow<String?>(null)
    val selectedUserProfileId = MutableStateFlow<String?>(null)
    val selectedUserProfileName = MutableStateFlow<String?>(null)
    val targetReelId = MutableStateFlow<String?>(null)

    fun openPost(postId: String) {
        highlightedPostId.value = postId
        _currentScreen.value = Screen.HOME
    }

    fun clearHighlightedPost() {
        highlightedPostId.value = null
    }

    fun openUserProfile(userId: String, userName: String = "") {
        val myId = currentUser.value?.id ?: ""
        if (userId.isNotBlank() && userId == myId) {
            selectedUserProfileId.value = null
            selectedUserProfileName.value = null
            _currentScreen.value = Screen.PROFILE
        } else {
            selectedUserProfileId.value = userId.ifBlank { null }
            selectedUserProfileName.value = userName.ifBlank { null }
            _currentScreen.value = Screen.PROFILE
        }
    }

    fun clearSelectedUserProfile() {
        selectedUserProfileId.value = null
        selectedUserProfileName.value = null
    }

    fun openReel(reelId: String) {
        targetReelId.value = reelId
        _currentScreen.value = Screen.REELS
    }

    init {
        viewModelScope.launch {
            repository.allPosts.collect { currentPosts ->
                val videoReels = currentPosts.filter { !it.videoUrl.isNullOrBlank() }.map { p ->
                    ReelModel(
                        id = "post_${p.id}",
                        userId = p.userId,
                        authorName = p.authorName,
                        authorAvatar = p.authorAvatar,
                        isVerified = p.isVerified,
                        mediaUrl = p.videoUrl!!,
                        caption = p.content.ifBlank { "Video Reel" },
                        audioTitle = "Original Audio - ${p.authorName}",
                        likesCount = p.likesCount,
                        commentsCount = p.commentsCount,
                        sharesCount = p.sharesCount,
                        isLiked = p.isLiked,
                        userReaction = p.userReaction
                    )
                }
                if (videoReels.isNotEmpty()) {
                    val curr = _reels.value
                    _reels.value = (curr + videoReels).distinctBy { it.mediaUrl }
                }
            }
        }
    }

    fun toggleDarkMode() {
        isDarkMode.value = !isDarkMode.value
    }

    val isRefreshing = MutableStateFlow(false)

    fun refreshFeed() {
        viewModelScope.launch {
            isRefreshing.value = true
            repository.syncPostsFromServer()
            syncStories()
            syncReelsFromServer()
            currentUser.value?.id?.let { uid ->
                repository.syncChatsFromServer(uid)
            }
            isRefreshing.value = false
        }
    }

    fun syncStories() {
        viewModelScope.launch {
            val result = repository.syncStoriesFromServer()
            if (result.isSuccess) {
                val list = result.getOrNull()
                if (!list.isNullOrEmpty()) {
                    _stories.value = list
                }
            }
        }
    }

    private val _currentScreen = MutableStateFlow(Screen.SPLASH)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    val currentUser: StateFlow<User?> = repository.loggedInUser.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val posts: StateFlow<List<Post>> = repository.allPosts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val chats: StateFlow<List<ChatMessage>> = repository.allChats.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _stories = MutableStateFlow<List<Story>>(repository.getSampleStories())
    val stories: StateFlow<List<Story>> = _stories.asStateFlow()

    fun addStory(storyImage: String?, textContent: String?, backgroundColor: String?) {
        _currentScreen.value = Screen.HOME
        _postUploadState.value = PostUploadState(
            isUploading = true,
            progress = 0.01f,
            postSnippet = if (!storyImage.isNullOrBlank()) "Photo Story" else (textContent ?: "Text Story"),
            postImage = storyImage,
            postVideo = null,
            isCompleted = false
        )

        viewModelScope.launch {
            var finalStoryUrl: String? = null

            // If image is selected, upload to Cloudflare R2 into "stories" folder
            if (!storyImage.isNullOrBlank()) {
                try {
                    val file = java.io.File(storyImage)
                    val bytes = if (file.exists()) {
                        file.readBytes()
                    } else if (storyImage.startsWith("content://") || storyImage.startsWith("file://")) {
                        val uri = Uri.parse(storyImage)
                        val cr = getApplication<Application>().contentResolver
                        cr.openInputStream(uri)?.use { it.readBytes() }
                    } else null

                    if (bytes != null && bytes.isNotEmpty()) {
                        val ext = if (storyImage.endsWith(".png")) "png" else if (storyImage.endsWith(".webp")) "webp" else "jpg"
                        val mime = if (ext == "png") "image/png" else if (ext == "webp") "image/webp" else "image/jpeg"
                        val uploadResult = repository.uploadMediaFile(
                            bytes = bytes,
                            fileName = "story_${System.currentTimeMillis()}.$ext",
                            mimeType = mime,
                            folder = "stories",
                            onProgress = { p ->
                                val scaled = (p * 0.90f).coerceIn(0.01f, 0.95f)
                                _postUploadState.value = _postUploadState.value.copy(progress = scaled)
                            }
                        )
                        if (uploadResult.isSuccess && !uploadResult.getOrNull().isNullOrBlank()) {
                            finalStoryUrl = uploadResult.getOrNull()
                            android.util.Log.d("FlikataStory", "Uploaded story image to R2 stories folder: $finalStoryUrl")
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("FlikataStory", "Error uploading story image: ${e.message}", e)
                }
            } else {
                _postUploadState.value = _postUploadState.value.copy(progress = 0.40f)
                kotlinx.coroutines.delay(100)
                _postUploadState.value = _postUploadState.value.copy(progress = 0.80f)
                kotlinx.coroutines.delay(100)
            }

            _postUploadState.value = _postUploadState.value.copy(progress = 0.95f)

            // Save story to MySQL database
            repository.addStory(
                storyImage = finalStoryUrl,
                textContent = textContent,
                backgroundColor = backgroundColor
            )

            // Re-sync stories from MySQL server so it's live across all devices
            val syncResult = repository.syncStoriesFromServer()
            if (syncResult.isSuccess) {
                val list = syncResult.getOrNull()
                if (!list.isNullOrEmpty()) {
                    _stories.value = list
                }
            }

            _postUploadState.value = _postUploadState.value.copy(isCompleted = true, progress = 1.0f)
            kotlinx.coroutines.delay(1500)
            _postUploadState.value = PostUploadState(isUploading = false)
        }
    }

    // Selected Chat Detail state
    private val _selectedChat = MutableStateFlow<ChatMessage?>(null)
    val selectedChat: StateFlow<ChatMessage?> = _selectedChat.asStateFlow()

    // Selected Image for Zoom Viewer
    val fullScreenImageUrl = MutableStateFlow<String?>(null)

    // Selected Story Index for Story Viewer
    val activeStoryIndex = MutableStateFlow(0)

    // Search Query State
    val searchQuery = MutableStateFlow("")

    // Auth Form States
    val loginEmailOrPhone = MutableStateFlow("")
    val loginPassword = MutableStateFlow("")
    val authError = MutableStateFlow<String?>(null)
    val authLoading = MutableStateFlow(false)

    // Registration Form States
    val regFirstName = MutableStateFlow("")
    val regLastName = MutableStateFlow("")
    val regAccountType = MutableStateFlow("phone") // "phone" or "email"
    val regIdentifier = MutableStateFlow("")
    val regDob = MutableStateFlow("2000-01-01")
    val regDobDay = MutableStateFlow("15")
    val regDobMonth = MutableStateFlow("08")
    val regDobYear = MutableStateFlow("1998")
    val regGender = MutableStateFlow("Male") // "Male", "Female", "Other"
    val regPassword = MutableStateFlow("")
    val regConfirmPassword = MutableStateFlow("")

    // Create Post States
    val postContent = MutableStateFlow("")
    val postPrivacy = MutableStateFlow("Public") // "Public", "Friends", "Only Me"
    val postBackgroundStyle = MutableStateFlow<String?>(null)
    val postSelectedImageUrl = MutableStateFlow<String?>(null)
    val postSelectedVideoUrl = MutableStateFlow<String?>(null)
    val postSelectedIsVideo = MutableStateFlow(false)

    // Reels State - Starts clean, no dummy reels!
    private val _reels = MutableStateFlow<List<ReelModel>>(emptyList())
    val reels: StateFlow<List<ReelModel>> = _reels.asStateFlow()

    val reelMaxDurationSeconds = MutableStateFlow(60)

    fun syncReelsFromServer() {
        viewModelScope.launch {
            repository.syncPostsFromServer()
            val result = repository.syncReelsFromServer()
            val serverList = if (result.isSuccess) result.getOrNull() ?: emptyList() else emptyList()

            // Also convert any video posts to reels so no video is ever missed!
            val localPosts = repository.getAllPostsDirect()
            val videoPostReels = localPosts.filter { !it.videoUrl.isNullOrBlank() }.map { p ->
                ReelModel(
                    id = "post_${p.id}",
                    userId = p.userId,
                    authorName = p.authorName,
                    authorAvatar = p.authorAvatar,
                    isVerified = p.isVerified,
                    mediaUrl = p.videoUrl!!,
                    caption = p.content.ifBlank { "Video Reel" },
                    audioTitle = "Original Audio - ${p.authorName}",
                    likesCount = p.likesCount,
                    commentsCount = p.commentsCount,
                    sharesCount = p.sharesCount,
                    isLiked = p.isLiked,
                    userReaction = p.userReaction
                )
            }

            val combined = (serverList + videoPostReels).distinctBy { it.mediaUrl }
            _reels.value = combined

            val maxDur = repository.getReelMaxDurationSeconds()
            reelMaxDurationSeconds.value = maxDur
        }
    }

    fun uploadReelFromUri(uri: Uri, caption: String = "My new Reel 🎬", onResult: (Boolean, String?) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val maxSeconds = repository.getReelMaxDurationSeconds()
            reelMaxDurationSeconds.value = maxSeconds

            // 1. Measure duration using MediaMetadataRetriever
            val durationSeconds = try {
                val retriever = android.media.MediaMetadataRetriever()
                retriever.setDataSource(context, uri)
                val durStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
                val durMs = durStr?.toLongOrNull() ?: 0L
                retriever.release()
                (durMs / 1000).toInt()
            } catch (e: Exception) {
                0
            }

            if (maxSeconds > 0 && durationSeconds > maxSeconds) {
                val msg = "Video duration ($durationSeconds s) exceeds maximum allowed limit ($maxSeconds s)."
                android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                onResult(false, msg)
                return@launch
            }

            // 2. Read bytes
            val bytes = try {
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            } catch (e: Exception) {
                null
            }

            if (bytes == null || bytes.isEmpty()) {
                val msg = "Could not load video file."
                android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                onResult(false, msg)
                return@launch
            }

            android.widget.Toast.makeText(context, "Uploading reel...", android.widget.Toast.LENGTH_SHORT).show()

            // 3. Upload to Cloudflare R2
            val uploadResult = repository.uploadMediaFile(
                bytes = bytes,
                fileName = "reel_${System.currentTimeMillis()}.mp4",
                mimeType = "video/mp4",
                folder = "reels"
            )

            if (uploadResult.isSuccess && !uploadResult.getOrNull().isNullOrBlank()) {
                val cloudflareUrl = uploadResult.getOrNull()!!
                // 4. Save to MySQL reels table
                repository.addReel(
                    mediaUrl = cloudflareUrl,
                    caption = caption,
                    audioTitle = "Original Audio - ${currentUser.value?.fullName ?: "Me"}"
                )
                syncReelsFromServer()
                android.widget.Toast.makeText(context, "Reel uploaded successfully!", android.widget.Toast.LENGTH_SHORT).show()
                onResult(true, null)
            } else {
                val err = uploadResult.exceptionOrNull()?.message ?: "Upload failed"
                android.widget.Toast.makeText(context, "Upload failed: $err", android.widget.Toast.LENGTH_LONG).show()
                onResult(false, err)
            }
        }
    }

    fun deleteReel(reelId: String) {
        viewModelScope.launch {
            val prev = _reels.value.toMutableList()
            prev.removeAll { it.id == reelId }
            _reels.value = prev
            repository.deleteReel(reelId)
            syncReelsFromServer()
            repository.syncPostsFromServer()
        }
    }

    fun addReel(
        mediaUrl: String,
        caption: String = "My new Reel 🎬",
        audioTitle: String? = null
    ) {
        viewModelScope.launch {
            repository.addReel(mediaUrl, caption, audioTitle)
            syncReelsFromServer()
        }
    }

    fun toggleReelLike(reelId: String, reaction: String = "Like") {
        viewModelScope.launch {
            repository.toggleReelLike(reelId, reaction)
        }
    }

    fun setReelReaction(reelId: String, reaction: String) {
        viewModelScope.launch {
            repository.setReelReaction(reelId, reaction)
        }
    }

    fun recordReelView(reelId: String) {
        viewModelScope.launch {
            repository.recordReelView(reelId)
        }
    }

    fun addReelComment(reelId: String, text: String, onAdded: (ReelCommentItem) -> Unit) {
        viewModelScope.launch {
            val user = currentUser.value
            val authorName = user?.fullName ?: "Me"
            val authorAvatar = user?.profilePic ?: ""
            val isVerified = user?.isVerified ?: false

            val result = repository.addReelComment(reelId, text)
            val newComment = if (result.isSuccess && result.getOrNull() != null) {
                val data = result.getOrNull()!!
                ReelCommentItem(
                    id = data.id,
                    authorName = data.authorName,
                    authorAvatar = data.authorAvatar.ifEmpty { authorAvatar },
                    text = data.text,
                    timestamp = "Just now",
                    isVerified = isVerified
                )
            } else {
                ReelCommentItem(
                    id = System.currentTimeMillis().toString(),
                    authorName = authorName,
                    authorAvatar = authorAvatar,
                    text = text,
                    timestamp = "Just now",
                    isVerified = isVerified
                )
            }
            onAdded(newComment)
        }
    }

    // Post Upload Pipe State
    private val _postUploadState = MutableStateFlow(PostUploadState())
    val postUploadState: StateFlow<PostUploadState> = _postUploadState.asStateFlow()

    // Notifications State
    private val _notifications = MutableStateFlow<List<FlikataNotification>>(emptyList())
    val notifications: StateFlow<List<FlikataNotification>> = _notifications.asStateFlow()

    val unreadNotificationsCount: StateFlow<Int> = _notifications.map { list ->
        list.count { !it.isRead }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun markAllNotificationsAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }

    fun markNotificationAsRead(id: String) {
        _notifications.value = _notifications.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
    }

    fun deleteNotification(id: String) {
        _notifications.value = _notifications.value.filter { it.id != id }
    }

    fun clearAllNotifications() {
        _notifications.value = emptyList()
    }

    // Edit Profile Form States matching Screenshots 1 & 2
    val editFullName = MutableStateFlow("")
    val editBio = MutableStateFlow("")
    val editProfilePic = MutableStateFlow("")
    val editCoverPic = MutableStateFlow("")
    val editOccupation = MutableStateFlow("")
    val editGender = MutableStateFlow("Male")
    val editDob = MutableStateFlow("")
    val editEmail = MutableStateFlow("")
    val editPhone = MutableStateFlow("")
    val editLocation = MutableStateFlow("")
    val editWebsite = MutableStateFlow("")
    val editNid = MutableStateFlow("")
    val isSavingProfile = MutableStateFlow(false)

    // Privacy Toggles
    val hideUsername = MutableStateFlow(true)
    val hidePhone = MutableStateFlow(false)
    val hideEmail = MutableStateFlow(false)
    val hideBirthday = MutableStateFlow(true)
    val hideAge = MutableStateFlow(true)
    val hideGender = MutableStateFlow(false)
    val hideLocation = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
            repository.syncPostsFromServer()
            syncStories()
            syncReelsFromServer()
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun openChatDetail(chat: ChatMessage) {
        _selectedChat.value = chat
        _currentScreen.value = Screen.CHAT_DETAIL
    }

    fun openChatWithUser(name: String, avatar: String, isVerified: Boolean = false) {
        val existing = chats.value.find { it.senderName.equals(name, ignoreCase = true) }
        if (existing != null) {
            _selectedChat.value = existing
        } else {
            val newChat = ChatMessage(
                id = "chat_${System.currentTimeMillis()}",
                threadId = "t_${System.currentTimeMillis()}",
                senderId = "u_${name.hashCode()}",
                senderName = name,
                senderAvatar = avatar,
                isVerified = isVerified,
                message = "Active now",
                timestamp = "Just now",
                isVoiceMessage = false,
                isMissedCall = false,
                unreadCount = 0
            )
            _selectedChat.value = newChat
        }
        _currentScreen.value = Screen.CHAT_DETAIL
    }

    fun openImageViewer(imageUrl: String) {
        fullScreenImageUrl.value = imageUrl
        _currentScreen.value = Screen.FULL_IMAGE_VIEWER
    }

    fun openStoryViewer(storyIndex: Int) {
        activeStoryIndex.value = storyIndex
        _currentScreen.value = Screen.STORY_VIEWER
    }

    fun prepareEditProfile() {
        val user = currentUser.value
        if (user != null) {
            editFullName.value = user.fullName
            editBio.value = user.bio ?: ""
            editProfilePic.value = user.profilePic ?: ""
            editCoverPic.value = user.coverPic ?: ""
            editGender.value = user.gender ?: "Male"
            editDob.value = user.dob ?: "15/08/1998"
            editEmail.value = if (user.emailOrPhone.contains("@")) user.emailOrPhone else "marufma143g@gmail.com"
            editPhone.value = if (!user.emailOrPhone.contains("@")) user.emailOrPhone else ""
        }
        _currentScreen.value = Screen.EDIT_PROFILE
    }

    fun saveProfileChanges() {
        val user = currentUser.value ?: return
        val nameInput = editFullName.value.trim()
        val parts = nameInput.split(" ")
        val newFirst = parts.firstOrNull()?.ifBlank { user.firstName } ?: user.firstName
        val newLast = parts.drop(1).joinToString(" ").ifBlank { user.lastName }

        viewModelScope.launch {
            isSavingProfile.value = true
            var finalProfilePic = editProfilePic.value.ifBlank { user.profilePic }
            var finalCoverPic = editCoverPic.value.ifBlank { user.coverPic }

            if (!finalProfilePic.isNullOrBlank() && !finalProfilePic.startsWith("http://") && !finalProfilePic.startsWith("https://")) {
                try {
                    val file = java.io.File(finalProfilePic)
                    val bytes = if (file.exists()) {
                        file.readBytes()
                    } else if (finalProfilePic.startsWith("content://") || finalProfilePic.startsWith("file://")) {
                        val uri = Uri.parse(finalProfilePic)
                        val cr = getApplication<Application>().contentResolver
                        cr.openInputStream(uri)?.use { it.readBytes() }
                    } else null

                    if (bytes != null && bytes.isNotEmpty()) {
                        val uploadResult = repository.uploadMediaFile(bytes, "avatar_${System.currentTimeMillis()}.jpg", "image/jpeg")
                        if (uploadResult.isSuccess && !uploadResult.getOrNull().isNullOrBlank()) {
                            finalProfilePic = uploadResult.getOrNull()
                            android.util.Log.d("FlikataProfile", "Uploaded profile picture to Cloudflare R2: $finalProfilePic")
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("FlikataProfile", "Avatar upload to R2 error: ${e.message}", e)
                }
            }

            if (!finalCoverPic.isNullOrBlank() && !finalCoverPic.startsWith("http://") && !finalCoverPic.startsWith("https://")) {
                try {
                    val file = java.io.File(finalCoverPic)
                    val bytes = if (file.exists()) {
                        file.readBytes()
                    } else if (finalCoverPic.startsWith("content://") || finalCoverPic.startsWith("file://")) {
                        val uri = Uri.parse(finalCoverPic)
                        val cr = getApplication<Application>().contentResolver
                        cr.openInputStream(uri)?.use { it.readBytes() }
                    } else null

                    if (bytes != null && bytes.isNotEmpty()) {
                        val uploadResult = repository.uploadMediaFile(bytes, "cover_${System.currentTimeMillis()}.jpg", "image/jpeg")
                        if (uploadResult.isSuccess && !uploadResult.getOrNull().isNullOrBlank()) {
                            finalCoverPic = uploadResult.getOrNull()
                            android.util.Log.d("FlikataProfile", "Uploaded banner/cover to Cloudflare R2: $finalCoverPic")
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("FlikataProfile", "Cover banner upload to R2 error: ${e.message}", e)
                }
            }

            val updatedUser = user.copy(
                firstName = newFirst,
                lastName = newLast,
                bio = editBio.value,
                profilePic = finalProfilePic,
                coverPic = finalCoverPic,
                gender = editGender.value,
                dob = editDob.value
            )
            repository.updateUser(updatedUser)
            isSavingProfile.value = false
            _currentScreen.value = Screen.PROFILE
        }
    }

    fun performLogin() {
        val identifier = loginEmailOrPhone.value.trim()
        val pass = loginPassword.value
        if (identifier.isEmpty() || pass.isEmpty()) {
            authError.value = "Please enter email/phone and password"
            return
        }

        viewModelScope.launch {
            authLoading.value = true
            authError.value = null
            val result = repository.login(identifier, pass)
            authLoading.value = false
            if (result.isSuccess) {
                repository.syncPostsFromServer()
                result.getOrNull()?.id?.let { uid ->
                    repository.syncChatsFromServer(uid)
                }
                _currentScreen.value = Screen.HOME
            } else {
                authError.value = result.exceptionOrNull()?.message ?: "Invalid email/phone or password"
            }
        }
    }

    fun performRegister() {
        val fName = regFirstName.value.trim()
        val lName = regLastName.value.trim()
        val identifier = regIdentifier.value.trim()
        val dob = regDob.value
        val gender = regGender.value
        val pass = regPassword.value
        val confirmPass = regConfirmPassword.value

        if (fName.isEmpty() || lName.isEmpty() || identifier.isEmpty()) {
            authError.value = "Please fill in all required fields"
            return
        }

        if (pass.length < 6) {
            authError.value = "Password must be at least 6 characters"
            return
        }

        if (pass != confirmPass) {
            authError.value = "Passwords do not match"
            return
        }

        viewModelScope.launch {
            authLoading.value = true
            authError.value = null
            val result = repository.register(
                firstName = fName,
                lastName = lName,
                accountType = regAccountType.value,
                emailOrPhone = identifier,
                dob = dob,
                gender = gender,
                password = pass
            )
            authLoading.value = false
            if (result.isSuccess) {
                repository.syncPostsFromServer()
                _currentScreen.value = Screen.HOME
            } else {
                authError.value = result.exceptionOrNull()?.message ?: "Registration failed"
            }
        }
    }

    fun cacheMediaFromUri(uri: Uri, isVideo: Boolean): String {
        return try {
            val context = getApplication<Application>()
            val cr = context.contentResolver
            val ext = if (isVideo) "mp4" else {
                val mime = cr.getType(uri) ?: "image/jpeg"
                if (mime.contains("png")) "png" else if (mime.contains("webp")) "webp" else "jpg"
            }
            val fileName = "cached_${System.currentTimeMillis()}.$ext"
            val file = java.io.File(context.cacheDir, fileName)
            cr.openInputStream(uri)?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            android.util.Log.e("FlikataCache", "Error caching media: ${e.message}", e)
            uri.toString()
        }
    }

    fun submitPost() {
        val content = postContent.value.trim()
        val bgStyle = postBackgroundStyle.value
        val privacy = postPrivacy.value
        val imgUrl = postSelectedImageUrl.value
        val videoUrl = postSelectedVideoUrl.value
        val isVideo = postSelectedIsVideo.value

        if (content.isEmpty() && imgUrl == null && videoUrl == null) {
            return
        }

        // Clear create form immediately and navigate back to Home to watch pipe progress
        postContent.value = ""
        postBackgroundStyle.value = null
        postSelectedImageUrl.value = null
        postSelectedVideoUrl.value = null
        postSelectedIsVideo.value = false
        _currentScreen.value = Screen.HOME

        val snippet = if (content.isNotEmpty()) {
            if (content.length > 50) content.take(50) + "..." else content
        } else if (isVideo) {
            "Video post"
        } else {
            "Photo post"
        }

        // Start upload progress smoothly from 1% immediately so the UI responds instantly
        _postUploadState.value = PostUploadState(
            isUploading = true,
            progress = 0.01f,
            postSnippet = snippet,
            postImage = if (!isVideo) imgUrl else null,
            postVideo = if (isVideo) videoUrl else null,
            isVideo = isVideo,
            isCompleted = false
        )

        viewModelScope.launch {
            // Upload to Cloudflare R2 if local image or video exists
            var uploadedImageUrl: String? = null
            var uploadedVideoUrl: String? = null

            if (!imgUrl.isNullOrBlank()) {
                val bytes: ByteArray? = try {
                    val file = java.io.File(imgUrl)
                    if (file.exists()) {
                        file.readBytes()
                    } else if (imgUrl.startsWith("content://") || imgUrl.startsWith("file://")) {
                        val uri = Uri.parse(imgUrl)
                        val cr = getApplication<Application>().contentResolver
                        cr.openInputStream(uri)?.use { it.readBytes() }
                    } else null
                } catch (e: Exception) {
                    android.util.Log.e("FlikataUpload", "Cannot read image bytes: ${e.message}", e)
                    null
                }

                if (bytes != null && bytes.isNotEmpty()) {
                    val ext = if (imgUrl.endsWith(".png")) "png" else if (imgUrl.endsWith(".webp")) "webp" else "jpg"
                    val mime = if (ext == "png") "image/png" else if (ext == "webp") "image/webp" else "image/jpeg"
                    val uploadResult = repository.uploadMediaFile(
                        bytes = bytes,
                        fileName = "post_${System.currentTimeMillis()}.$ext",
                        mimeType = mime,
                        folder = "images",
                        onProgress = { p ->
                            val scaled = (p * 0.90f).coerceIn(0.01f, 0.95f)
                            _postUploadState.value = _postUploadState.value.copy(progress = scaled)
                        }
                    )
                    if (uploadResult.isSuccess) {
                        uploadedImageUrl = uploadResult.getOrNull()
                        android.util.Log.d("FlikataUpload", "Successfully uploaded to Cloudflare R2: $uploadedImageUrl")
                    } else {
                        android.util.Log.e("FlikataUpload", "Cloudflare R2 upload error: ${uploadResult.exceptionOrNull()?.message}")
                    }
                }
            }

            if (!videoUrl.isNullOrBlank()) {
                val bytes: ByteArray? = try {
                    val file = java.io.File(videoUrl)
                    if (file.exists()) {
                        file.readBytes()
                    } else if (videoUrl.startsWith("content://") || videoUrl.startsWith("file://")) {
                        val uri = Uri.parse(videoUrl)
                        val cr = getApplication<Application>().contentResolver
                        cr.openInputStream(uri)?.use { it.readBytes() }
                    } else null
                } catch (e: Exception) {
                    null
                }

                if (bytes != null && bytes.isNotEmpty()) {
                    val uploadResult = repository.uploadMediaFile(
                        bytes = bytes,
                        fileName = "reel_${System.currentTimeMillis()}.mp4",
                        mimeType = "video/mp4",
                        folder = "reels",
                        onProgress = { p ->
                            val scaled = (p * 0.90f).coerceIn(0.01f, 0.95f)
                            _postUploadState.value = _postUploadState.value.copy(progress = scaled)
                        }
                    )
                    if (uploadResult.isSuccess) {
                        uploadedVideoUrl = uploadResult.getOrNull()
                    }
                }
            }

            if (imgUrl.isNullOrBlank() && videoUrl.isNullOrBlank()) {
                _postUploadState.value = _postUploadState.value.copy(progress = 0.35f)
                delay(100)
                _postUploadState.value = _postUploadState.value.copy(progress = 0.75f)
                delay(100)
            }

            _postUploadState.value = _postUploadState.value.copy(progress = 0.95f)

            // Save actual post to local database / repository with real Cloudflare R2 URL
            repository.createPost(
                content = content,
                backgroundStyle = bgStyle,
                privacy = privacy,
                imageUrl = if (!isVideo) uploadedImageUrl else null,
                videoUrl = if (isVideo) uploadedVideoUrl else null,
                isVideo = isVideo
            )
            repository.syncPostsFromServer()

            // Sync reels and posts so the new video post and reel appear across feeds
            if (isVideo && uploadedVideoUrl != null) {
                syncReelsFromServer()
            }

            // Mark completion
            _postUploadState.value = _postUploadState.value.copy(isCompleted = true, progress = 1.0f)

            // Add notification to list and increment unread badge
            val newNotification = FlikataNotification(
                id = System.currentTimeMillis().toString(),
                title = "Your post was successfully published",
                message = "\"$snippet\"",
                type = "post",
                timestamp = "Just now",
                isRead = false,
                avatarUrl = currentUser.value?.profilePic,
                postThumbnail = if (!isVideo) imgUrl else null
            )
            val currentNotifs = _notifications.value.toMutableList()
            currentNotifs.add(0, newNotification)
            _notifications.value = currentNotifs

            // Keep "Completed" banner visible for 1.5 seconds, then clear pipe
            delay(1500)
            _postUploadState.value = PostUploadState(isUploading = false)
        }
    }

    fun toggleLike(postId: String) {
        viewModelScope.launch {
            repository.toggleLike(postId)
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            repository.deletePost(postId)
            repository.syncPostsFromServer()
            syncReelsFromServer()
        }
    }

    fun editPost(postId: String, newContent: String, newPrivacy: String) {
        viewModelScope.launch {
            val result = repository.editPost(postId, newContent, newPrivacy)
            if (result.isSuccess) {
                android.widget.Toast.makeText(
                    getApplication(),
                    "Post updated successfully!",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            } else {
                val err = result.exceptionOrNull()?.message ?: "Edit failed"
                android.widget.Toast.makeText(getApplication(), err, android.widget.Toast.LENGTH_SHORT).show()
                repository.syncPostsFromServer()
            }
        }
    }

    fun setReaction(postId: String, emoji: String?) {
        viewModelScope.launch {
            repository.setReaction(postId, emoji)
        }
    }

    fun addComment(postId: String, text: String) {
        viewModelScope.launch {
            repository.addComment(postId, text)
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _currentScreen.value = Screen.LOGIN
        }
    }
}
