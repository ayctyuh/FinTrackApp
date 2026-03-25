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
import com.fintrack.project.ui.screens.ProfileScreen
import com.fintrack.project.ui.screens.ForgotPasswordScreen
import com.fintrack.project.ui.screens.EditProfileScreen
import com.fintrack.project.ui.screens.LoginScreen
import com.fintrack.project.ui.screens.OnboardingScreen // Đừng quên import màn hình mới
import com.fintrack.project.ui.screens.SignupScreen
import com.fintrack.project.ui.screens.SplashScreen
import com.fintrack.project.ui.screens.WelcomeScreen
import com.fintrack.project.ui.theme.FinTrackProjectTheme
import com.fintrack.project.ui.screens.NotificationScreen
import com.fintrack.project.ui.screens.TransactionHistoryScreen

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

                                    // Lấy ID của người dùng vừa đăng nhập thành công
                                    val loggedInUserId = sharedPreferences.getInt("LOGGED_IN_USER_ID", -1)

                                    // Tạo khóa riêng biệt cho từng người dùng (VD: HAS_SEEN_ONBOARDING_1)
                                    val userOnboardingKey = "HAS_SEEN_ONBOARDING_$loggedInUserId"

                                    // Kiểm tra xem người dùng NÀY đã xem Onboarding chưa
                                    val hasSeenOnboarding = sharedPreferences.getBoolean(userOnboardingKey, false)

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

                        AppState.ONBOARDING -> {
                            OnboardingScreen(
                                onNextClick = {
                                    // Lấy ID user hiện tại
                                    val loggedInUserId = sharedPreferences.getInt("LOGGED_IN_USER_ID", -1)
                                    val userOnboardingKey = "HAS_SEEN_ONBOARDING_$loggedInUserId"

                                    // Lưu lại cờ là user này đã xem Onboarding
                                    sharedPreferences.edit().putBoolean(userOnboardingKey, true).apply()

                                    backStack.clear()
                                    appState = AppState.DASHBOARD
                                },
                                onBackClick = {
                                    // Nếu người dùng bấm Back ở màn Onboarding, ta cho họ đăng xuất luôn
                                    sharedPreferences.edit().remove("LOGGED_IN_USER_ID").apply()
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
                            DashboardScreen(
                                onNotificationClick = { navigateTo(AppState.NOTIFICATIONS) },
                                onProfileClick = { navigateTo(AppState.PROFILE) }, // Thêm dòng này
                                onSeeAllClick = { navigateTo(AppState.TRANSACTION_HISTORY) }
                            )
                        }

                        AppState.PROFILE -> {
                            ProfileScreen(
                                onNavigateToHome = { navigateTo(AppState.DASHBOARD) },
                                onNavigateToEdit = { navigateTo(AppState.EDIT_PROFILE) }, // Mở Edit Profile
                                onNavigateToSecurity = { /* Lát làm Security */ },
                                onLogout = {
                                    val sharedPreferences = getSharedPreferences("FinTrackPrefs", Context.MODE_PRIVATE)
                                    sharedPreferences.edit().remove("LOGGED_IN_USER_ID").apply()
                                    backStack.clear()
                                    appState = AppState.LOGIN
                                }
                            )
                        }

                        AppState.EDIT_PROFILE -> {
                            EditProfileScreen(
                                onBackClick = { navigateBack() }, // Quay lại Profile
                                onHomeClick = { navigateTo(AppState.DASHBOARD) } // Bấm Trang chủ ở NavBar
                            )
                        }
                        AppState.NOTIFICATIONS -> {
                            NotificationScreen(
                                onBackClick = { navigateBack() }
                            )
                        }
                        AppState.TRANSACTION_HISTORY -> {
                            TransactionHistoryScreen(
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

    NOTIFICATIONS,
    PROFILE,
    EDIT_PROFILE,
    TRANSACTION_HISTORY
}