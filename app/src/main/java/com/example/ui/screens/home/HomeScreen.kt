package com.example.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Story
import com.example.ui.components.FlikataAvatar
import com.example.ui.components.FlikataHeader
import com.example.ui.components.PostCard
import com.example.ui.components.PostUploadPipeCard
import com.example.ui.components.ShimmerHomeFeed
import com.example.ui.theme.FlikataPrimary
import com.example.ui.theme.FlikataSoftBlue
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onCreatePostClick: () -> Unit,
    onMenuClick: () -> Unit,
    onCreateStoryClick: () -> Unit = {}
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val posts by viewModel.posts.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val stories by viewModel.stories.collectAsState()
    val unreadNotificationsCount by viewModel.unreadNotificationsCount.collectAsState()
    val uploadState by viewModel.postUploadState.collectAsState()

    val userAvatarUrl = currentUser?.profilePic ?: ""

    val bg = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
    ) {
        // Top Header
        FlikataHeader(
            unreadNotificationsCount = unreadNotificationsCount,
            onSearchClick = { viewModel.navigateTo(Screen.SEARCH) },
            onNotificationsClick = { viewModel.navigateTo(Screen.NOTIFICATIONS) },
            onMenuClick = onMenuClick
        )

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refreshFeed() },
            modifier = Modifier.fillMaxSize()
        ) {
            if (isRefreshing && posts.isEmpty()) {
                ShimmerHomeFeed()
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 60.dp)
                ) {
                    // "What's on your mind?" Composer Card
                    item {
                        Surface(
                            color = surfaceColor,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                // User Profile Avatar with Initials Logo Fallback
                                FlikataAvatar(
                                    name = currentUser?.fullName ?: "User",
                                    avatarUrl = currentUser?.profilePic,
                                    size = 40.dp
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                // Pill input box
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                        .clickable { onCreatePostClick() }
                                        .padding(horizontal = 14.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = "What's on your mind?",
                                        color = textSecondary,
                                        fontSize = 15.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                // Green Photo Gallery Icon
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = "Gallery",
                                    tint = Color(0xFF45BD62),
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clickable { onCreatePostClick() }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // Stories Section
                    item {
                        Surface(
                            color = surfaceColor,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // 1. Always exactly one Create Story Card at the start
                                item(key = "create_story_card_item") {
                                    CreateStoryCard(
                                        userAvatar = userAvatarUrl,
                                        surfaceColor = surfaceColor,
                                        onClick = onCreateStoryClick
                                    )
                                }

                                // 2. Real user and friend stories
                                val actualStories = stories.filter { it.id != "story_create" }
                                itemsIndexed(actualStories, key = { _, s -> s.id }) { index, story ->
                                    StoryItemCard(
                                        story = story,
                                        onStoryClick = { viewModel.openStoryViewer(index) }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // Post Upload Pipe (animates 0% to 100% until post is complete)
                    if (uploadState.isUploading) {
                        item {
                            PostUploadPipeCard(
                                uploadState = uploadState,
                                userAvatar = userAvatarUrl
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }

                    // Post Feed List
                    itemsIndexed(posts, key = { _, post -> post.id }) { _, post ->
                        PostCard(
                            post = post,
                            currentUser = currentUser,
                            onLikeClick = { viewModel.toggleLike(it) },
                            onReactionSelect = { postId, emoji -> viewModel.setReaction(postId, emoji) },
                            onImageClick = { imageUrl -> viewModel.openImageViewer(imageUrl) },
                            onDeletePost = { postId -> viewModel.deletePost(postId) },
                            onAddComment = { postId, text -> viewModel.addComment(postId, text) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateStoryCard(
    userAvatar: String,
    surfaceColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(105.dp)
            .height(165.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            .clickable { onClick() }
            .testTag("create_story_card")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (userAvatar.isNotBlank()) {
                    AsyncImage(
                        model = userAvatar,
                        contentDescription = "Create Story Avatar Background",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    FlikataAvatar(
                        name = "Me",
                        avatarUrl = null,
                        size = 46.dp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(surfaceColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Create story",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 34.dp)
                .size(32.dp)
                .clip(CircleShape)
                .background(FlikataPrimary)
                .border(2.dp, surfaceColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Story",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun StoryItemCard(
    story: Story,
    onStoryClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(105.dp)
            .height(165.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.DarkGray)
            .clickable { onStoryClick() }
    ) {
        if (!story.textContent.isNullOrBlank()) {
            val bgBrush = when (story.backgroundColor) {
                "purple" -> Brush.verticalGradient(listOf(Color(0xFF8E24AA), Color(0xFF4A148C)))
                "orange" -> Brush.verticalGradient(listOf(Color(0xFFFF6D00), Color(0xFFD50000)))
                "teal" -> Brush.verticalGradient(listOf(Color(0xFF00BFA5), Color(0xFF004D40)))
                "black" -> Brush.verticalGradient(listOf(Color(0xFF212121), Color(0xFF000000)))
                "crimson" -> Brush.verticalGradient(listOf(Color(0xFFFF1744), Color(0xFF880E4F)))
                "gradient_blue" -> Brush.verticalGradient(listOf(Color(0xFF00B0FF), Color(0xFF0044FF)))
                else -> Brush.verticalGradient(listOf(Color(0xFF1877F2), Color(0xFF0D47A1)))
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bgBrush)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = story.textContent,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else {
            AsyncImage(
                model = story.storyImage,
                contentDescription = story.authorName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        AsyncImage(
            model = story.authorAvatar,
            contentDescription = story.authorName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .padding(8.dp)
                .size(34.dp)
                .align(Alignment.TopStart)
                .clip(CircleShape)
                .border(2.dp, FlikataPrimary, CircleShape)
        )

        Text(
            text = story.authorName,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
        )
    }
}
