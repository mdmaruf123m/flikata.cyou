package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FlikataDivider
import com.example.ui.theme.FlikataSurface
import com.example.ui.theme.FlikataTextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsBottomSheet(
    sheetState: SheetState,
    isOwnProfile: Boolean = true,
    onDismiss: () -> Unit,
    onOptionClick: (String) -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    val sheetSurface = androidx.compose.material3.MaterialTheme.colorScheme.surface
    val sheetOnSurface = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
    val sheetOutline = androidx.compose.material3.MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = if (isOwnProfile) "Profile Settings" else "Profile Options",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = sheetOnSurface,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            if (isOwnProfile) {
                // 1. Dashboard
                BottomSheetMenuItem(
                    icon = Icons.Default.BarChart,
                    iconTint = Color(0xFF1877F2),
                    bgColor = Color(0xFF1877F2).copy(alpha = 0.15f),
                    title = "Dashboard",
                    onClick = {
                        onOptionClick("Dashboard")
                        onDismiss()
                    }
                )

                // 2. Pages
                BottomSheetMenuItem(
                    icon = Icons.Default.Flag,
                    iconTint = Color(0xFFF7B125),
                    bgColor = Color(0xFFF7B125).copy(alpha = 0.15f),
                    title = "Pages",
                    onClick = {
                        onOptionClick("Pages")
                        onDismiss()
                    }
                )

                // 3. Groups
                BottomSheetMenuItem(
                    icon = Icons.Default.Group,
                    iconTint = Color(0xFF42B72A),
                    bgColor = Color(0xFF42B72A).copy(alpha = 0.15f),
                    title = "Groups",
                    onClick = {
                        onOptionClick("Groups")
                        onDismiss()
                    }
                )

                // 4. Verified
                BottomSheetMenuItem(
                    icon = Icons.Default.Verified,
                    iconTint = Color(0xFF00A884),
                    bgColor = Color(0xFF00A884).copy(alpha = 0.15f),
                    title = "Verified",
                    onClick = {
                        onOptionClick("Verified")
                        onDismiss()
                    }
                )

                // 5. Settings
                BottomSheetMenuItem(
                    icon = Icons.Default.Settings,
                    iconTint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    bgColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
                    title = "Settings",
                    onClick = {
                        onOptionClick("Settings")
                        onDismiss()
                    }
                )

                Divider(color = sheetOutline, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 10.dp))
            }

            // Copy Profile Link (available for both own profile and others)
            BottomSheetMenuItem(
                icon = Icons.Default.ContentCopy,
                iconTint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                bgColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
                title = "Copy Profile Link",
                onClick = {
                    onOptionClick("Copy Link")
                    onDismiss()
                }
            )

            if (!isOwnProfile) {
                BottomSheetMenuItem(
                    icon = Icons.Default.Share,
                    iconTint = Color(0xFF1877F2),
                    bgColor = Color(0xFF1877F2).copy(alpha = 0.15f),
                    title = "Share Profile",
                    onClick = {
                        onOptionClick("Share Profile")
                        onDismiss()
                    }
                )
            }

            if (isOwnProfile) {
                Divider(color = sheetOutline, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 10.dp))

                // 7. Log Out
                BottomSheetMenuItem(
                    icon = Icons.Default.Logout,
                    iconTint = Color(0xFFD32F2F),
                    bgColor = Color(0xFFFFEBEE),
                    title = "Log Out",
                    onClick = {
                        onDismiss()
                        onLogoutClick()
                    }
                )
            }
        }
    }
}

@Composable
private fun BottomSheetMenuItem(
    icon: ImageVector,
    iconTint: Color,
    bgColor: Color,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
        )
    }
}
