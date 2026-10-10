package com.example.ui.screens.auth

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.SoftNeumorphicBackground
import com.example.ui.components.SoftNeumorphicCard
import com.example.ui.components.SoftNeumorphicTextField
import com.example.ui.components.SoftPrimaryBlueButton
import com.example.ui.components.SoftSecondaryPillButton
import com.example.ui.components.SoftUITheme
import com.example.ui.theme.FlikataError
import com.example.ui.viewmodel.MainViewModel

@Composable
fun LoginScreen(
    viewModel: MainViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    val emailOrPhone by viewModel.loginEmailOrPhone.collectAsState()
    val password by viewModel.loginPassword.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val isLoading by viewModel.authLoading.collectAsState()

    var rememberMe by remember { mutableStateOf(true) }
    val scrollState = rememberScrollState()

    SoftNeumorphicBackground(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Top Right: Dark mode toggle button in soft rounded square matching screenshot
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                SoftNeumorphicCard(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(13.dp),
                    elevation = 4.dp,
                    backgroundColor = Color(0xFFF1F6FD),
                    onClick = { viewModel.toggleDarkMode() }
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bedtime,
                            contentDescription = "Theme Mode",
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Center: Refined, elegant Neumorphic Logo Badge with smooth concentric rings (compact & aesthetic)
            Box(
                modifier = Modifier
                    .size(108.dp)
                    .shadow(
                        elevation = 10.dp,
                        shape = RoundedCornerShape(34.dp),
                        ambientColor = SoftUITheme.ShadowDark,
                        spotColor = SoftUITheme.ShadowDark
                    )
                    .clip(RoundedCornerShape(34.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFFFFFF),
                                Color(0xFFE2EDFB)
                            )
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFFFFFFF), Color(0x66B0D0F5))
                        ),
                        shape = RoundedCornerShape(34.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Inner concentric soft ring
                Box(
                    modifier = Modifier
                        .size(82.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFFFFFFFF), Color(0xFFD6E6FA))
                            )
                        )
                        .border(
                            width = 1.5.dp,
                            color = Color(0xAAFFFFFF),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // App Logo (from root logo.png -> R.drawable.app_logo)
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "Flikata Logo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Title "Welcome Back"
            Text(
                text = "Welcome Back",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0B132A),
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Subtitle "Sign in to continue"
            Text(
                text = "Sign in to continue",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6B7280),
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Field 1: Phone number or Email (as requested: Phone number or Email in English)
            SoftNeumorphicTextField(
                value = emailOrPhone,
                onValueChange = { viewModel.loginEmailOrPhone.value = it },
                placeholder = "Phone number or Email",
                leadingIcon = Icons.Default.PersonOutline,
                modifier = Modifier.testTag("login_email_phone_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Field 2: Password
            SoftNeumorphicTextField(
                value = password,
                onValueChange = { viewModel.loginPassword.value = it },
                placeholder = "Password",
                leadingIcon = Icons.Default.Lock,
                isPassword = true,
                modifier = Modifier.testTag("login_password_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Row: "Remember Me" checkbox & "Forgot Password?"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Remember me checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { rememberMe = !rememberMe }
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(if (rememberMe) Color(0xFF0075FF) else Color(0xFFDCE6F5))
                            .border(
                                width = 1.dp,
                                color = if (rememberMe) Color(0xFF0052D4) else Color(0xFFB8C8DF),
                                shape = RoundedCornerShape(5.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (rememberMe) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Remember Me",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF334155)
                    )
                }

                // Forgot Password link
                Text(
                    text = "Forgot Password?",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0070F3),
                    modifier = Modifier.clickable {
                        onNavigateToForgotPassword()
                    }
                )
            }

            if (authError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = authError!!,
                    color = FlikataError,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Primary Button: "Login ->"
            SoftPrimaryBlueButton(
                text = "Login",
                onClick = { viewModel.performLogin() },
                isLoading = isLoading,
                modifier = Modifier.testTag("login_submit_btn")
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Secondary Pill Button: "Create New Account ->"
            SoftSecondaryPillButton(
                text = "Create New Account",
                leadingIcon = Icons.Default.PersonAdd,
                onClick = onNavigateToRegister,
                modifier = Modifier.testTag("login_goto_register_btn")
            )

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}
