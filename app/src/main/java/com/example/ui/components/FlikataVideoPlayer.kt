package com.example.ui.components

import android.net.Uri
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.focus.focusProperties
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun FlikataVideoPlayer(
    videoUri: String,
    modifier: Modifier = Modifier,
    autoPlay: Boolean = true,
    repeat: Boolean = true,
    isMutedDefault: Boolean = false,
    showControls: Boolean = true,
    showRedScrubber: Boolean = true,
    scrubberBottomPadding: Dp = 0.dp,
    resizeMode: Int = AspectRatioFrameLayout.RESIZE_MODE_FIT,
    onSingleTap: (() -> Unit)? = null,
    onDoubleTapWithOffset: ((Offset) -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isPlaying by remember { mutableStateOf(autoPlay) }
    var isMuted by remember { mutableStateOf(isMutedDefault) }
    var showOverlayControls by remember { mutableStateOf(false) }
    var currentProgress by remember { mutableFloatStateOf(0f) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }

    // Flash Play/Pause icon badge on tap
    var playPauseBadge by remember { mutableStateOf<Boolean?>(null) } // true=play, false=pause

    val exoPlayer = remember(videoUri) {
        ExoPlayer.Builder(context).build().apply {
            try {
                val uri = Uri.parse(videoUri)
                val mediaItem = MediaItem.fromUri(uri)
                setMediaItem(mediaItem)
                repeatMode = if (repeat) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
                volume = if (isMutedDefault) 0f else 1f
                playWhenReady = autoPlay
                prepare()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // React to autoPlay changes (e.g. when scrolling in VerticalPager!)
    LaunchedEffect(autoPlay) {
        if (autoPlay) {
            exoPlayer.playWhenReady = true
            exoPlayer.play()
            isPlaying = true
        } else {
            exoPlayer.playWhenReady = false
            exoPlayer.pause()
            isPlaying = false
        }
    }

    // React to mute toggle
    LaunchedEffect(isMutedDefault) {
        isMuted = isMutedDefault
        exoPlayer.volume = if (isMutedDefault) 0f else 1f
    }

    // Sync isPlaying state with ExoPlayer listener
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    val d = exoPlayer.duration
                    if (d > 0L) {
                        durationMs = d
                    }
                }
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Observe app lifecycle to pause player when paused/stopped
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    exoPlayer.playWhenReady = false
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (autoPlay) {
                        exoPlayer.playWhenReady = true
                    }
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Continuous progress and duration tracking loop
    LaunchedEffect(exoPlayer) {
        while (true) {
            val dur = exoPlayer.duration
            val pos = exoPlayer.currentPosition
            currentPositionMs = pos.coerceAtLeast(0L)
            if (dur > 0L) {
                durationMs = dur
                currentProgress = (pos.toFloat() / dur.toFloat()).coerceIn(0f, 1f)
            }
            delay(100)
        }
    }

    // Auto-hide play/pause indicator badge
    LaunchedEffect(playPauseBadge) {
        if (playPauseBadge != null) {
            delay(750)
            playPauseBadge = null
        }
    }

    // Auto-hide overlay controls
    LaunchedEffect(showOverlayControls) {
        if (showOverlayControls && isPlaying) {
            delay(3000)
            showOverlayControls = false
        }
    }

    Box(
        modifier = modifier
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { offset ->
                        onDoubleTapWithOffset?.invoke(offset)
                    },
                    onTap = {
                        if (onSingleTap != null) {
                            onSingleTap.invoke()
                        } else {
                            if (exoPlayer.isPlaying) {
                                exoPlayer.pause()
                                isPlaying = false
                                playPauseBadge = false
                            } else {
                                exoPlayer.play()
                                isPlaying = true
                                playPauseBadge = true
                            }
                        }
                    }
                )
            }
    ) {
        // ExoPlayer View (Explicitly unfocusable to prevent LazyColumn jumping to top on recomposition)
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    this.resizeMode = resizeMode
                    isFocusable = false
                    isFocusableInTouchMode = false
                    descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { playerView ->
                playerView.player = exoPlayer
                playerView.resizeMode = resizeMode
                playerView.isFocusable = false
                playerView.isFocusableInTouchMode = false
                playerView.descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
            },
            modifier = Modifier
                .fillMaxSize()
                .focusProperties { canFocus = false }
        )

        // Center Play / Pause Indicator Badge that flashes on tap
        if (playPauseBadge != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f))
                    .zIndex(20f),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (playPauseBadge == true) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }
        }

        // Standard Overlay Controls (if enabled)
        if (showControls) {
            AnimatedVisibility(
                visible = showOverlayControls || !isPlaying,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f))
                ) {
                    // Center Big Play/Pause Button
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.65f))
                            .pointerInput(Unit) {
                                detectTapGestures {
                                    if (exoPlayer.isPlaying) {
                                        exoPlayer.pause()
                                        isPlaying = false
                                    } else {
                                        exoPlayer.play()
                                        isPlaying = true
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Top Bar (Mute Toggle Button)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        IconButton(
                            onClick = {
                                isMuted = !isMuted
                                exoPlayer.volume = if (isMuted) 0f else 1f
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                                contentDescription = if (isMuted) "Unmute" else "Mute",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Bottom Bar (Time display)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = formatTime(currentPositionMs),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Text(
                                text = formatTime(durationMs),
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // BRIGHT RED SEEKBAR / SCRUBBER PIPE (Like Facebook / TikTok / YouTube Shorts)
        // Always rendered and visible at the bottom of the video, easily draggable!
        if (showRedScrubber) {
            var isScrubbing by remember { mutableStateOf(false) }
            var scrubProgress by remember { mutableFloatStateOf(0f) }
            val activeProgress = if (isScrubbing) scrubProgress else currentProgress

            val screenWidth = LocalConfiguration.current.screenWidthDp.dp

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = scrubberBottomPadding)
                    .height(36.dp)
                    .zIndex(50f)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = { offset ->
                                isScrubbing = true
                                val ratio = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                                scrubProgress = ratio
                                val totalDur = if (exoPlayer.duration > 0) exoPlayer.duration else durationMs
                                if (totalDur > 0) {
                                    val seekTarget = (ratio * totalDur).toLong()
                                    exoPlayer.seekTo(seekTarget)
                                }
                                tryAwaitRelease()
                                isScrubbing = false
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                isScrubbing = true
                                val ratio = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                                scrubProgress = ratio
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val ratio = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                                scrubProgress = ratio
                                val totalDur = if (exoPlayer.duration > 0) exoPlayer.duration else durationMs
                                if (totalDur > 0) {
                                    val seekTarget = (ratio * totalDur).toLong()
                                    exoPlayer.seekTo(seekTarget)
                                }
                            },
                            onDragEnd = { isScrubbing = false },
                            onDragCancel = { isScrubbing = false }
                        )
                    },
                contentAlignment = Alignment.BottomStart
            ) {
                // Background Track: Gray/Translucent Pipe
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .height(if (isScrubbing) 6.dp else 4.dp)
                        .background(Color.White.copy(alpha = 0.35f))
                )

                // Played Progress Track: SOLID RADIANT RED PIPE
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth(activeProgress)
                        .height(if (isScrubbing) 6.dp else 4.dp)
                        .background(Color(0xFFFF0000))
                )

                // Red Dragging Knob / Thumb (Always visible so the user can easily see and grab it!)
                val thumbOffset = ((screenWidth * activeProgress) - 7.dp).coerceAtLeast(0.dp)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = thumbOffset, y = 4.dp)
                        .size(if (isScrubbing) 16.dp else 12.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF0000))
                        .border(1.5.dp, Color.White, CircleShape)
                )
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    if (ms <= 0L) return "00:00"
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
