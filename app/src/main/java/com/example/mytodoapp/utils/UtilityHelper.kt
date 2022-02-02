package com.example.mytodoapp.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


import android.net.ConnectivityManager
import android.app.ProgressDialog

import android.R
import android.net.NetworkCapabilities

import android.net.NetworkInfo
import android.os.Build
import androidx.core.content.ContextCompat

import androidx.core.content.ContextCompat.getSystemService
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo


object UtilityHelper {

    fun epoch2DateString(epochMilliSeconds: Long, formatString: String): String {
        val sdf = SimpleDateFormat(formatString, Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(epochMilliSeconds)
    }

    fun dateStringToEpoch(str: String, format: String): Long {
        return try {
            SimpleDateFormat(format, Locale.getDefault()).parse(str).time
        } catch (e: ParseException) {
            e.printStackTrace()
            0
        }
    }

    fun hideSoftKeyboard(activity: Activity) {
        val inputMethodManager =
            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocusedView = activity.currentFocus
        currentFocusedView?.let {
            inputMethodManager.hideSoftInputFromWindow(
                currentFocusedView.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }

    fun isNetworkConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true

                else -> false
            }
        } else {
            @Suppress("DEPRECATION") val networkInfo =
                connectivityManager.activeNetworkInfo ?: return false
            return networkInfo.isConnected
        }
    }

    fun isAppRunning(context: Context, packageName: String): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val procInfos = activityManager.runningAppProcesses
        if (procInfos != null) {
            for (processInfo in procInfos) {
                if (processInfo.processName == packageName) {
                    return true
                }
            }
        }
        return false
    }
}