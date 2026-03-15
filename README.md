# FinTrack - Ứng Dụng Quản Lý Tài Chính Cá Nhân

## 🎯 Mô Tả Dự Án

FinTrack là một ứng dụng Android hiện đại giúp người dùng quản lý tài chính cá nhân một cách hiệu quả. Ứng dụng cung cấp các tính năng:

- **Quản lý giao dịch**: Ghi nhận, chỉnh sửa, xóa giao dịch thu/chi
- **Phân loại giao dịch**: Tổ chức giao dịch theo danh mục
- **Thống kê chi tiết**: Biểu đồ tổng hợp, theo danh mục, xu hướng, so sánh
- **Quản lý ngân sách**: Thiết lập, theo dõi, cảnh báo vượt quá ngân sách
- **Nhập/xuất dữ liệu**: Nhập tự động từ ngân hàng, xuất CSV

## 📱 Công Nghệ Sử Dụng

### Frontend
- **Jetpack Compose**: Modern UI framework
- **Material Design 3**: Design system
- **Kotlin**: Ngôn ngữ lập trình

### Backend & Database
- **Room Database**: Local SQLite database
- **Kotlin Coroutines**: Asynchronous operations
- **LiveData & StateFlow**: Reactive data management

### Architecture
- **MVVM (Model-View-ViewModel)**: Architecture pattern
- **Repository Pattern**: Data abstraction
- **Dependency Injection**: Using Hilt

### Additional Libraries
- **Retrofit**: HTTP client for API calls
- **MPAndroidChart**: Chart visualization
- **Timber**: Logging
- **DataStore**: Secure preferences storage

## 🚀 Cài Đặt & Chạy

### Yêu Cầu
- Android Studio 2023.1 hoặc cao hơn
- JDK 11 hoặc cao hơn
- Android SDK 24+ (minSdk = 24)
- Android SDK Target 36

### Các Bước Cài Đặt

1. **Clone dự án**
   ```bash
   git clone https://github.com/username/FinTrackApp.git
   cd FinTrackApp
   ```

2. **Mở trong Android Studio**
   - File → Open
   - Chọn folder `FinTrackApp`
   - Android Studio sẽ tự động sync Gradle

3. **Build dự án**
   ```bash
   ./gradlew clean build
   ```

4. **Chạy trên emulator hoặc thiết bị thực**
   - Kết nối thiết bị Android hoặc khởi động emulator
   - Nhấn Run (Shift + F10) hoặc:
   ```bash
   ./gradlew installDebug
   ```

## 📁 Cấu Trúc Thư Mục

Xem file `PROJECT_STRUCTURE.md` để biết chi tiết về cấu trúc dự án.

```
FinTrackApp/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/fintrack/project/
│   │   │   │   ├── data/               # Data layer (entities, DAOs, repos)
│   │   │   │   ├── presentation/       # UI layer (Compose screens, ViewModels)
│   │   │   │   ├── di/                 # Dependency injection
│   │   │   │   └── utils/              # Utilities
│   │   │   └── res/                    # Resources
│   │   ├── androidTest/
│   │   └── test/
│   └── build.gradle.kts
├── gradle/libs.versions.toml
└── README.md
```

## 📚 Hướng Dẫn Sử Dụng (Users)

### 1. Đăng Ký Tài Khoản
- Nhấn "Đăng Ký"
- Điền thông tin: tên đăng nhập, email, mật khẩu
- Mật khẩu phải có:
  - Ít nhất 8 ký tự
  - Chứa ít nhất 1 chữ hoa, 1 chữ thường, 1 số

### 2. Quản Lý Giao Dịch
- **Thêm giao dịch**: Nhấn "+" → Chọn loại (Thu/Chi) → Điền thông tin
- **Chỉnh sửa**: Nhấn trên giao dịch → Chỉnh sửa → Lưu
- **Xóa**: Swipe sang trái hoặc nhấn delete

### 3. Xem Thống Kê
- **Dashboard**: Xem tổng quan thu/chi hôm nay, tháng này
- **Biểu đồ**: Đi tới mục "Thống Kê" để xem các biểu đồ chi tiết
- **So sánh**: Chọn khoảng thời gian để so sánh

### 4. Thiết Lập Ngân Sách
- Đi tới "Ngân Sách" → "Thiết Lập Mới"
- Chọn loại (Tổng/Theo danh mục)
- Điền số tiền giới hạn
- Ứng dụng sẽ cảnh báo khi gần vượt quá

## 👥 Phân Công Công Việc

| STT | Thành Viên | Công Việc |
|-----|-----------|----------|
| 1 | Nguyễn Quang Huy | Biểu đồ thống kê (tổng hợp, danh mục, xu hướng) |
| 2 | Cao Đăng Khánh | Xác thực, nhập liệu tự động, ngân sách |
| 3 | Vũ Đức Mạnh | Quản lý giao dịch & danh mục |
| 4 | Đinh Quang Lâm | Biểu đồ so sánh, cảnh báo, báo cáo |

## 🔐 Bảo Mật

- Mật khẩu được hash bằng SHA-256 trước khi lưu
- Dữ liệu lưu trữ cục bộ, không gửi qua internet (hiện tại)
- Session management để logout tự động

## 🧪 Testing

### Unit Tests
```bash
./gradlew test
```

### Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

## 📝 Changelog

### Version 1.0.0
- ✅ Xác thực người dùng (Đăng ký/Đăng nhập)
- ✅ Quản lý giao dịch cơ bản
- ✅ Danh mục giao dịch
- ✅ Thống kê với biểu đồ
- ✅ Quản lý ngân sách
- ⏳ Nhập/xuất dữ liệu (đang phát triển)

## 🐛 Báo Cáo Lỗi

Nếu bạn tìm thấy lỗi, vui lòng:
1. Kiểm tra xem lỗi đó đã được báo cáo chưa
2. Tạo issue mới với:
   - Mô tả chi tiết lỗi
   - Steps to reproduce
   - Expected vs Actual behavior
   - Thông tin thiết bị (Model, Android version)

## 💡 Đề Xuất Tính Năng

Các đề xuất được chào đón! Vui lòng tạo issue với tag "enhancement".

## 📧 Liên Hệ

- **Email**: fintrack@example.com
- **GitHub Issues**: [FinTrackApp Issues](https://github.com/username/FinTrackApp/issues)

## 📄 License

Dự án này được cấp phép dưới MIT License. Xem file [LICENSE](LICENSE) để biết chi tiết.

## 🙏 Cảm Ơn

Cảm ơn các thành viên nhóm đã đóng góp vào dự án này!

---

**Thông Tin Dự Án**
- Môn học: Phát Triển Ứng Dụng Cho Các Thiết Bị Di Động (INT1449)
- Giảng viên hướng dẫn: Ths. Nguyễn Hoàng Anh
- Nhóm lớp: 02
- Nhóm: 01
- Năm học: 2025-2026

