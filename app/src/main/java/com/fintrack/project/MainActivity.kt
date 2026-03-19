package com.fintrack.project

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
import com.fintrack.project.ui.screens.SignupScreen
import com.fintrack.project.ui.screens.SplashScreen
import com.fintrack.project.ui.screens.WelcomeScreen
import com.fintrack.project.ui.theme.FinTrackProjectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
                            appState = backStack.removeLast()
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
                                    // Đăng nhập thành công → xóa stack, vào Dashboard
                                    backStack.clear()
                                    appState = AppState.DASHBOARD
                                },
                                onSignupClick = { navigateTo(AppState.SIGNUP) },
                                onForgotPasswordClick = { navigateTo(AppState.FORGOT_PASSWORD) }
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
                            // Không back khỏi Dashboard
                            BackHandler(enabled = true) { /* Chặn back */ }
                            DashboardScreen(
                                onLogout = {
                                    backStack.clear()
                                    appState = AppState.LOGIN
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

enum class AppState {
    SPLASH,
    WELCOME,
    LOGIN,
    SIGNUP,
    FORGOT_PASSWORD,
    DASHBOARD
}