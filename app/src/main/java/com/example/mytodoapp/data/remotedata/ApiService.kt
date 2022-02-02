package com.example.mytodoapp.data.remotedata

import com.example.mytodoapp.data.ApiUrls
import com.example.mytodoapp.data.remotedata.remotemodel.TodoRemoteModel
import retrofit2.http.GET

interface ApiService {
    @GET(ApiUrls.GET_TODO_LIST)
    suspend fun todoList(): TodoRemoteModel
}