package com.hordesurvival

import android.app.Application
import android.util.Log

class HordeSurvivalApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Global crash handler — log but don't crash
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("HordeSurvival", "Uncaught exception in ${thread.name}", throwable)
            // Don't kill the process immediately — let the game try to recover
        }
    }

    companion object {
        lateinit var instance: HordeSurvivalApp
            private set
    }
}
