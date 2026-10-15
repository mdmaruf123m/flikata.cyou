package com.example.ui.screens.wallet

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.data.model.WithdrawMethod
import com.example.ui.components.SoftUITheme
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WithdrawScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isDark = SoftUITheme.isDark
    val currentUser by viewModel.currentUser.collectAsState()

    var amountInput by remember { mutableStateOf("500") }
    var selectedMethod by remember { mutableStateOf<WithdrawMethod?>(null) }
    var accountNameInput by remember { mutableStateOf(currentUser?.fullName ?: "") }
    var accountNumberInput by remember { mutableStateOf("") }
    var methodsList by remember { mutableStateOf<List<WithdrawMethod>>(emptyList()) }
    var isLoadingMethods by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }
    var availableBalance by remember { mutableStateOf(0.0) }
    var isSuccessStep by remember { mutableStateOf(false) }
    var submissionTimestamp by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val walletRes = viewModel.repository.getWallet()
        var loaded: List<WithdrawMethod>? = null
        if (walletRes.isSuccess) {
            val wd = walletRes.getOrNull()
            availableBalance = wd?.walletBalance ?: 0.0
            val wm = wd?.withdrawMethods
            if (!wm.isNullOrEmpty()) {
                loaded = wm.filter { it.isActive == 1 }
            }
        }
        if (loaded.isNullOrEmpty()) {
            val res = viewModel.repository.getAdminWithdrawMethods()
            if (res.isSuccess) {
                loaded = (res.getOrNull() ?: emptyList()).filter { it.isActive == 1 }
            }
        }
        methodsList = loaded ?: emptyList()
        if (methodsList.isNotEmpty()) {
            selectedMethod = methodsList.firstOrNull()
        }
        isLoadingMethods = false
    }

    val screenBg = if (isDark) {
        Brush.verticalGradient(listOf(Color(0xFF0F1115), Color(0xFF161920), Color(0xFF0A0C10)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFF4F8FD), Color(0xFFE8F1FC), Color(0xFFDFEAF7)))
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
        if (isSuccessStep) {
            // ==========================================
            // SUCCESS / THANK YOU STATEMENT PAGE
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .shadow(10.dp, CircleShape, ambientColor = Color(0x6645BD62))
                        .clip(CircleShape)
                        .background(Color(0xFF45BD62)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Success",
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Thank You!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = SoftUITheme.TextDark
                )

                Text(
                    text = "Your withdrawal request has been received and is being processed.",
                    fontSize = 13.5.sp,
                    color = SoftUITheme.TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Detailed Statement Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, RoundedCornerShape(22.dp), ambientColor = Color(0x22000000))
                        .clip(RoundedCornerShape(22.dp))
                        .background(cardBg)
                        .border(1.2.dp, cardBorder, RoundedCornerShape(22.dp))
                        .padding(22.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Withdrawal Statement",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftUITheme.TextDark
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Color(0xFFFEF3C7))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "PENDING APPROVAL",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD97706)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Large Amount Row
                        Text(
                            text = "Amount to Withdraw",
                            fontSize = 12.sp,
                            color = SoftUITheme.TextSecondary
                        )
                        Text(
                            text = "৳ ${String.format(Locale.getDefault(), "%.2f", amountInput.toDoubleOrNull() ?: 0.0)}",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1570EF)
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(cardBorder)
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Statement Items
                        StatementRowItem(label = "Payment Method", value = selectedMethod?.name ?: "Online Transfer")
                        Spacer(modifier = Modifier.height(10.dp))
                        StatementRowItem(label = "Account Name", value = accountNameInput)
                        Spacer(modifier = Modifier.height(10.dp))
                        StatementRowItem(label = "Account / Mobile No", value = accountNumberInput)
                        Spacer(modifier = Modifier.height(10.dp))
                        StatementRowItem(label = "Date & Time", value = submissionTimestamp)
                        Spacer(modifier = Modifier.height(10.dp))
                        StatementRowItem(label = "Status", value = "Pending Admin Review")

                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDark) Color(0xFF252B36) else Color(0xFFF1F5F9))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Notice: Funds will be sent directly to your account. You can check the status anytime in your Wallet Transaction History.",
                                fontSize = 11.5.sp,
                                color = SoftUITheme.TextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .shadow(4.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0866FF))
                        .clickable { onBack() }
                        .testTag("withdraw_done_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Back to Wallet",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }
        } else {
            // ==========================================
            // MAIN WITHDRAW FORM
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("withdraw_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SoftUITheme.TextDark
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Withdraw Funds",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoftUITheme.TextDark
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 1. AMOUNT SECTION
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(3.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(cardBg)
                        .border(1.2.dp, cardBorder, RoundedCornerShape(20.dp))
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Enter Withdrawal Amount (BDT)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftUITheme.TextDark
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Color(0xFFE0F2FE))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Balance: ৳ ${String.format(Locale.getDefault(), "%.2f", availableBalance)}",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0369A1)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = amountInput,
                            onValueChange = { amountInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
                            leadingIcon = {
                                Text(
                                    text = "৳",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0866FF),
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                            },
                            placeholder = { Text("500") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("withdraw_amount_input")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick Amount Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("100", "300", "500", "1000").forEach { preset ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .shadow(1.dp, RoundedCornerShape(12.dp))
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (amountInput == preset) Color(0xFF0866FF) else if (isDark) Color(0xFF2C3240) else Color(0xFFF0F4FA))
                                        .clickable { amountInput = preset }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "৳ $preset",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (amountInput == preset) Color.White else SoftUITheme.TextDark
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 2. PAYMENT METHOD SELECTION
                Text(
                    text = "Select Withdrawal Method",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = SoftUITheme.TextDark,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                if (isLoadingMethods) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF0866FF), modifier = Modifier.size(32.dp))
                    }
                } else if (methodsList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(18.dp))
                            .clip(RoundedCornerShape(18.dp))
                            .background(cardBg)
                            .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No withdrawal methods configured by admin yet.",
                            fontSize = 13.sp,
                            color = SoftUITheme.TextSecondary
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        methodsList.forEach { m ->
                            val isSelected = selectedMethod?.id == m.id
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
                                    .clickable { selectedMethod = m }
                                    .padding(14.dp)
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
                                                .size(22.dp)
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
                                                        .size(10.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFF0866FF))
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        WithdrawMethodLogoBadge(method = m, modifier = Modifier.size(42.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = m.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = SoftUITheme.TextDark
                                            )
                                            Text(
                                                text = "Min ৳ ${m.minAmount.toInt()} - Max ৳ ${m.maxAmount.toInt()}",
                                                fontSize = 12.sp,
                                                color = SoftUITheme.TextSecondary
                                            )
                                        }
                                    }

                                    Icon(
                                        imageVector = Icons.Default.Payment,
                                        contentDescription = null,
                                        tint = if (isSelected) Color(0xFF0866FF) else SoftUITheme.TextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 3. ACCOUNT DETAILS SECTION
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(3.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(cardBg)
                        .border(1.2.dp, cardBorder, RoundedCornerShape(20.dp))
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = "Account Holder & Number",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftUITheme.TextDark
                        )

                        Column {
                            Text(
                                text = "Your Full Name (Account Name)",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SoftUITheme.TextDark,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            OutlinedTextField(
                                value = accountNameInput,
                                onValueChange = { accountNameInput = it },
                                placeholder = { Text("Enter your account holder name") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("withdraw_account_name_input")
                            )
                        }

                        Column {
                            Text(
                                text = "Account / Mobile Number",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SoftUITheme.TextDark,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            OutlinedTextField(
                                value = accountNumberInput,
                                onValueChange = { accountNumberInput = it },
                                placeholder = { Text("e.g. 017XXXXXXXX or Bank A/C") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("withdraw_account_number_input")
                            )
                        }

                        if (!selectedMethod?.description.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isDark) Color(0xFF262C38) else Color(0xFFF8FAFC))
                                    .border(1.dp, cardBorder, RoundedCornerShape(10.dp))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = "Instructions: ${selectedMethod?.description}",
                                    fontSize = 12.sp,
                                    color = SoftUITheme.TextSecondary,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // SUBMIT BUTTON
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .shadow(4.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSubmitting) Color(0xFF93C5FD) else Color(0xFF0866FF))
                        .clickable(enabled = !isSubmitting) {
                            val amt = amountInput.toDoubleOrNull() ?: 0.0
                            if (amt <= 0) {
                                Toast.makeText(context, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                                return@clickable
                            }
                            if (amt > availableBalance) {
                                Toast.makeText(context, "Insufficient balance! Available: ৳ ${String.format(Locale.getDefault(), "%.2f", availableBalance)}", Toast.LENGTH_SHORT).show()
                                return@clickable
                            }
                            val min = selectedMethod?.minAmount ?: 50.0
                            if (amt < min) {
                                Toast.makeText(context, "Minimum withdrawal is ৳ ${min.toInt()}", Toast.LENGTH_SHORT).show()
                                return@clickable
                            }
                            if (accountNameInput.isBlank()) {
                                Toast.makeText(context, "Please enter your account name", Toast.LENGTH_SHORT).show()
                                return@clickable
                            }
                            if (accountNumberInput.isBlank()) {
                                Toast.makeText(context, "Please enter your account number", Toast.LENGTH_SHORT).show()
                                return@clickable
                            }

                            isSubmitting = true
                            scope.launch {
                                val res = viewModel.repository.submitWithdraw(
                                    methodId = selectedMethod?.id ?: 0,
                                    methodName = selectedMethod?.name ?: "Online Transfer",
                                    amount = amt,
                                    accountName = accountNameInput.trim(),
                                    accountNumber = accountNumberInput.trim()
                                )
                                isSubmitting = false
                                if (res.isSuccess) {
                                    val now = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
                                    submissionTimestamp = now
                                    isSuccessStep = true
                                } else {
                                    Toast.makeText(context, res.exceptionOrNull()?.message ?: "Withdrawal request failed", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                        .testTag("submit_withdraw_btn"),
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
                                text = "Submit Withdrawal Request",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}

@Composable
fun StatementRowItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = SoftUITheme.TextSecondary
        )
        Text(
            text = value,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold,
            color = SoftUITheme.TextDark,
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun WithdrawMethodLogoBadge(
    method: WithdrawMethod,
    modifier: Modifier = Modifier.size(42.dp)
) {
    val cleanUrl = resolveMethodImageUrl(method.imageUrl)
    val nameLower = method.name.lowercase(Locale.getDefault())

    val (badgeBg, badgeText, badgeTextColor) = when {
        nameLower.contains("bkash") -> Triple(Color(0xFFE2136E), "bKash", Color.White)
        nameLower.contains("nagad") -> Triple(Color(0xFFF7941D), "Nagad", Color.White)
        nameLower.contains("rocket") -> Triple(Color(0xFF8C3494), "Rocket", Color.White)
        nameLower.contains("upay") -> Triple(Color(0xFF002A54), "Upay", Color(0xFFFFD200))
        nameLower.contains("cellfin") -> Triple(Color(0xFF0B6330), "Cellfin", Color.White)
        nameLower.contains("bank") -> Triple(Color(0xFF1E88E5), "Bank", Color.White)
        else -> Triple(Color(0xFF0866FF), method.name.take(4).uppercase(), Color.White)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .border(0.8.dp, Color(0x22000000), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (!cleanUrl.isNullOrBlank()) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(cleanUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = method.name,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(3.dp),
                contentScale = ContentScale.Fit,
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                            .background(badgeBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = badgeTextColor
                        )
                    }
                },
                error = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                            .background(badgeBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = badgeTextColor
                        )
                    }
                }
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(badgeBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badgeText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = badgeTextColor
                )
            }
        }
    }
}
