package com.example.mytodoapp.services

import android.app.*
import android.app.job.JobService
import android.app.job.JobParameters
import android.content.Context
import android.os.Build
import com.example.mytodoapp.R
import com.example.mytodoapp.data.localdata.model.TodoData
import com.example.mytodoapp.utils.Constants
import org.greenrobot.eventbus.EventBus
import timber.log.Timber


import com.example.mytodoapp.ui.activity.MainActivity

import android.content.Intent
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.example.mytodoapp.utils.Constants.KEY_SYNC
import com.example.mytodoapp.utils.Constants.SYNC_CHANNEL_ID
import com.example.mytodoapp.utils.UtilityHelper


class SyncScheduler : JobService() {
    private var jobCancelled = false
    override fun onStartJob(params: JobParameters): Boolean {
        Timber.d("Job started")
        doBackgroundWork(params)
        return true
    }

    private fun doBackgroundWork(params: JobParameters) {
        if (jobCancelled) {
            return
        }
        EventBus.getDefault().post(Constants.ACTION_SYNC);
        if (!UtilityHelper.isAppRunning(this, packageName))
            showNotification()
        setPreferenceValue()
        Timber.d("Job finished")
        jobFinished(params, false)
    }

    private fun setPreferenceValue() {
        val settings = PreferenceManager.getDefaultSharedPreferences(this)
        val editor: SharedPreferences.Editor = settings.edit()
        editor.putBoolean(Constants.ACTION_SYNC, true)
        editor.apply()
    }

    private fun showNotification() {

        val intentNotification = Intent(this, MainActivity::class.java)
        intentNotification.action = KEY_SYNC
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intentNotification,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val builderNotificationCompat: Notification.Builder =
                Notification.Builder(
                    this,
                    SYNC_CHANNEL_ID
                )
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setContentTitle(getString(R.string.sync_notification_title))
                    .setContentText(getString(R.string.sync_notification_body))
                    .setSmallIcon(R.drawable.ic_clock)

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val notificationChannel = NotificationChannel(
                SYNC_CHANNEL_ID,
                "Sync Notification",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(notificationChannel)
            notificationManager.notify(0, builderNotificationCompat.build())
        }

    }

    override fun onStopJob(params: JobParameters): Boolean {
        Timber.d("Job cancelled before completion")
        jobCancelled = true
        return true
    }


}