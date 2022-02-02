package com.example.mytodoapp.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import com.example.mytodoapp.R
import dagger.hilt.android.AndroidEntryPoint
import com.example.mytodoapp.services.MyForegroundService
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.PersistableBundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.preference.PreferenceManager
import com.example.mytodoapp.data.localdata.model.TodoData
import com.example.mytodoapp.services.MyJobSchedulerService
import com.example.mytodoapp.services.SyncScheduler
import com.example.mytodoapp.ui.viewmodel.TodoViewModel
import com.example.mytodoapp.utils.Constants
import com.example.mytodoapp.utils.Constants.ACTION_SHOW_TODO_DETAILS
import com.example.mytodoapp.utils.Constants.JOB_ID_SYNC_JOB
import com.example.mytodoapp.utils.Constants.KEY_NIGHT_MODE
import com.example.mytodoapp.utils.Constants.KEY_SYNC
import com.example.mytodoapp.utils.Constants.KEY_SYNC_PERIOD
import com.example.mytodoapp.utils.Constants.KEY_TODO_DATA
import com.example.mytodoapp.utils.DataState
import com.google.gson.Gson
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import java.util.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var listener: OnSharedPreferenceChangeListener
    private val sharedPreferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }
    private val todoViewModel: TodoViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navController = findNavController(R.id.navHostFragment)
        setupActionBarWithNavController(this, navController)
        checkForNightMode()
        initSharedPrefListener()
        setStatusBarColor()
        startScheduleForTodo()
        checkIntentAction(intent)
        checkForSync()
    }


    private fun checkForNightMode() {
        val syncValue = sharedPreferences.getBoolean(KEY_NIGHT_MODE, false)
        setToNightMode(syncValue)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun setStatusBarColor() {
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)
    }

    private fun startScheduleForTodo() {
        todoViewModel.getAllTodoData.observe(this) {
            todoViewModel.filterData()
            Timber.e(it.size.toString())
            if (it.isNotEmpty()) {
                it.forEach { element ->
                    if (!isJobServiceOn(todoViewModel.getJobIdFromTodo(element)))
                        setScheduleJob(element, todoViewModel.getJobIdFromTodo(element))
                }
            }
        }
    }

    fun stopScheduleForTodo(todoData: TodoData) {
        MyForegroundService.stopService(this)
        if (todoData.time.toLong() > Constants.MIN_5_MS.toLong())
            cancelJob(todoViewModel.getJobIdFromTodo(todoData))
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        checkIntentAction(intent)
    }


    private fun checkIntentAction(intent: Intent?) {
        if (intent?.action == ACTION_SHOW_TODO_DETAILS) {
            val str = intent.getStringExtra(KEY_TODO_DATA)
            navController.navigate(
                R.id.action_global_update_fragment,
                bundleOf(KEY_TODO_DATA to str)
            )
        } else if (intent?.action == KEY_SYNC) {
            todoViewModel.getTodoFromServer()
            getDataFromAPI()
        }
    }

    private fun setScheduleJob(todoData: TodoData, index: Int) {

        val componentName = ComponentName(this, MyJobSchedulerService::class.java)
        val bundle = PersistableBundle()

        bundle.putString(KEY_TODO_DATA, Gson().toJson(todoData))
        val period =
            todoData.time.toLong() - Calendar.getInstance().timeInMillis - Constants.MIN_5_MS.toLong()
        val info = JobInfo.Builder(index, componentName)
            .setExtras(bundle)
            .setPersisted(true)
            .setMinimumLatency(period)
            .build()
        val scheduler = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
        val resultCode = scheduler.schedule(info)

        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Timber.d("Job scheduled")
        } else {
            Timber.d("Job scheduling failed")
        }
    }

    private fun cancelJob(jobId: Int) {
        val scheduler = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
        scheduler.cancel(jobId)
        Timber.d("Job cancelled")
    }

    private fun isJobServiceOn(jobId: Int): Boolean {
        val scheduler = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
        var hasBeenScheduled = false
        for (jobInfo in scheduler.allPendingJobs) {
            if (jobInfo.id == jobId) {
                hasBeenScheduled = true
                break
            }
        }
        return hasBeenScheduled
    }

    private fun getDataFromAPI() {
        todoViewModel.todo.observe(this) {
            when (it) {
                is DataState.Loading -> {
                    Timber.e("Loading")
                }
                is DataState.Success -> {
                    Timber.e(it.data.size.toString())
                    Toast.makeText(
                        this,
                        getString(R.string.data_sync_successful),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
                is DataState.Error -> {
                    Timber.e("Error")
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: String) {
        if (event == Constants.ACTION_SYNC) {
            todoViewModel.getTodoFromServer()
            getDataFromAPI()
            val settings = PreferenceManager.getDefaultSharedPreferences(this)
            val editor: SharedPreferences.Editor = settings.edit()
            editor.putBoolean(Constants.ACTION_SYNC, false)
            editor.apply()
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onResume() {
        super.onResume()
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    private fun initSharedPrefListener() {
        listener =
            OnSharedPreferenceChangeListener { sharedPreferences, key ->
                when (key) {
                    KEY_NIGHT_MODE -> {
                        val isNightMode = sharedPreferences.getBoolean(key, false)
                        setToNightMode(isNightMode)
                    }
                    KEY_SYNC_PERIOD -> {
                        val syncValue = sharedPreferences.getString(key, "3600000")
                        if (syncValue != null) {
                            startSyncScheduler(syncValue.toLong())
                        }
                    }
                    KEY_SYNC -> {
                        val syncValue = sharedPreferences.getBoolean(key, false)
                        if (!syncValue) {
                            stopSyncScheduler()
                        } else {
                            val syncValue = sharedPreferences.getString(KEY_SYNC_PERIOD, "3600000")
                            if (syncValue != null) {
                                startSyncScheduler(syncValue.toLong())
                            }
                        }
                    }
                }
            }
    }

    private fun setToNightMode(nightMode: Boolean) {
        if (nightMode) {
            AppCompatDelegate
                .setDefaultNightMode(
                    AppCompatDelegate
                        .MODE_NIGHT_YES
                )
        } else {
            AppCompatDelegate
                .setDefaultNightMode(
                    AppCompatDelegate
                        .MODE_NIGHT_NO
                )
        }

    }

    private fun checkForSync() {
        val settings = PreferenceManager.getDefaultSharedPreferences(this)
        val editor: SharedPreferences.Editor = settings.edit()
        val syncTime = settings.getBoolean(Constants.ACTION_SYNC, false)
        if (syncTime) {
            todoViewModel.getTodoFromServer()
            getDataFromAPI()
            editor.putBoolean(Constants.ACTION_SYNC, false)
            editor.apply()
        }
    }

    private fun stopSyncScheduler() {
        val scheduler = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
        scheduler.cancel(JOB_ID_SYNC_JOB)
        Timber.d("Sync Job cancelled")
    }

    private fun startSyncScheduler(syncValue: Long) {
        val componentName = ComponentName(this, SyncScheduler::class.java)
        val info = JobInfo.Builder(JOB_ID_SYNC_JOB, componentName)
            .setPersisted(true)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .setPeriodic(syncValue)
            .build()
        val scheduler = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
        val resultCode = scheduler.schedule(info)

        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Timber.d("Job scheduled")
        } else {
            Timber.d("Job scheduling failed")
        }
    }
}