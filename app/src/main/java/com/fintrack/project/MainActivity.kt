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
import com.fintrack.project.ui.screens.*
import com.fintrack.project.ui.theme.FinTrackProjectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sharedPreferences = getSharedPreferences("FinTrackPrefs", Context.MODE_PRIVATE)

        setContent {
            FinTrackProjectTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var appState by remember { mutableStateOf<AppState>(AppState.SPLASH) }
                    val backStack = remember { mutableStateListOf<AppState>() }

                    var selectedCategoryIdToEdit by remember { mutableIntStateOf(-1) }

                    fun navigateTo(next: AppState) {
                        backStack.add(appState)
                        appState = next
                    }

                    fun navigateBack() {
                        if (backStack.isNotEmpty()) {
                            appState = backStack.removeAt(backStack.lastIndex)
                        }
                    }

                    BackHandler(enabled = backStack.isNotEmpty()) { navigateBack() }

                    when (appState) {
                        AppState.SPLASH -> {
                            SplashScreen(onSplashComplete = { backStack.clear(); appState = AppState.WELCOME })
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
                                    backStack.clear()
                                    val loggedInUserId = sharedPreferences.getInt("LOGGED_IN_USER_ID", -1)
                                    val userOnboardingKey = "HAS_SEEN_ONBOARDING_$loggedInUserId"
                                    val hasSeenOnboarding = sharedPreferences.getBoolean(userOnboardingKey, false)
                                    if (!hasSeenOnboarding) appState = AppState.ONBOARDING else appState = AppState.DASHBOARD
                                },
                                onSignupClick = { navigateTo(AppState.SIGNUP) },
                                onForgotPasswordClick = { navigateTo(AppState.FORGOT_PASSWORD) }
                            )
                        }
                        AppState.ONBOARDING -> {
                            OnboardingScreen(
                                onNextClick = {
                                    val loggedInUserId = sharedPreferences.getInt("LOGGED_IN_USER_ID", -1)
                                    val userOnboardingKey = "HAS_SEEN_ONBOARDING_$loggedInUserId"
                                    sharedPreferences.edit().putBoolean(userOnboardingKey, true).apply()
                                    backStack.clear(); appState = AppState.DASHBOARD
                                },
                                onBackClick = {
                                    sharedPreferences.edit().remove("LOGGED_IN_USER_ID").apply()
                                    appState = AppState.LOGIN
                                }
                            )
                        }
                        AppState.SIGNUP -> {
                            SignupScreen(onSignupSuccess = { backStack.clear(); appState = AppState.LOGIN }, onBackClick = { navigateBack() })
                        }
                        AppState.FORGOT_PASSWORD -> {
                            ForgotPasswordScreen(onBackClick = { navigateBack() }, onSignupClick = { navigateTo(AppState.SIGNUP) })
                        }
                        AppState.DASHBOARD -> {
                            DashboardScreen(
                                onNotificationClick = { navigateTo(AppState.NOTIFICATIONS) },
                                onProfileClick = { navigateTo(AppState.PROFILE) },
                                onSeeAllClick = { navigateTo(AppState.TRANSACTION_HISTORY) },
                                onAddClick = { navigateTo(AppState.ADD_TRANSACTION) },
                                onStatisticsClick = { navigateTo(AppState.STATISTICS) }
                            )
                        }
                        AppState.PROFILE -> {
                            ProfileScreen(
                                onNavigateToHome = { navigateTo(AppState.DASHBOARD) },
                                onNavigateToEdit = { navigateTo(AppState.EDIT_PROFILE) },
                                onNavigateToSecurity = { navigateTo(AppState.SECURITY) },
                                onAddClick = { navigateTo(AppState.ADD_TRANSACTION) },
                                onLogout = {
                                    sharedPreferences.edit().remove("LOGGED_IN_USER_ID").apply()
                                    backStack.clear()
                                    appState = AppState.LOGIN
                                },
                                onNavigateToCategory = { navigateTo(AppState.CATEGORY_LIST) },
                                onStatisticsClick = { navigateTo(AppState.STATISTICS) } // ĐÃ FIX LỖI THIẾU THAM SỐ
                            )
                        }
                        AppState.EDIT_PROFILE -> {
                            EditProfileScreen(onBackClick = { navigateBack() }, onHomeClick = { navigateTo(AppState.DASHBOARD) }, onAddClick = { navigateTo(AppState.ADD_TRANSACTION) })
                        }
                        AppState.NOTIFICATIONS -> { NotificationScreen(onBackClick = { navigateBack() }) }
                        AppState.TRANSACTION_HISTORY -> { TransactionHistoryScreen(onBackClick = { navigateBack() }) }
                        AppState.ADD_TRANSACTION -> {
                            AddTransactionScreen(onBackClick = { navigateBack() }, onHomeClick = { navigateTo(AppState.DASHBOARD) })
                        }
                        AppState.SECURITY -> {
                            SecurityScreen(onBackClick = { navigateBack() }, onHomeClick = { navigateTo(AppState.DASHBOARD) }, onNavigateToPinSetup = { navigateTo(AppState.PIN_SETUP) }, onNavigateToTerms = { navigateTo(AppState.TERMS_OF_SERVICE) }, onAddClick = { navigateTo(AppState.ADD_TRANSACTION) })
                        }
                        AppState.TERMS_OF_SERVICE -> { TermsOfServiceScreen(onBackClick = { navigateBack() }) }
                        AppState.PIN_SETUP -> { PinSetupScreen(onBackClick = { navigateBack() }, onPinSaved = { navigateBack() }) }
                        AppState.STATISTICS -> {
                            StatisticsScreen(onNavigateToHome = { navigateTo(AppState.DASHBOARD) }, onNavigateToProfile = { navigateTo(AppState.PROFILE) }, onAddClick = { navigateTo(AppState.ADD_TRANSACTION) })
                        }
                        AppState.CATEGORY_LIST -> {
                            CategoryScreen(
                                onBackClick = { navigateBack() }, onHomeClick = { navigateTo(AppState.DASHBOARD) }, onAddClick = { navigateTo(AppState.ADD_TRANSACTION) }, onNavigateToAddCategory = { navigateTo(AppState.ADD_CATEGORY) },
                                onCategoryClick = { categoryId ->
                                    selectedCategoryIdToEdit = categoryId
                                    navigateTo(AppState.EDIT_CATEGORY)
                                }
                            )
                        }
                        AppState.ADD_CATEGORY -> { AddCategoryScreen(onBackClick = { navigateBack() }) }
                        AppState.EDIT_CATEGORY -> { EditCategoryScreen(categoryId = selectedCategoryIdToEdit, onBackClick = { navigateBack() }) }
                    }
                }
            }
        }
    }
}

enum class AppState {
    SPLASH, WELCOME, LOGIN, SIGNUP, FORGOT_PASSWORD, ONBOARDING, DASHBOARD,
    NOTIFICATIONS, PROFILE, EDIT_PROFILE, SECURITY, PIN_SETUP, TERMS_OF_SERVICE,
    TRANSACTION_HISTORY, ADD_TRANSACTION, STATISTICS,
    CATEGORY_LIST, ADD_CATEGORY, EDIT_CATEGORY
}