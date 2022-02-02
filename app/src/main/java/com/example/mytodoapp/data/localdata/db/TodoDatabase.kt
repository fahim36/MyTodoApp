package com.example.mytodoapp.data.localdata.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.mytodoapp.data.localdata.model.TodoData
import com.example.mytodoapp.utils.TodoConverter

@Database(entities = [TodoData::class], version = 2, exportSchema = false)
@TypeConverters(TodoConverter::class)
abstract class TodoDatabase : RoomDatabase() {

    abstract fun toDoDao(): TodoDao

}