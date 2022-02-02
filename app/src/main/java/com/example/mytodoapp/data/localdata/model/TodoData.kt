package com.example.mytodoapp.data.localdata.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todo_table")
data class TodoData(
    @PrimaryKey
    var id: String,
    var title: String,
    var time: String,
    var description: List<String>
)