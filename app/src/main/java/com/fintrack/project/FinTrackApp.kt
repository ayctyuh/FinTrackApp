package com.fintrack.project

import android.app.Application
import timber.log.Timber
import com.fintrack.project.di.ServiceLocator

class FinTrackApp : Application() {
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
