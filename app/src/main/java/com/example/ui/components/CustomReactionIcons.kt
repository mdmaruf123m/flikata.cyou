package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GraphicReactionIcon(
    type: String,
    size: Dp = 36.dp,
    modifier: Modifier = Modifier
) {
    when (type) {
        "Like", "👍" -> {
            Box(
                modifier = modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(Color(0xFF1877F2)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ThumbUp,
                    contentDescription = "Like",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.55f)
                )
            }
        }

        "Love", "❤️" -> {
            Box(
                modifier = modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(Color(0xFFFA383E)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Love",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.58f)
                )
            }
        }

        "Care", "😍" -> {
            Box(
                modifier = modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(Color(0xFFF7B125)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(size * 0.7f)) {
                    val w = this.size.width
                    val h = this.size.height
                    // Heart Eyes
                    drawCircle(Color(0xFFFA383E), radius = w * 0.15f, center = Offset(w * 0.3f, h * 0.35f))
                    drawCircle(Color(0xFFFA383E), radius = w * 0.15f, center = Offset(w * 0.7f, h * 0.35f))
                    // Smile
                    drawArc(
                        color = Color(0xFF212121),
                        startAngle = 0f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(w * 0.25f, h * 0.45f),
                        size = Size(w * 0.5f, h * 0.4f)
                    )
                }
            }
        }

        "Haha", "😆" -> {
            Box(
                modifier = modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(Color(0xFFF7B125)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(size * 0.7f)) {
                    val w = this.size.width
                    val h = this.size.height
                    // Squinting eyes (X or squint arches)
                    val eyePath = Path().apply {
                        moveTo(w * 0.2f, h * 0.35f)
                        lineTo(w * 0.35f, h * 0.28f)
                        lineTo(w * 0.2f, h * 0.22f)

                        moveTo(w * 0.8f, h * 0.35f)
                        lineTo(w * 0.65f, h * 0.28f)
                        lineTo(w * 0.8f, h * 0.22f)
                    }
                    drawPath(eyePath, color = Color(0xFF212121), style = androidx.compose.ui.graphics.drawscope.Stroke(width = w * 0.08f))

                    // Open laughing mouth
                    drawArc(
                        color = Color(0xFF212121),
                        startAngle = 0f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(w * 0.2f, h * 0.4f),
                        size = Size(w * 0.6f, h * 0.45f)
                    )
                }
            }
        }

        "Wow", "😮" -> {
            Box(
                modifier = modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(Color(0xFFF7B125)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(size * 0.7f)) {
                    val w = this.size.width
                    val h = this.size.height
                    // Round surprised eyes
                    drawCircle(Color(0xFF212121), radius = w * 0.1f, center = Offset(w * 0.3f, h * 0.3f))
                    drawCircle(Color(0xFF212121), radius = w * 0.1f, center = Offset(w * 0.7f, h * 0.3f))
                    // Open oval mouth
                    drawOval(
                        color = Color(0xFF212121),
                        topLeft = Offset(w * 0.35f, h * 0.5f),
                        size = Size(w * 0.3f, h * 0.38f)
                    )
                }
            }
        }

        "Sad", "😢" -> {
            Box(
                modifier = modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(Color(0xFFF7B125)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(size * 0.7f)) {
                    val w = this.size.width
                    val h = this.size.height
                    // Sad eyes
                    drawCircle(Color(0xFF212121), radius = w * 0.08f, center = Offset(w * 0.3f, h * 0.35f))
                    drawCircle(Color(0xFF212121), radius = w * 0.08f, center = Offset(w * 0.7f, h * 0.35f))
                    // Sad mouth
                    drawArc(
                        color = Color(0xFF212121),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(w * 0.3f, h * 0.6f),
                        size = Size(w * 0.4f, h * 0.3f)
                    )
                    // Tear drop
                    drawCircle(Color(0xFF2196F3), radius = w * 0.08f, center = Offset(w * 0.78f, h * 0.55f))
                }
            }
        }

        "Angry", "😡" -> {
            Box(
                modifier = modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(Color(0xFFE44D3A)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(size * 0.7f)) {
                    val w = this.size.width
                    val h = this.size.height
                    // Slanted eyebrows
                    drawLine(Color(0xFF212121), start = Offset(w * 0.15f, h * 0.2f), end = Offset(w * 0.42f, h * 0.35f), strokeWidth = w * 0.09f)
                    drawLine(Color(0xFF212121), start = Offset(w * 0.85f, h * 0.2f), end = Offset(w * 0.58f, h * 0.35f), strokeWidth = w * 0.09f)
                    // Angry eyes
                    drawCircle(Color(0xFF212121), radius = w * 0.08f, center = Offset(w * 0.3f, h * 0.42f))
                    drawCircle(Color(0xFF212121), radius = w * 0.08f, center = Offset(w * 0.7f, h * 0.42f))
                    // Angry frowning mouth
                    drawArc(
                        color = Color(0xFF212121),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(w * 0.25f, h * 0.62f),
                        size = Size(w * 0.5f, h * 0.3f)
                    )
                }
            }
        }

        else -> {
            Box(
                modifier = modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(Color(0xFF1877F2)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ThumbUp,
                    contentDescription = "Like",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.55f)
                )
            }
        }
    }
}
