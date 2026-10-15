package com.example.ui.screens.verified

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.VerificationPackage
import com.example.data.model.VerificationPackagesData
import com.example.ui.components.FlikataAvatar
import com.example.ui.components.SoftUITheme
import com.example.ui.components.VerifiedBadge
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Verified Badge Showcase, Expiration Counter, Requirements & Wallet Purchase Screen
 * No header, No footer, No back button on screen — Mobile system back button handles back navigation.
 */
@Composable
fun VerifiedBadgeScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentUser by viewModel.currentUser.collectAsState()
    val isDark = SoftUITheme.isDark

    var packagesData by remember { mutableStateOf<VerificationPackagesData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedPackage by remember { mutableStateOf<VerificationPackage?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    // Upload inputs (URLs / placeholders)
    var idFrontUrl by remember { mutableStateOf("") }
    var idBackUrl by remember { mutableStateOf("") }
    var selfieUrl by remember { mutableStateOf("") }

    // Live countdown timer state
    var remainingSeconds by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        val res = viewModel.repository.getVerificationPackages()
        if (res.isSuccess) {
            val data = res.getOrNull()
            packagesData = data
            remainingSeconds = data?.user?.remainingSeconds ?: 0L
            if (!data?.packages.isNullOrEmpty()) {
                selectedPackage = data?.packages?.firstOrNull()
            }
        }
        isLoading = false
    }

    // Tick countdown every second
    LaunchedEffect(remainingSeconds) {
        if (remainingSeconds > 0) {
            delay(1000)
            remainingSeconds = (remainingSeconds - 1).coerceAtLeast(0L)
        }
    }

    val screenBg = if (isDark) {
        Brush.verticalGradient(listOf(Color(0xFF0F1115), Color(0xFF161920), Color(0xFF0A0C10)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFF0F5FD), Color(0xFFE4EDFA), Color(0xFFDAE6F6)))
    }
    val cardBg = if (isDark) Color(0xFF1E222B) else Color.White
    val cardBorder = if (isDark) Color(0x33FFFFFF) else Color(0x66B0C8E8)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF0866FF))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                // 1. Profile Preview Card with Live Verification Rosette
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                        .background(cardBg)
                        .border(1.2.dp, cardBorder, RoundedCornerShape(24.dp))
                ) {
                    Column {
                        // Banner Preview
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(115.dp)
                                .background(Brush.linearGradient(listOf(Color(0xFF0075FF), Color(0xFF0052D4))))
                        ) {
                            if (!currentUser?.coverPic.isNullOrEmpty() && !currentUser?.coverPic!!.contains("unsplash")) {
                                AsyncImage(
                                    model = currentUser?.coverPic,
                                    contentDescription = "Cover",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        // Avatar & Name Preview
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(86.dp)
                                    .offset(y = (-40).dp)
                                    .shadow(6.dp, CircleShape)
                                    .clip(CircleShape)
                                    .border(3.5.dp, cardBg, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                FlikataAvatar(
                                    name = currentUser?.fullName ?: "User",
                                    avatarUrl = currentUser?.profilePic,
                                    size = 86.dp
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.offset(y = (-30).dp)
                            ) {
                                Text(
                                    text = currentUser?.fullName ?: "User",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftUITheme.TextDark
                                )
                                Spacer(modifier = Modifier.width(7.dp))
                                // Verified Badge Showcase preview
                                VerifiedBadge(size = 24.dp)
                            }

                            Text(
                                text = "Preview of how your profile will appear with Verified Badge",
                                fontSize = 12.sp,
                                color = SoftUITheme.TextSecondary,
                                modifier = Modifier.offset(y = (-26).dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Active Verification Live Expiration Countdown (if user already verified)
                val userStatus = packagesData?.user
                if (userStatus?.isVerified == true) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF0A2540), Color(0xFF0D3B66))
                                )
                            )
                            .border(1.dp, Color(0x660866FF), RoundedCornerShape(20.dp))
                            .padding(18.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x330075FF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = Color(0xFF64B5F6),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (userStatus.isLifetime) "Lifetime Verified Account" else "Verified Badge Active",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = if (userStatus.isGraceActive) "Grace Period Active (Renew Now)" else if (userStatus.isLifetime) "Permanent badge access" else "Time remaining on badge subscription",
                                        fontSize = 11.5.sp,
                                        color = if (userStatus.isGraceActive) Color(0xFFFFB74D) else Color(0xFFB0BEC5)
                                    )
                                }
                            }

                            if (!userStatus.isLifetime && remainingSeconds > 0) {
                                Spacer(modifier = Modifier.height(14.dp))
                                val days = remainingSeconds / 86400
                                val hours = (remainingSeconds % 86400) / 3600
                                val minutes = (remainingSeconds % 3600) / 60
                                val seconds = remainingSeconds % 60

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    CountdownBlock(value = "$days", label = "Days")
                                    CountdownBlock(value = String.format(Locale.getDefault(), "%02d", hours), label = "Hours")
                                    CountdownBlock(value = String.format(Locale.getDefault(), "%02d", minutes), label = "Mins")
                                    CountdownBlock(value = String.format(Locale.getDefault(), "%02d", seconds), label = "Secs")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 3. Pending Verification Notice
                if (packagesData?.pendingRequest != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(18.dp))
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (isDark) Color(0xFF2C2216) else Color(0xFFFFF8E1))
                            .border(1.dp, Color(0xFFFFB300), RoundedCornerShape(18.dp))
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.HourglassTop,
                                contentDescription = null,
                                tint = Color(0xFFF57F17),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Verification Request Under Review",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFFF57F17)
                                )
                                Text(
                                    text = "Your request for ${packagesData?.pendingRequest?.packageTitle ?: "Verified Badge"} is being verified by admin.",
                                    fontSize = 12.sp,
                                    color = SoftUITheme.TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 4. Verification Packages Section
                Text(
                    text = "Select Verification Package",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SoftUITheme.TextDark,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                val packagesList = packagesData?.packages ?: emptyList()
                if (packagesList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(18.dp))
                            .clip(RoundedCornerShape(18.dp))
                            .background(cardBg)
                            .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No packages currently available. Please check back later.",
                            fontSize = 13.sp,
                            color = SoftUITheme.TextSecondary
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        packagesList.forEach { pkg ->
                            val isSelected = selectedPackage?.id == pkg.id
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(if (isSelected) 4.dp else 2.dp, RoundedCornerShape(18.dp))
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(cardBg)
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) Color(0xFF0866FF) else cardBorder,
                                        shape = RoundedCornerShape(18.dp)
                                    )
                                    .clickable { selectedPackage = pkg }
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .border(
                                                    2.dp,
                                                    if (isSelected) Color(0xFF0866FF) else Color.Gray,
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(12.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFF0866FF))
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Text(
                                                text = pkg.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = SoftUITheme.TextDark
                                            )
                                            Text(
                                                text = "Duration: ${pkg.durationText}" + (if (!pkg.description.isNullOrBlank()) " • ${pkg.description}" else ""),
                                                fontSize = 12.sp,
                                                color = SoftUITheme.TextSecondary
                                            )
                                        }
                                    }

                                    Text(
                                        text = "৳ ${String.format(Locale.getDefault(), "%.2f", pkg.price)}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF0866FF)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 5. Verification Requirements Section (ID Card & Selfie)
                val settings = packagesData?.settings
                val idReq = settings?.idCardRequired == true
                val selfieReq = settings?.selfieRequired == true

                if (idReq || selfieReq) {
                    Text(
                        text = "Verification Documentation",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoftUITheme.TextDark,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(18.dp))
                            .clip(RoundedCornerShape(18.dp))
                            .background(cardBg)
                            .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (idReq) {
                                Column {
                                    Text(
                                        text = "National ID / Passport (Required)",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.5.sp,
                                        color = SoftUITheme.TextDark
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = idFrontUrl,
                                        onValueChange = { idFrontUrl = it },
                                        label = { Text("ID Front Page Image URL") },
                                        leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = idBackUrl,
                                        onValueChange = { idBackUrl = it },
                                        label = { Text("ID Back Page Image URL") },
                                        leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                }
                            }

                            if (selfieReq) {
                                Column {
                                    Text(
                                        text = "Live Face Selfie (Required)",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.5.sp,
                                        color = SoftUITheme.TextDark
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = selfieUrl,
                                        onValueChange = { selfieUrl = it },
                                        label = { Text("Selfie Photo Image URL") },
                                        leadingIcon = { Icon(Icons.Default.Face, contentDescription = null) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                }

                // 6. Wallet Balance & Payment Deduction Check
                val walletBal = userStatus?.walletBalance ?: 0.0
                val selectedPrice = selectedPackage?.price ?: 0.0
                val hasSufficientBalance = walletBal >= selectedPrice

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(3.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(cardBg)
                        .border(1.dp, cardBorder, RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE8F5E9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Wallet Balance",
                                        fontSize = 12.sp,
                                        color = SoftUITheme.TextSecondary
                                    )
                                    Text(
                                        text = "৳ ${String.format(Locale.getDefault(), "%.2f", walletBal)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = SoftUITheme.TextDark
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .shadow(2.dp, RoundedCornerShape(12.dp))
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF0866FF))
                                    .clickable { viewModel.navigateTo(Screen.DEPOSIT) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "Deposit",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        if (selectedPackage != null && !hasSufficientBalance) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFFD32F2F),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Insufficient balance! Please deposit at least ৳ ${String.format(Locale.getDefault(), "%.2f", selectedPrice - walletBal)} more.",
                                    fontSize = 12.sp,
                                    color = Color(0xFFD32F2F),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // 7. Submit Application Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .shadow(4.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (selectedPackage != null && !isSubmitting) {
                                Brush.linearGradient(listOf(Color(0xFF0866FF), Color(0xFF0052D4)))
                            } else {
                                Brush.linearGradient(listOf(Color.Gray, Color.DarkGray))
                            }
                        )
                        .clickable(enabled = selectedPackage != null && !isSubmitting) {
                            val pkg = selectedPackage ?: return@clickable
                            if (!hasSufficientBalance) {
                                Toast.makeText(context, "Insufficient balance! Please deposit to your wallet first.", Toast.LENGTH_LONG).show()
                                viewModel.navigateTo(Screen.DEPOSIT)
                                return@clickable
                            }

                            if (idReq && (idFrontUrl.isBlank() || idBackUrl.isBlank())) {
                                Toast.makeText(context, "Please provide ID Card front & back images.", Toast.LENGTH_SHORT).show()
                                return@clickable
                            }

                            if (selfieReq && selfieUrl.isBlank()) {
                                Toast.makeText(context, "Please provide a selfie photo.", Toast.LENGTH_SHORT).show()
                                return@clickable
                            }

                            isSubmitting = true
                            scope.launch {
                                val res = viewModel.repository.submitVerificationRequest(
                                    packageId = pkg.id,
                                    idFrontUrl = idFrontUrl.ifBlank { null },
                                    idBackUrl = idBackUrl.ifBlank { null },
                                    selfieUrl = selfieUrl.ifBlank { null }
                                )
                                isSubmitting = false
                                if (res.isSuccess) {
                                    Toast.makeText(context, "Verification request submitted! Awaiting admin approval.", Toast.LENGTH_LONG).show()
                                    onBack()
                                } else {
                                    Toast.makeText(context, res.exceptionOrNull()?.message ?: "Submission failed", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                        .testTag("submit_verification_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (hasSufficientBalance) "Pay ৳ ${String.format(Locale.getDefault(), "%.2f", selectedPrice)} & Get Badge" else "Deposit to Wallet",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
private fun CountdownBlock(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .shadow(2.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x33000000))
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFFB0BEC5),
            fontWeight = FontWeight.Medium
        )
    }
}
