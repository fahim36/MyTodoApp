package com.example.mytodoapp.services

import android.app.*
import android.app.job.JobScheduler
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.mytodoapp.R
import com.example.mytodoapp.data.localdata.model.TodoData
import com.example.mytodoapp.ui.activity.MainActivity
import com.example.mytodoapp.utils.Constants
import com.example.mytodoapp.utils.Constants.KEY_TODO_DATA
import com.google.gson.Gson
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import timber.log.Timber

class MyForegroundService : Service() {

    companion object {
        const val CHANNEL_ID = "ForegroundService"
        var job: Job? = null

        fun startService(context: Context, str: String) {
            val startIntent = Intent(context, MyForegroundService::class.java)
            startIntent.putExtra(KEY_TODO_DATA, str)
            ContextCompat.startForegroundService(context, startIntent)
        }

        fun stopService(context: Context) {
            job?.cancel()
            val stopIntent = Intent(context, MyForegroundService::class.java)
            EventBus.getDefault().post(Constants.ACTION_SHOW_TODO_CHANGED);
            context.stopService(stopIntent)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //do heavy work on a background thread
        val data: String? = intent?.getStringExtra(KEY_TODO_DATA)
        val todoData: TodoData = Gson().fromJson(data, TodoData::class.java)
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.action = Constants.ACTION_SHOW_TODO_DETAILS
        notificationIntent.putExtra(KEY_TODO_DATA, data)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT

        )

        val foregroundNotification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(todoData.title)
            .setContentText(getString(R.string.expire_soon_notification))
            .setSmallIcon(R.drawable.ic_clock)
            .setContentIntent(pendingIntent)
            .build()
        startUpdates(todoData)
        startForeground(1, foregroundNotification)
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, Constants.CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager: NotificationManager? = getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }
    }

    private fun startUpdates(todoData: TodoData) {
        job?.cancel()
        job = CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                delay(Constants.MIN_5_MS.toLong())
                cancelJob(getJobIdFromTodo(todoData))
            }
        }
    }

    private fun getJobIdFromTodo(element: TodoData): Int {
        var jobId = element.time.toLong()
        jobId /= 1000
        return jobId.toInt()
    }

    private fun cancelJob(jobId: Int) {
        val scheduler = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
        scheduler.cancel(jobId)
        stopService(this)
        Timber.d("Job cancelled")
    }
}