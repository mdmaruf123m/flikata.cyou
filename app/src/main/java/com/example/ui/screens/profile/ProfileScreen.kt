package com.example.ui.screens.profile

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import android.content.ClipboardManager
import android.content.ClipData
import android.widget.Toast
import com.example.data.model.ReelModel
import com.example.ui.components.VerifiedBadge
import com.example.ui.viewmodel.Screen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.components.FlikataAvatar
import com.example.ui.components.PostCard
import com.example.ui.components.ProfileSettingsBottomSheet
import com.example.ui.theme.FlikataBackground
import com.example.ui.theme.FlikataPrimary
import com.example.ui.theme.FlikataSoftBlue
import com.example.ui.theme.FlikataSurface
import com.example.ui.theme.FlikataTextPrimary
import com.example.ui.theme.FlikataTextSecondary
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onCreatePostClick: () -> Unit = {},
    onCreateStoryClick: () -> Unit = {}
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allPosts by viewModel.posts.collectAsState()
    val allReelsList by viewModel.reels.collectAsState()
    val selectedUserProfileId by viewModel.selectedUserProfileId.collectAsState()
    val selectedUserProfileName by viewModel.selectedUserProfileName.collectAsState()
    val context = LocalContext.current

    val isOwnProfile = selectedUserProfileId == null || selectedUserProfileId == currentUser?.id

    BackHandler(!isOwnProfile) {
        viewModel.clearSelectedUserProfile()
        onBack()
    }

    val tabs = listOf("All", "Reels", "Photos", "About", "More ▾")
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var dragAccumulator by remember { mutableFloatStateOf(0f) }

    val swipeModifier = Modifier.pointerInput(selectedTabIndex) {
        detectHorizontalDragGestures(
            onDragStart = { dragAccumulator = 0f },
            onHorizontalDrag = { change, dragAmount ->
                change.consume()
                dragAccumulator += dragAmount
            },
            onDragEnd = {
                val threshold = 50f
                if (dragAccumulator < -threshold) {
                    // Swiped Left -> Next tab
                    if (selectedTabIndex < tabs.size - 1) {
                        selectedTabIndex++
                    }
                } else if (dragAccumulator > threshold) {
                    // Swiped Right -> Previous tab
                    if (selectedTabIndex > 0) {
                        selectedTabIndex--
                    }
                }
                dragAccumulator = 0f
            },
            onDragCancel = {
                dragAccumulator = 0f
            }
        )
    }

    var showSettingsSheet by remember { mutableStateOf(false) }
    val settingsSheetState = rememberModalBottomSheetState()

    val userPosts = remember(allPosts, isOwnProfile, selectedUserProfileId, selectedUserProfileName, currentUser) {
        if (isOwnProfile) {
            allPosts.filter { it.userId == currentUser?.id || it.authorName == currentUser?.fullName }
        } else {
            allPosts.filter { it.userId == selectedUserProfileId || (selectedUserProfileName != null && it.authorName.equals(selectedUserProfileName, ignoreCase = true)) }
        }
    }

    val displayUserName = if (isOwnProfile) (currentUser?.fullName ?: "User") else (selectedUserProfileName ?: userPosts.firstOrNull()?.authorName ?: "Flikata User")
    val displayUserAvatar = if (isOwnProfile) (currentUser?.profilePic ?: "") else (userPosts.firstOrNull()?.authorAvatar ?: "")
    val displayIsVerified = if (isOwnProfile) (currentUser?.isVerified ?: false) else (userPosts.firstOrNull()?.isVerified ?: false)
    var isFollowingOtherUser by remember(selectedUserProfileId) { mutableStateOf(false) }

    val userReels = remember(userPosts, allReelsList, isOwnProfile, selectedUserProfileId, selectedUserProfileName, currentUser) {
        val videoPosts = userPosts.filter { !it.videoUrl.isNullOrBlank() || it.isVideo }
        val matchingReels = allReelsList.filter { r ->
            if (isOwnProfile) {
                r.userId == currentUser?.id || r.authorName.equals(currentUser?.fullName, ignoreCase = true)
            } else {
                r.userId == selectedUserProfileId || (selectedUserProfileName != null && r.authorName.equals(selectedUserProfileName, ignoreCase = true))
            }
        }
        val fromPosts = videoPosts.map { p ->
            ReelModel(
                id = "post_${p.id}",
                userId = p.userId,
                authorName = p.authorName,
                authorAvatar = p.authorAvatar,
                isVerified = p.isVerified,
                mediaUrl = p.videoUrl ?: "",
                caption = p.content.ifBlank { "Video Reel" },
                audioTitle = "Original Audio - ${p.authorName}",
                likesCount = p.likesCount,
                commentsCount = p.commentsCount,
                sharesCount = p.sharesCount,
                viewsCount = "${(p.likesCount * 13 + 120)}",
                isLiked = p.isLiked,
                userReaction = p.userReaction
            )
        }
        (fromPosts + matchingReels).distinctBy { it.mediaUrl.ifBlank { it.id } }
    }

    val userPhotos = userPosts.mapNotNull { it.imageUrl }

    val screenBg = MaterialTheme.colorScheme.background
    val screenSurface = MaterialTheme.colorScheme.surface
    val screenVariant = MaterialTheme.colorScheme.surfaceVariant
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Top Cover Banner + Overlapping Curved Profile Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(screenBg)
                ) {
                    // 1. Blue Gradient Taller Cover Banner (Height 260.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF0D47A1),
                                        Color(0xFF1877F2),
                                        Color(0xFF42A5F5)
                                    )
                                )
                            )
                    ) {
                        val coverModel: Any = if (!currentUser?.coverPic.isNullOrEmpty() && !currentUser?.coverPic!!.contains("unsplash")) {
                            currentUser?.coverPic!!
                        } else {
                            R.drawable.default_banner
                        }

                        AsyncImage(
                            model = coverModel,
                            contentDescription = "Cover Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Top Bar Action Buttons with status bars padding for clear status bar visibility
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CoverCircleButton(
                                icon = Icons.Default.ArrowBack,
                                onClick = {
                                    if (!isOwnProfile) {
                                        viewModel.clearSelectedUserProfile()
                                    }
                                    onBack()
                                }
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (isOwnProfile) {
                                    CoverCircleButton(icon = Icons.Default.Edit, onClick = { viewModel.prepareEditProfile() })
                                }
                                CoverCircleButton(icon = Icons.Default.Search, onClick = onSearchClick)
                                CoverCircleButton(icon = Icons.Default.MoreHoriz, onClick = { showSettingsSheet = true })
                            }
                        }

                        // Floating "Drop a thought..." Speech Bubble on Banner
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .offset(y = (-15).dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(
                                    color = screenSurface,
                                    shape = RoundedCornerShape(20.dp),
                                    shadowElevation = 4.dp
                                ) {
                                    Text(
                                        text = if (isOwnProfile) (currentUser?.bio?.ifBlank { "Drop a thought..." } ?: "Drop a thought...") else "Welcome to my profile!",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = textPrimary,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(GenericShape { size, _ ->
                                            moveTo(0f, 0f)
                                            lineTo(size.width, 0f)
                                            lineTo(size.width / 2, size.height)
                                            close()
                                        })
                                        .background(screenSurface)
                                )
                            }
                        }
                    }

                    // 2. White Curved Container Card Overlapping the Banner
                    Surface(
                        color = screenSurface,
                        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 210.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(top = 56.dp, bottom = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Name & Dropdown Arrow
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = displayUserName,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )
                                if (displayIsVerified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    VerifiedBadge(size = 18.dp)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(screenVariant)
                                        .clickable { showSettingsSheet = true },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Options",
                                        tint = textPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Followers, Following, Posts line
                            val followersCount = if (isOwnProfile) (currentUser?.followersCount ?: 0) else (if (isFollowingOtherUser) 1241 else 1240)
                            val followingCount = if (isOwnProfile) (currentUser?.followingCount ?: 0) else 185
                            Text(
                                text = "$followersCount followers • $followingCount following • ${userPosts.size} posts",
                                fontSize = 13.sp,
                                color = textSecondary
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Side-by-side Action Buttons
                            if (isOwnProfile) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = { },
                                        colors = ButtonDefaults.buttonColors(containerColor = FlikataPrimary),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                            .testTag("dashboard_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.BarChart,
                                            contentDescription = "Dashboard",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Dashboard",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color.White
                                        )
                                    }

                                    Button(
                                        onClick = onCreateStoryClick,
                                        colors = ButtonDefaults.buttonColors(containerColor = screenVariant),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                            .testTag("add_to_story_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add Story",
                                            tint = FlikataPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Add to Story",
                                            fontWeight = FontWeight.Bold,
                                            color = FlikataPrimary,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = { isFollowingOtherUser = !isFollowingOtherUser },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isFollowingOtherUser) screenVariant else FlikataPrimary
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                            .testTag("follow_user_btn")
                                    ) {
                                        Icon(
                                            imageVector = if (isFollowingOtherUser) Icons.Default.Check else Icons.Default.PersonAdd,
                                            contentDescription = null,
                                            tint = if (isFollowingOtherUser) FlikataPrimary else Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isFollowingOtherUser) "Following" else "Follow",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = if (isFollowingOtherUser) FlikataPrimary else Color.White
                                        )
                                    }

                                    Button(
                                        onClick = { viewModel.navigateTo(Screen.CHATS) },
                                        colors = ButtonDefaults.buttonColors(containerColor = screenVariant),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                            .testTag("message_user_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ChatBubbleOutline,
                                            contentDescription = "Message",
                                            tint = FlikataPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Message",
                                            fontWeight = FontWeight.Bold,
                                            color = FlikataPrimary,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Filter Tabs ("All", "Reels", "Photos", "About", "More ▾")
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(swipeModifier),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                tabs.forEachIndexed { index, tab ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clickable { selectedTabIndex = index }
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = tab,
                                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                            color = if (selectedTabIndex == index) FlikataPrimary else textSecondary,
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        if (selectedTabIndex == index) {
                                            Box(
                                                modifier = Modifier
                                                    .width(36.dp)
                                                    .height(3.dp)
                                                    .background(FlikataPrimary, CircleShape)
                                            )
                                        } else {
                                            Spacer(modifier = Modifier.height(3.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 3. Overlapping Circular Profile Picture Badge with initials logo fallback & Camera Badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = 150.dp)
                    ) {
                        FlikataAvatar(
                            name = displayUserName,
                            avatarUrl = displayUserAvatar.ifBlank { null },
                            size = 110.dp,
                            borderWidth = 4.dp,
                            borderColor = screenSurface,
                            modifier = Modifier.clickable {
                                if (isOwnProfile) {
                                    viewModel.prepareEditProfile()
                                } else if (displayUserAvatar.isNotBlank()) {
                                    viewModel.openImageViewer(displayUserAvatar)
                                }
                            }
                        )

                        // Camera overlay badge on avatar (only for own profile)
                        if (isOwnProfile) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset(x = (-4).dp, y = (-4).dp)
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(FlikataPrimary)
                                    .border(2.dp, screenSurface, CircleShape)
                                    .clickable { viewModel.prepareEditProfile() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Change Profile Picture",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Tab Content Rendering with Hand Swipe Gesture & Animated Transition
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(swipeModifier)
                ) {
                    AnimatedContent(
                        targetState = selectedTabIndex,
                        transitionSpec = {
                            if (targetState > initialState) {
                                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                    slideOutHorizontally { width -> -width } + fadeOut()
                                )
                            } else {
                                (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                    slideOutHorizontally { width -> width } + fadeOut()
                                )
                            }
                        },
                        label = "ProfileTabContent"
                    ) { targetIndex ->
                        when (tabs.getOrElse(targetIndex) { "All" }) {
                            "All" -> {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (userPosts.isEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 32.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "No posts yet",
                                                color = textSecondary,
                                                fontSize = 14.sp
                                            )
                                        }
                                    } else {
                                        userPosts.forEach { post ->
                                            PostCard(
                                                post = post,
                                                currentUser = currentUser,
                                                onLikeClick = { viewModel.toggleLike(it) },
                                                onReactionSelect = { postId, emoji -> viewModel.setReaction(postId, emoji) },
                                                onImageClick = { imageUrl -> viewModel.openImageViewer(imageUrl) },
                                                onDeletePost = { postId -> viewModel.deletePost(postId) },
                                                onEditPost = { postId, content, privacy -> viewModel.editPost(postId, content, privacy) }
                                            )
                                        }
                                    }
                                }
                            }

                            "Reels" -> {
                                Surface(color = screenSurface, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = "Uploaded Reels",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = textPrimary,
                                            modifier = Modifier.padding(bottom = 10.dp)
                                        )

                                        if (userReels.isEmpty()) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 32.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "No reels or videos uploaded yet",
                                                    color = textSecondary,
                                                    fontSize = 14.sp
                                                )
                                            }
                                        } else {
                                            val chunkedReels = userReels.chunked(3)
                                            chunkedReels.forEach { rowItems ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                rowItems.forEach { reel ->
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .aspectRatio(0.75f)
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(Color.DarkGray)
                                                            .clickable { viewModel.openReel(reel.id) }
                                                    ) {
                                                        AsyncImage(
                                                            model = reel.mediaUrl.ifBlank { reel.authorAvatar },
                                                            contentDescription = "Reel Thumbnail",
                                                            contentScale = ContentScale.Crop,
                                                            modifier = Modifier.fillMaxSize()
                                                        )

                                                        Icon(
                                                            imageVector = Icons.Default.PlayArrow,
                                                            contentDescription = "Play",
                                                            tint = Color.White,
                                                            modifier = Modifier
                                                                .align(Alignment.TopEnd)
                                                                .padding(6.dp)
                                                                .size(20.dp)
                                                        )

                                                        Row(
                                                            modifier = Modifier
                                                                .align(Alignment.BottomStart)
                                                                .fillMaxWidth()
                                                                .background(
                                                                    Brush.verticalGradient(
                                                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                                                    )
                                                                )
                                                                .padding(6.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.PlayArrow,
                                                                contentDescription = "Views",
                                                                tint = Color.White,
                                                                modifier = Modifier.size(12.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(3.dp))
                                                            Text(
                                                                text = "${reel.viewsCount.ifBlank { (reel.likesCount * 12 + 140).toString() }} views",
                                                                color = Color.White,
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                }

                                                repeat(3 - rowItems.size) {
                                                    Spacer(modifier = Modifier.weight(1f))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                            "Photos" -> {
                                Surface(color = screenSurface, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = "Uploaded Photos",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = textPrimary,
                                            modifier = Modifier.padding(bottom = 10.dp)
                                        )

                                        if (userPhotos.isEmpty()) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 32.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "No photos uploaded yet",
                                                    color = textSecondary,
                                                    fontSize = 14.sp
                                                )
                                            }
                                        } else {
                                            val chunkedPhotos = userPhotos.chunked(3)
                                            chunkedPhotos.forEach { photoRow ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    photoRow.forEach { photoUrl ->
                                                        Box(
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .aspectRatio(1f)
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                                                .clickable { viewModel.openImageViewer(photoUrl) }
                                                        ) {
                                                            AsyncImage(
                                                                model = photoUrl,
                                                                contentDescription = "Photo",
                                                                contentScale = ContentScale.Crop,
                                                                modifier = Modifier.fillMaxSize()
                                                            )
                                                        }
                                                    }
                                                    repeat(3 - photoRow.size) {
                                                        Spacer(modifier = Modifier.weight(1f))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            "About" -> {
                                Surface(color = screenSurface, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "About & Intro",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp,
                                                color = textPrimary
                                            )

                                            IconButton(onClick = { viewModel.prepareEditProfile() }) {
                                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = textPrimary)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        AboutDetailRow(icon = Icons.Default.Work, text = viewModel.editOccupation.value.ifBlank { "Software Engineer" })
                                        AboutDetailRow(icon = Icons.Default.School, text = "Studied Computer Science")
                                        AboutDetailRow(icon = Icons.Default.Home, text = "Lives in ${viewModel.editLocation.value.ifBlank { "Dhaka, Bangladesh" }}")
                                        AboutDetailRow(icon = Icons.Default.Favorite, text = "Single")
                                        AboutDetailRow(icon = Icons.Default.CalendarMonth, text = currentUser?.joinedDate ?: "Joined September 2026")
                                    }
                                }
                            }

                            else -> {
                                Surface(color = screenSurface, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "More Options",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = textPrimary,
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        )

                                        MoreOptionItem(icon = Icons.Default.Group, title = "Friends", subtitle = "1,240 friends")
                                        MoreOptionItem(icon = Icons.Default.Bookmark, title = "Saved Posts", subtitle = "24 saved items")
                                        MoreOptionItem(icon = Icons.Default.Favorite, title = "Liked Content", subtitle = "Posts & reels you liked")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet for Profile 3-Dots Settings matching Screenshot 3
    if (showSettingsSheet) {
        ProfileSettingsBottomSheet(
            sheetState = settingsSheetState,
            isOwnProfile = isOwnProfile,
            onDismiss = { showSettingsSheet = false },
            onOptionClick = { option ->
                if (option == "Copy Link" || option.contains("Link", ignoreCase = true)) {
                    val targetUserId = if (isOwnProfile) (currentUser?.id ?: "") else (selectedUserProfileId ?: "")
                    val profileUrl = if (targetUserId.isNotBlank()) {
                        "https://flikata.cyou/backend/profile.php?id=$targetUserId"
                    } else {
                        "https://flikata.cyou/backend/profile.php"
                    }
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Flikata Profile Link", profileUrl)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Profile link copied to clipboard!", Toast.LENGTH_SHORT).show()
                } else if (option == "Share Profile") {
                    val targetUserId = if (isOwnProfile) (currentUser?.id ?: "") else (selectedUserProfileId ?: "")
                    val profileUrl = "https://flikata.cyou/backend/profile.php?id=$targetUserId"
                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(android.content.Intent.EXTRA_TEXT, "Check out this profile on Flikata:\n$profileUrl")
                    }
                    context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Profile"))
                }
            },
            onLogoutClick = { viewModel.logout() }
        )
    }
}

@Composable
private fun AboutDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = FlikataPrimary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun MoreOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = FlikataPrimary)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CoverCircleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}
