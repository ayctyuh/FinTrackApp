package com.fintrack.project

import android.content.Context
import android.os.Bundle
import android.widget.Toast
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
import com.fintrack.project.di.ServiceLocator
import com.fintrack.project.presentation.viewmodel.BudgetViewModel
import com.fintrack.project.presentation.viewmodel.TransactionViewModel
import com.fintrack.project.presentation.viewmodel.ChangePasswordViewModel
import com.fintrack.project.data.model.Category
import com.fintrack.project.data.database.FinTrackDatabase
import com.fintrack.project.data.model.CategoryType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sharedPreferences = getSharedPreferences("FinTrackPrefs", Context.MODE_PRIVATE)

        setContent {
            FinTrackProjectTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val budgetViewModel = remember {
                        BudgetViewModel(
                            ServiceLocator.getBudgetRepository(),
                            ServiceLocator.getTransactionRepository()
                        )
                    }
                    val transactionViewModel = remember {
                        TransactionViewModel(
                            ServiceLocator.getTransactionRepository(),
                            ServiceLocator.getCategoryRepository()
                        )
                    }
                    val categories = remember { mutableStateListOf<Category>() }

                    var appState by remember { mutableStateOf<AppState>(AppState.SPLASH) }
                    val backStack = remember { mutableStateListOf<AppState>() }

                    var selectedCategoryIdToEdit by remember { mutableIntStateOf(-1) }
                    var selectedTransactionIdToEdit by remember { mutableIntStateOf(-1) }

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
                            SignupScreen(
                                onSignupSuccess = { backStack.clear(); appState = AppState.LOGIN },
                                onLoginClick = { appState = AppState.LOGIN } // <--- GỌI onLoginClick ĐỂ VỀ LOGIN
                            )
                        }
                        AppState.FORGOT_PASSWORD -> {
                            ForgotPasswordScreen(
                                onLoginClick = { appState = AppState.LOGIN }, // <--- GỌI onLoginClick ĐỂ VỀ LOGIN
                                onSignupClick = { navigateTo(AppState.SIGNUP) }
                            )
                        }
                        AppState.DASHBOARD -> {
                            DashboardScreen(
                                onNotificationClick = { navigateTo(AppState.NOTIFICATIONS) },
                                onProfileClick = { navigateTo(AppState.PROFILE) },
                                onBudgetClick  = { navigateTo(AppState.BUDGET) },
                                onSeeAllClick = { navigateTo(AppState.TRANSACTION_HISTORY) },
                                onAddClick = { navigateTo(AppState.ADD_TRANSACTION) },
                                onStatisticsClick = { navigateTo(AppState.STATISTICS) },
                                onEditTransactionClick = { transactionId ->
                                    selectedTransactionIdToEdit = transactionId
                                    navigateTo(AppState.EDIT_TRANSACTION)
                                }
                            )
                        }
                        AppState.PROFILE -> {
                            ProfileScreen(
                                onNavigateToHome = { navigateTo(AppState.DASHBOARD) },
                                onNavigateToEdit = { navigateTo(AppState.EDIT_PROFILE) },
                                onNavigateToSecurity = { navigateTo(AppState.SECURITY) },
                                onNavigateToBudget = { navigateTo(AppState.BUDGET) },
                                onAddClick = { navigateTo(AppState.ADD_TRANSACTION) },
                                onLogout = {
                                    sharedPreferences.edit().remove("LOGGED_IN_USER_ID").apply()
                                    backStack.clear()
                                    appState = AppState.LOGIN
                                },
                                onNavigateToCategory = { navigateTo(AppState.CATEGORY_LIST) },
                                onStatisticsClick = { navigateTo(AppState.STATISTICS) },
                                onNavigateToChangePassword = { navigateTo(AppState.CHANGE_PASSWORD) }
                            )
                        }
                        AppState.CHANGE_PASSWORD -> {
                            val userId = sharedPreferences.getInt("LOGGED_IN_USER_ID", -1)
                            val db = FinTrackDatabase.getInstance(this)
                            val cpViewModel = remember { ChangePasswordViewModel(db.userDao()) }

                            ChangePasswordScreen(
                                onBackClick = { navigateBack() },
                                onConfirmClick = { oldPass, newPass -> // Tên này phải khớp với Screen ở Bước 1
                                    cpViewModel.changePassword(
                                        userId = userId,
                                        oldPass = oldPass,
                                        newPass = newPass,
                                        onSuccess = {
                                            Toast.makeText(this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show()
                                            navigateBack()
                                        },
                                        onError = { error ->
                                            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            )
                        }
                        AppState.EDIT_PROFILE -> {
                            EditProfileScreen(onBackClick = { navigateBack() }, onHomeClick = { navigateTo(AppState.DASHBOARD) }, onNavigateToBudget = {navigateTo(AppState.BUDGET)},
                                onNavigateToStatistics = { navigateTo(AppState.STATISTICS) }, onAddClick = { navigateTo(AppState.ADD_TRANSACTION) })
                        }
                        AppState.NOTIFICATIONS -> { NotificationScreen(onBackClick = { navigateBack() }) }
                        AppState.TRANSACTION_HISTORY -> { TransactionHistoryScreen(onBackClick = { navigateBack() }, onEditTransactionClick = { transactionId ->
                            selectedTransactionIdToEdit = transactionId
                            navigateTo(AppState.EDIT_TRANSACTION)
                        })
                        }
                        AppState.ADD_TRANSACTION -> {
                            AddTransactionScreen(onBackClick = { navigateBack() }, onHomeClick = { navigateTo(AppState.DASHBOARD) })
                        }
                        AppState.EDIT_TRANSACTION -> {
                            EditTransactionScreen(
                                transactionId = selectedTransactionIdToEdit,
                                onBackClick = { navigateBack() },
                                onSaveSuccess = { navigateBack() }
                            )
                        }
                        AppState.SECURITY -> {
                            SecurityScreen(onBackClick = { navigateBack() }, onHomeClick = { navigateTo(AppState.DASHBOARD) }, onNavigateToPinSetup = { navigateTo(AppState.PIN_SETUP) }, onNavigateToTerms = { navigateTo(AppState.TERMS_OF_SERVICE) }, onNavigateToBudget = { navigateTo(AppState.BUDGET)}, onNavigateToStatistics = { navigateTo(AppState.STATISTICS)}, onAddClick = { navigateTo(AppState.ADD_TRANSACTION) })
                        }
                        AppState.TERMS_OF_SERVICE -> { TermsOfServiceScreen(onBackClick = { navigateBack() }) }
                        AppState.PIN_SETUP -> {
                            PinSetupScreen(
                                onBackClick = { navigateBack() },
                                onNavigateToHome = { navigateTo(AppState.DASHBOARD) },
                                onAddClick = { navigateTo(AppState.ADD_TRANSACTION) },
                                onNavigateToProfile = { navigateTo(AppState.PROFILE) },
                                onNavigateToBudget = { navigateTo(AppState.BUDGET) },
                                onNavigateToStatistics = { navigateTo(AppState.STATISTICS) },
                                onPinSaved = { navigateBack() }
                            )
                        }
                        AppState.STATISTICS -> {
                            StatisticsScreen(onNavigateToHome = { navigateTo(AppState.DASHBOARD) }, onNavigateToProfile = { navigateTo(AppState.PROFILE) }, onNavigateToBudget = { navigateTo(AppState.BUDGET) }, onAddClick = { navigateTo(AppState.ADD_TRANSACTION) }, onNavigateToReport = { navigateTo(AppState.MONTHLY_REPORT)})
                        }
                        AppState.BUDGET -> {
                            val userId = sharedPreferences.getInt("LOGGED_IN_USER_ID", -1)
                            LaunchedEffect(userId, budgetViewModel.budgets.collectAsState().value) {
                                if (userId != -1) {
                                    withContext(Dispatchers.IO) {
                                        val loaded = ServiceLocator.getCategoryRepository().getCategoriesByType(userId, CategoryType.EXPENSE)
                                        withContext(Dispatchers.Main) {
                                            categories.clear()
                                            categories.addAll(loaded)
                                        }
                                    }
                                }
                            }

                            BudgetScreen(
                                userId          = userId,
                                viewModel       = budgetViewModel,
                                spentByCategory = budgetViewModel.spentByCategoryMonth.collectAsState().value,
                                categories      = categories,
                                onNavigateTo    = { index ->
                                    when (index) {
                                        0 -> navigateTo(AppState.DASHBOARD)
                                        1 -> navigateTo(AppState.STATISTICS)
                                        2 -> navigateTo(AppState.ADD_TRANSACTION)
                                        4 -> navigateTo(AppState.PROFILE)
                                    }
                                },
                                onSeeReportClick = { navigateTo(AppState.MONTHLY_REPORT) }
                            )
                        }
                        AppState.MONTHLY_REPORT -> {
                            val userId = sharedPreferences.getInt("LOGGED_IN_USER_ID", -1)
                            
                            LaunchedEffect(userId) {
                                if (userId != -1) {
                                    withContext(Dispatchers.IO) {
                                        val loaded = ServiceLocator.getCategoryRepository().getCategoriesByType(userId, CategoryType.EXPENSE)
                                        withContext(Dispatchers.Main) {
                                            categories.clear()
                                            categories.addAll(loaded)
                                        }
                                    }
                                }
                            }

                            MonthlyReportScreen(
                                userId = userId,
                                viewModel = budgetViewModel,
                                categories = categories,
                                onBackClick = { navigateBack() }
                            )
                        }

                        AppState.CATEGORY_LIST -> {
                            CategoryScreen(
                                onBackClick = { navigateBack() }, onHomeClick = { navigateTo(AppState.DASHBOARD) }, onAddClick = { navigateTo(AppState.ADD_TRANSACTION) }, onNavigateToAddCategory = { navigateTo(AppState.ADD_CATEGORY) },
                                onCategoryClick = { categoryId ->
                                    selectedCategoryIdToEdit = categoryId
                                    navigateTo(AppState.EDIT_CATEGORY)
                                },
                                onNavigateToBudget = { navigateTo(AppState.BUDGET) },
                                onNavigateToStatistics = { navigateTo(AppState.STATISTICS) },
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
    TRANSACTION_HISTORY, ADD_TRANSACTION, STATISTICS, BUDGET, MONTHLY_REPORT,
    CATEGORY_LIST, ADD_CATEGORY, EDIT_CATEGORY, EDIT_TRANSACTION, CHANGE_PASSWORD
}
