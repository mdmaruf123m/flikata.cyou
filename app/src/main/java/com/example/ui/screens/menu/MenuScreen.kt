package com.example.ui.screens.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.ApiClient
import com.example.ui.components.FlikataAvatar
import com.example.ui.components.SoftUITheme
import com.example.ui.theme.FlikataError
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen

/**
 * 3D Soft Neumorphic Menu Screen with tactile clay cards, profile banner, and shortcuts
 */
@Composable
fun MenuScreen(
    viewModel: MainViewModel,
    onProfileClick: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    val screenBgBrush = if (isDarkMode) {
        Brush.verticalGradient(listOf(Color(0xFF121418), Color(0xFF181A20)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFF4F8FD), Color(0xFFE8F1FC)))
    }
    val cardBg = if (isDarkMode) Color(0xFF242730) else Color.White
    val cardBorderBrush = if (isDarkMode) {
        Brush.verticalGradient(listOf(Color(0x33FFFFFF), Color(0x11FFFFFF)))
    } else {
        Brush.verticalGradient(listOf(Color(0xEEFFFFFF), Color(0x33A0C0E8)))
    }
    val headerBgBrush = if (isDarkMode) {
        Brush.verticalGradient(listOf(Color(0xFF242730), Color(0xFF1E2128), Color(0xFF16181E)))
    } else {
        Brush.verticalGradient(listOf(Color.White, Color(0xFFF3F7FD), Color(0xFFE8F1FC)))
    }
    val circleButtonBg = if (isDarkMode) {
        Brush.linearGradient(listOf(Color(0xFF2C303B), Color(0xFF20232B)))
    } else {
        Brush.linearGradient(listOf(Color.White, Color(0xFFE8F1FC)))
    }
    val circleButtonBorder = if (isDarkMode) Color(0x33FFFFFF) else Color(0xEEFFFFFF)
    val buttonIconTint = if (isDarkMode) Color(0xFFE4E6EB) else Color(0xFF1E3A5F)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBgBrush)
    ) {
        // 3D Neumorphic Top Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = if (isDarkMode) 2.dp else 1.dp,
                    shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                    ambientColor = if (isDarkMode) Color(0x33000000) else Color(0x0A000000),
                    spotColor = if (isDarkMode) Color(0x44000000) else Color(0x0C000000)
                )
                .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                .background(headerBgBrush)
                .border(
                    width = 1.dp,
                    brush = if (isDarkMode) Brush.verticalGradient(listOf(Color(0x33FFFFFF), Color(0x11FFFFFF))) else Brush.verticalGradient(listOf(Color(0xEEFFFFFF), Color(0x1A000000))),
                    shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 3D Circular Back Button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .shadow(3.dp, CircleShape, ambientColor = SoftUITheme.ShadowDark)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color.White, Color(0xFFE8F1FC))))
                            .border(1.dp, Color(0xEEFFFFFF), CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { viewModel.navigateTo(Screen.HOME) }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1E3A5F),
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "Menu",
                        style = TextStyle(
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFF0075FF), Color(0xFF0052D4))
                            ),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.2.sp
                        )
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // 3D Search Button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .shadow(3.dp, CircleShape, ambientColor = SoftUITheme.ShadowDark)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color.White, Color(0xFFE8F1FC))))
                            .border(1.dp, Color(0xEEFFFFFF), CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { viewModel.navigateTo(Screen.SEARCH) }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF1E3A5F),
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // 3D Settings Button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .shadow(3.dp, CircleShape, ambientColor = SoftUITheme.ShadowDark)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color.White, Color(0xFFE8F1FC))))
                            .border(1.dp, Color(0xEEFFFFFF), CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color(0xFF1E3A5F),
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 3D Neumorphic User Profile Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 5.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = SoftUITheme.ShadowDark,
                        spotColor = SoftUITheme.ShadowDark
                    )
                    .clip(RoundedCornerShape(20.dp))
                    .background(cardBg)
                    .border(
                        width = 1.dp,
                        brush = cardBorderBrush,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onProfileClick
                    )
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // 3D Avatar Frame
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .shadow(2.dp, CircleShape, ambientColor = SoftUITheme.ShadowDark)
                                .clip(CircleShape)
                                .background(if (isDarkMode) Color(0xFF1E2128) else Color.White)
                                .border(1.5.dp, if (isDarkMode) Color(0x33FFFFFF) else Color(0x99FFFFFF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            FlikataAvatar(
                                name = currentUser?.fullName ?: "User",
                                avatarUrl = currentUser?.profilePic,
                                size = 50.dp
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = currentUser?.fullName ?: "Maruf Hossain",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.5.sp,
                                color = SoftUITheme.TextDark
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "See your profile",
                                fontSize = 12.5.sp,
                                color = Color(0xFF0075FF),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Go",
                        tint = SoftUITheme.IconTint
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3D Neumorphic Dark Mode Switch Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 2.dp,
                        shape = RoundedCornerShape(18.dp),
                        ambientColor = SoftUITheme.ShadowDark
                    )
                    .clip(RoundedCornerShape(18.dp))
                    .background(cardBg)
                    .border(
                        1.dp,
                        cardBorderBrush,
                        RoundedCornerShape(18.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .shadow(2.dp, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFFF3E0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LightMode,
                                contentDescription = "Sun",
                                tint = Color(0xFFF57C00),
                                modifier = Modifier.size(23.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Dark Mode",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.5.sp,
                                color = SoftUITheme.TextDark
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isDarkMode) "On (Dark Theme)" else "Off (Light Theme)",
                                fontSize = 12.sp,
                                color = SoftUITheme.TextSecondary
                            )
                        }
                    }

                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF0075FF)
                        ),
                        modifier = Modifier.testTag("dark_mode_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // "All Shortcuts" Section Title
            Text(
                text = "All Shortcuts",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = SoftUITheme.TextDark,
                modifier = Modifier.padding(bottom = 10.dp, start = 2.dp)
            )

            // 3D Grid Cards (Large vertical cards with 3D elevation and soft icons)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Large3DShortcutCard(
                        icon = Icons.Default.People,
                        title = "Friends",
                        subtitle = "Find friends & requests",
                        iconBg = Color(0xFFE7F3FF),
                        iconTint = Color(0xFF0075FF),
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(Screen.FRIENDS) }
                    )
                    Large3DShortcutCard(
                        icon = Icons.Default.VideoLibrary,
                        title = "Reels",
                        subtitle = "Watch short videos",
                        iconBg = Color(0xFFF3E5F5),
                        iconTint = Color(0xFF8E24AA),
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(Screen.REELS) }
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Large3DShortcutCard(
                        icon = Icons.Default.Bookmark,
                        title = "Saved",
                        subtitle = "Posts & bookmarks",
                        iconBg = Color(0xE0E0F7FA),
                        iconTint = Color(0xFF00897B),
                        modifier = Modifier.weight(1f),
                        onClick = { }
                    )
                    Large3DShortcutCard(
                        icon = Icons.Default.Groups,
                        title = "Groups",
                        subtitle = "Communities you follow",
                        iconBg = Color(0xFFFFF3E0),
                        iconTint = Color(0xFFF57C00),
                        modifier = Modifier.weight(1f),
                        onClick = { }
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Large3DShortcutCard(
                        icon = Icons.Default.History,
                        title = "Memories",
                        subtitle = "Reminisce past posts",
                        iconBg = Color(0xFFE8EAF6),
                        iconTint = Color(0xFF3F51B5),
                        modifier = Modifier.weight(1f),
                        onClick = { }
                    )
                    Large3DShortcutCard(
                        icon = Icons.Default.DynamicFeed,
                        title = "Feeds",
                        subtitle = "Most recent updates",
                        iconBg = Color(0xFFFFEBEE),
                        iconTint = Color(0xFFE53935),
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(Screen.HOME) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3D Connected Server Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = if (isDarkMode) 2.dp else 1.dp,
                        shape = RoundedCornerShape(18.dp),
                        ambientColor = SoftUITheme.ShadowDark
                    )
                    .clip(RoundedCornerShape(18.dp))
                    .background(cardBg)
                    .border(
                        1.dp,
                        cardBorderBrush,
                        RoundedCornerShape(18.dp)
                    )
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .shadow(2.dp, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDarkMode) Color(0xFF1E2638) else Color(0xFFE8F1FC)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Dns,
                                contentDescription = "Server",
                                tint = Color(0xFF0075FF),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Connected Server",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp,
                                color = SoftUITheme.TextDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = ApiClient.baseUrl,
                                fontSize = 12.sp,
                                color = SoftUITheme.TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .shadow(2.dp, RoundedCornerShape(10.dp))
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isDarkMode) Color(0xFF1B3020) else Color(0xFFE8F7EC))
                            .border(1.dp, Color(0x4445BD62), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Live",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = Color(0xFF45BD62)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3D Neumorphic Log Out Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .shadow(
                        elevation = if (isDarkMode) 2.dp else 3.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = Color(0x44FF3B30)
                    )
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isDarkMode) Color(0xFF2C1E20) else Color(0xFFFFF0F1))
                    .border(
                        width = 1.2.dp,
                        brush = Brush.verticalGradient(
                            listOf(if (isDarkMode) Color(0x33FF3B30) else Color.White, Color(0x66FF3B30))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { viewModel.logout() }
                    )
                    .testTag("menu_logout_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Log Out",
                        tint = FlikataError,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Log Out",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = FlikataError
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

/**
 * 3D Soft Neumorphic Large Shortcut Card
 */
@Composable
private fun Large3DShortcutCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconBg: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val isDark = SoftUITheme.isDark
    val cardBg = if (isDark) Color(0xFF242730) else Color.White
    val cardBorderBrush = if (isDark) {
        Brush.verticalGradient(listOf(Color(0x33FFFFFF), Color(0x11FFFFFF)))
    } else {
        Brush.verticalGradient(listOf(Color(0xEEFFFFFF), Color(0x33A0C0E8)))
    }
    val badgeBg = if (isDark) Color(0xFF1E2128) else iconBg

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDark) 3.dp else 2.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = SoftUITheme.ShadowDark,
                spotColor = SoftUITheme.ShadowDark
            )
            .clip(RoundedCornerShape(18.dp))
            .background(cardBg)
            .border(
                width = 1.dp,
                brush = cardBorderBrush,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(14.dp)
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            // 3D Soft Icon Badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .shadow(2.dp, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(badgeBg)
                    .border(1.dp, if (isDark) Color(0x22FFFFFF) else Color(0x44FFFFFF), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(23.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.5.sp,
                color = SoftUITheme.TextDark
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = subtitle,
                fontSize = 11.5.sp,
                color = SoftUITheme.TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
