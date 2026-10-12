package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Comment
import com.example.data.model.CommentsListResponse
import com.example.data.model.Post
import com.example.data.model.User
import com.example.data.remote.ApiClient
import com.example.ui.components.FlikataAvatar
import com.example.ui.theme.FlikataDivider
import com.example.ui.theme.FlikataPrimary
import com.example.ui.theme.FlikataSurface
import com.example.ui.theme.FlikataTextPrimary
import com.example.ui.theme.FlikataTextSecondary
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsBottomSheet(
    post: Post,
    currentUser: User?,
    sheetState: SheetState,
    onAddComment: (String, String) -> Unit = { _, _ -> },
    onDismiss: () -> Unit
) {
    var commentText by remember { mutableStateOf("") }
    var replyingToComment by remember { mutableStateOf<Comment?>(null) }

    val commentsList = remember {
        mutableStateListOf<Comment>().apply { addAll(post.commentsList) }
    }

    LaunchedEffect(post.id) {
        try {
            val resp = ApiClient.apiService.getPostComments(post.id)
            if (resp.isSuccessful && resp.body()?.success == true && resp.body()?.data != null) {
                val serverComments = resp.body()!!.data!!
                if (serverComments.isNotEmpty()) {
                    commentsList.clear()
                    commentsList.addAll(serverComments)
                }
            }
        } catch (_: Exception) {}
    }

    val sheetSurface = MaterialTheme.colorScheme.surface
    val sheetOnSurface = MaterialTheme.colorScheme.onSurface
    val sheetVariant = MaterialTheme.colorScheme.surfaceVariant
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = sheetSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .width(42.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.72f)
        ) {
            // Header with "Comments" title and Close "X" button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Comments (${commentsList.sumOf { 1 + it.replies.size }})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = sheetOnSurface
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = sheetOnSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), thickness = 0.5.dp)

            // Comments List or Empty State
            if (commentsList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No comments yet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = sheetOnSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Be the first to comment.",
                            fontSize = 13.sp,
                            color = textSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(commentsList) { comment ->
                        CommentItemRow(
                            comment = comment,
                            onLikeClick = {
                                val idx = commentsList.indexOf(comment)
                                if (idx != -1) {
                                    val updated = comment.copy(
                                        isLiked = !comment.isLiked,
                                        likesCount = if (comment.isLiked) comment.likesCount - 1 else comment.likesCount + 1
                                    )
                                    commentsList[idx] = updated
                                }
                            },
                            onReplyClick = { replyingToComment = comment }
                        )
                    }
                }
            }

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), thickness = 0.5.dp)

            // Reply banner if active
            if (replyingToComment != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(sheetVariant)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Replying to ${replyingToComment!!.authorName}",
                        fontSize = 12.sp,
                        color = FlikataPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Cancel",
                        fontSize = 12.sp,
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { replyingToComment = null }
                    )
                }
            }

            // Write Comment Input Bar (Sticky at Bottom)
            Surface(
                color = sheetSurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .navigationBarsPadding()
                        .imePadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FlikataAvatar(
                        name = currentUser?.fullName ?: "User",
                        avatarUrl = currentUser?.profilePic,
                        size = 38.dp
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    // Light Gray Pill Input Box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(sheetVariant.copy(alpha = 0.65f))
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        androidx.compose.foundation.text.BasicTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = sheetOnSurface,
                                fontSize = 14.sp
                            ),
                            singleLine = true,
                            decorationBox = { innerTextField ->
                                if (commentText.isEmpty()) {
                                    Text(
                                        text = "Write a comment...",
                                        color = textSecondary.copy(alpha = 0.7f),
                                        fontSize = 14.sp
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Blue Circular Send Button
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(FlikataPrimary)
                            .clickable {
                                if (commentText.isNotBlank()) {
                                    val entered = commentText.trim()
                                    onAddComment(post.id, entered)
                                    val newC = Comment(
                                        id = UUID.randomUUID().toString(),
                                        postId = post.id,
                                        authorName = currentUser?.fullName ?: "User",
                                        authorAvatar = currentUser?.profilePic ?: "",
                                        text = entered,
                                        timestamp = "Just now"
                                    )

                                    if (replyingToComment != null) {
                                        val parentIdx = commentsList.indexOf(replyingToComment)
                                        if (parentIdx != -1) {
                                            val parent = commentsList[parentIdx]
                                            val updatedReplies = parent.replies + newC
                                            commentsList[parentIdx] = parent.copy(replies = updatedReplies)
                                        }
                                        replyingToComment = null
                                    } else {
                                        commentsList.add(0, newC)
                                    }
                                    commentText = ""
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentItemRow(
    comment: Comment,
    onLikeClick: () -> Unit,
    onReplyClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            FlikataAvatar(
                name = comment.authorName,
                avatarUrl = comment.authorAvatar,
                size = 36.dp
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Text(
                                text = comment.authorName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (comment.isVerified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                VerifiedBadge(size = 12.dp)
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = comment.text,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Row(
                    modifier = Modifier.padding(start = 12.dp, top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = comment.timestamp,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = if (comment.isLiked) "Liked (${comment.likesCount})" else "Like",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (comment.isLiked) FlikataPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable { onLikeClick() }
                    )

                    Text(
                        text = "Reply",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable { onReplyClick() }
                    )
                }
            }
        }

        // Render replies if present
        if (comment.replies.isNotEmpty()) {
            Column(
                modifier = Modifier.padding(start = 42.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                comment.replies.forEach { reply ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        AsyncImage(
                            model = reply.authorAvatar,
                            contentDescription = reply.authorName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                Text(
                                    text = reply.authorName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = reply.text,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
