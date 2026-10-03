package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.example.R
import com.example.data.model.Post
import com.example.data.model.User
import com.example.ui.theme.FlikataDivider
import com.example.ui.theme.FlikataError
import com.example.ui.theme.FlikataPrimary
import com.example.ui.theme.FlikataSurface
import com.example.ui.theme.FlikataTextPrimary
import com.example.ui.theme.FlikataTextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostCard(
    post: Post,
    currentUser: User? = null,
    onLikeClick: (String) -> Unit = {},
    onReactionSelect: (String, String?) -> Unit = { _, _ -> },
    onImageClick: (String) -> Unit = {},
    onDeletePost: (String) -> Unit = {},
    onAddComment: (String, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var showReactionPicker by remember { mutableStateOf(false) }
    var activeFloatingEmoji by remember { mutableStateOf<String?>(null) }

    var showReactionsSheet by remember { mutableStateOf(false) }
    var showCommentsSheet by remember { mutableStateOf(false) }
    var showOptionsSheet by remember { mutableStateOf(false) }

    val reactionsSheetState = rememberModalBottomSheetState()
    val commentsSheetState = rememberModalBottomSheetState()
    val optionsSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    val isOwnPost = currentUser != null && (currentUser.id == post.userId || currentUser.fullName == post.authorName)

    val cardSurface = androidx.compose.material3.MaterialTheme.colorScheme.surface
    val textPrimary = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
    val textSecondary = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        color = cardSurface,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FlikataAvatar(
                        name = post.authorName,
                        avatarUrl = post.authorAvatar,
                        size = 42.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = post.authorName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = textPrimary
                            )
                            if (post.isVerified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                VerifiedBadge(size = 14.dp)
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = post.timestamp,
                                fontSize = 12.sp,
                                color = textSecondary
                            )
                            Text(
                                text = " • ",
                                fontSize = 12.sp,
                                color = textSecondary
                            )
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = post.privacy,
                                tint = textSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                // 3-Dots Options Button
                IconButton(onClick = { showOptionsSheet = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = "Options",
                        tint = textSecondary
                    )
                }
            }

            // Text Body / Background Styled Post
            if (post.backgroundStyle != null) {
                val bgBrush = PostBackgroundHelper.getBrush(post.backgroundStyle)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(bgBrush),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = post.content,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            } else if (post.content.isNotEmpty()) {
                Text(
                    text = post.content,
                    fontSize = 15.sp,
                    color = textPrimary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            // Attached Video Player if post is a video
            if (post.isVideo && !post.videoUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(Color.Black)
                ) {
                    FlikataVideoPlayer(
                        videoUri = post.videoUrl,
                        modifier = Modifier.fillMaxSize(),
                        autoPlay = false,
                        repeat = true,
                        showControls = true
                    )
                }
            } else if (!post.imageUrl.isNullOrBlank()) {
                // Attached Image with clean, robust AsyncImage
                Spacer(modifier = Modifier.height(4.dp))
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = "Post Image",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onImageClick(post.imageUrl) }
                )
            }

            // Reaction Counts Summary Row - only show if there are likes, comments, or shares
            if (post.likesCount > 0 || post.commentsCount > 0 || post.sharesCount > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (post.likesCount > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { showReactionsSheet = true }
                        ) {
                            val reactionEmoji = post.userReaction ?: "Like"
                            GraphicReactionIcon(reactionEmoji, size = 18.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${post.likesCount}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textSecondary
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (post.commentsCount > 0) {
                            Text(
                                text = "${post.commentsCount} ${if (post.commentsCount == 1) "comment" else "comments"}",
                                fontSize = 13.sp,
                                color = textSecondary,
                                modifier = Modifier.clickable { showCommentsSheet = true }
                            )
                        }
                        if (post.sharesCount > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${post.sharesCount} shares",
                                fontSize = 13.sp,
                                color = textSecondary
                            )
                        }
                    }
                }
            }

            Divider(color = androidx.compose.material3.MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), thickness = 0.5.dp)

            // Action Buttons Row with Reaction Picker Floating Popup
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                if (showReactionPicker) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .zIndex(50f)
                            .pointerInput(Unit) {
                                detectTapGestures(onTap = { showReactionPicker = false })
                            }
                    )

                    FacebookReactionPicker(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = 12.dp, y = (-54).dp)
                            .zIndex(100f),
                        onReactionSelected = { reactionName ->
                            onReactionSelect(post.id, reactionName)
                            activeFloatingEmoji = reactionName
                            showReactionPicker = false
                        }
                    )
                }

                if (activeFloatingEmoji != null) {
                    FloatingEmojiAnimation(
                        reactionType = activeFloatingEmoji!!,
                        onAnimationEnd = { activeFloatingEmoji = null },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = 32.dp, y = (-20).dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Box(
                        modifier = Modifier
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = {
                                        if (post.userReaction != null) {
                                            onReactionSelect(post.id, null)
                                        } else {
                                            onReactionSelect(post.id, "Like")
                                            activeFloatingEmoji = "Like"
                                        }
                                    },
                                    onLongPress = {
                                        showReactionPicker = !showReactionPicker
                                    }
                                )
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("like_post_${post.id}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (post.userReaction != null) {
                                GraphicReactionIcon(
                                    type = post.userReaction,
                                    size = 20.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = post.userReaction,
                                    color = when (post.userReaction) {
                                        "Love", "❤️" -> Color(0xFFFA383E)
                                        "Angry", "😡" -> Color(0xFFE44D3A)
                                        "Like", "👍" -> FlikataPrimary
                                        else -> Color(0xFFF7B125)
                                    },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_custom_like),
                                    contentDescription = "Like",
                                    tint = textSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Like",
                                    color = textSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .clickable { showCommentsSheet = true }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("comment_post_${post.id}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_custom_comment),
                            contentDescription = "Comment",
                            tint = textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Comment",
                            color = textSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(
                        modifier = Modifier
                            .clickable { }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("share_post_${post.id}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_custom_share),
                            contentDescription = "Share",
                            tint = textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Share",
                            color = textSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }

    // Modal Sheet for Post Options (3-Dots)
    if (showOptionsSheet) {
        PostOptionsBottomSheet(
            isOwnPost = isOwnPost,
            sheetState = optionsSheetState,
            onDismiss = {
                scope.launch { optionsSheetState.hide() }.invokeOnCompletion { showOptionsSheet = false }
            },
            onDeleteClick = {
                onDeletePost(post.id)
                scope.launch { optionsSheetState.hide() }.invokeOnCompletion { showOptionsSheet = false }
            }
        )
    }

    if (showReactionsSheet) {
        ReactionsBottomSheet(
            post = post,
            sheetState = reactionsSheetState,
            onDismiss = {
                scope.launch { reactionsSheetState.hide() }.invokeOnCompletion { showReactionsSheet = false }
            }
        )
    }

    if (showCommentsSheet) {
        CommentsBottomSheet(
            post = post,
            currentUser = currentUser,
            sheetState = commentsSheetState,
            onAddComment = onAddComment,
            onDismiss = {
                scope.launch { commentsSheetState.hide() }.invokeOnCompletion { showCommentsSheet = false }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostOptionsBottomSheet(
    isOwnPost: Boolean,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val sheetSurface = androidx.compose.material3.MaterialTheme.colorScheme.surface
    val sheetOnSurface = androidx.compose.material3.MaterialTheme.colorScheme.onSurface

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetSurface,
        contentColor = sheetOnSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(sheetSurface)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            if (isOwnPost) {
                // Own Post Options (Matching Screenshot 1 without Boost Post, Boost Analytics, Report Post)
                PostOptionRow(
                    icon = Icons.Default.Edit,
                    iconTint = FlikataPrimary,
                    title = "Edit Post",
                    onClick = onDismiss
                )

                PostOptionRow(
                    icon = Icons.Default.Delete,
                    iconTint = FlikataError,
                    title = "Delete Post",
                    onClick = onDeleteClick
                )

                PostOptionRow(
                    icon = Icons.Default.ContentCopy,
                    iconTint = FlikataTextSecondary,
                    title = "Copy Link",
                    onClick = onDismiss
                )

                PostOptionRow(
                    icon = Icons.Default.Share,
                    iconTint = FlikataTextSecondary,
                    title = "Share to...",
                    onClick = onDismiss
                )
            } else {
                // Someone Else's Post Options (Matching Screenshot 2)
                PostOptionRow(
                    icon = Icons.Default.ReportProblem,
                    iconTint = Color(0xFFFF9800),
                    title = "Report Post",
                    onClick = onDismiss
                )

                PostOptionRow(
                    icon = Icons.Default.ContentCopy,
                    iconTint = FlikataTextSecondary,
                    title = "Copy Link",
                    onClick = onDismiss
                )

                PostOptionRow(
                    icon = Icons.Default.Share,
                    iconTint = FlikataTextSecondary,
                    title = "Share to...",
                    onClick = onDismiss
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PostOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
        )
    }
}
