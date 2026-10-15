package com.example.ui.screens.profile

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.Call
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import android.content.ClipboardManager
import android.content.ClipData
import android.widget.Toast
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
import androidx.compose.ui.draw.shadow
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
import com.example.ui.components.SoftUITheme
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

    BackHandler {
        if (!isOwnProfile) {
            viewModel.clearSelectedUserProfile()
        }
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

    val externalProfile by viewModel.selectedUserProfileData.collectAsState()

    val displayUserName = if (isOwnProfile) (currentUser?.fullName ?: "User") else (externalProfile?.fullName ?: selectedUserProfileName ?: userPosts.firstOrNull()?.authorName ?: "Flikata User")
    val displayUserAvatar = if (isOwnProfile) (currentUser?.profilePic ?: "") else (externalProfile?.profilePic ?: userPosts.firstOrNull()?.authorAvatar ?: "")
    val displayUserCover = if (isOwnProfile) (currentUser?.coverPic ?: "") else (externalProfile?.coverPic ?: "")
    val displayIsVerified = if (isOwnProfile) (currentUser?.isVerified ?: false) else (externalProfile?.isVerifiedBool ?: userPosts.firstOrNull()?.isVerified ?: false)
    val displayIsOnline = if (isOwnProfile) true else (externalProfile?.isOnlineBool ?: false)
    val displayBio = if (isOwnProfile) (currentUser?.bio ?: "Drop a thought...") else (externalProfile?.bio ?: "Hey there! I am using Flikata.")
    val displayOccupation = if (isOwnProfile) "Digital Creator" else (externalProfile?.occupation ?: "Digital Creator")
    val displayLocation = if (isOwnProfile) "Dhaka, Bangladesh" else (externalProfile?.location ?: "Dhaka, Bangladesh")
    val displayJoinedDate = if (isOwnProfile) (currentUser?.joinedDate ?: "Joined September 2026") else (externalProfile?.joinedDate ?: "Joined October 2026")
    var isFollowingOtherUser by remember(selectedUserProfileId) { mutableStateOf(false) }

    val micLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val targetUid = selectedUserProfileId?.removePrefix("user_")?.toIntOrNull() ?: 1
            if (!displayIsOnline) {
                Toast.makeText(context, "Offline", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.startAudioCall(
                    partnerId = targetUid,
                    partnerName = displayUserName,
                    partnerAvatar = displayUserAvatar,
                    isVerified = displayIsVerified
                )
            }
        } else {
            Toast.makeText(context, "মাইক্রোফোন পারমিশন ছাড়া অডিও কল করা সম্ভব নয়", Toast.LENGTH_SHORT).show()
        }
    }

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
            val matchedReel = allReelsList.find { r ->
                (r.mediaUrl.isNotBlank() && r.mediaUrl == p.videoUrl) || r.id == "post_${p.id}"
            }
            val realViews = matchedReel?.viewsCount?.ifBlank { null } ?: "${p.viewsCount}"
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
                viewsCount = realViews,
                isLiked = p.isLiked,
                userReaction = p.userReaction
            )
        }
        (fromPosts + matchingReels).distinctBy { it.mediaUrl.ifBlank { it.id } }
    }

    val userPhotos = userPosts.mapNotNull { it.imageUrl }

    val isDark = SoftUITheme.isDark
    val screenBg = MaterialTheme.colorScheme.background
    val screenSurface = MaterialTheme.colorScheme.surface
    val screenVariant = MaterialTheme.colorScheme.surfaceVariant
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant

    val cardBg = if (isDark) Color(0xFF242730) else Color.White
    val secondaryBtnBg = if (isDark) Color(0xFF2D3039) else Color(0xFFF3F7FD)
    val secondaryBtnBorder = if (isDark) Color(0x33FFFFFF) else Color(0x33A0C0E8)
    val secondaryBtnText = if (isDark) Color(0xFF3897F0) else Color(0xFF0075FF)

    val isRefreshingProfile by viewModel.isRefreshingProfile.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
    ) {
        PullToRefreshBox(
            isRefreshing = isRefreshingProfile,
            onRefresh = { viewModel.refreshProfile() },
            modifier = Modifier.fillMaxSize()
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
                        val coverModel: Any = if (isOwnProfile) {
                            if (!currentUser?.coverPic.isNullOrEmpty() && !currentUser?.coverPic!!.contains("unsplash")) {
                                currentUser?.coverPic!!
                            } else {
                                R.drawable.default_banner
                            }
                        } else {
                            if (displayUserCover.isNotBlank() && !displayUserCover.contains("unsplash")) {
                                displayUserCover
                            } else {
                                R.drawable.default_banner
                            }
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
                                icon = Icons.AutoMirrored.Filled.ArrowBack,
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

                    // 2. 3D Neumorphic Curved Container Card Overlapping the Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 210.dp)
                            .shadow(
                                elevation = if (isDark) 5.dp else 8.dp,
                                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                                ambientColor = com.example.ui.components.SoftUITheme.ShadowDark,
                                spotColor = com.example.ui.components.SoftUITheme.ShadowDark
                            )
                            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                            .background(cardBg)
                            .border(
                                width = 1.dp,
                                brush = if (isDark)
                                    Brush.verticalGradient(listOf(Color(0x33FFFFFF), Color(0x11FFFFFF)))
                                else
                                    Brush.verticalGradient(listOf(Color(0xEEFFFFFF), Color(0x33A0C0E8))),
                                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(top = 56.dp, bottom = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Name & Verification Badge
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
                                    Spacer(modifier = Modifier.width(6.dp))
                                    VerifiedBadge(size = 26.dp)
                                }
                                if (displayIsOnline && !isOwnProfile) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(11.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF45BD62))
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Followers, Following, Posts line
                            val followersCount = if (isOwnProfile) (currentUser?.followersCount ?: 0) else (if (isFollowingOtherUser) 1241 else 1240)
                            val followingCount = if (isOwnProfile) (currentUser?.followingCount ?: 0) else 185
                            val actualPostsCount = if (isOwnProfile) userPosts.size else (externalProfile?.postsCount ?: userPosts.size)
                            Text(
                                text = "$followersCount followers • $followingCount following • $actualPostsCount posts",
                                fontSize = 13.sp,
                                color = textSecondary
                            )

                            if (displayBio.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = displayBio,
                                    fontSize = 13.5.sp,
                                    color = textPrimary,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // 3D Side-by-side Action Buttons
                            if (isOwnProfile) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // 3D Primary Button: Dashboard
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(46.dp)
                                            .shadow(
                                                elevation = 6.dp,
                                                shape = RoundedCornerShape(14.dp),
                                                ambientColor = Color(0x660075FF),
                                                spotColor = Color(0x880052D4)
                                            )
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(Color(0xFF0075FF), Color(0xFF0052D4))
                                                )
                                            )
                                            .border(1.dp, Color(0x88FFFFFF), RoundedCornerShape(14.dp))
                                            .clickable {
                                                viewModel.navigateTo(Screen.DASHBOARD)
                                            }
                                            .testTag("dashboard_btn"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
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
                                    }

                                    // 3D Secondary Button: Add to Story
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(46.dp)
                                            .shadow(
                                                elevation = if (isDark) 2.dp else 4.dp,
                                                shape = RoundedCornerShape(14.dp),
                                                ambientColor = com.example.ui.components.SoftUITheme.ShadowDark
                                            )
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(secondaryBtnBg)
                                            .border(1.dp, secondaryBtnBorder, RoundedCornerShape(14.dp))
                                            .clickable { onCreateStoryClick() }
                                            .testTag("add_to_story_btn"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Add Story",
                                                tint = secondaryBtnText,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Add to Story",
                                                fontWeight = FontWeight.Bold,
                                                color = secondaryBtnText,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // 3D Follow Button
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(46.dp)
                                            .shadow(
                                                elevation = if (isFollowingOtherUser) (if (isDark) 2.dp else 3.dp) else 6.dp,
                                                shape = RoundedCornerShape(14.dp),
                                                ambientColor = if (isFollowingOtherUser) com.example.ui.components.SoftUITheme.ShadowDark else Color(0x660075FF)
                                            )
                                            .clip(RoundedCornerShape(14.dp))
                                            .then(
                                                if (isFollowingOtherUser) Modifier.background(secondaryBtnBg)
                                                else Modifier.background(Brush.verticalGradient(listOf(Color(0xFF0075FF), Color(0xFF0052D4))))
                                            )
                                            .border(
                                                1.dp,
                                                if (isFollowingOtherUser) secondaryBtnBorder else Color(0x88FFFFFF),
                                                RoundedCornerShape(14.dp)
                                            )
                                            .clickable { isFollowingOtherUser = !isFollowingOtherUser }
                                            .testTag("follow_user_btn"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (isFollowingOtherUser) Icons.Default.Check else Icons.Default.PersonAdd,
                                                contentDescription = null,
                                                tint = if (isFollowingOtherUser) secondaryBtnText else Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (isFollowingOtherUser) "Following" else "Follow",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = if (isFollowingOtherUser) secondaryBtnText else Color.White
                                            )
                                        }
                                    }

                                    // 3D Message Button
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(46.dp)
                                            .shadow(
                                                elevation = if (isDark) 2.dp else 4.dp,
                                                shape = RoundedCornerShape(14.dp),
                                                ambientColor = com.example.ui.components.SoftUITheme.ShadowDark
                                            )
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(secondaryBtnBg)
                                            .border(1.dp, secondaryBtnBorder, RoundedCornerShape(14.dp))
                                            .clickable {
                                                val targetUid = selectedUserProfileId ?: userPosts.firstOrNull()?.userId
                                                viewModel.openChatWithUser(
                                                    name = displayUserName,
                                                    avatar = displayUserAvatar,
                                                    isVerified = displayIsVerified,
                                                    userId = targetUid
                                                )
                                            }
                                            .testTag("message_user_btn"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.ChatBubbleOutline,
                                                contentDescription = "Message",
                                                tint = secondaryBtnText,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Message",
                                                fontWeight = FontWeight.Bold,
                                                color = secondaryBtnText,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }

                                    // 3D Audio Call Button
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .shadow(
                                                elevation = if (isDark) 2.dp else 4.dp,
                                                shape = RoundedCornerShape(14.dp),
                                                ambientColor = com.example.ui.components.SoftUITheme.ShadowDark
                                            )
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color(0xFF0075FF), Color(0xFF0052D4))))
                                            .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(14.dp))
                                            .clickable {
                                                val hasMic = ContextCompat.checkSelfPermission(
                                                    context,
                                                    Manifest.permission.RECORD_AUDIO
                                                ) == PackageManager.PERMISSION_GRANTED
                                                if (hasMic) {
                                                    val targetUid = selectedUserProfileId?.removePrefix("user_")?.toIntOrNull() ?: 1
                                                    if (!displayIsOnline) {
                                                        Toast.makeText(context, "Offline", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        viewModel.startAudioCall(
                                                            partnerId = targetUid,
                                                            partnerName = displayUserName,
                                                            partnerAvatar = displayUserAvatar,
                                                            isVerified = displayIsVerified
                                                        )
                                                    }
                                                } else {
                                                    micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                                }
                                            }
                                            .testTag("profile_audio_call_btn"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Call,
                                            contentDescription = "Audio Call",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
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

                        // Active green status dot on avatar for other online user
                        if (displayIsOnline && !isOwnProfile) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset(x = (-4).dp, y = (-4).dp)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF45BD62))
                                    .border(3.5.dp, screenSurface, CircleShape)
                            )
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
                                                    val context = LocalContext.current
                                                    val videoRequest = remember(reel.mediaUrl) {
                                                        if (reel.mediaUrl.isNotBlank() && (reel.mediaUrl.endsWith(".mp4", ignoreCase = true) || reel.mediaUrl.contains("/video", ignoreCase = true))) {
                                                            ImageRequest.Builder(context)
                                                                .data(reel.mediaUrl)
                                                                .decoderFactory { result, options, _ -> VideoFrameDecoder(result.source, options) }
                                                                .videoFrameMillis(1000)
                                                                .crossfade(true)
                                                                .build()
                                                        } else {
                                                            reel.mediaUrl.ifBlank { reel.authorAvatar }
                                                        }
                                                    }

                                                    val rawViews = reel.viewsCount.toIntOrNull() ?: 0
                                                    val formattedViews = if (rawViews >= 1000) {
                                                        String.format(java.util.Locale.US, "%.1fK", rawViews / 1000.0)
                                                    } else {
                                                        "$rawViews"
                                                    }

                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .aspectRatio(0.5625f) // 9:16 vertical ratio for taller reels
                                                            .clip(RoundedCornerShape(10.dp))
                                                            .background(Color(0xFF1E2128))
                                                            .clickable { viewModel.openReel(reel.id) }
                                                    ) {
                                                        AsyncImage(
                                                            model = videoRequest,
                                                            contentDescription = "Reel Thumbnail",
                                                            contentScale = ContentScale.Crop,
                                                            modifier = Modifier.fillMaxSize()
                                                        )

                                                        // Subtle top gradient & play badge
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .height(40.dp)
                                                                .background(
                                                                    Brush.verticalGradient(
                                                                        listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent)
                                                                    )
                                                                )
                                                        )

                                                        Icon(
                                                            imageVector = Icons.Default.PlayArrow,
                                                            contentDescription = "Play",
                                                            tint = Color.White,
                                                            modifier = Modifier
                                                                .align(Alignment.TopEnd)
                                                                .padding(6.dp)
                                                                .size(18.dp)
                                                        )

                                                        // Bottom views bar with verified real view count
                                                        Row(
                                                            modifier = Modifier
                                                                .align(Alignment.BottomStart)
                                                                .fillMaxWidth()
                                                                .background(
                                                                    Brush.verticalGradient(
                                                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                                                    )
                                                                )
                                                                .padding(horizontal = 6.dp, vertical = 6.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.PlayArrow,
                                                                contentDescription = "Views",
                                                                tint = Color.White,
                                                                modifier = Modifier.size(13.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(3.dp))
                                                            Text(
                                                                text = "$formattedViews views",
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

                                             if (isOwnProfile) {
                                                IconButton(onClick = { viewModel.prepareEditProfile() }) {
                                                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = textPrimary)
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        AboutDetailRow(icon = Icons.Default.Work, text = displayOccupation)
                                        AboutDetailRow(icon = Icons.Default.School, text = "Studied at University")
                                        AboutDetailRow(icon = Icons.Default.Home, text = "Lives in $displayLocation")
                                        AboutDetailRow(icon = Icons.Default.Favorite, text = "Single")
                                        AboutDetailRow(icon = Icons.Default.CalendarMonth, text = displayJoinedDate)
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
}

    // Modal Bottom Sheet for Profile 3-Dots Settings matching Screenshot 3
    if (showSettingsSheet) {
        ProfileSettingsBottomSheet(
            sheetState = settingsSheetState,
            isOwnProfile = isOwnProfile,
            onDismiss = { showSettingsSheet = false },
            onOptionClick = { option ->
                if (option.equals("Dashboard", ignoreCase = true)) {
                    viewModel.navigateTo(Screen.DASHBOARD)
                } else if (option == "Copy Link" || option.contains("Link", ignoreCase = true)) {
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
                } else if (option == "Verified Badge" || option == "Verified") {
                    viewModel.navigateTo(Screen.VERIFIED_BADGE)
                } else if (option == "Share Profile") {
                    val targetUserId = if (isOwnProfile) (currentUser?.id ?: "") else (selectedUserProfileId ?: "")
                    val profileUrl = "https://flikata.cyou/backend/profile.php?id=$targetUserId"
                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(android.content.Intent.EXTRA_TEXT, "Check out this profile on Flikata:\n$profileUrl")
                    }
                    context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Profile"))
                } else if (option.equals("Pages", ignoreCase = true)) {
                    Toast.makeText(context, "Pages coming soon!", Toast.LENGTH_SHORT).show()
                } else if (option.equals("Groups", ignoreCase = true)) {
                    Toast.makeText(context, "Groups coming soon!", Toast.LENGTH_SHORT).show()
                } else if (option.equals("Settings", ignoreCase = true)) {
                    Toast.makeText(context, "Settings coming soon!", Toast.LENGTH_SHORT).show()
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
