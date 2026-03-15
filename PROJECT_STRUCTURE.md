# FinTrack - Ứng Dụng Quản Lý Tài Chính Cá Nhân

## Cấu Trúc Dự Án MVVM

```
com/fintrack/project/
├── FinTrackApp.kt                    # Application class với Hilt support
├── MainActivity.kt                    # Main Activity
│
├── data/                              # Data Layer
│   ├── model/                         # Entity models
│   │   ├── User.kt                    # User entity
│   │   ├── Category.kt                # Category entity
│   │   ├── Transaction.kt             # Transaction entity
│   │   └── Budget.kt                  # Budget entity
│   ├── dao/                           # Data Access Objects
│   │   ├── UserDao.kt
│   │   ├── CategoryDao.kt
│   │   ├── TransactionDao.kt
│   │   └── BudgetDao.kt
│   ├── database/                      # Database configuration
│   │   └── FinTrackDatabase.kt        # Room database
│   └── repository/                    # Repository pattern
│       ├── UserRepository.kt
│       ├── CategoryRepository.kt
│       ├── TransactionRepository.kt
│       └── BudgetRepository.kt
│
├── presentation/                      # Presentation Layer (UI)
│   ├── ui/                            # Screens/Composables
│   │   ├── auth/                      # Authentication screens
│   │   │   ├── LoginScreen.kt
│   │   │   └── SignupScreen.kt
│   │   ├── transaction/               # Transaction screens
│   │   │   ├── TransactionListScreen.kt
│   │   │   ├── AddTransactionScreen.kt
│   │   │   └── TransactionDetailScreen.kt
│   │   ├── category/                  # Category screens
│   │   │   └── CategoryManagementScreen.kt
│   │   ├── statistics/                # Statistics screens
│   │   │   ├── DashboardScreen.kt
│   │   │   ├── OverviewChartScreen.kt
│   │   │   ├── CategoryChartScreen.kt
│   │   │   ├── TrendChartScreen.kt
│   │   │   └── ComparisonChartScreen.kt
│   │   ├── budget/                    # Budget screens
│   │   │   ├── BudgetScreen.kt
│   │   │   ├── SetBudgetScreen.kt
│   │   │   ├── BudgetReportScreen.kt
│   │   │   └── BudgetAlertScreen.kt
│   │   ├── import/                    # Import/Export screens
│   │   │   ├── ImportDataScreen.kt
│   │   │   └── ExportDataScreen.kt
│   │   └── settings/                  # Settings screens
│   │       └── SettingsScreen.kt
│   ├── viewmodel/                     # ViewModels
│   │   ├── BaseViewModel.kt           # Base ViewModel with common logic
│   │   ├── AuthViewModel.kt           # Auth logic
│   │   ├── TransactionViewModel.kt    # Transaction management
│   │   └── BudgetViewModel.kt         # Budget management
│   └── navigation/                    # Navigation
│       └── AppNavigation.kt           # Navigation graph
│
├── di/                                # Dependency Injection (Hilt)
│   └── AppModule.kt                   # Hilt modules
│
└── utils/                             # Utilities
    ├── Constants.kt                   # App constants
    ├── DateUtils.kt                   # Date utilities
    └── SecurityUtils.kt               # Security utilities (hashing, validation)
```

## Dependencies Được Cài Đặt

### Core
- **androidx.core:core-ktx** - Kotlin extensions for Android
- **androidx.lifecycle:lifecycle-runtime-ktx** - Lifecycle support
- **androidx.activity:activity-compose** - Activity Compose integration

### Compose UI
- **androidx.compose:compose-bom** - Compose Bill of Materials
- **androidx.compose.ui:ui** - Core Compose UI
- **androidx.compose.material3:material3** - Material Design 3

### Database
- **androidx.room:room-runtime** - Room database
- **androidx.room:room-ktx** - Room coroutines support
- **androidx.room:room-compiler** - KSP annotation processor

### Dependency Injection
- **com.google.dagger:hilt-android** - Hilt DI
- **androidx.hilt:hilt-navigation-compose** - Hilt Compose integration

### Navigation
- **androidx.navigation:navigation-compose** - Compose navigation

### Networking
- **com.squareup.retrofit2:retrofit** - Retrofit API client
- **com.squareup.okhttp3:okhttp** - OkHttp client
- **com.squareup.okhttp3:logging-interceptor** - HTTP logging

### JSON Serialization
- **com.google.code.gson:gson** - GSON JSON parser
- **com.squareup.moshi:moshi-kotlin** - Moshi JSON parser

### Coroutines
- **org.jetbrains.kotlinx:kotlinx-coroutines-android** - Coroutines Android support
- **org.jetbrains.kotlinx:kotlinx-coroutines-core** - Core coroutines

### Data Storage
- **androidx.datastore:datastore-preferences** - DataStore for preferences

### Charts
- **com.github.PhilJay:MPAndroidChart** - Chart library for statistics

### Logging
- **com.jakewharton.timber:timber** - Timber logging library

## Các Tính Năng Chính

### 1. Xác Thực Người Dùng
- Đăng ký tài khoản mới
- Đăng nhập với username/email
- Xác thực mật khẩu an toàn (SHA-256)
- Validation input

### 2. Quản Lý Giao Dịch
- Thêm/chỉnh sửa/xóa giao dịch
- Phân loại giao dịch (thu/chi)
- Gán danh mục
- Tìm kiếm theo ngày, danh mục, số tiền
- Sắp xếp theo nhiều tiêu chí

### 3. Quản Lý Danh Mục
- Danh mục mặc định được khởi tạo tự động
- Tạo danh mục tùy chỉnh
- Phân biệt danh mục thu và chi

### 4. Thống Kê
- Biểu đồ tổng hợp thu/chi
- Biểu đồ phân bổ theo danh mục
- Biểu đồ xu hướng theo thời gian
- Biểu đồ so sánh khoảng thời gian

### 5. Quản Lý Ngân Sách
- Thiết lập ngân sách tổng tháng
- Thiết lập ngân sách theo danh mục
- Cảnh báo khi gần vượt/vượt ngân sách
- Báo cáo ngân sách chi tiết

### 6. Nhập/Xuất Dữ Liệu
- Nhập giao dịch từ ngân hàng/dịch vụ thanh toán (demo)
- Xem trước trước khi nhập
- Xuất dữ liệu CSV

## Database Schema

### Users Table
- id (PK)
- username (UNIQUE)
- email (UNIQUE)
- phoneNumber
- passwordHash
- createdAt
- updatedAt

### Categories Table
- id (PK)
- userId (FK)
- name
- icon
- color
- type (INCOME/EXPENSE)
- isDefault
- createdAt
- updatedAt

### Transactions Table
- id (PK)
- userId (FK)
- categoryId (FK)
- amount
- type (INCOME/EXPENSE)
- description
- transactionDate
- createdAt
- updatedAt
- sourceBank

### Budgets Table
- id (PK)
- userId (FK)
- categoryId (FK, nullable)
- limitAmount
- month
- year
- createdAt
- updatedAt
- alertThreshold

## Phân Công Công Việc

1. **Nguyễn Quang Huy** - Biểu đồ thống kê
   - Biểu đồ tổng hợp thu/chi
   - Biểu đồ theo danh mục
   - Biểu đồ xu hướng

2. **Cao Đăng Khánh** - Xác thực & Ngân sách
   - Đăng ký/Đăng nhập
   - Nhập liệu tự động
   - Thiết lập ngân sách

3. **Vũ Đức Mạnh** - Quản lý giao dịch
   - Thêm/chỉnh sửa giao dịch
   - Phân loại giao dịch
   - Xem lịch sử
   - Tìm kiếm & sắp xếp

4. **Đinh Quang Lâm** - Báo cáo & Cảnh báo
   - Biểu đồ so sánh
   - Theo dõi & cảnh báo ngân sách
   - Báo cáo ngân sách tháng

## Hướng Dẫn Phát Triển

### Chạy Ứng Dụng
```bash
./gradlew assembleDebug
./gradlew installDebug
```

### Build Release
```bash
./gradlew assembleRelease
```

### Chạy Tests
```bash
./gradlew test
./gradlew connectedAndroidTest
```

### Code Style
- Sử dụng Kotlin coding conventions
- Format code: Ctrl+Alt+L (Android Studio)
- Lint checks: Analyze > Run Inspection by Name > Android Lint

## Notes
- Dữ liệu được lưu trữ cục bộ trong SQLite (Room)
- Tất cả thao tác database sử dụng coroutines
- UI được xây dựng với Jetpack Compose
- Dependency Injection sử dụng Hilt

