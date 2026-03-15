# FinTrack - Hướng Dẫn Chi Tiết Cho Từng Thành Viên

## 🎯 Phân Công Chi Tiết

---

## 1️⃣ Nguyễn Quang Huy - Biểu Đồ Thống Kê

### Công Việc Chính
- 3.1. Biểu đồ thu–chi tổng hợp
- 3.2. Biểu đồ theo danh mục
- 3.3. Biểu đồ xu hướng

### Resources Sẵn Có
- **ViewModel**: Có thể tạo `DashboardViewModel.kt` (extends BaseViewModel)
- **Database**: `TransactionRepository` có method:
  - `getTotalAmount(userId, type)` - Tổng thu/chi
  - `getTotalAmountByDateRange()` - Lọc theo khoảng thời gian
  - `getTotalAmountByCategory()` - Tính tổng theo danh mục
- **Utils**: `DateUtils.kt` có sẵn:
  - `getStartOfMonth()`, `getEndOfMonth()`
  - `getCurrentMonthYear()`
- **Chart Library**: MPAndroidChart (3.1.0) đã được add

### Các Screens Cần Tạo

#### a) Dashboard/Overview Screen
```
ui/statistics/DashboardScreen.kt
- Tổng thu/chi hôm nay
- Tổng thu/chi tháng này
- Tổng thu/chi năm nay
- Button để chuyển đến chi tiết biểu đồ
```

#### b) Overview Chart Screen
```
ui/statistics/OverviewChartScreen.kt
- Biểu đồ cột (BarChart) hoặc pie: Thu vs Chi
- Selector: Tuần/Tháng/Năm
- Hiển thị chênh lệch (Balance)
```

#### c) Category Chart Screen
```
ui/statistics/CategoryChartScreen.kt
- Biểu đồ Pie: Phân bổ chi theo danh mục
- Biểu đồ Pie: Phân bổ thu theo danh mục
- Tab switch: Chi/Thu
- Hiển thị % từng danh mục
```

#### d) Trend Chart Screen
```
ui/statistics/TrendChartScreen.kt
- Biểu đồ Line: Thu/Chi theo từng tháng
- X-axis: Tháng (Jan -> Dec)
- Y-axis: Số tiền
- 2 dòng: một cho thu (xanh), một cho chi (đỏ)
- Selector: Năm/6 tháng/Full year
```

### Implementation Steps

1. **Tạo ViewModel**:
```kotlin
// DashboardViewModel.kt
@file:Suppress("UNCHECKED_CAST")
class DashboardViewModel(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : BaseViewModel() {
    // State flows
    private val _totalIncome = MutableStateFlow(0.0)
    private val _totalExpense = MutableStateFlow(0.0)
    private val _categoryData = MutableStateFlow<Map<String, Double>>(emptyMap())
    
    // Methods
    fun calculateStats(userId: Int, startDate: Long, endDate: Long) { ... }
    fun getChartData(userId: Int, type: TransactionType) { ... }
}
```

2. **Tạo Screens**:
- Sử dụng Jetpack Compose
- Integrate MPAndroidChart hoặc Compose charts
- Get data từ ViewModel
- Update UI khi data thay đổi (using StateFlow)

3. **Data Calculation**:
- Query database qua Repository
- Group by category/date
- Calculate percentages
- Format numbers để hiển thị

### Testing
```bash
# Build
./gradlew build

# Run on device
./gradlew installDebug
```

---

## 2️⃣ Cao Đăng Khánh - Xác Thực & Ngân Sách

### Công Việc Chính
- 1.1. Đăng ký
- 1.2. Đăng nhập
- 2.5. Nhập liệu tự động
- 4.1. Thiết lập ngân sách

### Resources Sẵn Có
- **ViewModel**: `AuthViewModel.kt` - Xác thực logic đã sẵn
- **Repository**: `UserRepository.kt`, `BudgetRepository.kt`
- **Utils**: `SecurityUtils.kt` - Password hashing, validation
- **Models**: `User.kt`, `Budget.kt`
- **Strings**: `strings.xml` - Tất cả auth strings

### Các Screens Cần Tạo

#### a) Login Screen
```
ui/auth/LoginScreen.kt
- EditText: Username/Email
- EditText: Password (masked)
- Button: Login
- Text: Forgot Password?
- Text: Don't have account? Sign up
- Show loading indicator khi đang login
- Show error message nếu login fail
```

#### b) Signup Screen
```
ui/auth/SignupScreen.kt
- EditText: Username (validate: 3-20 chars, alphanumeric)
- EditText: Email (validate format)
- EditText: Password (validate: 8+ chars, uppercase, lowercase, digit)
- EditText: Confirm Password
- Button: Sign Up
- Link: Already have account? Login
- Show validation errors
```

#### c) Set Budget Screen
```
ui/budget/SetBudgetScreen.kt
- RadioButton: Monthly Total / By Category
- If monthly: Input amount
- If by category: Select category, input limit, set alert threshold
- Button: Save Budget
- Show existing budgets
```

#### d) Budget Management Screen
```
ui/budget/BudgetScreen.kt
- List of budgets for current month
- Each item shows: category, limit, spent, remaining %
- Button: Add New Budget
- Button: Edit/Delete
- Visual progress bar (color changes: green->orange->red)
```

### Implementation Steps

1. **Login Screen**:
```kotlin
// ui/auth/LoginScreen.kt
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    val isLoading by authViewModel.loading.collectAsState()
    val error by authViewModel.error.collectAsState()
    val loginSuccess by authViewModel.loginSuccess.collectAsState()
    
    // UI code...
    
    Button(onClick = {
        authViewModel.login(username, password)
    })
    
    LaunchedEffect(loginSuccess) {
        if (loginSuccess) onLoginSuccess()
    }
}
```

2. **Set Budget Screen**:
```kotlin
// ui/budget/SetBudgetScreen.kt
@Composable
fun SetBudgetScreen(
    budgetViewModel: BudgetViewModel,
    userId: Int
) {
    var selectedType by remember { mutableStateOf("monthly") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf<Category?>(null) }
    
    Button(onClick = {
        val budget = Budget(
            userId = userId,
            categoryId = if (selectedType == "monthly") null else category?.id,
            limitAmount = amount.toDouble(),
            month = currentMonth,
            year = currentYear
        )
        budgetViewModel.createBudget(budget)
    })
}
```

3. **Import Data Screen** (Advanced):
```kotlin
// ui/import/ImportDataScreen.kt
// Giả lập API call từ ngân hàng
// Preview data
// Let user select which transactions to import
// Import to database
```

### Database Operations
```kotlin
// Already available in repositories:
userRepository.insertUser(user)
userRepository.authenticateUser(username, hash)
budgetRepository.insertBudget(budget)
budgetRepository.getBudgetsByMonth(userId, month, year)
```

### Testing
```bash
# Test login with test user
# Test password validation
# Test budget calculation
```

---

## 3️⃣ Vũ Đức Mạnh - Quản Lý Giao Dịch

### Công Việc Chính
- 2.1. Thêm khoản thu/chi
- 2.2. Phân loại giao dịch
- 2.3. Xem lịch sử giao dịch
- 2.4. Tìm kiếm & sắp xếp

### Resources Sẵn Có
- **ViewModel**: `TransactionViewModel.kt` - Tất cả logic sẵn
- **Repository**: `TransactionRepository.kt` - 15+ query methods
- **DAO**: `TransactionDao.kt`, `CategoryDao.kt`
- **Models**: `Transaction.kt`, `Category.kt`
- **Utils**: `DateUtils.kt` - Date formatting

### Các Screens Cần Tạo

#### a) Transaction List Screen
```
ui/transaction/TransactionListScreen.kt
- List of transactions (newest first)
- Each item: Date | Category | Description | Amount ±Color
- Pull to refresh
- Floating action button: Add transaction
- Swipe to delete
- Tap to view details
- Filter button: Show filter sheet
```

#### b) Add/Edit Transaction Screen
```
ui/transaction/AddTransactionScreen.kt
- RadioButton: Income / Expense
- EditText: Amount
- DatePicker: Transaction date
- Dropdown: Category (with defaults)
- EditText: Description
- Button: Save
- Validation: Amount > 0, category selected
```

#### c) Transaction Detail Screen
```
ui/transaction/TransactionDetailScreen.kt
- Show all transaction info
- Button: Edit
- Button: Delete (with confirmation)
- Button: Back
```

#### d) Filter/Search Sheet
```
ui/transaction/TransactionFilterSheet.kt
- DateRange picker
- Category multi-select
- Sort by: Date (asc/desc), Amount (asc/desc)
- Button: Apply Filters
- Button: Clear Filters
```

### Implementation Steps

1. **Transaction List Screen**:
```kotlin
@Composable
fun TransactionListScreen(
    transactionViewModel: TransactionViewModel,
    userId: Int
) {
    val transactions by transactionViewModel.filteredTransactions.collectAsState()
    
    LaunchedEffect(userId) {
        transactionViewModel.getTransactions(userId)
    }
    
    LazyColumn {
        items(transactions) { txn ->
            TransactionItem(txn, onDelete = {
                transactionViewModel.deleteTransaction(it)
            })
        }
    }
}
```

2. **Add Transaction Screen**:
```kotlin
@Composable
fun AddTransactionScreen(
    transactionViewModel: TransactionViewModel,
    userId: Int,
    categoryRepository: CategoryRepository
) {
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var category by remember { mutableStateOf<Category?>(null) }
    var date by remember { mutableStateOf(System.currentTimeMillis()) }
    
    val categories by categoryRepository.getCategoriesByType(userId, type)
        .collectAsState(initial = emptyList())
    
    Button(onClick = {
        val transaction = Transaction(
            userId = userId,
            amount = amount.toDouble(),
            type = type,
            categoryId = category?.id,
            transactionDate = date
        )
        transactionViewModel.addTransaction(transaction)
    })
}
```

3. **Filter/Sort**:
```kotlin
// Buttons trong list screen
Button("Sort by Amount ↕") {
    transactionViewModel.sortByAmount(ascending = !isAscending)
}

Button("Filter") {
    showFilterSheet = true
}
```

### Database Operations
```kotlin
// All methods available in TransactionViewModel:
getTransactions(userId)
getTransactionsByDateRange(userId, start, end)
addTransaction(transaction)
updateTransaction(transaction)
deleteTransaction(transaction)
sortByAmount(ascending)
sortByDate(ascending)
```

### Default Categories
```kotlin
// In Category.kt - use these
DEFAULT_EXPENSE_CATEGORIES = listOf(
    "Ăn uống", "Giáo dục", "Y tế", "Giải trí",
    "Giao thông", "Utilities", "Mua sắm", "Khác"
)
DEFAULT_INCOME_CATEGORIES = listOf(
    "Lương", "Thưởng", "Kinh doanh", "Đầu tư", "Khác"
)
```

---

## 4️⃣ Đinh Quang Lâm - Báo Cáo & Cảnh Báo

### Công Việc Chính
- 3.4. Biểu đồ so sánh thu–chi
- 4.2. Theo dõi & cảnh báo ngân sách
- 4.3. Báo cáo ngân sách tháng

### Resources Sẵn Có
- **ViewModel**: `BudgetViewModel.kt` - Alert logic sẵn
- **Repository**: `BudgetRepository.kt`, `TransactionRepository.kt`
- **Models**: `Budget.kt`
- **Utils**: `DateUtils.kt`

### Các Screens Cần Tạo

#### a) Comparison Chart Screen
```
ui/statistics/ComparisonChartScreen.kt
- Select 2 periods to compare (month/year)
- Biểu đồ cột so sánh:
  - Income comparison
  - Expense comparison
  - Balance comparison
- % change indicator
```

#### b) Budget Tracking Screen
```
ui/budget/BudgetTrackingScreen.kt
- Show current month budget progress
- Progress bars for each category
- Color coding:
  - Green: < 70%
  - Orange: 70-90%
  - Red: > 90%
- Show alerts
```

#### c) Alert/Notification Screen
```
ui/budget/AlertScreen.kt
- List of budget alerts
- Time of alert
- Which budget was exceeded
- % exceeded
- Button: View budget
- Mark as read
```

#### d) Monthly Report Screen
```
ui/budget/MonthlyReportScreen.kt
- Select month/year
- Summary:
  - Total budget set
  - Total spent
  - Balance (remaining or exceeded)
- Breakdown by category:
  - Limit
  - Spent
  - % used
  - Status (On track / Near limit / Over budget)
- Export button (PDF/CSV)
```

### Implementation Steps

1. **Comparison Chart Screen**:
```kotlin
@Composable
fun ComparisonChartScreen(
    transactionRepository: TransactionRepository
) {
    var period1 by remember { mutableStateOf(Pair(currentMonth - 1, currentYear)) }
    var period2 by remember { mutableStateOf(Pair(currentMonth, currentYear)) }
    
    val data1 = getMonthData(period1)
    val data2 = getMonthData(period2)
    
    // Show bar chart comparing two periods
    BarChart(
        data = listOf(
            ChartEntry("Income", data1.income, data2.income),
            ChartEntry("Expense", data1.expense, data2.expense)
        )
    )
}
```

2. **Budget Alert Logic** (Already in BudgetViewModel):
```kotlin
// BudgetViewModel.kt has:
checkBudgetAlerts(userId, month, year)
// This returns _budgetAlerts StateFlow with list of alert messages
```

3. **Monthly Report Screen**:
```kotlin
@Composable
fun MonthlyReportScreen(
    budgetViewModel: BudgetViewModel,
    transactionRepository: TransactionRepository,
    userId: Int
) {
    val budgets by budgetViewModel.categoryBudgets.collectAsState()
    val alerts by budgetViewModel.budgetAlerts.collectAsState()
    
    LaunchedEffect(month, year) {
        budgetViewModel.getBudgetsByMonth(userId, month, year)
    }
    
    // Show summary
    // Show budget breakdown table
    // Color code each row
}
```

4. **Visual Indicators**:
```kotlin
// Progress bar with color based on %
fun BudgetProgressBar(spent: Double, limit: Double) {
    val percentage = (spent / limit) * 100
    val color = when {
        percentage < 70 -> Color.Green
        percentage < 90 -> Color.Yellow
        else -> Color.Red
    }
    LinearProgressIndicator(
        progress = percentage.toFloat() / 100,
        color = color
    )
}
```

### Database Queries
```kotlin
// Available methods:
budgetRepository.getBudgetsByMonth(userId, month, year)
transactionRepository.getTotalAmountByDateRange(userId, type, start, end)
transactionRepository.getTotalAmountByCategory(userId, catId, type, start, end)

// BudgetViewModel already has alert checking built-in
// Just call budgetViewModel.getBudgetsByMonth() to trigger alert check
```

---

## 📱 Navigation Setup (Common for All)

Cần tạo file:
```kotlin
// ui/navigation/AppNavigation.kt
@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    transactionViewModel: TransactionViewModel,
    budgetViewModel: BudgetViewModel
) {
    val navController = rememberNavController()
    
    NavHost(navController, startDestination = "login") {
        // Auth routes
        composable("login") { LoginScreen(authViewModel, onLoginSuccess = { ... }) }
        composable("signup") { SignupScreen(authViewModel, onSignupSuccess = { ... }) }
        
        // Main routes (after login)
        composable("dashboard") { DashboardScreen(...) }
        composable("transactions") { TransactionListScreen(...) }
        composable("add_transaction") { AddTransactionScreen(...) }
        // etc.
    }
}
```

Cập nhật MainActivity.kt:
```kotlin
// MainActivity.kt
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val authViewModel = AuthViewModel(ServiceLocator.getUserRepository())
        val transactionViewModel = TransactionViewModel(...)
        val budgetViewModel = BudgetViewModel(...)
        
        setContent {
            AppNavigation(authViewModel, transactionViewModel, budgetViewModel)
        }
    }
}
```

---

## 🛠️ Common Tasks

### Initialize ViewModel từ Screen
```kotlin
@Composable
fun SomeScreen() {
    val viewModel = remember {
        AuthViewModel(ServiceLocator.getUserRepository())
    }
    
    val state by viewModel.someState.collectAsState()
    
    // Use viewModel...
}
```

### Collect StateFlow in Compose
```kotlin
val data by viewModel.data.collectAsState(initial = emptyList())

// Or with LaunchedEffect
LaunchedEffect(Unit) {
    viewModel.loadData()
}
```

### Show Dialog/Toast
```kotlin
// Dialog
var showDialog by remember { mutableStateOf(false) }
if (showDialog) {
    AlertDialog(
        onDismissRequest = { showDialog = false },
        title = { Text("Confirm Delete") },
        confirmButton = { Button(onClick = { ... }) }
    )
}

// Snackbar (in Scaffold)
val snackbarHostState = remember { SnackbarHostState() }
Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) }
)
```

### Date Picker
```kotlin
// Use built-in Compose date picker
// Or use external library like accompanist/material-date-picker
```

---

## 🧪 Testing Database Queries

```bash
# SSH vào device/emulator
adb shell

# Access database
sqlite3 /data/data/com.fintrack.project/databases/fintrack_database

# Query
sqlite> SELECT * FROM users;
sqlite> SELECT * FROM transactions;
sqlite> SELECT * FROM budgets;
```

---

## ✅ Checklist

### For All
- [ ] Clone latest code
- [ ] Run `./gradlew clean build` successfully
- [ ] Understand MVVM architecture
- [ ] Understand how to use ViewModels & StateFlow

### Nguyễn Quang Huy
- [ ] Create DashboardViewModel
- [ ] Create 4 Chart Screens
- [ ] Test data display
- [ ] Verify calculations

### Cao Đăng Khánh
- [ ] Create Login/Signup Screens
- [ ] Create Budget screens
- [ ] Test auth flow
- [ ] Test budget CRUD

### Vũ Đức Mạnh
- [ ] Create Transaction screens
- [ ] Implement filtering/sorting
- [ ] Test add/edit/delete
- [ ] Test category selection

### Đinh Quang Lâm
- [ ] Create Comparison chart
- [ ] Create Alert screen
- [ ] Create Monthly report
- [ ] Test alert triggering

---

## 🤝 Communication

- Discuss issues in group chat
- Update shared documentation
- Test each other's features
- Merge features carefully

**Let's build an awesome app! 🚀**

