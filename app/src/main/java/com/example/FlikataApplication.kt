package com.example

import android.app.Application
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FlikataApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("FlikataCrash", "Uncaught exception on thread ${thread.name}: ${throwable.message}", throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }

        // Immediately initialize auth credentials so all background / network operations have credentials
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                val db = com.example.data.local.FlikataDatabase.getDatabase(this@FlikataApplication)
                val user = db.userDao().getLoggedInUser()
                if (user != null) {
                    com.example.data.remote.ApiClient.currentAuthToken = user.authToken
                    com.example.data.remote.ApiClient.currentUserId = user.id
                }
            } catch (e: Exception) {
                Log.w("FlikataApp", "Initial auth load failed: ${e.message}")
            }
        }
    }
}

