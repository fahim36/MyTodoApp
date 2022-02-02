package com.example.mytodoapp.broadcast

import android.annotation.SuppressLint
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PersistableBundle
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.viewModelScope
import com.example.mytodoapp.R
import com.example.mytodoapp.data.localdata.model.TodoData
import com.example.mytodoapp.data.repository.TodoDBRepository
import com.example.mytodoapp.data.repository.TodoRemoteRepository
import com.example.mytodoapp.services.MyForegroundService
import com.example.mytodoapp.services.MyJobSchedulerService
import com.example.mytodoapp.utils.Constants
import com.example.mytodoapp.utils.DataState
import com.example.mytodoapp.utils.UtilityHelper
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MyBroadcastReceiver : BroadcastReceiver() {
    private lateinit var context: Context
    @Inject
    lateinit var repo: TodoRemoteRepository
    @Inject
    lateinit var localRepo: TodoDBRepository

    override fun onReceive(context: Context, intent: Intent) {
        this.context = context
        getTodoFromServer()
    }

    private fun getTodoFromServer() {
        GlobalScope.launch {
            repo.todoList().onEach { it ->
                when (it) {
                    is DataState.Success -> {
                        it.data.forEach {
                            val time =
                                UtilityHelper.dateStringToEpoch(it.time, "yyyy-MM-dd HH:mm aa")
                                    .toString()
                            if (time.toLong() > Calendar.getInstance().timeInMillis) {
                                val todoDbData =
                                    TodoData(it.id, it.title, time, it.todo)
                                localRepo.insertData(todoDbData)
                                if (!isJobServiceOn(getJobIdFromTodo(todoDbData)))
                                    setScheduleJob(todoDbData, getJobIdFromTodo(todoDbData))
                            }
                        }
                    }
                    else -> {}
                }
            }.launchIn(GlobalScope)
        }
    }

    @SuppressLint("MissingPermission")
    private fun setScheduleJob(todoData: TodoData, index: Int) {

        val componentName = ComponentName(context, MyJobSchedulerService::class.java)
        val bundle = PersistableBundle()

        bundle.putString(Constants.KEY_TODO_DATA, Gson().toJson(todoData))
        val period =
            todoData.time.toLong() - Calendar.getInstance().timeInMillis - Constants.MIN_5_MS.toLong()
        val info = JobInfo.Builder(index, componentName)
            .setExtras(bundle)
            .setPersisted(true)
            .setMinimumLatency(period)
            .build()
        val scheduler =
            context.getSystemService(AppCompatActivity.JOB_SCHEDULER_SERVICE) as JobScheduler
        val resultCode = scheduler.schedule(info)

        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Timber.d("Job scheduled")
        } else {
            Timber.d("Job scheduling failed")
        }
    }

    private fun cancelJob(jobId: Int) {
        val scheduler =
            context.getSystemService(AppCompatActivity.JOB_SCHEDULER_SERVICE) as JobScheduler
        scheduler.cancel(jobId)
        Timber.d("Job cancelled")
    }

    private fun isJobServiceOn(jobId: Int): Boolean {
        val scheduler =
            context.getSystemService(AppCompatActivity.JOB_SCHEDULER_SERVICE) as JobScheduler
        var hasBeenScheduled = false
        for (jobInfo in scheduler.allPendingJobs) {
            if (jobInfo.id == jobId) {
                hasBeenScheduled = true
                break
            }
        }
        return hasBeenScheduled
    }

    private fun getJobIdFromTodo(element: TodoData): Int {
        var jobId = element.time.toLong()
        jobId /= 1000
        return jobId.toInt()
    }
}