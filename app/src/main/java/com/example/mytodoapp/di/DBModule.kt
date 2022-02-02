package com.example.mytodoapp.di

import android.content.Context
import androidx.room.Room
import com.example.mytodoapp.data.localdata.db.TodoDatabase
import com.example.mytodoapp.utils.TodoConverter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DBModule {

    @Singleton
    @Provides
    fun provideTodoDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app,
        TodoDatabase::class.java,
        "todo_database"
    )
        .fallbackToDestructiveMigration()
        .addTypeConverter(TodoConverter())
        .build()

    @Singleton
    @Provides
    fun provideTodoDao(db: TodoDatabase) = db.toDoDao()
}