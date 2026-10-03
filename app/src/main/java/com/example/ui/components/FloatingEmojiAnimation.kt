package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun FloatingEmojiAnimation(
    reactionType: String,
    onAnimationEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val yAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(1f) }
    val alphaAnim = remember { Animatable(1f) }

    LaunchedEffect(reactionType) {
        yAnim.animateTo(-120f, animationSpec = tween(700, easing = LinearOutSlowInEasing))
    }
    LaunchedEffect(reactionType) {
        scaleAnim.animateTo(2.5f, animationSpec = tween(500))
    }
    LaunchedEffect(reactionType) {
        alphaAnim.animateTo(0f, animationSpec = tween(700, easing = FastOutLinearInEasing))
        onAnimationEnd()
    }

    Box(
        modifier = modifier
            .offset { IntOffset(0, yAnim.value.dp.roundToPx()) }
            .scale(scaleAnim.value)
            .alpha(alphaAnim.value),
        contentAlignment = Alignment.Center
    ) {
        GraphicReactionIcon(
            type = reactionType,
            size = 36.dp
        )
    }
}
