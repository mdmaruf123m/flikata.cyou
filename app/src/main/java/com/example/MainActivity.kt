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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.example.ui.components.FlikataBottomNav
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.RegisterScreen
import com.example.ui.screens.chats.ChatDetailScreen
import com.example.ui.screens.chats.ChatsScreen
import com.example.ui.screens.createpost.CreatePostScreen
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
        enableEdgeToEdge()

        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()

            val isTransparentStatusBar = currentScreen == Screen.PROFILE || currentScreen == Screen.REELS || currentScreen == Screen.STORY_VIEWER || currentScreen == Screen.CREATE_STORY

            // Update status bar & nav bar icon colors according to theme
            WindowCompat.getInsetsController(window, window.decorView).apply {
                isAppearanceLightStatusBars = if (isTransparentStatusBar) false else !isDarkMode
                isAppearanceLightNavigationBars = !isDarkMode
            }
            window.statusBarColor = if (isTransparentStatusBar) {
                AndroidColor.TRANSPARENT
            } else if (isDarkMode) {
                AndroidColor.parseColor("#18191A")
            } else {
                AndroidColor.WHITE
            }
            window.navigationBarColor = if (isDarkMode) AndroidColor.parseColor("#18191A") else AndroidColor.WHITE

            MyApplicationTheme(darkTheme = isDarkMode) {
                MainAppContent(viewModel = viewModel)
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
                Screen.CHAT_DETAIL -> viewModel.navigateTo(Screen.CHATS)
                Screen.EDIT_PROFILE -> viewModel.navigateTo(Screen.PROFILE)
                Screen.SEARCH -> viewModel.navigateTo(Screen.HOME)
                Screen.CREATE_STORY -> viewModel.navigateTo(Screen.HOME)
                Screen.NOTIFICATIONS -> viewModel.navigateTo(Screen.HOME)
                else -> viewModel.navigateTo(Screen.HOME)
            }
        }
    }

    val showBottomBar = (currentScreen == Screen.HOME ||
            currentScreen == Screen.REELS ||
            currentScreen == Screen.CHATS ||
            currentScreen == Screen.PROFILE) && currentScreen != Screen.SEARCH && currentScreen != Screen.CREATE_STORY && currentScreen != Screen.NOTIFICATIONS

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
                    onNavigateToRegister = { viewModel.navigateTo(Screen.REGISTER) }
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
            FlikataBottomNav(
                currentScreen = currentScreen,
                onNavigate = { viewModel.navigateTo(it) },
                onCreatePostClick = { viewModel.navigateTo(Screen.CREATE_POST) },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
