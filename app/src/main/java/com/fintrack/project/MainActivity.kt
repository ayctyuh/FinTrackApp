package com.fintrack.project

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.fintrack.project.ui.screens.DashboardScreen
import com.fintrack.project.ui.screens.ForgotPasswordScreen
import com.fintrack.project.ui.screens.LoginScreen
import com.fintrack.project.ui.screens.OnboardingScreen // Đừng quên import màn hình mới
import com.fintrack.project.ui.screens.SignupScreen
import com.fintrack.project.ui.screens.SplashScreen
import com.fintrack.project.ui.screens.WelcomeScreen
import com.fintrack.project.ui.theme.FinTrackProjectTheme
import com.fintrack.project.ui.screens.NotificationScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Khởi tạo SharedPreferences để lưu cờ kiểm tra lần đầu đăng nhập
        val sharedPreferences = getSharedPreferences("FinTrackPrefs", Context.MODE_PRIVATE)

        setContent {
            FinTrackProjectTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var appState by remember { mutableStateOf<AppState>(AppState.SPLASH) }

                    // Stack lịch sử điều hướng
                    val backStack = remember { mutableStateListOf<AppState>() }

                    // Hàm điều hướng — tự động lưu lịch sử
                    fun navigateTo(next: AppState) {
                        backStack.add(appState)
                        appState = next
                    }

                    // Hàm quay lại
                    fun navigateBack() {
                        if (backStack.isNotEmpty()) {
                            appState = backStack.removeAt(backStack.lastIndex) // <-- Đã sửa
                        }
                    }

                    // Bật back khi có lịch sử
                    BackHandler(enabled = backStack.isNotEmpty()) {
                        navigateBack()
                    }

                    when (appState) {
                        AppState.SPLASH -> {
                            SplashScreen(
                                onSplashComplete = {
                                    // Splash không lưu vào stack
                                    backStack.clear()
                                    appState = AppState.WELCOME
                                }
                            )
                        }
                        AppState.WELCOME -> {
                            WelcomeScreen(
                                onLoginClick = { navigateTo(AppState.LOGIN) },
                                onSignupClick = { navigateTo(AppState.SIGNUP) },
                                onForgotPasswordClick = { navigateTo(AppState.FORGOT_PASSWORD) }
                            )
                        }
                        AppState.LOGIN -> {
                            LoginScreen(
                                onLoginSuccess = {
                                    backStack.clear() // Xóa lịch sử sau khi đăng nhập

                                    // Kiểm tra xem user đã xem Onboarding chưa
                                    val hasSeenOnboarding = sharedPreferences.getBoolean("HAS_SEEN_ONBOARDING", false)

                                    if (!hasSeenOnboarding) {
                                        appState = AppState.ONBOARDING
                                    } else {
                                        appState = AppState.DASHBOARD
                                    }
                                },
                                onSignupClick = { navigateTo(AppState.SIGNUP) },
                                onForgotPasswordClick = { navigateTo(AppState.FORGOT_PASSWORD) }
                            )
                        }
                        // Thêm logic cho màn hình Onboarding
                        AppState.ONBOARDING -> {
                            OnboardingScreen(
                                onNextClick = {
                                    // Lưu lại cờ là đã xem Onboarding
                                    sharedPreferences.edit().putBoolean("HAS_SEEN_ONBOARDING", true).apply()
                                    backStack.clear()
                                    appState = AppState.DASHBOARD
                                },
                                onBackClick = {
                                    // Có thể cho họ quay lại Login nếu muốn
                                    appState = AppState.LOGIN
                                }
                            )
                        }
                        AppState.SIGNUP -> {
                            SignupScreen(
                                onSignupSuccess = {
                                    // Đăng ký xong → về Login, xóa stack
                                    backStack.clear()
                                    appState = AppState.LOGIN
                                },
                                onBackClick = { navigateBack() }
                            )
                        }
                        AppState.FORGOT_PASSWORD -> {
                            ForgotPasswordScreen(
                                onBackClick = { navigateBack() },
                                onSignupClick = { navigateTo(AppState.SIGNUP) }
                            )
                        }
                        AppState.DASHBOARD -> {
                            BackHandler(enabled = true) { /* Chặn back */ }
                            DashboardScreen(
                                onLogout = {
                                    backStack.clear()
                                    appState = AppState.LOGIN
                                },
                                onNotificationClick = { // Bấm cái chuông thì gọi lệnh này
                                    navigateTo(AppState.NOTIFICATIONS)
                                }
                            )
                        }
                        AppState.NOTIFICATIONS -> {
                            NotificationScreen(
                                onBackClick = { navigateBack() }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Cập nhật Enum để thêm ONBOARDING
enum class AppState {
    SPLASH,
    WELCOME,
    LOGIN,
    SIGNUP,
    FORGOT_PASSWORD,
    ONBOARDING, // <-- Thêm dòng này
    DASHBOARD,

    NOTIFICATIONS
}