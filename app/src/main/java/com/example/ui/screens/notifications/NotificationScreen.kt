package com.example.ui.screens.notifications

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.FlikataNotification
import com.example.ui.components.SoftUITheme
import com.example.ui.viewmodel.MainViewModel

@Composable
fun NotificationScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    BackHandler {
        onBack()
    }

    val isDark = SoftUITheme.isDark
    val notifications by viewModel.notifications.collectAsState()
    val unreadCount by viewModel.unreadNotificationsCount.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") } // "All", "Unread", "Posts"

    val filteredList = remember(notifications, selectedFilter) {
        when (selectedFilter) {
            "Unread" -> notifications.filter { !it.isRead }
            "Posts" -> notifications.filter { it.type == "post" }
            else -> notifications
        }
    }

    val cardBorderBrush = if (isDark) {
        Brush.verticalGradient(listOf(Color(0x33FFFFFF), Color(0x11FFFFFF)))
    } else {
        Brush.verticalGradient(listOf(Color(0xEEFFFFFF), Color(0x33A0C0E8)))
    }

    val headerBg = if (isDark) {
        Brush.verticalGradient(listOf(Color(0xFF242730), Color(0xFF1E2128), Color(0xFF16181E)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFF4F8FD), Color(0xFFE9F2FC)))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        SoftUITheme.BackgroundGradientTop,
                        SoftUITheme.BackgroundGradientBottom
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // 3D Neumorphic Top Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = if (isDark) 3.dp else 2.dp,
                    shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                    ambientColor = SoftUITheme.ShadowDark,
                    spotColor = SoftUITheme.ShadowDark
                )
                .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                .background(headerBg)
                .border(
                    width = 1.dp,
                    brush = cardBorderBrush,
                    shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
                )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // 3D Circular Back Button
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .shadow(2.dp, CircleShape, ambientColor = SoftUITheme.ShadowDark)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF1E2128) else Color.White)
                                .border(1.dp, if (isDark) Color(0x33FFFFFF) else Color(0xEEFFFFFF), CircleShape)
                                .clickable { onBack() }
                                .testTag("notifications_back_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = SoftUITheme.TextDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "Notifications",
                            style = TextStyle(
                                brush = Brush.horizontalGradient(
                                    listOf(Color(0xFF0075FF), Color(0xFF0052D4))
                                ),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.2.sp
                            )
                        )

                        if (unreadCount > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .shadow(2.dp, CircleShape, ambientColor = Color(0x400075FF))
                                    .clip(CircleShape)
                                    .background(Color(0xFF0075FF))
                                    .padding(horizontal = 7.dp, vertical = 2.5.dp)
                            ) {
                                Text(
                                    text = unreadCount.toString(),
                                    color = Color.White,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (unreadCount > 0) {
                            // 3D "Read all" Pill Button
                            Box(
                                modifier = Modifier
                                    .shadow(2.dp, RoundedCornerShape(14.dp), ambientColor = Color(0x300075FF))
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isDark) Color(0xFF1E2D3E) else Color(0xFFE8F1FC))
                                    .border(1.dp, Color(0x330075FF), RoundedCornerShape(14.dp))
                                    .clickable { viewModel.markAllNotificationsAsRead() }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("mark_all_read_btn")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.DoneAll,
                                        contentDescription = null,
                                        tint = Color(0xFF0075FF),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Read all",
                                        color = Color(0xFF0075FF),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        if (notifications.isNotEmpty()) {
                            // 3D Clear All Button
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .shadow(2.dp, CircleShape, ambientColor = SoftUITheme.ShadowDark)
                                    .clip(CircleShape)
                                    .background(if (isDark) Color(0xFF1E2128) else Color.White)
                                    .border(1.dp, if (isDark) Color(0x33FFFFFF) else Color(0xEEFFFFFF), CircleShape)
                                    .clickable { viewModel.clearAllNotifications() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ClearAll,
                                    contentDescription = "Clear all",
                                    tint = SoftUITheme.TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // 3D Filter Pills Row
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("All", "Unread", "Posts").forEach { filter ->
                        val isSelected = selectedFilter == filter
                        item {
                            val count = when (filter) {
                                "Unread" -> unreadCount
                                "Posts" -> notifications.count { it.type == "post" }
                                else -> notifications.size
                            }
                            Box(
                                modifier = Modifier
                                    .shadow(
                                        elevation = if (isSelected) 3.dp else 2.dp,
                                        shape = RoundedCornerShape(20.dp),
                                        ambientColor = if (isSelected) Color(0x400075FF) else SoftUITheme.ShadowDark
                                    )
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isSelected) {
                                            Brush.horizontalGradient(listOf(Color(0xFF0075FF), Color(0xFF0052D4)))
                                        } else {
                                            if (isDark) Brush.verticalGradient(listOf(Color(0xFF242730), Color(0xFF1E2128)))
                                            else Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFF3F7FD)))
                                        }
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) Brush.verticalGradient(listOf(Color(0x66FFFFFF), Color(0x22FFFFFF)))
                                        else if (isDark) Brush.verticalGradient(listOf(Color(0x33FFFFFF), Color(0x11FFFFFF)))
                                        else Brush.verticalGradient(listOf(Color(0xEEFFFFFF), Color(0x44A0C0E8))),
                                        RoundedCornerShape(20.dp)
                                    )
                                    .clickable { selectedFilter = filter }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = filter,
                                        color = if (isSelected) Color.White else SoftUITheme.TextDark,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                                    )
                                    if (count > 0) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(if (isSelected) Color(0x33FFFFFF) else Color(0x1F0075FF))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = count.toString(),
                                                color = if (isSelected) Color.White else Color(0xFF0075FF),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Notifications List
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(20.dp), ambientColor = SoftUITheme.ShadowDark)
                        .clip(RoundedCornerShape(20.dp))
                        .background(SoftUITheme.SurfaceCard)
                        .border(1.dp, cardBorderBrush, RoundedCornerShape(20.dp))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF1E2128) else Color(0xFFF3F7FD))
                                .border(1.dp, if (isDark) Color(0x22FFFFFF) else Color(0x33A0C0E8), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsNone,
                                contentDescription = null,
                                tint = SoftUITheme.IconTint,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (selectedFilter == "Unread") "No unread notifications" else "No notifications yet",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftUITheme.TextDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "You're all caught up! Updates about likes, comments and friends will appear here.",
                            fontSize = 13.5.sp,
                            color = SoftUITheme.TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList, key = { it.id }) { notif ->
                    NeumorphicNotificationCard(
                        notification = notif,
                        onItemClick = { viewModel.markNotificationAsRead(notif.id) },
                        onDeleteClick = { viewModel.deleteNotification(notif.id) }
                    )
                }
            }
        }
    }
}

/**
 * 3D Neumorphic Notification Card
 */
@Composable
private fun NeumorphicNotificationCard(
    notification: FlikataNotification,
    onItemClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isDark = SoftUITheme.isDark
    val cardBg = if (!notification.isRead) {
        if (isDark) Color(0xFF1E2838) else Color(0xFFF0F6FF)
    } else {
        SoftUITheme.SurfaceCard
    }

    val cardBorder = if (!notification.isRead) {
        Brush.verticalGradient(listOf(Color(0x880075FF), Color(0x330075FF)))
    } else {
        if (isDark) Brush.verticalGradient(listOf(Color(0x33FFFFFF), Color(0x11FFFFFF)))
        else Brush.verticalGradient(listOf(Color(0xEEFFFFFF), Color(0x33A0C0E8)))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (!notification.isRead) 3.dp else 2.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = if (!notification.isRead) Color(0x300075FF) else SoftUITheme.ShadowDark,
                spotColor = if (!notification.isRead) Color(0x400075FF) else SoftUITheme.ShadowDark
            )
            .clip(RoundedCornerShape(18.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
            .clickable { onItemClick() }
            .padding(14.dp)
            .testTag("notification_item_${notification.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 3D Avatar / Icon with action badge
            Box(
                modifier = Modifier.size(54.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                if (notification.avatarUrl != null) {
                    AsyncImage(
                        model = notification.avatarUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(50.dp)
                            .shadow(2.dp, CircleShape)
                            .clip(CircleShape)
                            .border(1.5.dp, Color.White, CircleShape)
                            .align(Alignment.Center)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .shadow(2.dp, CircleShape, ambientColor = SoftUITheme.ShadowDark)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    when (notification.type) {
                                        "post" -> listOf(Color(0xFFE8F7EC), Color(0xFFD0F0D8))
                                        "like" -> listOf(Color(0xFFFFEEF0), Color(0xFFFFD5DA))
                                        "friend" -> listOf(Color(0xFFE8F1FC), Color(0xFFD4E5FA))
                                        else -> listOf(Color(0xFFFFF8E6), Color(0xFFFFECC4))
                                    }
                                )
                            )
                            .border(1.5.dp, Color.White, CircleShape)
                            .align(Alignment.Center),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (notification.type) {
                                "post" -> Icons.Default.PostAdd
                                "like" -> Icons.Default.Favorite
                                "friend" -> Icons.Default.PersonAdd
                                else -> Icons.Default.Notifications
                            },
                            contentDescription = null,
                            tint = when (notification.type) {
                                "post" -> Color(0xFF45BD62)
                                "like" -> Color(0xFFFA3E3E)
                                "friend" -> Color(0xFF0075FF)
                                else -> Color(0xFFF7B125)
                            },
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Small badge icon in corner
                val badgeBg = when (notification.type) {
                    "post" -> Color(0xFF45BD62)
                    "like" -> Color(0xFFFA3E3E)
                    "friend" -> Color(0xFF0075FF)
                    else -> Color(0xFFF7B125)
                }
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .shadow(1.dp, CircleShape)
                        .clip(CircleShape)
                        .background(badgeBg)
                        .border(1.5.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (notification.type) {
                            "post" -> Icons.Default.Check
                            "like" -> Icons.Default.Favorite
                            "friend" -> Icons.Default.PersonAdd
                            else -> Icons.Default.Notifications
                        },
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(13.dp))

            // Text Content
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = notification.title,
                        fontSize = 14.5.sp,
                        fontWeight = if (!notification.isRead) FontWeight.ExtraBold else FontWeight.Bold,
                        color = SoftUITheme.TextDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (!notification.isRead) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0075FF))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = notification.message,
                    fontSize = 13.sp,
                    color = SoftUITheme.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = notification.timestamp,
                    fontSize = 11.5.sp,
                    color = SoftUITheme.TextMuted,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Post Thumbnail if attached
            if (notification.postThumbnail != null) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .shadow(2.dp, RoundedCornerShape(10.dp))
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(10.dp))
                ) {
                    AsyncImage(
                        model = notification.postThumbnail,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
            }

            // 3D Delete Icon Button
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0xFF1E2128) else Color(0xFFF3F7FD))
                    .clickable { onDeleteClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete",
                    tint = SoftUITheme.TextMuted,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
