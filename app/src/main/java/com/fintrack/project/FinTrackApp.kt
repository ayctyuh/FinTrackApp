package com.fintrack.project

import android.app.Application
import timber.log.Timber
import com.fintrack.project.di.ServiceLocator

/**
 * Application class khoi tao cau hinh toan cuc.
 * Phu thuoc: `ServiceLocator`, `Timber`.
 * Duoc su dung boi Android framework khi app khoi dong.
 */
class FinTrackApp : Application() {
    /**
     * Khoi tao logging va database.
     */
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Timber for logging
        Timber.plant(Timber.DebugTree())
        ServiceLocator.initializeDatabase(this)
        Timber.d("FinTrack App Started!")
        
        // Database initialization will be done lazily when needed
        // to avoid crash due to annotation processor issues
    }
}
