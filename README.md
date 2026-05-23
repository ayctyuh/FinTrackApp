# FinTrack – Ứng Dụng Quản Lý Tài Chính Cá Nhân

## Thông Tin Môn Học

| Thông tin | Chi tiết |
|----------|---------|
| **Tên môn học** | Phát Triển Ứng Dụng Cho Các Thiết Bị Di Động |
| **Mã môn** | INT1449 |
| **Giảng viên hướng dẫn** | Ths. Nguyễn Hoàng Anh |
| **Nhóm lớp (QLĐT)** | 02 |
| **Nhóm BTL** | 01 |
| **Năm học** | 2025–2026 |

## Thành Viên Nhóm

| STT | Họ và Tên | MSSV | Nhiệm vụ |
|-----|----------|------|----------|
| 1 | Nguyễn Quang Huy | B22DCAT143 | Biểu đồ thu–chi tổng hợp, biểu đồ theo danh mục, biểu đồ xu hướng |
| 2 | Cao Đăng Khánh | B22DCAT... | Xác thực (đăng ký/đăng nhập), nhập liệu tự động, thiết lập ngân sách |
| 3 | Vũ Đức Mạnh | B22DCAT... | Quản lý giao dịch, phân loại, xem lịch sử, tìm kiếm & sắp xếp |
| 4 | Đinh Quang Lâm | B22DCAT... | Biểu đồ so sánh thu–chi, theo dõi & cảnh báo ngân sách, báo cáo tháng |

---

## Giới Thiệu Ứng Dụng

**FinTrack** là ứng dụng Android quản lý tài chính cá nhân, giúp người dùng theo dõi thu nhập – chi tiêu, phân tích xu hướng tài chính và kiểm soát ngân sách hàng tháng. Ứng dụng hoạt động hoàn toàn **offline**, dữ liệu lưu trữ cục bộ trên thiết bị người dùng.

### Các Chức Năng Chính

| Nhóm | Chức năng |
|------|----------|
| **Người dùng** | Đăng ký, đăng nhập, chỉnh sửa hồ sơ, đổi mật khẩu, đặt mã PIN |
| **Giao dịch** | Thêm/sửa/xóa giao dịch thu/chi, phân loại theo danh mục, xem lịch sử, tìm kiếm & lọc, nhập liệu tự động từ thông báo ngân hàng |
| **Thống kê** | Biểu đồ thu–chi tổng hợp (cột), biểu đồ theo danh mục (thanh ngang), biểu đồ xu hướng (đường), biểu đồ so sánh |
| **Ngân sách** | Thiết lập ngân sách tháng/theo danh mục, theo dõi mức sử dụng, cảnh báo khi vượt ngưỡng, báo cáo tháng |

---

## Công Nghệ Sử Dụng

### Ngôn ngữ & Nền tảng
- **Kotlin** – ngôn ngữ lập trình chính (first-class language cho Android)
- **Android SDK** – minSdk 24 (Android 7.0), targetSdk 36

### UI
- **Jetpack Compose** – UI framework khai báo hiện đại của Google
- **Material Design 3** – design system
- **Canvas API (Compose)** – vẽ biểu đồ tùy chỉnh (không dùng thư viện chart ngoài)

### Kiến trúc
- **MVVM** (Model–View–ViewModel)
- **Repository Pattern** – tầng trung gian giữa ViewModel và data source
- **Kotlin Coroutines + Dispatchers.IO** – xử lý bất đồng bộ

### Cơ sở dữ liệu
- **Room Database** (SQLite) – ORM chính thức của Google
- Gồm 5 bảng: `users`, `categories`, `transactions`, `budgets`, `notifications`

### Thư viện khác
- **Retrofit + OkHttp** – HTTP client (dùng cho tính năng nhập liệu)
- **Gson / Moshi** – JSON parsing
- **DataStore Preferences** – lưu cài đặt và session
- **Timber** – logging
- **Navigation Compose** – điều hướng màn hình

---

## Cấu Trúc Dự Án

```
FinTrackApp/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/fintrack/project/
│   │   │   │   ├── data/
│   │   │   │   │   ├── dao/                   # Data Access Objects (Room queries)
│   │   │   │   │   │   ├── TransactionDao.kt
│   │   │   │   │   │   ├── CategoryDao.kt
│   │   │   │   │   │   ├── BudgetDao.kt
│   │   │   │   │   │   ├── UserDao.kt
│   │   │   │   │   │   └── NotificationDao.kt
│   │   │   │   │   ├── database/
│   │   │   │   │   │   └── FinTrackDatabase.kt # Room database singleton
│   │   │   │   │   ├── model/                 # Entity classes
│   │   │   │   │   │   ├── User.kt
│   │   │   │   │   │   ├── Transaction.kt
│   │   │   │   │   │   ├── Category.kt
│   │   │   │   │   │   ├── Budget.kt
│   │   │   │   │   │   └── Notification.kt
│   │   │   │   │   └── repository/            # Repository pattern
│   │   │   │   │       ├── UserRepository.kt
│   │   │   │   │       ├── TransactionRepository.kt
│   │   │   │   │       ├── CategoryRepository.kt
│   │   │   │   │       ├── BudgetRepository.kt
│   │   │   │   │       └── NotificationRepository.kt
│   │   │   │   ├── presentation/
│   │   │   │   │   └── viewmodel/             # ViewModels
│   │   │   │   │       ├── AuthViewModel.kt
│   │   │   │   │       ├── TransactionViewModel.kt
│   │   │   │   │       ├── BudgetViewModel.kt
│   │   │   │   │       ├── ChangePasswordViewModel.kt
│   │   │   │   │       └── BaseViewModel.kt
│   │   │   │   ├── ui/
│   │   │   │   │   ├── screens/               # Composable screens
│   │   │   │   │   │   ├── DashboardScreen.kt        # Màn hình chính
│   │   │   │   │   │   ├── StatisticsScreen.kt       # Thống kê biểu đồ
│   │   │   │   │   │   ├── MonthlyReportScreen.kt    # Báo cáo tháng
│   │   │   │   │   │   ├── TransactionHistoryScreen.kt
│   │   │   │   │   │   ├── AddTransactionScreen.kt
│   │   │   │   │   │   ├── EditTransactionScreen.kt
│   │   │   │   │   │   ├── BudgetScreen.kt
│   │   │   │   │   │   ├── CategoryScreen.kt
│   │   │   │   │   │   ├── LoginScreen.kt
│   │   │   │   │   │   ├── SignupScreen.kt
│   │   │   │   │   │   ├── ProfileScreen.kt
│   │   │   │   │   │   └── ...
│   │   │   │   │   └── theme/
│   │   │   │   │       ├── Color.kt
│   │   │   │   │       ├── Theme.kt
│   │   │   │   │       └── Type.kt
│   │   │   │   ├── service/
│   │   │   │   │   ├── BankNotificationListenerService.kt
│   │   │   │   │   └── BankNotificationParser.kt
│   │   │   │   ├── di/
│   │   │   │   │   └── ServiceLocator.kt      # Dependency injection
│   │   │   │   ├── utils/
│   │   │   │   │   ├── CurrencyUtils.kt       # Định dạng tiền VND
│   │   │   │   │   ├── DateUtils.kt
│   │   │   │   │   ├── SecurityUtils.kt       # SHA-256 password hashing
│   │   │   │   │   ├── CategoryUtils.kt
│   │   │   │   │   └── Constants.kt
│   │   │   │   ├── MainActivity.kt
│   │   │   │   └── FinTrackApp.kt
│   │   │   └── res/                           # Resources (icons, strings...)
│   │   ├── androidTest/                       # Instrumented tests
│   │   └── test/                              # Unit tests
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml                     # Version catalog
├── report-final/                              # Tài liệu báo cáo
└── README.md
```

---

## Cài Đặt & Chạy

### Yêu Cầu

- **Android Studio** 2023.1 (Hedgehog) trở lên
- **JDK** 11 trở lên
- **Android SDK** API 24+ (minSdk = 24)
- **Target SDK** 36

### Các Bước

**1. Clone dự án**
```bash
git clone https://github.com/ayctyuh/FinTrackApp.git
cd FinTrackApp
```

**2. Mở trong Android Studio**
- Chọn **File → Open** → duyệt đến thư mục `FinTrackApp`
- Android Studio tự động đồng bộ Gradle (khoảng 2–3 phút lần đầu)

**3. Build dự án**
```bash
./gradlew clean assembleDebug
```

**4. Chạy trên emulator hoặc thiết bị thực**
```bash
# Cài trực tiếp lên thiết bị đang kết nối
./gradlew installDebug
```
Hoặc nhấn **Run** (Shift + F10) trong Android Studio.

**5. Cấp quyền (tùy chọn)**
- Để dùng tính năng **nhập liệu tự động từ ngân hàng**: vào Cài đặt → Trợ năng → Quyền truy cập thông báo → bật FinTrack.

---

## Hướng Dẫn Sử Dụng

### Đăng Ký & Đăng Nhập
- Màn hình đầu tiên: Đăng nhập hoặc Đăng ký
- Mật khẩu yêu cầu: tối thiểu 8 ký tự, có chữ hoa, chữ thường và số
- Mật khẩu được mã hóa SHA-256 trước khi lưu

### Quản Lý Giao Dịch
- Nhấn nút **"+"** ở thanh điều hướng dưới để thêm giao dịch mới
- Chọn loại: Thu hoặc Chi; nhập số tiền, chọn danh mục, ngày và mô tả
- Vuốt sang trái hoặc nhấn giữ giao dịch để xóa/sửa

### Xem Thống Kê
- Tab **"Thống kê"** → chọn loại biểu đồ (Tổng hợp / Danh mục / Xu hướng)
- Chuyển bộ lọc **Tuần / Tháng / Năm** để thay đổi khoảng thời gian

### Thiết Lập Ngân Sách
- Tab **"Ngân sách"** → Thiết lập ngân sách tổng hoặc theo từng danh mục
- Ứng dụng cảnh báo khi chi tiêu đạt 80% ngân sách

---

## Bảo Mật

- Mật khẩu được hash bằng **SHA-256** trước khi lưu vào CSDL
- **Không có server** – dữ liệu lưu hoàn toàn trên thiết bị, không truyền qua Internet
- Hỗ trợ đặt **mã PIN** để khóa ứng dụng

---

## Testing

```bash
# Unit tests
./gradlew test

# Instrumented tests (cần thiết bị/emulator)
./gradlew connectedAndroidTest
```

---

## Liên Hệ

- **Email:** huynq0307@gmail.com
- **GitHub:** [ayctyuh](https://github.com/ayctyuh)
