package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay

data class ReactionIconItem(
    val name: String,
    val typeKey: String
)

val REACTION_ICON_ITEMS = listOf(
    ReactionIconItem("Like", "👍"),
    ReactionIconItem("Love", "❤️"),
    ReactionIconItem("Care", "😍"),
    ReactionIconItem("Haha", "😆"),
    ReactionIconItem("Wow", "😮"),
    ReactionIconItem("Sad", "😢"),
    ReactionIconItem("Angry", "😡")
)

@Composable
fun FacebookReactionPicker(
    modifier: Modifier = Modifier,
    onReactionSelected: (String) -> Unit
) {
    var activeIndex by remember { mutableStateOf<Int?>(null) } // Clean single row, no default raised item

    Surface(
        color = androidx.compose.material3.MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(32.dp),
        shadowElevation = 8.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            REACTION_ICON_ITEMS.forEachIndexed { index, item ->
                val isHovered = activeIndex == index
                val itemScale = remember { Animatable(0f) }

                LaunchedEffect(Unit) {
                    delay(index * 25L)
                    itemScale.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                }

                Box(
                    contentAlignment = Alignment.TopCenter,
                    modifier = Modifier.zIndex(if (isHovered) 30f else 1f)
                ) {
                    // Black Tooltip Pill DIRECTLY ABOVE THIS SPECIFIC HOVERED EMOJI!
                    if (isHovered) {
                        Surface(
                            color = Color(0xFF1C1E21),
                            shape = CircleShape,
                            shadowElevation = 8.dp,
                            modifier = Modifier
                                .offset(y = (-48).dp)
                                .zIndex(50f)
                        ) {
                            Text(
                                text = item.name,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                maxLines = 1
                            )
                        }
                    }

                    // Scaled Graphic Reaction Icon
                    val activeScale = if (isHovered) 1.55f else itemScale.value
                    val activeY = if (isHovered) (-12).dp else 0.dp

                    Box(
                        modifier = Modifier
                            .offset(y = activeY)
                            .scale(activeScale)
                            .size(36.dp)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        activeIndex = index
                                        tryAwaitRelease()
                                        onReactionSelected(item.name)
                                    },
                                    onTap = {
                                        onReactionSelected(item.name)
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        GraphicReactionIcon(
                            type = item.name,
                            size = 36.dp
                        )
                    }
                }
            }
        }
    }
}
