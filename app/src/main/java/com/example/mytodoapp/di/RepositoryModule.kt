package com.example.mytodoapp.di


import com.example.mytodoapp.data.remotedata.ApiService
import com.example.mytodoapp.data.repository.TodoRemoteRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideTodoRepository(apiService: ApiService): TodoRemoteRepository {
        return TodoRemoteRepository(apiService)
    }
}