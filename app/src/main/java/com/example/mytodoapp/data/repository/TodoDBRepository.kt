package com.example.mytodoapp.data.repository

import androidx.lifecycle.LiveData
import com.example.mytodoapp.data.localdata.db.TodoDao
import com.example.mytodoapp.data.localdata.model.TodoData
import javax.inject.Inject

class TodoDBRepository @Inject constructor(private val todoDao: TodoDao) {

    val getAllData = todoDao.getAllData()


    suspend fun insertData(todo: TodoData) {
        todoDao.insertData(todo)
    }

    suspend fun updateData(todo: TodoData) {
        todoDao.updateData(todo)
    }

    suspend fun deleteData(todo: TodoData) {
        todoDao.deleteData(todo)
    }

    suspend fun deleteAllData() {
        todoDao.deleteAllData()
    }

    fun searchData(query: String): LiveData<List<TodoData>> {
        return todoDao.searchData(query)
    }

}