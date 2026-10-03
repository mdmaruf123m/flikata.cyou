package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.FlikataPrimary
import com.example.ui.viewmodel.Screen

@Composable
fun FlikataBottomNav(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    onCreatePostClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navSurface = MaterialTheme.colorScheme.surface
    val dividerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(navSurface)
            .navigationBarsPadding()
            .height(50.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

            // Compact Bottom Nav Bar Surface
            Surface(
                color = navSurface,
                shadowElevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(49.5.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Home (18.dp)
                    BottomNavItem(
                        label = "Home",
                        drawableRes = R.drawable.ic_custom_home,
                        isSelected = currentScreen == Screen.HOME,
                        iconSize = 18.dp,
                        onClick = { onNavigate(Screen.HOME) },
                        testTag = "nav_home"
                    )

                    // Reels (20.dp)
                    BottomNavItem(
                        label = "Reels",
                        drawableRes = R.drawable.ic_custom_reels,
                        isSelected = currentScreen == Screen.REELS,
                        iconSize = 20.dp,
                        onClick = { onNavigate(Screen.REELS) },
                        testTag = "nav_reels"
                    )

                    // Spacer for Center '+' Button
                    Box(modifier = Modifier.size(42.dp))

                    // Chats (18.dp)
                    BottomNavItem(
                        label = "Chats",
                        drawableRes = R.drawable.ic_custom_chats,
                        isSelected = currentScreen == Screen.CHATS || currentScreen == Screen.CHAT_DETAIL,
                        iconSize = 18.dp,
                        onClick = { onNavigate(Screen.CHATS) },
                        testTag = "nav_chats"
                    )

                    // Profile (20.dp)
                    BottomNavItem(
                        label = "Profile",
                        drawableRes = R.drawable.ic_custom_profile,
                        isSelected = currentScreen == Screen.PROFILE,
                        iconSize = 20.dp,
                        onClick = { onNavigate(Screen.PROFILE) },
                        testTag = "nav_profile"
                    )
                }
            }
        }

        // Floating Center '+' Button
        Surface(
            shape = CircleShape,
            color = FlikataPrimary,
            shadowElevation = 3.dp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-6).dp)
                .size(42.dp)
                .border(2.dp, navSurface, CircleShape)
                .clickable { onCreatePostClick() }
                .testTag("nav_create_post")
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Post",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    drawableRes: Int,
    isSelected: Boolean,
    iconSize: Dp = 20.dp,
    onClick: () -> Unit,
    testTag: String
) {
    val activeColor = FlikataPrimary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 1.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = drawableRes),
                contentDescription = label,
                tint = if (isSelected) activeColor else inactiveColor,
                modifier = Modifier
                    .size(iconSize)
                    .alpha(if (isSelected) 1.0f else 0.8f)
            )
        }
        Text(
            text = label,
            fontSize = 10.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) activeColor else inactiveColor,
            modifier = Modifier.padding(top = 1.dp)
        )
    }
}
