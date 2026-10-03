package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.FlikataError
import com.example.ui.theme.FlikataPrimary

@Composable
fun FlikataHeader(
    unreadNotificationsCount: Int = 0,
    onSearchClick: () -> Unit = {},
    onFriendsClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onMenuClick: () -> Unit = {}
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Official App Logo & Title
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "Flikata Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(34.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Flikata",
                    color = FlikataPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Right Action Buttons with Custom PNG Icons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search icon (smaller per user request)
                HeaderIconButton(
                    drawableRes = R.drawable.ic_custom_search,
                    contentDescription = "Search",
                    iconSize = 15.dp,
                    onClick = onSearchClick
                )

                HeaderIconButton(
                    drawableRes = R.drawable.ic_custom_friends,
                    contentDescription = "Friends",
                    iconSize = 20.dp,
                    onClick = onFriendsClick
                )

                // Notification Bell with Badge
                Box(contentAlignment = Alignment.Center) {
                    HeaderIconButton(
                        drawableRes = R.drawable.ic_custom_notification,
                        contentDescription = "Notifications",
                        iconSize = 20.dp,
                        onClick = onNotificationsClick
                    )
                    if (unreadNotificationsCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 2.dp, y = (-2).dp)
                                .size(17.dp)
                                .clip(CircleShape)
                                .background(FlikataError),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (unreadNotificationsCount > 9) "9+" else unreadNotificationsCount.toString(),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 10.sp
                            )
                        }
                    }
                }

                HeaderIconButton(
                    drawableRes = R.drawable.ic_custom_menu,
                    contentDescription = "Menu",
                    iconSize = 20.dp,
                    onClick = onMenuClick
                )
            }
        }
    }
}

@Composable
private fun HeaderIconButton(
    drawableRes: Int,
    contentDescription: String,
    iconSize: androidx.compose.ui.unit.Dp = 20.dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.Icon(
            painter = painterResource(id = drawableRes),
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(iconSize)
        )
    }
}
