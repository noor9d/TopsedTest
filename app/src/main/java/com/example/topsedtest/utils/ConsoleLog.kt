package com.example.topsedtest.utils

import android.util.Log
import com.example.topsedtest.BuildConfig

object ConsoleLog {

    fun i(tag: String = "info", message: String) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, message)
        }
    }

    fun w(tag: String = "warn", message: String) {
        if (BuildConfig.DEBUG) {
            Log.w(tag, message)
        }
    }


    fun e(tag: String = "error", e: Exception) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, "EXCEPTION")
            e.printStackTrace()
        }
    }
}