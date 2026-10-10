package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlin.math.abs

// Harmonious, vibrant gradients for the initial-letter logo avatar
private val AVATAR_GRADIENTS = listOf(
    listOf(Color(0xFF1877F2), Color(0xFF00C6FF)), // Flikata Blue
    listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0)), // Royal Violet
    listOf(Color(0xFF11998E), Color(0xFF38EF7D)), // Emerald Green
    listOf(Color(0xFFFF416C), Color(0xFFFF4B2B)), // Coral Crimson
    listOf(Color(0xFFF7971E), Color(0xFFFFD200)), // Warm Amber
    listOf(Color(0xFF4776E6), Color(0xFF8E54E9)), // Indigo Purple
    listOf(Color(0xFF00B4DB), Color(0xFF0083B0)), // Sea Teal
    listOf(Color(0xFFEC008C), Color(0xFFFC6767))  // Vibrant Rose
)

@Composable
fun FlikataAvatar(
    name: String,
    avatarUrl: String?,
    size: Dp = 40.dp,
    borderWidth: Dp = 0.dp,
    borderColor: Color = Color.Transparent,
    modifier: Modifier = Modifier
) {
    val cleanName = name.trim().ifEmpty { "User" }
    val initialChar = cleanName.first().uppercaseChar().toString()
    val gradientIndex = (cleanName.hashCode().let { if (it == Int.MIN_VALUE) 0 else abs(it) }) % AVATAR_GRADIENTS.size
    val gradientColors = AVATAR_GRADIENTS[gradientIndex]

    val baseModifier = modifier
        .size(size)
        .clip(CircleShape)
        .then(
            if (borderWidth > 0.dp) {
                Modifier.border(borderWidth, borderColor, CircleShape)
            } else {
                Modifier
            }
        )

    val resolvedUrl = androidx.compose.runtime.remember(avatarUrl) {
        val u = avatarUrl?.trim()
        if (u.isNullOrBlank()) null
        else if (u.startsWith("http://") || u.startsWith("https://") || u.startsWith("file://") || u.startsWith("content://") || u.startsWith("data:")) u
        else {
            val base = com.example.data.remote.ApiClient.baseUrl.trimEnd('/')
            val cleanPath = u.trimStart('/')
            "$base/$cleanPath"
        }
    }

    if (!resolvedUrl.isNullOrBlank()) {
        AsyncImage(
            model = resolvedUrl,
            contentDescription = cleanName,
            contentScale = ContentScale.Crop,
            modifier = baseModifier
        )
    } else {
        // Beautiful Initial Letter Logo Avatar (Like Google / modern apps)
        Box(
            modifier = baseModifier.background(Brush.linearGradient(gradientColors)),
            contentAlignment = Alignment.Center
        ) {
            val fontSize = (size.value * 0.44f).coerceAtLeast(10f).sp
            Text(
                text = initialChar,
                color = Color.White,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
