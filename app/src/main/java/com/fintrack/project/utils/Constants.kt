package com.fintrack.project.utils

object Constants {
    // Database
    const val DATABASE_NAME = "fintrack_database"

    // Preferences
    const val PREFS_NAME = "fintrack_prefs"
    const val KEY_CURRENT_USER_ID = "current_user_id"
    const val KEY_IS_LOGGED_IN = "is_logged_in"
    const val KEY_LAST_LOGIN = "last_login"

    // Date Formats
    const val DATE_FORMAT = "dd/MM/yyyy"
    const val DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm:ss"
    const val TIME_FORMAT = "HH:mm:ss"

    // Budget Alert Thresholds (%)
    const val BUDGET_ALERT_WARNING = 75.0
    const val BUDGET_ALERT_DANGER = 90.0

    // Pagination
    const val PAGE_SIZE = 20
    const val INITIAL_PAGE = 0

    // API Endpoints
    const val BASE_URL = "https://api.fintrack.com/"

    // Timeout (seconds)
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L
}

