package com.viginfotech.spark

import android.app.Application
import timber.log.Timber
import timber.log.Timber.DebugTree
import android.os.StrictMode
import com.squareup.leakcanary.LeakCanary





class SparkApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
            setupLeakCanary();
        }
    }

    private fun setupLeakCanary() {
        enabledStrictMode()
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        LeakCanary.install(this)
    }

    private fun enabledStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder() //
                .detectAll() //
                .penaltyLog() //
                .penaltyDeath() //
                .build()
        )
    }
}