package com.example.mytodoapp.services

import android.app.ActivityManager
import android.app.job.JobService
import android.app.job.JobParameters
import android.content.Context
import android.os.Build
import com.example.mytodoapp.R
import com.example.mytodoapp.data.localdata.model.TodoData
import com.example.mytodoapp.utils.Constants.KEY_TODO_DATA
import com.example.mytodoapp.utils.Constants.MIN_5_MS
import com.google.gson.Gson
import timber.log.Timber
import java.util.*

class MyJobSchedulerService : JobService() {
    private var jobCancelled = false
    private lateinit var todoData: TodoData
    override fun onStartJob(params: JobParameters): Boolean {
        Timber.d("Job started")
        Timber.d("onStartJob: %s", params.extras.getString(KEY_TODO_DATA))
        doBackgroundWork(params)
        return true
    }

    private fun doBackgroundWork(params: JobParameters) {
        if (jobCancelled) {
            return
        }
        todoData = Gson().fromJson(params.extras.getString(KEY_TODO_DATA), TodoData::class.java)
        if (todoData.time.toLong() < Calendar.getInstance().timeInMillis + MIN_5_MS.toLong()) {
            startService(todoData)
        }
        Timber.d("Job finished")
        jobFinished(params, false)
    }

    override fun onStopJob(params: JobParameters): Boolean {
        Timber.d("Job cancelled before completion")
        if (isServiceRunning()) {
            MyForegroundService.stopService(this)
        }
        jobCancelled = true
        return true
    }

    private fun startService(todoData: TodoData) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!isServiceRunning()) {
                val str = Gson().toJson(todoData)
                MyForegroundService.startService(this, str)
            }
        }
    }

    private fun isServiceRunning(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in activityManager.getRunningServices(Int.MAX_VALUE)) {
            if (MyForegroundService::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }
}