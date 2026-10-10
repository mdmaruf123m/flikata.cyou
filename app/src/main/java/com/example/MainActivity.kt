package com.example

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.example.ui.components.FlikataBottomNav
import com.example.ui.screens.auth.ForgotPasswordScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.RegisterScreen
import com.example.ui.screens.chats.ChatDetailScreen
import com.example.ui.screens.chats.ChatsScreen
import com.example.ui.screens.createpost.CreatePostScreen
import com.example.ui.screens.friends.FriendsScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.menu.MenuScreen
import com.example.ui.screens.notifications.NotificationScreen
import com.example.ui.screens.profile.EditProfileScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.reels.ReelsScreen
import com.example.ui.screens.search.SearchScreen
import com.example.ui.screens.splash.SplashScreen
import com.example.ui.screens.story.CreateStoryScreen
import com.example.ui.screens.story.StoryViewerScreen
import com.example.ui.screens.viewer.FullImageViewerScreen
import com.example.ui.theme.FlikataBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleDeepLink(intent)
        try {
            enableEdgeToEdge()
        } catch (e: Throwable) {
            android.util.Log.w("MainActivity", "enableEdgeToEdge not fully supported on this OS: ${e.message}")
        }

        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()

            val isTransparentStatusBar = currentScreen == Screen.PROFILE || currentScreen == Screen.REELS || currentScreen == Screen.STORY_VIEWER || currentScreen == Screen.CREATE_STORY

            // Update status bar & nav bar icon colors according to theme safely
            try {
                val controller = WindowCompat.getInsetsController(window, window.decorView)
                controller.isAppearanceLightStatusBars = if (isTransparentStatusBar) false else !isDarkMode
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    controller.isAppearanceLightNavigationBars = !isDarkMode
                    window.navigationBarColor = if (isDarkMode) AndroidColor.parseColor("#18191A") else AndroidColor.WHITE
                } else {
                    window.navigationBarColor = AndroidColor.BLACK
                }
                window.statusBarColor = if (isTransparentStatusBar) {
                    AndroidColor.TRANSPARENT
                } else if (isDarkMode) {
                    AndroidColor.parseColor("#18191A")
                } else {
                    AndroidColor.WHITE
                }
            } catch (e: Throwable) {
                android.util.Log.w("MainActivity", "Window decor adjustment error: ${e.message}")
            }

            MyApplicationTheme(darkTheme = isDarkMode) {
                MainAppContent(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: android.content.Intent?) {
        val data = intent?.data ?: return
        val path = data.path ?: ""
        val host = data.host ?: ""

        val idFromQuery = data.getQueryParameter("id")
            ?: data.getQueryParameter("post_id")
            ?: data.getQueryParameter("reel_id")
            ?: data.getQueryParameter("user_id")

        val pathSegments = data.pathSegments ?: emptyList()
        val idFromPath = if (pathSegments.size >= 2) pathSegments[1] else if (pathSegments.size == 1 && (host == "post" || host == "reel" || host == "profile" || host == "user")) pathSegments[0] else null
        val targetId = idFromQuery ?: idFromPath ?: ""

        if (host == "reel" || path.startsWith("/reel") || data.getQueryParameter("reel_id") != null) {
            if (targetId.isNotBlank()) {
                viewModel.openReel(targetId)
            } else {
                viewModel.navigateTo(Screen.REELS)
            }
        } else if (host == "post" || path.startsWith("/post") || data.getQueryParameter("post_id") != null || (host == "flikata.cyou" && path.contains("post.php"))) {
            if (targetId.isNotBlank()) {
                viewModel.openPost(targetId)
            } else {
                viewModel.navigateTo(Screen.HOME)
            }
        } else if (host == "profile" || host == "user" || path.startsWith("/profile") || path.startsWith("/user") || (host == "flikata.cyou" && path.contains("profile.php"))) {
            if (targetId.isNotBlank()) {
                viewModel.openUserProfile(targetId)
            } else {
                viewModel.navigateTo(Screen.PROFILE)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val fullScreenImageUrl by viewModel.fullScreenImageUrl.collectAsState()
    val activeStoryIndex by viewModel.activeStoryIndex.collectAsState()
    val stories by viewModel.stories.collectAsState()

    // Handle back button behavior
    if (currentScreen != Screen.HOME && currentScreen != Screen.LOGIN && currentScreen != Screen.SPLASH) {
        BackHandler {
            when (currentScreen) {
                Screen.REGISTER -> viewModel.navigateTo(Screen.LOGIN)
                Screen.FORGOT_PASSWORD -> viewModel.navigateTo(Screen.LOGIN)
                Screen.CHAT_DETAIL -> viewModel.navigateTo(Screen.CHATS)
                Screen.EDIT_PROFILE -> viewModel.navigateTo(Screen.PROFILE)
                Screen.SEARCH -> viewModel.navigateTo(Screen.HOME)
                Screen.CREATE_STORY -> viewModel.navigateTo(Screen.HOME)
                Screen.NOTIFICATIONS -> viewModel.navigateTo(Screen.HOME)
                Screen.FRIENDS -> viewModel.navigateTo(Screen.HOME)
                else -> viewModel.navigateTo(Screen.HOME)
            }
        }
    }

    val showBottomBar = (currentScreen == Screen.HOME ||
            currentScreen == Screen.REELS ||
            currentScreen == Screen.CHATS ||
            currentScreen == Screen.PROFILE) && currentScreen != Screen.SEARCH && currentScreen != Screen.CREATE_STORY && currentScreen != Screen.NOTIFICATIONS && currentScreen != Screen.FRIENDS

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
    ) {
        // Screen Content filling full height
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when (currentScreen) {
                Screen.SPLASH -> SplashScreen(
                    viewModel = viewModel
                )

                Screen.LOGIN -> LoginScreen(
                    viewModel = viewModel,
                    onNavigateToRegister = { viewModel.navigateTo(Screen.REGISTER) },
                    onNavigateToForgotPassword = { viewModel.navigateTo(Screen.FORGOT_PASSWORD) }
                )

                Screen.FORGOT_PASSWORD -> ForgotPasswordScreen(
                    viewModel = viewModel,
                    onNavigateBack = { viewModel.navigateTo(Screen.LOGIN) }
                )

                Screen.REGISTER -> RegisterScreen(
                    viewModel = viewModel,
                    onNavigateBack = { viewModel.navigateTo(Screen.LOGIN) }
                )

                Screen.HOME -> HomeScreen(
                    viewModel = viewModel,
                    onCreatePostClick = { viewModel.navigateTo(Screen.CREATE_POST) },
                    onMenuClick = { viewModel.navigateTo(Screen.MENU) },
                    onCreateStoryClick = { viewModel.navigateTo(Screen.CREATE_STORY) }
                )

                Screen.REELS -> ReelsScreen(
                    viewModel = viewModel
                )

                Screen.CREATE_POST -> CreatePostScreen(
                    viewModel = viewModel,
                    onClose = { viewModel.navigateTo(Screen.HOME) }
                )

                Screen.CREATE_STORY -> CreateStoryScreen(
                    viewModel = viewModel,
                    onClose = { viewModel.navigateTo(Screen.HOME) }
                )

                Screen.CHATS -> ChatsScreen(
                    viewModel = viewModel,
                    onMenuClick = { viewModel.navigateTo(Screen.MENU) }
                )

                Screen.CHAT_DETAIL -> ChatDetailScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(Screen.CHATS) }
                )

                Screen.PROFILE -> ProfileScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(Screen.HOME) },
                    onSearchClick = { viewModel.navigateTo(Screen.SEARCH) },
                    onCreatePostClick = { viewModel.navigateTo(Screen.CREATE_POST) },
                    onCreateStoryClick = { viewModel.navigateTo(Screen.CREATE_STORY) }
                )

                Screen.EDIT_PROFILE -> EditProfileScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(Screen.PROFILE) }
                )

                Screen.SEARCH -> SearchScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(Screen.HOME) }
                )

                Screen.NOTIFICATIONS -> NotificationScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(Screen.HOME) }
                )

                Screen.MENU -> MenuScreen(
                    viewModel = viewModel,
                    onProfileClick = { viewModel.navigateTo(Screen.PROFILE) }
                )

                Screen.FRIENDS -> FriendsScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(Screen.HOME) }
                )

                Screen.STORY_VIEWER -> StoryViewerScreen(
                    stories = stories,
                    initialIndex = activeStoryIndex,
                    onClose = { viewModel.navigateTo(Screen.HOME) }
                )

                Screen.FULL_IMAGE_VIEWER -> FullImageViewerScreen(
                    imageUrl = fullScreenImageUrl,
                    onClose = { viewModel.navigateTo(Screen.HOME) }
                )
            }
        }

        // Floating Bottom Navigation Bar
        if (showBottomBar) {
            val currentUser by viewModel.currentUser.collectAsState()
            val chats by viewModel.chats.collectAsState()
            val unreadCount = remember(chats) { chats.sumOf { it.unreadCount } }
            FlikataBottomNav(
                currentScreen = currentScreen,
                onNavigate = { viewModel.navigateTo(it) },
                onCreatePostClick = { viewModel.navigateTo(Screen.CREATE_POST) },
                userAvatarUrl = currentUser?.profilePic,
                unreadChatsCount = unreadCount,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
