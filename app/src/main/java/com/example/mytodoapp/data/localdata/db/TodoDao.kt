package com.example.mytodoapp.data.localdata.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.mytodoapp.data.localdata.model.TodoData

@Dao
interface TodoDao {

    @Query("SELECT * FROM todo_table ORDER BY time ASC")
    fun getAllData(): LiveData<List<TodoData>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertData(todo: TodoData)

    @Update
    suspend fun updateData(todo: TodoData)

    @Delete
    suspend fun deleteData(todo: TodoData)

    @Query("DELETE FROM todo_table")
    suspend fun deleteAllData()

    @Query("SELECT * FROM todo_table WHERE title LIKE :query")
    fun searchData(query: String): LiveData<List<TodoData>>

}