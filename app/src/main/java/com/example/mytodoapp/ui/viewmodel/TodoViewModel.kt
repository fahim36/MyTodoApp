package com.example.mytodoapp.ui.viewmodel

import androidx.lifecycle.*
import com.example.mytodoapp.data.localdata.model.TodoData
import com.example.mytodoapp.data.remotedata.remotemodel.TodoRemoteModel
import com.example.mytodoapp.data.repository.TodoRemoteRepository
import com.example.mytodoapp.data.repository.TodoDBRepository
import com.example.mytodoapp.utils.DataState
import com.example.mytodoapp.utils.UtilityHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TodoViewModel @Inject constructor(
    private val repo: TodoRemoteRepository,
    private val localRepo: TodoDBRepository
) : ViewModel(),
    LifecycleObserver {
    var getAllTodoData: LiveData<List<TodoData>> = localRepo.getAllData

    private val _todo = MutableLiveData<DataState<TodoRemoteModel>>()

    val todo: LiveData<DataState<TodoRemoteModel>> get() = _todo

    fun getTodoFromServer() {
        viewModelScope.launch {
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
                            }
                        }
                        _todo.value = it
                    }
                    is DataState.Error -> _todo.value = it
                    DataState.Loading -> _todo.value = it
                }
            }.launchIn(viewModelScope)
        }
    }


    fun isEmptyDb(): Boolean {
        return getAllTodoData.value.isNullOrEmpty()
    }

    fun insertData(todo: TodoData) {
        viewModelScope.launch(Dispatchers.IO) {
            localRepo.insertData(todo)
        }
    }

    fun updateData(todo: TodoData) {
        viewModelScope.launch(Dispatchers.IO) {
            localRepo.updateData(todo)
            getAllTodoData = localRepo.getAllData
        }
    }

    fun deleteData(todo: TodoData) {
        viewModelScope.launch(Dispatchers.IO) {
            localRepo.deleteData(todo)
            getAllTodoData = localRepo.getAllData
        }
    }

    fun deleteAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            localRepo.deleteAllData()
        }
    }

    fun searchData(query: String): LiveData<List<TodoData>> {
        return localRepo.searchData(query)
    }

    fun getUpcomingTodo(list: List<TodoData>): TodoData? {
        list.forEach {
            if (it.time.toLong() > Calendar.getInstance().timeInMillis) {
                return it
            }
        }
        return null
    }

    fun filterData() {
        localRepo.getAllData.value?.forEach {
            if (it.time.toLong() < Calendar.getInstance().timeInMillis) {
                deleteData(it)
            }
        }
    }

    fun getJobIdFromTodo(element: TodoData): Int {
        var jobId = element.time.toLong()
        jobId /= 1000
        return jobId.toInt()
    }
}