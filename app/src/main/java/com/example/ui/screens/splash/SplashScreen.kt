package com.example.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.ui.theme.FlikataPrimary
import com.example.ui.theme.FlikataTextSecondary
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    viewModel: MainViewModel
) {
    val currentUser by viewModel.currentUser.collectAsState()

    val fullText = "Feel free with Flikata"
    var visibleTextLength by remember { mutableIntStateOf(0) }

    // Typewriter text animation effect
    LaunchedEffect(Unit) {
        for (i in 1..fullText.length) {
            visibleTextLength = i
            delay(75) // letter by letter typing speed
        }
        delay(1200) // Hold briefly after typing
        if (currentUser != null) {
            viewModel.navigateTo(Screen.HOME)
        } else {
            viewModel.navigateTo(Screen.LOGIN)
        }
    }

    // 3 Bouncing dots animation
    val dot1Offset = remember { Animatable(0f) }
    val dot2Offset = remember { Animatable(0f) }
    val dot3Offset = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        val animationSpec = infiniteRepeatable<Float>(
            animation = tween(durationMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )

        // Staggered bounce for 3 dots
        dot1Offset.animateTo(-12f, animationSpec)
    }
    LaunchedEffect(Unit) {
        delay(120)
        val animationSpec = infiniteRepeatable<Float>(
            animation = tween(durationMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
        dot2Offset.animateTo(-12f, animationSpec)
    }
    LaunchedEffect(Unit) {
        delay(240)
        val animationSpec = infiniteRepeatable<Float>(
            animation = tween(durationMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
        dot3Offset.animateTo(-12f, animationSpec)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        // Center Section: Logo + Animated Typewriter Tagline
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "Flikata Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(90.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = fullText.take(visibleTextLength),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = FlikataPrimary
            )
        }

        // Bottom Section: Bouncing Dots + "From Flikata"
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .offset(y = dot1Offset.value.dp)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(FlikataPrimary)
                )
                Box(
                    modifier = Modifier
                        .offset(y = dot2Offset.value.dp)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(FlikataPrimary)
                )
                Box(
                    modifier = Modifier
                        .offset(y = dot3Offset.value.dp)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(FlikataPrimary)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "From Flikata",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = FlikataTextSecondary
            )
        }
    }
}
