package com.example.mytodoapp.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mytodoapp.databinding.AddedTodoItemBinding
import com.example.mytodoapp.ui.OnItemClicked

class AddedTodoItemAdapter(var context: Context, var onItemClicked: OnItemClicked) :
    RecyclerView.Adapter<AddedTodoItemAdapter.TodoViewHolder>() {
    private val todoList: ArrayList<String> by lazy {
        ArrayList()
    }


    inner class TodoViewHolder(private val binding: AddedTodoItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindData(todo: String) {
            //bind data here
            binding.todoItemNameTv.text = todo
            binding.crossIcon.setOnClickListener {
                val index = todoList.indexOf(todo)
                todoList.remove(todo)
                notifyItemRemoved(index)
                onItemClicked.onClicked(todo)
            }
        }
    }

    fun addDataToAdapter(todo: ArrayList<String>) {
        todoList.clear()
        todoList.addAll(todo)
        todoList.reverse()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AddedTodoItemAdapter.TodoViewHolder {
        val bind = AddedTodoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TodoViewHolder(bind)
    }

    override fun onBindViewHolder(holder: AddedTodoItemAdapter.TodoViewHolder, position: Int) {
        holder.bindData(todoList[position])
    }

    override fun getItemCount(): Int = todoList.size
}