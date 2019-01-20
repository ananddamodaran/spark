package com.viginfotech.spark

import android.app.Application
import com.viginfotech.spark.base.BuildConfig


class SparkApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            //Timber.plant(DebugTree())
        }
    }
}