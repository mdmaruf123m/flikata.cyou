package com.example.ui.screens.menu

import android.widget.Toast
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.ApiClient
import com.example.ui.components.FlikataAvatar
import com.example.ui.components.SoftUITheme
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.FlikataError
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen

/**
 * 3D Soft Neumorphic Menu Screen
 * Cleaned menu layout with Dark Mode card at the top,
 * followed by: Wallet, Marketplace, Verified Badge, History, Saved, Dashboard, Advertisement, Settings,
 * and bottom Connected Server & Logout controls.
 */
@Composable
fun MenuScreen(
    viewModel: MainViewModel,
    onProfileClick: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val context = LocalContext.current

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
                    // Circular Back Button
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
                            )
                            .testTag("menu_back_button"),
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
                    // Search Button
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
                            )
                            .testTag("menu_search_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF1E3A5F),
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Settings Button
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
                                onClick = {
                                    Toast.makeText(context, "Settings is coming soon!", Toast.LENGTH_SHORT).show()
                                }
                            )
                            .testTag("menu_top_settings_button"),
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
                    .testTag("menu_profile_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currentUser?.fullName ?: "User Profile",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.5.sp,
                                    color = SoftUITheme.TextDark
                                )
                                if (currentUser?.isVerified == true) {
                                    Spacer(modifier = Modifier.width(5.dp))
                                    VerifiedBadge(size = 20.dp)
                                }
                            }
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

            // 3D Neumorphic Dark Mode Switch Card (Preserved per request)
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
                    .testTag("menu_dark_mode_card")
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

            // Section Header
            Text(
                text = "Shortcuts",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = SoftUITheme.TextDark,
                modifier = Modifier.padding(bottom = 12.dp, start = 2.dp)
            )

            // 8 Clean Cards in 2-column grid
            // Order:
            // 1. Wallet (First!)
            // 2. Marketplace (Coming Soon)
            // 3. Verified Badge
            // 4. History (Coming Soon)
            // 5. Saved (Coming Soon)
            // 6. Dashboard (Coming Soon)
            // 7. Advertisement (Coming Soon)
            // 8. Settings (Coming Soon)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Row 1: Wallet & Marketplace
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MenuShortcutCard(
                        icon = Icons.Default.AccountBalanceWallet,
                        title = "Wallet",
                        subtitle = "Balance & transactions",
                        iconBg = Color(0xFFE8F5E9),
                        iconTint = Color(0xFF2E7D32),
                        badgeText = null,
                        testTag = "menu_card_wallet",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(Screen.WALLET) }
                    )
                    MenuShortcutCard(
                        icon = Icons.Default.Storefront,
                        title = "Marketplace",
                        subtitle = "Buy & sell items",
                        iconBg = Color(0xFFE0F2FE),
                        iconTint = Color(0xFF0284C7),
                        badgeText = "Coming Soon",
                        testTag = "menu_card_marketplace",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(Screen.MARKETPLACE) }
                    )
                }

                // Row 2: Verified Badge & History
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MenuShortcutCard(
                        icon = Icons.Default.Verified,
                        title = "Verified Badge",
                        subtitle = "Meta verification & badge",
                        iconBg = Color(0xFFE3F2FD),
                        iconTint = Color(0xFF0075FF),
                        badgeText = null,
                        testTag = "menu_card_verified_badge",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(Screen.VERIFIED_BADGE) }
                    )
                    MenuShortcutCard(
                        icon = Icons.Default.History,
                        title = "History",
                        subtitle = "Activity log & views",
                        iconBg = Color(0xFFEDE7F6),
                        iconTint = Color(0xFF673AB7),
                        badgeText = "Coming Soon",
                        testTag = "menu_card_history",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            Toast.makeText(context, "History will be available soon!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                // Row 3: Saved & Dashboard
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MenuShortcutCard(
                        icon = Icons.Default.Bookmark,
                        title = "Saved",
                        subtitle = "Saved posts & bookmarks",
                        iconBg = Color(0xFFE0F7FA),
                        iconTint = Color(0xFF00897B),
                        badgeText = "Coming Soon",
                        testTag = "menu_card_saved",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            Toast.makeText(context, "Saved items will be available soon!", Toast.LENGTH_SHORT).show()
                        }
                    )
                    MenuShortcutCard(
                        icon = Icons.Default.Dashboard,
                        title = "Dashboard",
                        subtitle = "Professional tools & monetization",
                        iconBg = Color(0xFFFFF3E0),
                        iconTint = Color(0xFFE65100),
                        badgeText = null,
                        testTag = "menu_card_dashboard",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.navigateTo(Screen.DASHBOARD)
                        }
                    )
                }

                // Row 4: Advertisement & Settings
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MenuShortcutCard(
                        icon = Icons.Default.Campaign,
                        title = "Advertisement",
                        subtitle = "Promote & grow audience",
                        iconBg = Color(0xFFFFEBEE),
                        iconTint = Color(0xFFC2185B),
                        badgeText = "Coming Soon",
                        testTag = "menu_card_advertisement",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            Toast.makeText(context, "Ad Manager is coming soon!", Toast.LENGTH_SHORT).show()
                        }
                    )
                    MenuShortcutCard(
                        icon = Icons.Default.Settings,
                        title = "Settings",
                        subtitle = "Privacy & preferences",
                        iconBg = Color(0xFFECEFF1),
                        iconTint = Color(0xFF455A64),
                        badgeText = "Coming Soon",
                        testTag = "menu_card_settings",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            Toast.makeText(context, "Settings will be available in the next update!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3D Connected Server Card (Preserved per request)
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

            Spacer(modifier = Modifier.height(16.dp))

            // 3D Neumorphic Log Out Button (Preserved per request)
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
 * Modern 3D Neumorphic Shortcut Card with optional "Coming Soon" badge
 */
@Composable
private fun MenuShortcutCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconBg: Color,
    iconTint: Color,
    badgeText: String? = null,
    testTag: String = "",
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
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
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

                // "Coming Soon" Pill Badge
                if (!badgeText.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isDark) Color(0xFF352B12) else Color(0xFFFFF3E0)
                            )
                            .border(
                                0.8.dp,
                                if (isDark) Color(0x88FFB300) else Color(0xAAFFB74D),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.5.dp)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDark) Color(0xFFFFB300) else Color(0xFFE65100),
                            letterSpacing = 0.3.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
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
