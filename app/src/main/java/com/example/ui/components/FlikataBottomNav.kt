package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.viewmodel.Screen

/**
 * 3D Neumorphic Bottom Navigation Bar matching the Flikata application style:
 * - 3D elevated curved surface with top highlight rim and subtle depth
 * - Supports both Light and Dark modes seamlessly
 * - Only the icon and text change color dynamically on selection / hover / press (no colored block pills behind tabs)
 * - Tactile 3D spring micro-animations on selection and touch
 * - Raised 3D Glossy Floating Center '+' Button
 */
@Composable
fun FlikataBottomNav(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    onCreatePostClick: () -> Unit,
    modifier: Modifier = Modifier,
    userAvatarUrl: String? = null,
    unreadChatsCount: Int = 0
) {
    val isDark = SoftUITheme.isDark

    val barBgBrush = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF242730),
                Color(0xFF1E2128),
                Color(0xFF16181E)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFFFFFF),
                Color(0xFFF9FBFE),
                Color(0xFFF2F6FC)
            )
        )
    }

    val barBorderBrush = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0x33FFFFFF),
                Color(0x11FFFFFF)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xEEFFFFFF),
                Color(0x22A0C0E8)
            )
        )
    }

    val shadowElevation = if (isDark) 6.dp else 3.dp
    val shadowAmbient = if (isDark) Color(0x66000000) else Color(0x0C000000)
    val shadowSpot = if (isDark) Color(0x88000000) else Color(0x10000000)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(58.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // 3D Elevated Curved Nav Bar Surface with subtle top rim highlight
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(
                    elevation = shadowElevation,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    ambientColor = shadowAmbient,
                    spotColor = shadowSpot
                )
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(barBgBrush)
                .border(
                    width = 1.dp,
                    brush = barBorderBrush,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home
                BottomNavItem(
                    label = "Home",
                    drawableRes = R.drawable.ic_custom_home,
                    isSelected = currentScreen == Screen.HOME,
                    isDark = isDark,
                    iconSize = 22.dp,
                    onClick = { onNavigate(Screen.HOME) },
                    testTag = "nav_home"
                )

                // Reels (Loops)
                BottomNavItem(
                    label = "Reels",
                    drawableRes = R.drawable.ic_custom_reels,
                    isSelected = currentScreen == Screen.REELS,
                    isDark = isDark,
                    iconSize = 25.dp,
                    onClick = { onNavigate(Screen.REELS) },
                    testTag = "nav_reels"
                )

                // Center spacer for the elevated Floating Action Button
                Box(modifier = Modifier.size(52.dp))

                // Chats
                BottomNavItem(
                    label = "Chats",
                    drawableRes = R.drawable.ic_custom_chats,
                    isSelected = currentScreen == Screen.CHATS || currentScreen == Screen.CHAT_DETAIL,
                    isDark = isDark,
                    iconSize = 22.dp,
                    badgeCount = unreadChatsCount,
                    onClick = { onNavigate(Screen.CHATS) },
                    testTag = "nav_chats"
                )

                // Profile (with small avatar thumbnail)
                BottomNavItem(
                    label = "Profile",
                    drawableRes = R.drawable.ic_custom_profile,
                    isSelected = currentScreen == Screen.PROFILE,
                    isDark = isDark,
                    iconSize = 22.dp,
                    avatarUrl = userAvatarUrl,
                    onClick = { onNavigate(Screen.PROFILE) },
                    testTag = "nav_profile"
                )
            }
        }

        // 3D Glossy Center Floating Action Button with royal blue ambient glow
        val centerInteractionSource = remember { MutableInteractionSource() }
        val centerHovered by centerInteractionSource.collectIsHoveredAsState()
        val centerPressed by centerInteractionSource.collectIsPressedAsState()
        val centerScale by animateFloatAsState(
            targetValue = if (centerPressed) 0.90f else if (centerHovered) 1.08f else 1.0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "center_fab_scale"
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-13).dp)
                .scale(centerScale)
                .size(48.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    ambientColor = Color(0x880075FF),
                    spotColor = Color(0xCC0052D4)
                )
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0075FF),
                            Color(0xFF0052D4)
                        )
                    )
                )
                .border(
                    width = 2.5.dp,
                    color = if (isDark) Color(0xFF242730) else Color.White,
                    shape = CircleShape
                )
                .hoverable(centerInteractionSource)
                .clickable(
                    interactionSource = centerInteractionSource,
                    indication = null,
                    onClick = { onCreatePostClick() }
                )
                .testTag("nav_create_post"),
            contentAlignment = Alignment.Center
        ) {
            // Inner subtle specular highlight arc for 3D sphere depth
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .align(Alignment.TopCenter)
            ) {
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0x66FFFFFF), Color(0x00FFFFFF))
                    ),
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx(), 24.dp.toPx())
                )
            }

            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Create Post",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * 3D Bottom Nav Item:
 * - NO colored container background (stays clean and uniform with the bar surface)
 * - Only the icon and text change color dynamically on selection / hover / press
 * - Micro 3D indicator pill beneath active item
 * - Adaptive colors for Light and Dark themes
 */
@Composable
private fun BottomNavItem(
    label: String,
    drawableRes: Int,
    isSelected: Boolean,
    isDark: Boolean,
    iconSize: Dp = 22.dp,
    avatarUrl: String? = null,
    badgeCount: Int = 0,
    onClick: () -> Unit,
    testTag: String
) {
    val activeColor = if (isDark) Color(0xFF3897F0) else Color(0xFF0075FF)
    val inactiveColor = if (isDark) Color(0xFF9EA3AE) else Color(0xFF71767B)

    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()

    val isHighlighted = isSelected || isHovered

    // Smooth animated color transition for icon and label
    val itemColor by animateColorAsState(
        targetValue = if (isHighlighted) activeColor else inactiveColor,
        animationSpec = tween(durationMillis = 180),
        label = "nav_item_color"
    )

    // Smooth animated scale transition on selection / hover / press
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else if (isHighlighted) 1.08f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "nav_item_scale"
    )

    Column(
        modifier = Modifier
            .hoverable(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .scale(animatedScale),
            contentAlignment = Alignment.Center
        ) {
            if (avatarUrl != null && label == "Profile") {
                // Profile avatar thumbnail with border indicator when selected/hovered
                Box(
                    modifier = Modifier
                        .size(23.dp)
                        .clip(CircleShape)
                        .border(
                            width = if (isHighlighted) 1.8.dp else 1.dp,
                            color = if (isHighlighted) itemColor else (if (isDark) Color(0x33FFFFFF) else Color(0x33000000)),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    FlikataAvatar(
                        name = "Profile",
                        avatarUrl = avatarUrl,
                        size = 22.dp
                    )
                }
            } else {
                Icon(
                    painter = painterResource(id = drawableRes),
                    contentDescription = label,
                    tint = itemColor,
                    modifier = Modifier.size(iconSize)
                )
            }

            // Unread badge indicator if any
            if (badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = (-3).dp)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF3B30)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium,
            color = itemColor,
            modifier = Modifier.padding(top = 2.dp)
        )

        // Subtle 3D micro indicator beneath active tab
        if (isSelected) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .width(12.dp)
                    .height(2.5.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF0075FF),
                                Color(0xFF00C6FF)
                            )
                        )
                    )
            )
        } else {
            Spacer(modifier = Modifier.height(4.5.dp))
        }
    }
}
