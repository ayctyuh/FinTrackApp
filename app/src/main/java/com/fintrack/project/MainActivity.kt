package com.fintrack.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.fintrack.project.ui.screens.DashboardScreen
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
                    var appState by remember {
                        mutableStateOf<AppState>(AppState.SPLASH)
                    }

                    when (appState) {
                        AppState.SPLASH -> {
                            SplashScreen(
                                onSplashComplete = { appState = AppState.WELCOME }
                            )
                        }
                        AppState.WELCOME -> {
                            WelcomeScreen(
                                onLoginClick = { appState = AppState.LOGIN },
                                onSignupClick = { appState = AppState.SIGNUP },
                                onForgotPasswordClick = { appState = AppState.FORGOT_PASSWORD }
                            )
                        }
                        AppState.LOGIN -> {
                            LoginScreen(
                                onLoginSuccess = { appState = AppState.DASHBOARD },
                                onSignupClick = { appState = AppState.SIGNUP }
                            )
                        }
                        AppState.SIGNUP -> {
                            SignupScreen(
                                onSignupSuccess = { appState = AppState.LOGIN },
                                onBackClick = { appState = AppState.LOGIN }
                            )
                        }
                        AppState.FORGOT_PASSWORD -> {
                            SignupScreen(
                                onSignupSuccess = { appState = AppState.LOGIN },
                                onBackClick = { appState = AppState.LOGIN }
                            )
                        }
                        AppState.DASHBOARD -> {
                            DashboardScreen(
                                onLogout = { appState = AppState.LOGIN }
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
