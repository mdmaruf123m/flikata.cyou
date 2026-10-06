package com.example.ui.screens.story

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.data.model.Story
import com.example.ui.components.FacebookReactionPicker
import com.example.ui.components.FloatingEmojiAnimation
import com.example.ui.components.GraphicReactionIcon
import com.example.ui.components.REACTION_ICON_ITEMS
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.FlikataPrimary

@Composable
fun StoryViewerScreen(
    stories: List<Story>,
    initialIndex: Int,
    onClose: () -> Unit
) {
    val validStories = remember(stories) { stories.filter { it.id != "story_create" } }
    if (validStories.isEmpty()) {
        androidx.compose.runtime.LaunchedEffect(Unit) {
            onClose()
        }
        return
    }

    var currentIndex by remember { mutableIntStateOf(initialIndex.coerceIn(0, (validStories.size - 1).coerceAtLeast(0))) }

    val currentStory = validStories.getOrNull(currentIndex) ?: validStories.firstOrNull()

    val progress = remember { Animatable(0f) }
    var replyText by remember { mutableStateOf("") }
    var storyLiked by remember { mutableStateOf(false) }
    var userStoryReaction by remember { mutableStateOf<String?>(null) }
    var floatingReaction by remember { mutableStateOf<String?>(null) }
    var showReactionPicker by remember { mutableStateOf(false) }

    LaunchedEffect(currentIndex) {
        userStoryReaction = null
        storyLiked = false
        floatingReaction = null
        showReactionPicker = false
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 4500, easing = LinearEasing)
        )
        // Auto-advance to next story
        if (currentIndex < validStories.size - 1) {
            currentIndex++
        } else {
            onClose()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Story Background Image or Text Story Content
        if (currentStory != null) {
            if (!currentStory.textContent.isNullOrBlank()) {
                val bgBrush = when (currentStory.backgroundColor) {
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
                        .padding(horizontal = 24.dp)
                        .pointerInput(currentIndex) {
                            detectTapGestures(
                                onTap = { offset ->
                                    if (offset.x < size.width / 2) {
                                        if (currentIndex > 0) currentIndex-- else onClose()
                                    } else {
                                        if (currentIndex < validStories.size - 1) currentIndex++ else onClose()
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentStory.textContent,
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 36.sp
                    )
                }
            } else {
                AsyncImage(
                    model = currentStory.storyImage,
                    contentDescription = currentStory.authorName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(currentIndex) {
                            detectTapGestures(
                                onTap = { offset ->
                                    if (offset.x < size.width / 2) {
                                        // Previous story
                                        if (currentIndex > 0) currentIndex-- else onClose()
                                    } else {
                                        // Next story
                                        if (currentIndex < validStories.size - 1) currentIndex++ else onClose()
                                    }
                                }
                            )
                        }
                )
            }
        }

        // Overlay Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Spacer(modifier = Modifier.height(24.dp))

                // Top Segmented Progress Bars
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    validStories.forEachIndexed { idx, _ ->
                        val segmentProgress = when {
                            idx < currentIndex -> 1f
                            idx == currentIndex -> progress.value
                            else -> 0f
                        }
                        LinearProgressIndicator(
                            progress = { segmentProgress },
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.35f),
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                        )
                    }
                }

                // Header User Info Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = currentStory?.authorAvatar ?: "",
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currentStory?.authorName ?: "Story",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                VerifiedBadge(size = 14.dp)
                            }
                            Text(
                                text = "3h ago",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }
                    }

                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }
            }

            // Bottom Reactions Bar
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Quick Graphic Reactions Row (Like, Love, Care, Haha, Wow, Sad, Angry)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    REACTION_ICON_ITEMS.forEach { item ->
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    userStoryReaction = item.name
                                    floatingReaction = item.name
                                    storyLiked = true
                                }
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            GraphicReactionIcon(
                                type = item.name,
                                size = 34.dp
                            )
                        }
                    }
                }

                // Send Message Input Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        placeholder = { Text("Send message...", color = Color.White.copy(0.7f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(0.6f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (userStoryReaction != null) Color.Transparent else Color.White.copy(alpha = 0.2f))
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = {
                                        if (userStoryReaction != null) {
                                            userStoryReaction = null
                                            storyLiked = false
                                        } else {
                                            userStoryReaction = "Love"
                                            floatingReaction = "Love"
                                            storyLiked = true
                                        }
                                    },
                                    onLongPress = {
                                        showReactionPicker = true
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (userStoryReaction != null) {
                            GraphicReactionIcon(type = userStoryReaction!!, size = 44.dp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Like Story",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Floating animated emoji particle effect when user reacts
        if (floatingReaction != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 120.dp, end = 30.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                FloatingEmojiAnimation(
                    reactionType = floatingReaction!!,
                    onAnimationEnd = { floatingReaction = null }
                )
            }
        }

        // Facebook Reaction Picker Floating Popup on Long Press
        if (showReactionPicker) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(10f)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { showReactionPicker = false })
                    }
            ) {
                FacebookReactionPicker(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 75.dp, end = 12.dp),
                    onReactionSelected = { reactionName ->
                        userStoryReaction = reactionName
                        floatingReaction = reactionName
                        storyLiked = true
                        showReactionPicker = false
                    }
                )
            }
        }
    }
}
