package com.example.ui.screens.profile

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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Work
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

    val userPosts = allPosts.filter {
        it.userId == currentUser?.id || it.authorName == currentUser?.fullName
    }

    val userReels = userPosts.filter { it.imageUrl != null }

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
                            CoverCircleButton(icon = Icons.Default.ArrowBack, onClick = onBack)

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                CoverCircleButton(icon = Icons.Default.Edit, onClick = { viewModel.prepareEditProfile() })
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
                                        text = currentUser?.bio?.ifBlank { "Drop a thought..." } ?: "Drop a thought...",
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
                                    text = currentUser?.fullName ?: "md sumon Islam",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )
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
                            Text(
                                text = "${currentUser?.followersCount ?: 0} followers • ${currentUser?.followingCount ?: 0} following • ${userPosts.size} posts",
                                fontSize = 13.sp,
                                color = textSecondary
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Side-by-side Action Buttons ("Dashboard" & "+ Add to Story")
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
                            name = currentUser?.fullName ?: "User",
                            avatarUrl = currentUser?.profilePic,
                            size = 110.dp,
                            borderWidth = 4.dp,
                            borderColor = screenSurface,
                            modifier = Modifier.clickable { viewModel.prepareEditProfile() }
                        )

                        // Camera overlay badge on avatar
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
                                                onDeletePost = { postId -> viewModel.deletePost(postId) }
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
                                                    ) {
                                                        AsyncImage(
                                                            model = reel.imageUrl ?: reel.authorAvatar,
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
                                                                text = "${reel.likesCount * 12 + 140} views",
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
            onDismiss = { showSettingsSheet = false },
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
