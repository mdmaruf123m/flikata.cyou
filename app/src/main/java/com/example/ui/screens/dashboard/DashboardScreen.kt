package com.example.ui.screens.dashboard

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.example.data.model.CriterionDetail
import com.example.data.model.DashboardContentItem
import com.example.data.model.SubmitMonetizationData
import com.example.ui.components.SoftUITheme
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.util.Locale

data class TimePeriodOption(val id: String, val label: String)

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val dashboardData by viewModel.dashboardData.collectAsState()
    val isLoading by viewModel.isLoadingDashboard.collectAsState()
    val currentPeriod by viewModel.dashboardPeriod.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showSeeMoreModal by remember { mutableStateOf(false) }
    var thankYouStatementData by remember { mutableStateOf<SubmitMonetizationData?>(null) }
    var isSubmittingApp by remember { mutableStateOf(false) }

    val periods = remember {
        listOf(
            TimePeriodOption("24h", "24 Hours"),
            TimePeriodOption("48h", "48 Hours"),
            TimePeriodOption("3d", "3 Days"),
            TimePeriodOption("7d", "7 Days"),
            TimePeriodOption("30d", "1 Month"),
            TimePeriodOption("all", "Lifetime")
        )
    }

    LaunchedEffect(Unit) {
        viewModel.loadDashboard(currentPeriod)
    }

    val screenBgBrush = if (isDarkMode) {
        Brush.verticalGradient(listOf(Color(0xFF121418), Color(0xFF181A20)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFF4F8FD), Color(0xFFE8F1FC)))
    }
    val cardBg = if (isDarkMode) Color(0xFF242730) else Color.White
    val cardBorderBrush = if (isDarkMode) {
        Brush.verticalGradient(listOf(Color(0x33FFFFFF), Color(0x11FFFFFF)))
    } else {
        Brush.verticalGradient(listOf(Color(0xEEFFFFFF), Color(0x33A0C0E8)))
    }
    val headerBgBrush = if (isDarkMode) {
        Brush.verticalGradient(listOf(Color(0xFF242730), Color(0xFF1E2128), Color(0xFF16181E)))
    } else {
        Brush.verticalGradient(listOf(Color.White, Color(0xFFF3F7FD), Color(0xFFE8F1FC)))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBgBrush)
    ) {
        // 3D Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = if (isDarkMode) 2.dp else 1.dp,
                    shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                    ambientColor = if (isDarkMode) Color(0x33000000) else Color(0x0A000000),
                    spotColor = if (isDarkMode) Color(0x44000000) else Color(0x0C000000)
                )
                .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                .background(headerBgBrush)
                .border(
                    width = 1.dp,
                    brush = if (isDarkMode) Brush.verticalGradient(listOf(Color(0x33FFFFFF), Color(0x11FFFFFF))) else Brush.verticalGradient(listOf(Color(0xEEFFFFFF), Color(0x1A000000))),
                    shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .shadow(3.dp, CircleShape, ambientColor = SoftUITheme.ShadowDark)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color.White, Color(0xFFE8F1FC))))
                            .border(1.dp, Color(0xEEFFFFFF), CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onBack
                            )
                            .testTag("dashboard_back_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1E3A5F),
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "Dashboard",
                        style = TextStyle(
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFF0075FF), Color(0xFF0052D4))
                            ),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.2.sp
                        )
                    )
                }

                // Refresh Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .shadow(3.dp, CircleShape, ambientColor = SoftUITheme.ShadowDark)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color.White, Color(0xFFE8F1FC))))
                        .border(1.dp, Color(0xEEFFFFFF), CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { viewModel.loadDashboard(currentPeriod) }
                        )
                        .testTag("dashboard_refresh_button"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF0075FF)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = Color(0xFF1E3A5F),
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // ==========================================
            // 1. TOP TIME PERIOD SELECTOR (24h, 48h, 3d, 7d, 30d, Lifetime)
            // ==========================================
            Text(
                text = "Select Timeframe",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = SoftUITheme.TextSecondary,
                modifier = Modifier.padding(start = 2.dp, bottom = 8.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_period_selector")
            ) {
                items(periods) { option ->
                    val isSelected = currentPeriod == option.id
                    val pillBg = if (isSelected) {
                        Brush.horizontalGradient(listOf(Color(0xFF0075FF), Color(0xFF0052D4)))
                    } else {
                        Brush.verticalGradient(
                            if (isDarkMode) listOf(Color(0xFF242730), Color(0xFF1E2128))
                            else listOf(Color.White, Color(0xFFF3F7FD))
                        )
                    }
                    val textColor = if (isSelected) Color.White else SoftUITheme.TextDark

                    Box(
                        modifier = Modifier
                            .shadow(if (isSelected) 3.dp else 1.dp, RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .background(pillBg)
                            .border(
                                1.dp,
                                if (isSelected) Color.Transparent else if (isDarkMode) Color(0x22FFFFFF) else Color(0x33A0C0E8),
                                RoundedCornerShape(20.dp)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    if (currentPeriod != option.id) {
                                        viewModel.loadDashboard(option.id)
                                    }
                                }
                            )
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = option.label,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                            fontSize = 12.5.sp,
                            color = textColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ==========================================
            // 2. PERFORMANCE OVERVIEW SECTION
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Performance Overview",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = SoftUITheme.TextDark,
                    modifier = Modifier.padding(start = 2.dp)
                )

                Text(
                    text = periods.find { it.id == currentPeriod }?.label ?: "Last 7 Days",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0075FF)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            val perf = dashboardData?.performance
            val totalViews = perf?.totalViews ?: 0
            val engagement = perf?.engagement ?: 0
            val newFollowers = perf?.newFollowers ?: 0
            val totalPosts = perf?.totalPosts ?: 0
            val totalReels = perf?.totalReels ?: 0

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Row 1: Total Views & Total Engagement
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PerformanceStatCard(
                        icon = Icons.Default.Visibility,
                        title = "Total Views",
                        value = formatNumber(totalViews),
                        subtitle = "Reels & Post views",
                        iconBg = Color(0xFFE0F2FE),
                        iconTint = Color(0xFF0284C7),
                        modifier = Modifier.weight(1f)
                    )
                    PerformanceStatCard(
                        icon = Icons.Default.FavoriteBorder,
                        title = "Engagement",
                        value = formatNumber(engagement),
                        subtitle = "Likes, comments & shares",
                        iconBg = Color(0xFFFFEBEE),
                        iconTint = Color(0xFFE53935),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 2: New Followers & Total Posts
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PerformanceStatCard(
                        icon = Icons.Default.PersonAdd,
                        title = "New Followers",
                        value = "+$newFollowers",
                        subtitle = "Gained in timeframe",
                        iconBg = Color(0xFFE8F5E9),
                        iconTint = Color(0xFF2E7D32),
                        modifier = Modifier.weight(1f)
                    )
                    PerformanceStatCard(
                        icon = Icons.Default.Article,
                        title = "Total Posts",
                        value = "$totalPosts",
                        subtitle = "Published in timeframe",
                        iconBg = Color(0xFFEDE7F6),
                        iconTint = Color(0xFF673AB7),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 3: Total Reels & Audience Insights (Coming Soon)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PerformanceStatCard(
                        icon = Icons.Default.VideoLibrary,
                        title = "Total Reels",
                        value = "$totalReels",
                        subtitle = "Uploaded in timeframe",
                        iconBg = Color(0xFFFFF3E0),
                        iconTint = Color(0xFFE65100),
                        modifier = Modifier.weight(1f)
                    )
                    PerformanceStatCard(
                        icon = Icons.Default.Insights,
                        title = "Audience Reach",
                        value = "Insights",
                        subtitle = "Demographics & reach",
                        iconBg = Color(0xFFE0F7FA),
                        iconTint = Color(0xFF00897B),
                        badge = "Coming Soon",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ==========================================
            // 3. MONETIZATION SECTION
            // ==========================================
            val mon = dashboardData?.monetization
            val isMonetized = mon?.isMonetized == true
            val appStatus = mon?.applicationStatus ?: "none"
            val allCriteriaMet = mon?.allCriteriaMet == true
            val fulfilledCount = mon?.fulfilledCount ?: 0

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(22.dp), ambientColor = SoftUITheme.ShadowDark)
                    .clip(RoundedCornerShape(22.dp))
                    .background(cardBg)
                    .border(1.dp, cardBorderBrush, RoundedCornerShape(22.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFE8F5E9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MonetizationOn,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Monetization",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SoftUITheme.TextDark
                                )
                                Text(
                                    text = "Partner Program",
                                    fontSize = 12.sp,
                                    color = SoftUITheme.TextSecondary
                                )
                            }
                        }

                        // Status Badge
                        if (isMonetized) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFE8F5E9))
                                    .border(1.dp, Color(0xFF43A047), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Active Partner",
                                    color = Color(0xFF2E7D32),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        } else if (appStatus == "pending") {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFFFF3E0))
                                    .border(1.dp, Color(0xFFFB8C00), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Pending Review",
                                    color = Color(0xFFE65100),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // English Revenue Share Text (Exact as requested)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isDarkMode) Color(0xFF1E2838) else Color(0xFFEAF2FF)
                            )
                            .border(
                                1.dp,
                                if (isDarkMode) Color(0x440075FF) else Color(0x660075FF),
                                RoundedCornerShape(14.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = null,
                                tint = Color(0xFF0075FF),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Earn 55% revenue share from ad views on your content",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkMode) Color(0xFF90CAF9) else Color(0xFF0D47A1)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Requirements Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Eligibility Criteria",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp,
                            color = SoftUITheme.TextDark
                        )
                        Text(
                            text = "$fulfilledCount of 4 criteria met",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (allCriteriaMet) Color(0xFF2E7D32) else Color(0xFF0075FF)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // The 4 Criteria Rows: Followers, Posts, Reels, Account Age
                    val criteria = mon?.criteria
                    CriterionItemRow(
                        title = "Followers",
                        current = criteria?.followers?.current ?: 0,
                        required = criteria?.followers?.required ?: 1000,
                        isMet = criteria?.followers?.isMet == true,
                        unit = "followers"
                    )
                    CriterionItemRow(
                        title = "Total Posts",
                        current = criteria?.posts?.current ?: 0,
                        required = criteria?.posts?.required ?: 10,
                        isMet = criteria?.posts?.isMet == true,
                        unit = "posts"
                    )
                    CriterionItemRow(
                        title = "Reels Videos",
                        current = criteria?.reels?.current ?: 0,
                        required = criteria?.reels?.required ?: 5,
                        isMet = criteria?.reels?.isMet == true,
                        unit = "reels"
                    )
                    CriterionItemRow(
                        title = "Account Age",
                        current = criteria?.accountAge?.current ?: 0,
                        required = criteria?.accountAge?.required ?: 30,
                        isMet = criteria?.accountAge?.isMet == true,
                        unit = "days"
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Application Action Button
                    if (isMonetized) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFE8F5E9))
                                .padding(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Monetization Active • Partner Benefits Enabled",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    } else if (appStatus == "pending") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isDarkMode) Color(0xFF332715) else Color(0xFFFFF8E1))
                                .border(1.dp, Color(0xFFFFB300), RoundedCornerShape(14.dp))
                                .padding(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.HourglassTop,
                                    contentDescription = null,
                                    tint = Color(0xFFF57C00),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Application Under Review by Admin",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = Color(0xFFE65100)
                                )
                            }
                        }
                    } else {
                        // Submit Application Button (Grey when not met, vibrant Blue when fulfilled!)
                        val isButtonEnabled = allCriteriaMet && !isSubmittingApp
                        val buttonBgColor = if (allCriteriaMet) Color(0xFF0075FF) else Color(0xFFB0BEC5)

                        Button(
                            onClick = {
                                if (allCriteriaMet) {
                                    scope.launch {
                                        isSubmittingApp = true
                                        val res = viewModel.submitMonetization()
                                        isSubmittingApp = false
                                        if (res.isSuccess) {
                                            thankYouStatementData = res.getOrNull()
                                        } else {
                                            Toast.makeText(
                                                context,
                                                res.exceptionOrNull()?.message ?: "Application failed",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Please fulfill all 4 eligibility criteria first ($fulfilledCount/4 met)",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            enabled = !isSubmittingApp,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = buttonBgColor,
                                disabledContainerColor = Color(0xFFB0BEC5)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .shadow(if (allCriteriaMet) 4.dp else 0.dp, RoundedCornerShape(14.dp))
                                .testTag("monetization_submit_button")
                        ) {
                            if (isSubmittingApp) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = if (allCriteriaMet) "Submit Monetization Application" else "Submit Application (Criteria Incomplete)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ==========================================
            // 4. POST CONTENT / RECENT CONTENT SECTION
            // ==========================================
            val recentContent = dashboardData?.recentContent ?: emptyList()
            val allContent = dashboardData?.allContent ?: emptyList()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Content Management",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoftUITheme.TextDark
                    )
                    Text(
                        text = "Recent posts and reels performance",
                        fontSize = 12.sp,
                        color = SoftUITheme.TextSecondary
                    )
                }

                // "See More" Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDarkMode) Color(0xFF1E2838) else Color(0xFFEAF2FF))
                        .border(1.dp, Color(0x440075FF), RoundedCornerShape(12.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { showSeeMoreModal = true }
                        )
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                        .testTag("see_more_content_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "See More",
                        color = Color(0xFF0075FF),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (recentContent.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No content uploaded yet.",
                        color = SoftUITheme.TextSecondary,
                        fontSize = 13.5.sp
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    recentContent.forEach { item ->
                        RecentContentCard(item = item)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // ==========================================
    // 5. THANK YOU / STATEMENT MODAL
    // ==========================================
    if (thankYouStatementData != null) {
        val statement = thankYouStatementData!!
        Dialog(
            onDismissRequest = { thankYouStatementData = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.65f))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                        .background(cardBg)
                        .border(1.5.dp, cardBorderBrush, RoundedCornerShape(24.dp))
                        .padding(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE8F5E9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(38.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Application Submitted!",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = SoftUITheme.TextDark
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Thank you for applying to the Flikata Monetization Program. Your application is officially received and is currently under review by our administration.",
                            fontSize = 13.sp,
                            color = SoftUITheme.TextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Statement Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isDarkMode) Color(0xFF1B202A) else Color(0xFFF8FAFD))
                                .border(1.dp, if (isDarkMode) Color(0x33FFFFFF) else Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                StatementItemRow("Application ID", "#MNZ-${statement.applicationId}")
                                StatementItemRow("Applicant", statement.userName)
                                StatementItemRow("Followers Verified", "${statement.followersCount}")
                                StatementItemRow("Posts / Reels", "${statement.postsCount} posts • ${statement.reelsCount} reels")
                                StatementItemRow("Account Age", "${statement.accountAgeDays} days")
                                StatementItemRow("Submitted At", statement.submissionTime)
                                StatementItemRow("Status", "Pending Review (Admin)")
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { thankYouStatementData = null },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0075FF)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Text("Done", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // 6. "SEE MORE" FULL CONTENT MODAL (3 In a Row Grid with Views Count)
    // ==========================================
    if (showSeeMoreModal) {
        Dialog(
            onDismissRequest = { showSeeMoreModal = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .background(screenBgBrush)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(14.dp)
                ) {
                    // Modal Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "All Content (${dashboardData?.allContent?.size ?: 0})",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SoftUITheme.TextDark
                        )

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(cardBg)
                                .border(1.dp, cardBorderBrush, CircleShape)
                                .clickable { showSeeMoreModal = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = SoftUITheme.TextDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val allItems = dashboardData?.allContent ?: emptyList()
                    if (allItems.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No posts or reels found.", color = SoftUITheme.TextSecondary)
                        }
                    } else {
                        // 3 in a Row Grid (Taller cards with real views count)
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(allItems) { item ->
                                GridContentItem(item = item)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Performance Stat Card
 */
@Composable
private fun PerformanceStatCard(
    icon: ImageVector,
    title: String,
    value: String,
    subtitle: String,
    iconBg: Color,
    iconTint: Color,
    badge: String? = null,
    modifier: Modifier = Modifier
) {
    val isDark = SoftUITheme.isDark
    val cardBg = if (isDark) Color(0xFF242730) else Color.White
    val cardBorderBrush = if (isDark) {
        Brush.verticalGradient(listOf(Color(0x33FFFFFF), Color(0x11FFFFFF)))
    } else {
        Brush.verticalGradient(listOf(Color(0xEEFFFFFF), Color(0x33A0C0E8)))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(cardBg)
            .border(1.dp, cardBorderBrush, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (!badge.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isDark) Color(0xFF332A15) else Color(0xFFFFF8E1))
                            .border(0.8.dp, Color(0xFFFFB300), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badge,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF9800)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = SoftUITheme.TextDark
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 12.5.sp,
                color = SoftUITheme.TextDark
            )

            Text(
                text = subtitle,
                fontSize = 10.5.sp,
                color = SoftUITheme.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Criterion Item Row with Green Checkmark
 */
@Composable
private fun CriterionItemRow(
    title: String,
    current: Int,
    required: Int,
    isMet: Boolean,
    unit: String
) {
    val isDark = SoftUITheme.isDark
    val rowBg = if (isDark) Color(0xFF1E2128) else Color(0xFFF8FAFD)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(rowBg)
            .border(
                1.dp,
                if (isMet) Color(0x4443A047) else Color(0x229E9E9E),
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = SoftUITheme.TextDark
                )
                Text(
                    text = "Requires $required $unit",
                    fontSize = 11.sp,
                    color = SoftUITheme.TextSecondary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$current / $required",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (isMet) Color(0xFF2E7D32) else SoftUITheme.TextSecondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (isMet) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Met",
                        tint = Color(0xFF43A047),
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF333333) else Color(0xFFE0E0E0))
                    )
                }
            }
        }
    }
}

/**
 * Recent Content Card (Shows thumbnail, content, views, likes, comments, shares)
 */
@Composable
private fun RecentContentCard(item: DashboardContentItem) {
    val isDark = SoftUITheme.isDark
    val cardBg = if (isDark) Color(0xFF242730) else Color.White
    val cardBorder = if (isDark) Color(0x22FFFFFF) else Color(0x33A0C0E8)
    val context = LocalContext.current

    val mediaModel = remember(item.mediaUrl, item.thumbnailUrl) {
        val url = item.thumbnailUrl ?: item.mediaUrl ?: ""
        if (url.endsWith(".mp4", ignoreCase = true) || url.contains("/video", ignoreCase = true)) {
            ImageRequest.Builder(context)
                .data(url)
                .decoderFactory { result, options, _ -> VideoFrameDecoder(result.source, options) }
                .videoFrameMillis(1000)
                .crossfade(true)
                .build()
        } else {
            url
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Media Thumbnail
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E2128))
            ) {
                AsyncImage(
                    model = mediaModel,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                if (item.type == "reel") {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (item.type == "reel") Color(0xFFFFF3E0) else Color(0xFFE8F5E9)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.type.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (item.type == "reel") Color(0xFFE65100) else Color(0xFF2E7D32)
                        )
                    }

                    Text(
                        text = item.createdAt?.take(10) ?: "",
                        fontSize = 11.sp,
                        color = SoftUITheme.TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.content.ifBlank { "Post Update" },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SoftUITheme.TextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Stats: Total Views, Likes, Comments, Shares
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatPill(Icons.Default.Visibility, "${item.viewsCount}", Color(0xFF0284C7))
                    StatPill(Icons.Default.FavoriteBorder, "${item.likesCount}", Color(0xFFE53935))
                    StatPill(Icons.Default.ChatBubbleOutline, "${item.commentsCount}", Color(0xFF673AB7))
                    StatPill(Icons.Default.Share, "${item.sharesCount}", Color(0xFF2E7D32))
                }
            }
        }
    }
}

@Composable
private fun StatPill(icon: ImageVector, count: String, tint: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = count,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            color = SoftUITheme.TextDark
        )
    }
}

/**
 * Grid Content Item for "See More" (3 columns, taller, views count)
 */
@Composable
private fun GridContentItem(item: DashboardContentItem) {
    val context = LocalContext.current
    val mediaModel = remember(item.mediaUrl, item.thumbnailUrl) {
        val url = item.thumbnailUrl ?: item.mediaUrl ?: ""
        if (url.endsWith(".mp4", ignoreCase = true) || url.contains("/video", ignoreCase = true)) {
            ImageRequest.Builder(context)
                .data(url)
                .decoderFactory { result, options, _ -> VideoFrameDecoder(result.source, options) }
                .videoFrameMillis(1000)
                .crossfade(true)
                .build()
        } else {
            url
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.5625f) // 9:16 vertical ratio for taller cards
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF1E2128))
    ) {
        AsyncImage(
            model = mediaModel,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Type Badge
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(5.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Text(
                text = item.type.uppercase(),
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Bottom Views Bar
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                    )
                )
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Visibility,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = formatNumber(item.viewsCount),
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun StatementItemRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.5.sp,
            color = SoftUITheme.TextSecondary
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = SoftUITheme.TextDark
        )
    }
}

private fun formatNumber(count: Int): String {
    return if (count >= 1000) {
        String.format(Locale.US, "%.1fK", count / 1000.0)
    } else {
        "$count"
    }
}
