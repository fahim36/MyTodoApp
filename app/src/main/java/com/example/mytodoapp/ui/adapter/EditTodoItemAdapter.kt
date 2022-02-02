package com.example.mytodoapp.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mytodoapp.databinding.AddedTodoItemBinding
import com.example.mytodoapp.ui.OnItemClicked
import com.example.mytodoapp.utils.hide
import com.example.mytodoapp.utils.show

class EditTodoItemAdapter(var context: Context, var onItemClicked: OnItemClicked) :
    RecyclerView.Adapter<EditTodoItemAdapter.TodoViewHolder>() {
    private var isEditOn = false
    private val todoList: ArrayList<String> by lazy {
        ArrayList()
    }


    inner class TodoViewHolder(private val binding: AddedTodoItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindData(todo: String) {
            //bind data here
            if (isEditOn)
                binding.crossIcon.show()
            else
                binding.crossIcon.hide()

            binding.todoItemNameTv.text = todo
            binding.crossIcon.setOnClickListener {
                val index = todoList.indexOf(todo)
                todoList.remove(todo)
                notifyItemRemoved(index)
                onItemClicked.onClicked(todo)
            }
        }
    }

    fun setEditOn(isEdit: Boolean) {
        isEditOn = isEdit
        notifyDataSetChanged()
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
    ): EditTodoItemAdapter.TodoViewHolder {
        val bind = AddedTodoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TodoViewHolder(bind)
    }

    override fun onBindViewHolder(holder: EditTodoItemAdapter.TodoViewHolder, position: Int) {
        holder.bindData(todoList[position])
    }

    override fun getItemCount(): Int = todoList.size
}