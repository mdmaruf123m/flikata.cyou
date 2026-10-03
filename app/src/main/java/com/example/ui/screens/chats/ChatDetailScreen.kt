package com.example.ui.screens.chats

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.ui.components.FlikataAvatar
import com.example.ui.components.FlikataVideoPlayer
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.FlikataError
import com.example.ui.theme.FlikataPrimary
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class QuotedMessage(
    val id: String,
    val senderName: String,
    val text: String,
    val mediaType: String? = null
)

data class DetailMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",
    val imageUrl: String? = null,
    val videoUrl: String? = null,
    val isMe: Boolean = true,
    val time: String = "Just now",
    val replyTo: QuotedMessage? = null,
    val isEdited: Boolean = false,
    val isCallNotice: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val chat by viewModel.selectedChat.collectAsState()
    val partnerName = chat?.senderName ?: "Chat Partner"
    val partnerAvatar = chat?.senderAvatar ?: ""
    val isVerified = chat?.isVerified ?: false

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    // Replying and Editing state
    var replyingTo by remember { mutableStateOf<DetailMessage?>(null) }
    var editingMessage by remember { mutableStateOf<DetailMessage?>(null) }

    // Message action bottom sheet state
    var selectedMessageForAction by remember { mutableStateOf<DetailMessage?>(null) }
    val actionSheetState = rememberModalBottomSheetState()

    // Audio call state
    var isAudioCallActive by remember { mutableStateOf(false) }

    // Video player dialog state
    var playingVideoUrl by remember { mutableStateOf<String?>(null) }

    // Initial messages with rich content
    val messages = remember(chat?.id) {
        mutableStateListOf(
            DetailMessage(
                id = "m1",
                text = "আসসালামু আলাইকুম! কেমন আছেন?",
                isMe = false,
                time = "10:15 AM"
            ),
            DetailMessage(
                id = "m2",
                text = "ওয়ালাইকুম আসসালাম! আলহামদুলিল্লাহ ভালো। আপনার খবর কী?",
                isMe = true,
                time = "10:16 AM"
            ),
            DetailMessage(
                id = "m3",
                text = "Flikata অ্যাপের নতুন ফিচারগুলো খুব সুন্দর হয়েছে!",
                isMe = false,
                time = "10:18 AM"
            )
        )
    }

    // Media Pickers
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val currentTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
            messages.add(
                DetailMessage(
                    id = UUID.randomUUID().toString(),
                    text = inputText.trim(),
                    imageUrl = uri.toString(),
                    isMe = true,
                    time = currentTime,
                    replyTo = replyingTo?.let {
                        QuotedMessage(
                            id = it.id,
                            senderName = if (it.isMe) "You" else partnerName,
                            text = if (it.text.isNotBlank()) it.text else if (it.imageUrl != null) "Photo" else "Video",
                            mediaType = if (it.imageUrl != null) "image" else if (it.videoUrl != null) "video" else null
                        )
                    }
                )
            )
            inputText = ""
            replyingTo = null
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val currentTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
            messages.add(
                DetailMessage(
                    id = UUID.randomUUID().toString(),
                    text = inputText.trim(),
                    videoUrl = uri.toString(),
                    isMe = true,
                    time = currentTime,
                    replyTo = replyingTo?.let {
                        QuotedMessage(
                            id = it.id,
                            senderName = if (it.isMe) "You" else partnerName,
                            text = if (it.text.isNotBlank()) it.text else if (it.imageUrl != null) "Photo" else "Video",
                            mediaType = if (it.imageUrl != null) "image" else if (it.videoUrl != null) "video" else null
                        )
                    }
                )
            )
            inputText = ""
            replyingTo = null
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    val screenBg = MaterialTheme.colorScheme.background
    val screenSurface = MaterialTheme.colorScheme.surface
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant

    Scaffold(
        topBar = {
            Surface(
                color = screenSurface,
                tonalElevation = 2.dp,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 6.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left Partner Profile info
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = textPrimary
                            )
                        }

                        Box {
                            FlikataAvatar(
                                name = partnerName,
                                avatarUrl = partnerAvatar,
                                size = 40.dp
                            )
                            Box(
                                modifier = Modifier
                                    .size(11.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF45BD62))
                                    .border(1.5.dp, screenSurface, CircleShape)
                                    .align(Alignment.BottomEnd)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = partnerName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (isVerified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    VerifiedBadge(size = 14.dp)
                                }
                            }
                            Text(
                                text = "Active now",
                                fontSize = 11.sp,
                                color = Color(0xFF45BD62),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Audio Call button ONLY (NO Video Call button as explicitly requested)
                    IconButton(
                        onClick = { isAudioCallActive = true },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE7F3FF))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Audio Call",
                            tint = FlikataPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        },
        containerColor = screenBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
        ) {
            // Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    ChatBubbleItem(
                        msg = msg,
                        partnerName = partnerName,
                        onLongClick = {
                            selectedMessageForAction = msg
                        },
                        onImageClick = { url ->
                            viewModel.openImageViewer(url)
                        },
                        onVideoClick = { url ->
                            playingVideoUrl = url
                        }
                    )
                }
            }

            // Replying To Banner
            AnimatedVisibility(
                visible = replyingTo != null,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                if (replyingTo != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(32.dp)
                                        .background(FlikataPrimary, RoundedCornerShape(2.dp))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Replying to ${if (replyingTo!!.isMe) "yourself" else partnerName}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FlikataPrimary
                                    )
                                    Text(
                                        text = if (replyingTo!!.text.isNotBlank()) replyingTo!!.text else if (replyingTo!!.imageUrl != null) "Photo" else "Video",
                                        fontSize = 12.sp,
                                        color = textSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            IconButton(
                                onClick = { replyingTo = null },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancel Reply",
                                    tint = textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Editing Banner
            AnimatedVisibility(
                visible = editingMessage != null,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                if (editingMessage != null) {
                    Surface(
                        color = Color(0xFFFFF3CD),
                        tonalElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = Color(0xFF856404),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Editing message",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF856404)
                                    )
                                    Text(
                                        text = editingMessage!!.text,
                                        fontSize = 12.sp,
                                        color = Color(0xFF856404).copy(alpha = 0.8f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    editingMessage = null
                                    inputText = ""
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancel Edit",
                                    tint = Color(0xFF856404),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Input Bar
            Surface(
                color = screenSurface,
                tonalElevation = 4.dp,
                shadowElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Photo Attachment Button
                    IconButton(
                        onClick = {
                            imagePickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Send Image",
                            tint = FlikataPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Video Attachment Button
                    IconButton(
                        onClick = {
                            videoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                            )
                        },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Send Video",
                            tint = Color(0xFF45BD62),
                            modifier = Modifier.size(23.dp)
                        )
                    }

                    // Text Input Pill Box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(21.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("chat_input_field"),
                            textStyle = TextStyle(
                                color = textPrimary,
                                fontSize = 14.sp
                            ),
                            maxLines = 4,
                            decorationBox = { innerTextField ->
                                if (inputText.isEmpty()) {
                                    Text(
                                        text = if (editingMessage != null) "Edit message..." else "Type a message...",
                                        color = textSecondary.copy(alpha = 0.7f),
                                        fontSize = 14.sp
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Send or Edit Save Button
                    if (editingMessage != null) {
                        IconButton(
                            onClick = {
                                if (inputText.isNotBlank()) {
                                    val idx = messages.indexOfFirst { it.id == editingMessage!!.id }
                                    if (idx != -1) {
                                        messages[idx] = messages[idx].copy(
                                            text = inputText.trim(),
                                            isEdited = true
                                        )
                                    }
                                    editingMessage = null
                                    inputText = ""
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF45BD62))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Save Edit",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else if (inputText.isNotBlank()) {
                        IconButton(
                            onClick = {
                                val currentTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
                                messages.add(
                                    DetailMessage(
                                        id = UUID.randomUUID().toString(),
                                        text = inputText.trim(),
                                        isMe = true,
                                        time = currentTime,
                                        replyTo = replyingTo?.let {
                                            QuotedMessage(
                                                id = it.id,
                                                senderName = if (it.isMe) "You" else partnerName,
                                                text = if (it.text.isNotBlank()) it.text else if (it.imageUrl != null) "Photo" else "Video",
                                                mediaType = if (it.imageUrl != null) "image" else if (it.videoUrl != null) "video" else null
                                            )
                                        }
                                    )
                                )
                                inputText = ""
                                replyingTo = null
                                coroutineScope.launch {
                                    listState.animateScrollToItem(messages.size - 1)
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(FlikataPrimary)
                                .testTag("chat_send_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = Color.White,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    } else {
                        // Quick Like/Thumb button when text is empty (standard Messenger)
                        IconButton(
                            onClick = {
                                val currentTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
                                messages.add(
                                    DetailMessage(
                                        id = UUID.randomUUID().toString(),
                                        text = "👍",
                                        isMe = true,
                                        time = currentTime
                                    )
                                )
                                coroutineScope.launch {
                                    listState.animateScrollToItem(messages.size - 1)
                                }
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ThumbUp,
                                contentDescription = "Like",
                                tint = FlikataPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Message Action Bottom Sheet (Reply, Edit, Copy, Delete)
    if (selectedMessageForAction != null) {
        val targetMsg = selectedMessageForAction!!
        ModalBottomSheet(
            onDismissRequest = { selectedMessageForAction = null },
            sheetState = actionSheetState,
            containerColor = screenSurface,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .navigationBarsPadding()
            ) {
                Text(
                    text = "Message Actions",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Reply Action (Applicable to anyone's message)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            replyingTo = targetMsg
                            selectedMessageForAction = null
                        }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Reply,
                        contentDescription = "Reply",
                        tint = FlikataPrimary
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(text = "Reply", fontSize = 15.sp, color = textPrimary, fontWeight = FontWeight.Medium)
                }

                // Copy Action
                if (targetMsg.text.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                clipboardManager.setText(AnnotatedString(targetMsg.text))
                                selectedMessageForAction = null
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = textPrimary
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(text = "Copy Text", fontSize = 15.sp, color = textPrimary, fontWeight = FontWeight.Medium)
                    }
                }

                // Edit Action (Only for user's own sent text messages)
                if (targetMsg.isMe && targetMsg.text.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                editingMessage = targetMsg
                                inputText = targetMsg.text
                                selectedMessageForAction = null
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color(0xFFF2994A)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(text = "Edit Message", fontSize = 15.sp, color = textPrimary, fontWeight = FontWeight.Medium)
                    }
                }

                // Delete / Remove Action
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            messages.remove(targetMsg)
                            if (replyingTo?.id == targetMsg.id) replyingTo = null
                            if (editingMessage?.id == targetMsg.id) {
                                editingMessage = null
                                inputText = ""
                            }
                            selectedMessageForAction = null
                        }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = FlikataError
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(text = "Delete / Remove Message", fontSize = 15.sp, color = FlikataError, fontWeight = FontWeight.Medium)
                }
            }
        }
    }

    // Audio Call Simulation Dialog / Overlay
    if (isAudioCallActive) {
        AudioCallScreenOverlay(
            partnerName = partnerName,
            partnerAvatar = partnerAvatar,
            onEndCall = { durationStr ->
                isAudioCallActive = false
                val currentTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
                messages.add(
                    DetailMessage(
                        id = UUID.randomUUID().toString(),
                        text = "📞 Audio call ended • $durationStr",
                        isMe = true,
                        time = currentTime,
                        isCallNotice = true
                    )
                )
            }
        )
    }

    // Video Player Dialog
    if (playingVideoUrl != null) {
        Dialog(
            onDismissRequest = { playingVideoUrl = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                FlikataVideoPlayer(
                    videoUri = playingVideoUrl!!,
                    modifier = Modifier.fillMaxSize(),
                    autoPlay = true,
                    repeat = true
                )

                IconButton(
                    onClick = { playingVideoUrl = null },
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(16.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun ChatBubbleItem(
    msg: DetailMessage,
    partnerName: String,
    onLongClick: () -> Unit,
    onImageClick: (String) -> Unit,
    onVideoClick: (String) -> Unit
) {
    if (msg.isCallNotice) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = msg.text,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
        return
    }

    val bubbleColor = if (msg.isMe) FlikataPrimary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (msg.isMe) Color.White else MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (msg.isMe) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = bubbleColor,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (msg.isMe) 16.dp else 4.dp,
                bottomEnd = if (msg.isMe) 4.dp else 16.dp
            ),
            modifier = Modifier
                .widthIn(max = 290.dp)
                .combinedClickable(
                    onClick = { /* normal click */ },
                    onLongClick = onLongClick
                )
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                // Quoted / Replied message preview
                if (msg.replyTo != null) {
                    Surface(
                        color = if (msg.isMe) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(26.dp)
                                    .background(if (msg.isMe) Color.White else FlikataPrimary, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = msg.replyTo.senderName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (msg.isMe) Color.White else FlikataPrimary
                                )
                                Text(
                                    text = msg.replyTo.text,
                                    fontSize = 11.sp,
                                    color = if (msg.isMe) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Image content
                if (msg.imageUrl != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.2f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onImageClick(msg.imageUrl) }
                    ) {
                        AsyncImage(
                            model = msg.imageUrl,
                            contentDescription = "Image message",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    if (msg.text.isNotBlank()) Spacer(modifier = Modifier.height(6.dp))
                }

                // Video content
                if (msg.videoUrl != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.3f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black)
                            .clickable { onVideoClick(msg.videoUrl) },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play Video",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Surface(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(6.dp)
                        ) {
                            Text(
                                text = "Video",
                                color = Color.White,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (msg.text.isNotBlank()) Spacer(modifier = Modifier.height(6.dp))
                }

                // Text content
                if (msg.text.isNotBlank()) {
                    Text(
                        text = msg.text,
                        color = textColor,
                        fontSize = 15.sp,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }

                // Meta row: Time, Edited tag, checkmarks
                Row(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 2.dp, end = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (msg.isEdited) {
                        Text(
                            text = "(edited) ",
                            fontSize = 10.sp,
                            color = if (msg.isMe) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    Text(
                        text = msg.time,
                        fontSize = 10.sp,
                        color = if (msg.isMe) Color.White.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (msg.isMe) {
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Delivered",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AudioCallScreenOverlay(
    partnerName: String,
    partnerAvatar: String,
    onEndCall: (String) -> Unit
) {
    var callSeconds by remember { mutableIntStateOf(0) }
    var isMuted by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            callSeconds++
        }
    }

    val callDurationString = remember(callSeconds) {
        val mins = callSeconds / 60
        val secs = callSeconds % 60
        String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
    }

    // Gentle pulse animation for caller avatar
    val infiniteTransition = rememberInfiniteTransition(label = "callPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avatarPulse"
    )

    Dialog(
        onDismissRequest = { onEndCall(callDurationString) },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F2027),
                            Color(0xFF203A43),
                            Color(0xFF2C5364)
                        )
                    )
                )
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header Info
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        // Outer pulse ring
                        Box(
                            modifier = Modifier
                                .size(136.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.12f))
                        )
                        FlikataAvatar(
                            name = partnerName,
                            avatarUrl = partnerAvatar,
                            size = 110.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = partnerName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (callSeconds < 3) "Connecting..." else callDurationString,
                        fontSize = 15.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )
                }

                // Bottom Call Controls
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 30.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mute Mic Button
                        CallActionButton(
                            icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            label = if (isMuted) "Unmute" else "Mute",
                            isActive = isMuted,
                            onClick = { isMuted = !isMuted }
                        )

                        // End Call (Large Red button)
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(FlikataError)
                                .clickable { onEndCall(callDurationString) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CallEnd,
                                contentDescription = "End Call",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        // Speaker Button
                        CallActionButton(
                            icon = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            label = if (isSpeakerOn) "Speaker" else "Earpiece",
                            isActive = isSpeakerOn,
                            onClick = { isSpeakerOn = !isSpeakerOn }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CallActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(if (isActive) Color.White else Color.White.copy(alpha = 0.2f))
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) Color.Black else Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}
