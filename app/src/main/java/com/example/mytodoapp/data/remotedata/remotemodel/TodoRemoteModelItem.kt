package com.example.mytodoapp.data.remotedata.remotemodel


import com.google.gson.annotations.SerializedName

data class TodoRemoteModelItem(
    @SerializedName("time")
    val time: String,
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("todo")
    val todo: List<String>
)