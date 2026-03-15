# 🏗️ FinTrack - Architecture Diagram

## System Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                        UI LAYER (Compose)                       │
│  ┌─────────────┐  ┌─────────────┐  ┌──────────────┐            │
│  │ Login/Signup│  │ Transactions│  │ Dashboard    │            │
│  └──────┬──────┘  └──────┬──────┘  └──────┬───────┘            │
│         │                │               │                     │
│  ┌──────┴────────────────┴───────────────┴──────┐              │
│  │      Navigation Layer (NavController)        │              │
│  └────────────┬─────────────────────────────────┘              │
│               │                                                 │
└───────────────┼─────────────────────────────────────────────────┘
                │
┌───────────────┼─────────────────────────────────────────────────┐
│               ▼         VIEWMODEL LAYER                         │
│  ┌────────────────────────────────────────────────────────┐    │
│  │  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐  │    │
│  │  │BaseViewModel │ │ AuthViewModel│ │TransactionVM│  │    │
│  │  ├──────────────┤ ├──────────────┤ ├──────────────┤  │    │
│  │  │ loading      │ │ login()      │ │ add()        │  │    │
│  │  │ error        │ │ signup()     │ │ delete()     │  │    │
│  │  │ setError()   │ │ logout()     │ │ filter()     │  │    │
│  │  │ clearError() │ │ validate()   │ │ sort()       │  │    │
│  │  └──────────────┘ └──────────────┘ └──────────────┘  │    │
│  │  ┌──────────────┐                                     │    │
│  │  │ BudgetViewModel                                    │    │
│  │  ├──────────────┤                                     │    │
│  │  │ create()     │                                     │    │
│  │  │ checkAlerts()│                                     │    │
│  │  │ update()     │                                     │    │
│  │  └──────────────┘                                     │    │
│  └────────┬─────────────────────────────────────────────┘    │
│           │                                                   │
└───────────┼───────────────────────────────────────────────────┘
            │
┌───────────┼────────────────────────────────────────────────────┐
│           ▼        REPOSITORY LAYER (Clean Architecture)      │
│  ┌────────────────────────────────────────────────────────┐   │
│  │  ┌──────────────┐  ┌──────────────┐                   │   │
│  │  │UserRepository│  │CategoryRepo  │  TransactionRepo  │   │
│  │  ├──────────────┤  ├──────────────┤  ├──────────────┤ │   │
│  │  │insertUser()  │  │insertCat()   │  │insertTxn()   │ │   │
│  │  │getUser()     │  │getCategories │  │getTransactions│ │   │
│  │  │authenticate()│  │updateCat()   │  │filterByDate()│ │   │
│  │  └──────────────┘  └──────────────┘  └──────────────┘ │   │
│  │  ┌──────────────┐                                      │   │
│  │  │BudgetRepository                                     │   │
│  │  ├──────────────┤                                      │   │
│  │  │createBudget()                                       │   │
│  │  │getBudgets()                                         │   │
│  │  │checkAlerts()                                        │   │
│  │  └──────────────┘                                      │   │
│  └────────┬──────────────────────────────────────────────┘    │
│           │                                                   │
└───────────┼───────────────────────────────────────────────────┘
            │
┌───────────┼────────────────────────────────────────────────────┐
│           ▼          DAO LAYER (Data Access Objects)          │
│  ┌────────────────────────────────────────────────────────┐   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐             │   │
│  │  │UserDao   │  │CategoryD │  │TransactionD│BudgetDao  │   │
│  │  ├──────────┤  ├──────────┤  ├──────────┤├──────────┤ │   │
│  │  │@Insert   │  │@Query    │  │@Delete   ││@Query    │ │   │
│  │  │@Update   │  │@Update   │  │@Update   ││@Insert   │ │   │
│  │  │@Delete   │  │@Delete   │  │@Insert   ││@Update   │ │   │
│  │  │@Query    │  │...       │  │...       ││...       │ │   │
│  │  └──────────┘  └──────────┘  └──────────┘└──────────┘ │   │
│  └────────┬──────────────────────────────────────────────┘    │
│           │                                                   │
└───────────┼───────────────────────────────────────────────────┘
            │
┌───────────┼────────────────────────────────────────────────────┐
│           ▼       DATABASE LAYER (Room/SQLite)                │
│  ┌────────────────────────────────────────────────────────┐   │
│  │                  FinTrackDatabase                      │   │
│  │  ┌──────────────────────────────────────────────┐     │   │
│  │  │  Table: Users                               │     │   │
│  │  │  ┌─────────────────────────────────────┐    │     │   │
│  │  │  │ id | username | email | password    │    │     │   │
│  │  │  └─────────────────────────────────────┘    │     │   │
│  │  ├──────────────────────────────────────────────┤     │   │
│  │  │  Table: Categories                          │     │   │
│  │  │  ┌─────────────────────────────────────┐    │     │   │
│  │  │  │ id | userId | name | type | color   │    │     │   │
│  │  │  └─────────────────────────────────────┘    │     │   │
│  │  ├──────────────────────────────────────────────┤     │   │
│  │  │  Table: Transactions                        │     │   │
│  │  │  ┌─────────────────────────────────────┐    │     │   │
│  │  │  │ id | userId | amount | type | date  │    │     │   │
│  │  │  │ categoryId | description             │    │     │   │
│  │  │  └─────────────────────────────────────┘    │     │   │
│  │  ├──────────────────────────────────────────────┤     │   │
│  │  │  Table: Budgets                             │     │   │
│  │  │  ┌─────────────────────────────────────┐    │     │   │
│  │  │  │ id | userId | categoryId | limit    │    │     │   │
│  │  │  │ month | year | alertThreshold       │    │     │   │
│  │  │  └─────────────────────────────────────┘    │     │   │
│  │  └──────────────────────────────────────────────┘     │   │
│  │         SQLite Database (fintrack_database)          │   │
│  └────────────────────────────────────────────────────────┘   │
│           ▲                                                    │
└───────────┼────────────────────────────────────────────────────┘
            │
     [Device Storage]

```

---

## Data Flow Diagram

### User Registration & Login Flow
```
┌─────────────────┐
│ Signup Screen   │
│ (Compose UI)    │
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────┐
│ AuthViewModel.signup()          │
│ - Validate input               │
│ - Hash password (SHA-256)       │
│ - Emit loading state            │
└────────┬────────────────────────┘
         │
         ▼
┌─────────────────────────────────┐
│ UserRepository.insertUser()     │
│ - Call UserDao                  │
│ - Insert into database          │
└────────┬────────────────────────┘
         │
         ▼
┌──────────────────────┐
│ Database Save        │
│ users table          │
└──────────────────────┘
         │
         ▼
┌──────────────────────────────────┐
│ AuthViewModel                     │
│ - Clear loading                   │
│ - Set signupSuccess = true        │
│ - Navigate to Login               │
└──────────────────────────────────┘
```

### Transaction Management Flow
```
┌──────────────────┐
│ Add Transaction  │
│ Screen           │
└────────┬─────────┘
         │ User input
         ▼
┌──────────────────────────────┐
│ TransactionViewModel.add()   │
│ - Validate amount > 0        │
│ - Create Transaction object  │
│ - Emit loading state         │
└────────┬─────────────────────┘
         │
         ▼
┌───────────────────────────────────┐
│ TransactionRepository.insert()    │
│ - Call TransactionDao             │
│ - Insert to database              │
└────────┬────────────────────────┘
         │
         ▼
┌──────────────────────┐
│ Database Save        │
│ transactions table   │
└──────────┬───────────┘
           │
           ▼
┌─────────────────────────────────┐
│ TransactionViewModel            │
│ - Get updated transactions      │
│ - Update filteredTransactions   │
│ - Update totals (income/expense)│
└─────────────────────────────────┘
```

### Budget Tracking & Alert Flow
```
┌─────────────────────────┐
│ Set Budget              │
│ (Amount + Month)        │
└────────┬────────────────┘
         │
         ▼
┌───────────────────────────────┐
│ BudgetViewModel.createBudget()│
└────────┬────────────────────┘
         │
         ▼
┌──────────────────────────┐
│ BudgetRepository.insert()│
└────────┬─────────────────┘
         │
         ▼
┌──────────────────────┐
│ Database Save        │
│ budgets table        │
└──────────┬───────────┘
           │
    ┌──────┴──────────────────┐
    │                         │
    ▼                         ▼
┌────────────────┐    ┌──────────────────────────────┐
│ User spends    │    │ getBudgetsByMonth() called   │
│ money          │    │ - Get all budgets for month  │
└────────┬───────┘    │ - Calculate spent amount     │
         │            │ - Check if > 80% (alert)     │
         │            │ - Add to alerts list         │
         │            └──────────┬───────────────────┘
         │                       │
         ├───────────────────────┤
         │                       │
         ▼                       ▼
    ┌────────────────────────────────────┐
    │ UI Updates:                         │
    │ - Show progress bar (color coded)   │
    │ - Display alerts if triggered       │
    │ - Show remaining/exceeded amount    │
    └────────────────────────────────────┘
```

---

## State Management (StateFlow)

### BaseViewModel Pattern
```
┌──────────────────────────────────┐
│ BaseViewModel                    │
├──────────────────────────────────┤
│ private _loading: MutableStateFlow
│ val loading: StateFlow            │
│                                   │
│ private _error: MutableStateFlow  │
│ val error: StateFlow              │
│                                   │
│ protected showLoading()           │
│ protected hideLoading()           │
│ protected setError()              │
│ protected clearError()            │
└──────────────────────────────────┘
```

### AuthViewModel State Example
```
┌─────────────────────────────────────────┐
│ AuthViewModel(extends BaseViewModel)    │
├─────────────────────────────────────────┤
│ _currentUser: MutableStateFlow           │
│ _isLoggedIn: MutableStateFlow            │
│ _loginSuccess: MutableStateFlow          │
│ _signupSuccess: MutableStateFlow         │
│                                         │
│ Functions:                              │
│ - login(username, password)             │
│   └─ emit loading → validate → auth    │
│   └─ emit success/error                 │
│                                         │
│ - signup(username, email, password)     │
│   └─ emit loading → validate → insert  │
│   └─ emit success/error                 │
└─────────────────────────────────────────┘
```

---

## Dependency Injection Pattern (ServiceLocator)

```
┌────────────────────────────────────────────┐
│ ServiceLocator (Manual DI)                 │
├────────────────────────────────────────────┤
│ Singleton instance management:             │
│                                            │
│ ┌──────────────────────────────────────┐  │
│ │ initializeDatabase(context)          │  │
│ │ - Creates FinTrackDatabase singleton │  │
│ └──────────────────────────────────────┘  │
│                                            │
│ ┌──────────────────────────────────────┐  │
│ │ getUserRepository(): UserRepository  │  │
│ │ - Returns cached or creates new      │  │
│ │ - Thread-safe with synchronized {}   │  │
│ └──────────────────────────────────────┘  │
│                                            │
│ Similar for:                               │
│ - getCategoryRepository()                  │
│ - getTransactionRepository()               │
│ - getBudgetRepository()                    │
└────────────────────────────────────────────┘
```

### Usage Example
```kotlin
// In ViewModel
val userRepo = ServiceLocator.getUserRepository()
val authVM = AuthViewModel(userRepo)

// In Fragment/Activity
val authVM = remember { 
    AuthViewModel(ServiceLocator.getUserRepository()) 
}
```

---

## Database Relationships

```
┌────────────────────┐
│      Users         │
├────────────────────┤
│ id (PK)            │
│ username           │
│ email              │
│ passwordHash       │
│ createdAt          │
└────────┬───────────┘
         │ 1:N
         │
    ┌────┴──────────────────┬─────────────────┬─────────────────┐
    │                       │                 │                 │
    ▼ 1:N                   ▼ 1:N              ▼ 1:N              ▼ 1:N
┌─────────────────┐  ┌────────────────┐  ┌────────────────┐  ┌──────────────┐
│  Categories     │  │  Transactions  │  │    Budgets     │  │   (Other)    │
├─────────────────┤  ├────────────────┤  ├────────────────┤  ├──────────────┤
│ id (PK)         │  │ id (PK)        │  │ id (PK)        │  │              │
│ userId (FK→U)   │  │ userId (FK→U)  │  │ userId (FK→U)  │  │              │
│ name            │  │ categoryId(FK→C)  │ categoryId(FK→C) │ │              │
│ type            │  │ amount         │  │ limitAmount    │  │              │
│ color           │  │ type           │  │ month/year     │  │              │
│ isDefault       │  │ date           │  │ alertThreshold │  │              │
└─────────────────┘  │ description    │  └────────────────┘  └──────────────┘
                     └────────────────┘

Legend:
PK = Primary Key
FK = Foreign Key
1:N = One-to-Many relationship
```

---

## File Organization Tree

```
FinTrackApp/
│
├── gradle/
│   └── libs.versions.toml        ← All dependency versions
│
├── app/
│   ├── build.gradle.kts           ← App build config
│   ├── src/main/
│   │   ├── AndroidManifest.xml    ← App declaration
│   │   │
│   │   ├── java/com/fintrack/project/
│   │   │   ├── FinTrackApp.kt      ← Application entry
│   │   │   │
│   │   │   ├── data/
│   │   │   │   ├── model/          ← Entities (User, Category, etc)
│   │   │   │   ├── dao/            ← DAOs (UserDao, CategoryDao, etc)
│   │   │   │   ├── database/       ← Room database config
│   │   │   │   └── repository/     ← Repositories
│   │   │   │
│   │   │   ├── presentation/
│   │   │   │   └── viewmodel/      ← ViewModels
│   │   │   │
│   │   │   ├── di/
│   │   │   │   └── ServiceLocator.kt ← Dependency injection
│   │   │   │
│   │   │   └── utils/              ← Utilities
│   │   │       ├── Constants.kt
│   │   │       ├── DateUtils.kt
│   │   │       └── SecurityUtils.kt
│   │   │
│   │   └── res/
│   │       └── values/
│   │           ├── strings.xml     ← 100+ Vietnamese strings
│   │           └── colors.xml      ← Material Design 3 palette
│   │
│   └── (To be created - UI Layer)
│       └── ui/
│           ├── auth/
│           │   ├── LoginScreen.kt
│           │   └── SignupScreen.kt
│           ├── transaction/
│           │   ├── TransactionListScreen.kt
│           │   └── ...
│           ├── statistics/
│           │   ├── DashboardScreen.kt
│           │   └── ...
│           ├── budget/
│           │   ├── BudgetScreen.kt
│           │   └── ...
│           └── navigation/
│               └── AppNavigation.kt
│
└── Documentation/
    ├── README.md                ← Full documentation
    ├── PROJECT_STRUCTURE.md     ← Architecture details
    ├── MEMBER_GUIDE.md          ← Team guide
    ├── QUICK_START.md           ← Quick reference
    ├── SETUP_COMPLETE.md        ← Completion summary
    ├── CHECKLIST.md             ← Complete checklist
    ├── FINAL_SUMMARY.md         ← Final summary
    └── ARCHITECTURE.md          ← This file
```

---

## Component Interaction Diagram

```
┌─────────────────────────────────────────────────────┐
│                  UI Layer                           │
│  [LoginScreen] [TransactionScreen] [ReportScreen]  │
└────────────────────┬────────────────────────────────┘
                     │
                     │ StateFlow collection
                     │ Button clicks
                     ▼
┌─────────────────────────────────────────────────────┐
│              ViewModel Layer                        │
│  [AuthVM] [TransactionVM] [BudgetVM]               │
│  - Manage state                                    │
│  - Handle business logic                          │
│  - Emit updates via StateFlow                      │
└────────────────────┬────────────────────────────────┘
                     │
                     │ Method calls
                     │ suspend functions
                     ▼
┌─────────────────────────────────────────────────────┐
│            Repository Layer                        │
│  [UserRepo] [CategoryRepo] [TransactionRepo] ...  │
│  - Clean data access interface                    │
│  - Coordinate DAO calls                           │
└────────────────────┬────────────────────────────────┘
                     │
                     │ DAO method calls
                     ▼
┌─────────────────────────────────────────────────────┐
│              DAO Layer                              │
│  [UserDao] [CategoryDao] [TransactionDao] ...      │
│  - Direct database queries                        │
│  - SQL execution                                  │
└────────────────────┬────────────────────────────────┘
                     │
                     │ SQL queries
                     ▼
┌─────────────────────────────────────────────────────┐
│          Room Database/SQLite                       │
│  [Tables: Users, Categories, Transactions, Budgets]│
└─────────────────────────────────────────────────────┘
```

---

**Architecture Ready! 🚀**
**Time to build amazing UIs!**

