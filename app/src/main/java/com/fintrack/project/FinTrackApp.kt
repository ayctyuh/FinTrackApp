package com.fintrack.project

import android.app.Application
import timber.log.Timber

class FinTrackApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Timber for logging
        Timber.plant(Timber.DebugTree())
        Timber.d("FinTrack App Started!")
        
        // Database initialization will be done lazily when needed
        // to avoid crash due to annotation processor issues
    }
}
