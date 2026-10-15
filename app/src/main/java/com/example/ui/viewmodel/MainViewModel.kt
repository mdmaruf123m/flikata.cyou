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
import androidx.compose.foundation.lazy.LazyListState
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
    FORGOT_PASSWORD,
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
    FRIENDS,
    VERIFIED_BADGE,
    WALLET,
    DEPOSIT,
    WITHDRAW,
    ADMIN_PANEL,
    MARKETPLACE,
    DASHBOARD
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FlikataDatabase.getDatabase(application)
    val repository = FlikataRepository(db.userDao(), db.postDao(), db.chatDao())

    val isDarkMode = MutableStateFlow(false)
    val highlightedPostId = MutableStateFlow<String?>(null)
    val selectedUserProfileId = MutableStateFlow<String?>(null)
    val selectedUserProfileName = MutableStateFlow<String?>(null)
    val selectedUserProfileData = MutableStateFlow<com.example.data.model.UserProfileData?>(null)
    val targetReelId = MutableStateFlow<String?>(null)

    val dashboardData = MutableStateFlow<com.example.data.model.DashboardData?>(null)
    val isLoadingDashboard = MutableStateFlow(false)
    val dashboardPeriod = MutableStateFlow("7d")

    val audioCallManager by lazy { com.example.data.audio.AudioCallManager(application) }
    val incomingCallState = MutableStateFlow<com.example.data.model.IncomingCallData?>(null)
    val activeCallState = MutableStateFlow<ActiveCallState?>(null)
    private var callPollingJob: kotlinx.coroutines.Job? = null

    fun loadDashboard(period: String = dashboardPeriod.value) {
        dashboardPeriod.value = period
        viewModelScope.launch {
            isLoadingDashboard.value = true
            val user = currentUser.value ?: repository.getLoggedInUserDirect()
            val effectiveUserId = user?.id?.removePrefix("user_")?.trim()?.ifBlank { null } ?: "1"
            val res = repository.getDashboardData(userId = effectiveUserId, period = period)
            if (res.isSuccess) {
                dashboardData.value = res.getOrNull()
            }
            isLoadingDashboard.value = false
        }
    }

    suspend fun submitMonetization(): Result<com.example.data.model.SubmitMonetizationData> {
        val user = currentUser.value ?: repository.getLoggedInUserDirect()
        val effectiveUserId = user?.id?.removePrefix("user_")?.trim()?.ifBlank { null } ?: "1"
        val res = repository.submitMonetization(userId = effectiveUserId)
        if (res.isSuccess) {
            // Reload dashboard to reflect pending application
            loadDashboard(dashboardPeriod.value)
        }
        return res
    }

    fun openPost(postId: String) {
        highlightedPostId.value = postId
        navigateTo(Screen.HOME)
    }

    fun clearHighlightedPost() {
        highlightedPostId.value = null
    }

    fun openUserProfile(userId: String, userName: String = "") {
        val myId = currentUser.value?.id ?: ""
        val clean = userId.removePrefix("user_").removePrefix("partner_").trim()
        if (clean.isNotBlank() && clean == myId) {
            selectedUserProfileId.value = null
            selectedUserProfileName.value = null
            selectedUserProfileData.value = null
            navigateTo(Screen.PROFILE)
        } else {
            selectedUserProfileId.value = clean.ifBlank { null }
            selectedUserProfileName.value = userName.ifBlank { null }
            selectedUserProfileData.value = null
            if (clean.isNotBlank()) {
                viewModelScope.launch {
                    val res = repository.fetchUserProfile(clean)
                    if (res.isSuccess) {
                        val profile = res.getOrNull()
                        selectedUserProfileData.value = profile
                        if (!profile?.fullName.isNullOrBlank()) {
                            selectedUserProfileName.value = profile!!.fullName
                        }
                    }
                }
            }
            navigateTo(Screen.PROFILE)
        }
    }

    fun clearSelectedUserProfile() {
        selectedUserProfileId.value = null
        selectedUserProfileName.value = null
        selectedUserProfileData.value = null
    }

    fun openReel(reelId: String) {
        targetReelId.value = reelId
        navigateTo(Screen.REELS)
    }

    val homeListState = LazyListState()

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

        // Auto-sync chats continuously so incoming messages from other phones show up in real-time
        viewModelScope.launch {
            while (true) {
                delay(3000)
                val uid = currentUser.value?.id
                if (!uid.isNullOrBlank()) {
                    repository.syncChatsFromServer(uid)
                }
            }
        }

        // Continuous polling for incoming audio calls
        viewModelScope.launch {
            while (true) {
                delay(2000)
                try {
                    val uid = currentUser.value?.id
                    if (!uid.isNullOrBlank() && activeCallState.value == null) {
                        val incoming = repository.checkIncomingCall()
                        if (incoming != null && incoming.hasCall && incoming.callId != null && incoming.status == "ringing") {
                            if (incomingCallState.value?.callId != incoming.callId) {
                                incomingCallState.value = incoming
                                audioCallManager.startRinging(isIncoming = true)
                            }
                        } else {
                            if (incomingCallState.value != null) {
                                audioCallManager.stopRinging()
                                incomingCallState.value = null
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Ignore polling network errors
                }
            }
        }

        // Initial load of notifications
        loadNotifications()
    }

    fun toggleDarkMode() {
        isDarkMode.value = !isDarkMode.value
    }

    val isRefreshing = MutableStateFlow(false)
    val isRefreshingNotifications = MutableStateFlow(false)
    val isRefreshingProfile = MutableStateFlow(false)
    val isRefreshingChats = MutableStateFlow(false)

    fun refreshFeed() {
        viewModelScope.launch {
            isRefreshing.value = true
            repository.syncPostsFromServer()
            syncStories()
            syncReelsFromServer()
            loadNotifications()
            currentUser.value?.id?.let { uid ->
                repository.syncChatsFromServer(uid)
            }
            isRefreshing.value = false
        }
    }

    fun refreshChats() {
        viewModelScope.launch {
            isRefreshingChats.value = true
            currentUser.value?.id?.let { uid ->
                repository.syncChatsFromServer(uid)
            }
            isRefreshingChats.value = false
        }
    }

    fun refreshProfile() {
        viewModelScope.launch {
            isRefreshingProfile.value = true
            val user = currentUser.value
            if (user != null) {
                val res = repository.fetchUserProfile(user.id)
                if (res.isSuccess) {
                    val p = res.getOrNull()
                    if (p != null) {
                        val names = (p.fullName ?: user.fullName).split(" ")
                        val updated = user.copy(
                            firstName = names.firstOrNull() ?: user.firstName,
                            lastName = names.drop(1).joinToString(" ").ifBlank { user.lastName },
                            isVerified = p.isVerifiedBool,
                            profilePic = p.profilePic ?: user.profilePic,
                            coverPic = p.coverPic ?: user.coverPic,
                            bio = p.bio ?: user.bio,
                            postsCount = p.postsCount ?: user.postsCount
                        )
                        repository.updateUserLocal(updated)
                    }
                }
            }
            selectedUserProfileId.value?.let { externalId ->
                val res = repository.fetchUserProfile(externalId)
                if (res.isSuccess) {
                    selectedUserProfileData.value = res.getOrNull()
                }
            }
            repository.syncPostsFromServer()
            isRefreshingProfile.value = false
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
    val forgotPasswordEmailOrPhone = MutableStateFlow("")
    val forgotPasswordSubmitted = MutableStateFlow(false)
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

    fun recordReelView(reelId: String, onResult: ((Int) -> Unit)? = null) {
        viewModelScope.launch {
            val res = repository.recordReelView(reelId)
            if (res.isSuccess) {
                val data = res.getOrNull()
                if (data != null) {
                    onResult?.invoke(data.viewsCount)
                }
            }
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

    fun getReelComments(reelId: String, onLoaded: (List<ReelCommentItem>) -> Unit) {
        viewModelScope.launch {
            val result = repository.getReelComments(reelId)
            if (result.isSuccess && result.getOrNull() != null) {
                val list = result.getOrNull()!!.map { c ->
                    ReelCommentItem(
                        id = c.id,
                        authorName = c.authorName,
                        authorAvatar = c.authorAvatar,
                        text = c.text,
                        timestamp = c.timestamp,
                        isLiked = c.isLiked,
                        isVerified = c.isVerified
                    )
                }
                onLoaded(list)
            }
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

    fun loadNotifications() {
        viewModelScope.launch {
            val list = repository.getNotifications()
            if (list.isNotEmpty() || _notifications.value.isEmpty()) {
                _notifications.value = list
            }
        }
    }

    fun refreshNotifications() {
        viewModelScope.launch {
            isRefreshingNotifications.value = true
            val list = repository.getNotifications()
            _notifications.value = list
            isRefreshingNotifications.value = false
        }
    }

    fun markAllNotificationsAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
        viewModelScope.launch {
            repository.markNotificationsRead(null)
        }
    }

    fun markNotificationAsRead(id: String) {
        _notifications.value = _notifications.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
        viewModelScope.launch {
            repository.markNotificationsRead(id)
        }
    }

    fun deleteNotification(id: String) {
        _notifications.value = _notifications.value.filter { it.id != id }
        viewModelScope.launch {
            repository.deleteNotification(id)
        }
    }

    fun clearAllNotifications() {
        _notifications.value = emptyList()
        viewModelScope.launch {
            repository.deleteNotification(null)
        }
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
            repository.getLoggedInUserDirect()
            repository.seedInitialDataIfEmpty()
            repository.syncPostsFromServer()
            syncStories()
            syncReelsFromServer()
        }
    }

    private var _previousScreen: Screen = Screen.HOME
    val previousScreen: Screen get() = _previousScreen

    private val _screenBackStack = mutableListOf<Screen>()
    val screenBackStack: List<Screen> get() = _screenBackStack.toList()

    fun navigateTo(screen: Screen) {
        if (screen == _currentScreen.value) return

        val current = _currentScreen.value
        if (current != Screen.SPLASH && current != Screen.LOGIN && current != Screen.REGISTER && current != Screen.FORGOT_PASSWORD) {
            if (_screenBackStack.isNotEmpty() && _screenBackStack.last() == screen) {
                _screenBackStack.removeAt(_screenBackStack.size - 1)
            } else {
                _screenBackStack.add(current)
            }
        }
        _previousScreen = current

        if (screen == Screen.PROFILE && selectedUserProfileId.value == null) {
            clearSelectedUserProfile()
        }
        _currentScreen.value = screen
    }

    fun navigateBack(): Boolean {
        if (fullScreenImageUrl.value != null) {
            fullScreenImageUrl.value = null
        }

        if (_screenBackStack.isNotEmpty()) {
            val prev = _screenBackStack.removeAt(_screenBackStack.size - 1)
            if (_currentScreen.value == Screen.PROFILE && selectedUserProfileId.value != null) {
                clearSelectedUserProfile()
            }
            _previousScreen = _currentScreen.value
            _currentScreen.value = prev
            return true
        } else {
            if (_currentScreen.value != Screen.HOME && _currentScreen.value != Screen.LOGIN && _currentScreen.value != Screen.SPLASH) {
                _previousScreen = _currentScreen.value
                _currentScreen.value = Screen.HOME
                return true
            }
            return false
        }
    }

    fun openChatDetail(chat: ChatMessage) {
        _selectedChat.value = chat
        navigateTo(Screen.CHAT_DETAIL)
    }

    fun openChatWithUser(name: String, avatar: String, isVerified: Boolean = false, userId: String? = null) {
        val cleanId = userId?.removePrefix("user_")?.removePrefix("partner_")?.trim()
        val resolvedId = if (!cleanId.isNullOrBlank() && cleanId.toIntOrNull() != null) {
            cleanId
        } else {
            val fromChat = chats.value.find { it.senderName.equals(name, ignoreCase = true) }?.senderId
            val fromPost = posts.value.find { it.authorName.equals(name, ignoreCase = true) }?.userId
            fromChat ?: fromPost ?: cleanId?.ifBlank { null } ?: "1"
        }

        val existing = chats.value.find {
            it.senderId == resolvedId || (resolvedId != "1" && it.id == resolvedId) || it.senderName.equals(name, ignoreCase = true)
        }

        if (existing != null) {
            val updated = existing.copy(
                id = resolvedId,
                senderIdRaw = resolvedId,
                senderNameRaw = name.ifBlank { existing.senderName },
                senderAvatarRaw = if (avatar.isNotBlank()) avatar else existing.senderAvatar,
                isVerifiedRaw = isVerified || existing.isVerified
            )
            _selectedChat.value = updated
        } else {
            val newChat = ChatMessage(
                id = resolvedId,
                threadIdRaw = resolvedId,
                senderIdRaw = resolvedId,
                senderNameRaw = name,
                senderAvatarRaw = avatar,
                isVerifiedRaw = isVerified,
                message = "Active now",
                timestamp = "Just now",
                isVoiceMessage = false,
                isMissedCall = false,
                unreadCountRaw = 0
            )
            _selectedChat.value = newChat
        }
        navigateTo(Screen.CHAT_DETAIL)
    }


    fun sendMessage(partnerId: String, partnerName: String, partnerAvatar: String, messageText: String) {
        viewModelScope.launch {
            repository.sendChatMessage(partnerId, partnerName, partnerAvatar, messageText)
        }
    }

    fun openImageViewer(imageUrl: String) {
        fullScreenImageUrl.value = imageUrl
        navigateTo(Screen.FULL_IMAGE_VIEWER)
    }

    fun openStoryViewer(storyIndex: Int) {
        activeStoryIndex.value = storyIndex
        navigateTo(Screen.STORY_VIEWER)
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
        navigateTo(Screen.EDIT_PROFILE)
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

    fun performForgotPassword() {
        val identifier = forgotPasswordEmailOrPhone.value.trim()
        if (identifier.isEmpty()) {
            authError.value = "Please enter your phone number or email"
            return
        }

        viewModelScope.launch {
            authLoading.value = true
            authError.value = null
            // Simulate network request for password recovery link/token
            kotlinx.coroutines.delay(1200)
            authLoading.value = false
            forgotPasswordSubmitted.value = true
        }
    }

    fun resetForgotPasswordState() {
        forgotPasswordEmailOrPhone.value = ""
        forgotPasswordSubmitted.value = false
        authError.value = null
        authLoading.value = false
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
            _screenBackStack.clear()
            _currentScreen.value = Screen.LOGIN
        }
    }

    fun startAudioCall(partnerId: Int, partnerName: String, partnerAvatar: String?, isVerified: Boolean) {
        viewModelScope.launch {
            val myId = currentUser.value?.id?.toIntOrNull() ?: 1
            val res = repository.initiateCall(partnerId.toString())
            if (res.isSuccess) {
                val callData = res.getOrNull()!!
                val callId = callData.callId
                activeCallState.value = ActiveCallState(
                    callId = callId,
                    partnerId = partnerId,
                    partnerName = partnerName,
                    partnerAvatar = partnerAvatar,
                    isVerified = isVerified,
                    isCaller = true,
                    status = "ringing",
                    durationSeconds = 0
                )
                audioCallManager.startRinging(isIncoming = false)

                callPollingJob?.cancel()
                callPollingJob = viewModelScope.launch {
                    var streamStarted = false
                    while (activeCallState.value?.callId == callId) {
                        delay(1200)
                        val statusData = repository.getCallStatus(callId)
                        val st = statusData?.status ?: "ringing"
                        if (st == "connected") {
                            if (!streamStarted) {
                                streamStarted = true
                                audioCallManager.stopRinging()
                                audioCallManager.startAudioStream(callId, myId, partnerId)
                            }
                            activeCallState.value = activeCallState.value?.copy(
                                status = "connected",
                                durationSeconds = (activeCallState.value?.durationSeconds ?: 0) + 1
                            )
                        } else if (st in listOf("declined", "ended", "missed")) {
                            audioCallManager.stopAudioCall()
                            activeCallState.value = null
                            break
                        }
                    }
                }
            } else {
                val err = res.exceptionOrNull()?.message ?: ""
                val msg = if (err.contains("offline", ignoreCase = true)) "Offline" else err.ifBlank { "Offline" }
                android.widget.Toast.makeText(getApplication(), msg, android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun acceptIncomingCall() {
        val inc = incomingCallState.value ?: return
        val callId = inc.callId ?: return
        val callerId = inc.callerId ?: return
        audioCallManager.stopRinging()
        incomingCallState.value = null

        viewModelScope.launch {
            repository.answerCall(callId)
            val myId = currentUser.value?.id?.toIntOrNull() ?: 1

            activeCallState.value = ActiveCallState(
                callId = callId,
                partnerId = callerId,
                partnerName = inc.callerName ?: "User",
                partnerAvatar = inc.callerAvatar,
                isVerified = inc.callerVerified == true,
                isCaller = false,
                status = "connected",
                durationSeconds = 0
            )

            audioCallManager.startAudioStream(callId, myId, callerId)

            callPollingJob?.cancel()
            callPollingJob = viewModelScope.launch {
                while (activeCallState.value?.callId == callId) {
                    delay(1000)
                    activeCallState.value = activeCallState.value?.copy(
                        durationSeconds = (activeCallState.value?.durationSeconds ?: 0) + 1
                    )
                    if ((activeCallState.value?.durationSeconds ?: 0) % 2 == 0) {
                        val statusData = repository.getCallStatus(callId)
                        if (statusData?.status in listOf("ended", "declined", "missed")) {
                            audioCallManager.stopAudioCall()
                            activeCallState.value = null
                            break
                        }
                    }
                }
            }
        }
    }

    fun declineIncomingCall() {
        val inc = incomingCallState.value ?: return
        val callId = inc.callId ?: return
        audioCallManager.stopRinging()
        incomingCallState.value = null
        viewModelScope.launch {
            repository.endCall(callId, reason = "declined")
        }
    }

    fun endActiveCall() {
        val current = activeCallState.value ?: return
        val callId = current.callId
        val duration = current.durationSeconds
        callPollingJob?.cancel()
        audioCallManager.stopAudioCall()
        activeCallState.value = null
        viewModelScope.launch {
            repository.endCall(callId, reason = "ended", duration = duration)
        }
    }
}
