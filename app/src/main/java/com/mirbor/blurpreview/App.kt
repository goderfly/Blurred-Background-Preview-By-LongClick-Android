package com.mirbor.blurpreview

import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.SystemClock

class App: Application() {

    override fun onCreate() {
        try {
            appContext = applicationContext
        } catch (ignore: Throwable) {
        }
        super.onCreate()
        if (appContext == null) {
           appContext = applicationContext
        }
        applicationHandler = Handler(applicationContext.mainLooper)
    }

    companion object {
        @Volatile
        var appContext: Context? = null

        @Volatile
        var applicationHandler: Handler? = null
    }

}