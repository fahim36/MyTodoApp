package com.example.mytodoapp.data.repository


import com.example.mytodoapp.data.remotedata.ApiService
import com.example.mytodoapp.data.remotedata.remotemodel.TodoRemoteModel
import com.example.mytodoapp.utils.DataState
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class TodoRemoteRepository @Inject constructor(private val apiService: ApiService) {


suspend fun todoList() = flow {
        emit(DataState.Loading)
        try {
            val todoList =   apiService.todoList()
            emit(DataState.Success(todoList))
        } catch (exception: Exception){
            emit(DataState.Error(exception))
        }
    }


}