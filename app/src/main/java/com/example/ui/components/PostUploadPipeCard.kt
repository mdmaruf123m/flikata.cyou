package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.PostUploadState
import com.example.ui.theme.FlikataPrimary

@Composable
fun PostUploadPipeCard(
    uploadState: PostUploadState,
    userAvatar: String?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = uploadState.isUploading,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        val animatedProgress by animateFloatAsState(
            targetValue = uploadState.progress.coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
            label = "pipe_progress"
        )
        val percent = (animatedProgress * 100).toInt().coerceIn(0, 100)
        val isStory = uploadState.postSnippet.contains("Story", ignoreCase = true)
        val statusTitle = if (uploadState.isCompleted) {
            if (isStory) "✓ Story Published!" else "✓ Post Published!"
        } else {
            if (isStory) "Sharing story..." else "Posting update..."
        }

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .testTag("post_upload_pipe_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Header Row with Avatar, Status & Percentage
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(38.dp)) {
                        FlikataAvatar(
                            name = "User",
                            avatarUrl = userAvatar,
                            size = 36.dp
                        )
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .align(Alignment.BottomEnd)
                                .clip(CircleShape)
                                .background(if (uploadState.isCompleted) Color(0xFF45BD62) else FlikataPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (uploadState.isCompleted) Icons.Default.CheckCircle else Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = statusTitle,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (uploadState.isCompleted) Color(0xFF45BD62) else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$percent%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (uploadState.isCompleted) Color(0xFF45BD62) else FlikataPrimary
                            )
                        }

                        if (uploadState.postSnippet.isNotEmpty()) {
                            Text(
                                text = uploadState.postSnippet,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (uploadState.postImage != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        AsyncImage(
                            model = uploadState.postImage,
                            contentDescription = "Thumbnail",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(6.dp))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // The Pipe / Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(
                                if (uploadState.isCompleted) {
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF45BD62), Color(0xFF2ECC71))
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF1877F2), Color(0xFF00C6FF))
                                    )
                                }
                            )
                    )
                }
            }
        }
    }
}
