package com.example.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LockReset
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SoftNeumorphicBackground
import com.example.ui.components.SoftNeumorphicCard
import com.example.ui.components.SoftNeumorphicTextField
import com.example.ui.components.SoftPrimaryBlueButton
import com.example.ui.components.SoftSecondaryPillButton
import com.example.ui.components.SoftUITheme
import com.example.ui.theme.FlikataError
import com.example.ui.viewmodel.MainViewModel

@Composable
fun ForgotPasswordScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val identifier by viewModel.forgotPasswordEmailOrPhone.collectAsState()
    val isSubmitted by viewModel.forgotPasswordSubmitted.collectAsState()
    val isLoading by viewModel.authLoading.collectAsState()
    val authError by viewModel.authError.collectAsState()
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
            Spacer(modifier = Modifier.height(14.dp))

            // Top Header: Back Button and Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SoftNeumorphicCard(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(13.dp),
                    elevation = 4.dp,
                    backgroundColor = Color(0xFFF1F6FD),
                    onClick = onNavigateBack
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Forgot Password",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0B132A),
                    letterSpacing = (-0.3).sp
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Center Badge: Lock Reset 3D Icon with concentric soft neumorphic styling
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(30.dp),
                        ambientColor = SoftUITheme.ShadowDark,
                        spotColor = SoftUITheme.ShadowDark
                    )
                    .clip(RoundedCornerShape(30.dp))
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
                        shape = RoundedCornerShape(30.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
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
                    Icon(
                        imageVector = if (isSubmitted) Icons.Default.CheckCircle else Icons.Default.LockReset,
                        contentDescription = "Reset Password Icon",
                        tint = if (isSubmitted) Color(0xFF10B981) else Color(0xFF0075FF),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main Title
            Text(
                text = if (isSubmitted) "Check Your Inbox" else "Reset Password",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0B132A),
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Informative subtitle
            Text(
                text = if (isSubmitted)
                    "We've sent password reset instructions to your phone number or email. Please check your messages."
                else
                    "Enter your registered phone number or email address. We'll send you verification instructions to reset your password.",
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF64748B),
                lineHeight = 21.sp,
                modifier = Modifier.padding(horizontal = 8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            if (!isSubmitted) {
                // Input Field: Phone number or Email
                SoftNeumorphicTextField(
                    value = identifier,
                    onValueChange = { viewModel.forgotPasswordEmailOrPhone.value = it },
                    placeholder = "Phone number or Email",
                    leadingIcon = Icons.Default.PersonOutline,
                    modifier = Modifier.testTag("forgot_password_identifier_input")
                )

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

                // Submit Button
                SoftPrimaryBlueButton(
                    text = "Send Instructions",
                    onClick = { viewModel.performForgotPassword() },
                    isLoading = isLoading,
                    modifier = Modifier.testTag("forgot_password_submit_btn")
                )
            } else {
                // Success message card
                SoftNeumorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    elevation = 4.dp,
                    backgroundColor = Color(0xFFF1FDF5)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Reset link/code requested successfully for $identifier",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF065F46)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                SoftPrimaryBlueButton(
                    text = "Back to Login",
                    onClick = {
                        viewModel.resetForgotPasswordState()
                        onNavigateBack()
                    },
                    modifier = Modifier.testTag("forgot_password_back_login_btn")
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Secondary option: Remember password?
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        viewModel.resetForgotPasswordState()
                        onNavigateBack()
                    }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Remember password? ",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B)
                )
                Text(
                    text = "Sign In",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0075FF)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
