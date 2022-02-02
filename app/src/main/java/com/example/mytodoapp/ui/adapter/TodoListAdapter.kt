package com.example.mytodoapp.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mytodoapp.data.localdata.model.TodoData
import com.example.mytodoapp.databinding.TodoItemBinding
import com.example.mytodoapp.ui.OnItemClicked
import com.example.mytodoapp.utils.UtilityHelper

class TodoListAdapter(var context: Context, var itemClicked: OnItemClicked) :
    RecyclerView.Adapter<TodoListAdapter.MovieViewHolder>() {
    val todoList = mutableListOf<TodoData>()


    inner class MovieViewHolder(private val binding: TodoItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindData(todo: TodoData) {
            //bind data here
            var str = UtilityHelper.epoch2DateString(todo.time.toLong(), "hh:mm aa")
            str = "$str\n${UtilityHelper.epoch2DateString(todo.time.toLong(), "dd/MM/yyyy")}"
            binding.titleTV.text = todo.title
            binding.descTV.text = todoToDesc(todo.description)
            binding.timeStampTV.text = str
            itemView.setOnClickListener {
                itemClicked.onClicked(todo)
            }
        }
    }

    fun addDataToAdapter(todoListData: List<TodoData>) {
        todoList.clear()
        todoList.addAll(todoListData)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TodoListAdapter.MovieViewHolder {
        val bind = TodoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MovieViewHolder(bind)
    }

    override fun onBindViewHolder(holder: TodoListAdapter.MovieViewHolder, position: Int) {
        holder.bindData(todoList[position])
    }

    override fun getItemCount(): Int = todoList.size

    private fun todoToDesc(todo: List<String>): String {
        var str = "* "
        todo.forEach {
            str = if (todo.lastIndex != todo.indexOf(it))
                "$str$it\n* "
            else
                "$str$it"
        }
        return str
    }
}