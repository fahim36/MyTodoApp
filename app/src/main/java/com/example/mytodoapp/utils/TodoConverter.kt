package com.example.mytodoapp.utils

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@ProvidedTypeConverter
class TodoConverter {
    @TypeConverter
    fun toTodoItem(todoItem: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(todoItem, type)
    }

    @TypeConverter
    fun toTodoItemJson(todoItem: List<String>): String {
        val type = object : TypeToken<List<String>>() {}.type
        return Gson().toJson(todoItem, type)
    }
}