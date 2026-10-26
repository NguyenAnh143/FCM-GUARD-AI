package com.example

import android.app.Application
import com.example.data.db.AppDatabase
import com.example.data.pref.AppPreferences
import com.example.service.FcmGuardService

class FcmGuardApplication : Application() {

    lateinit var appDatabase: AppDatabase
        private set

    lateinit var appPreferences: AppPreferences
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        appDatabase = AppDatabase.getDatabase(this)
        appPreferences = AppPreferences(this)

        if (appPreferences.isAutoGuardEnabled.value) {
            try {
                FcmGuardService.start(this)
            } catch (e: Exception) {
                // Background start restriction may occur on cold start before UI
            }
        }
    }

    companion object {
        lateinit var instance: FcmGuardApplication
            private set
    }
}
