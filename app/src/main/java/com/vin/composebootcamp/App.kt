package com.vin.composebootcamp

import android.app.Application
import android.content.Context

class App : Application() {
    init {
        instance = this
    }

    companion object {
        var instance: App? = null // set it above
        fun context(): Context {
            return instance!!.applicationContext
        }
    }
}