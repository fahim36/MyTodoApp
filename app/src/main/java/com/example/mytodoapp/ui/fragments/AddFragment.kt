package com.example.mytodoapp.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.example.mytodoapp.R
import com.example.mytodoapp.databinding.FragmentAddBinding

import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.*

import android.view.View.OnFocusChangeListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mytodoapp.ui.adapter.AddedTodoItemAdapter
import android.view.inputmethod.EditorInfo

import android.widget.Toast
import androidx.fragment.app.viewModels
import com.example.mytodoapp.data.localdata.model.TodoData
import com.example.mytodoapp.ui.OnItemClicked
import com.example.mytodoapp.ui.viewmodel.TodoViewModel
import com.example.mytodoapp.utils.Constants.MIN_5_MS
import com.example.mytodoapp.utils.UtilityHelper
import com.example.mytodoapp.utils.hide
import com.example.mytodoapp.utils.show
import jhonatan.sabadi.datetimepicker.showDateAndTimePicker


@AndroidEntryPoint
class AddFragment : Fragment() {
    private lateinit var binding: FragmentAddBinding
    private val todoViewModel: TodoViewModel by viewModels()
    private val todoList: ArrayList<String> by lazy {
        ArrayList()
    }
    private lateinit var itemAdapter: AddedTodoItemAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        
        initPicker()
        initAddTodoListener()
        initAddedTodoList()

        return binding.root
    }

    private fun initAddTodoListener() {
        binding.addTodoItemET.apply {

            setOnEditorActionListener { v, actionId, event ->
                val str = text.toString().trim()
                if (actionId == EditorInfo.IME_ACTION_DONE && str.isNotEmpty()) {
                    todoList.add(str)
                    setText("")
                    itemAdapter.addDataToAdapter(todoList)
                    if (todoList.size > 0)
                        binding.noToDoTv.hide()
                    true
                } else false
            }
        }
    }

    private fun initAddedTodoList() {

        binding.addTodoItemET.apply {
            imeOptions = EditorInfo.IME_ACTION_DONE;
            setImeActionLabel(getString(R.string.add), EditorInfo.IME_ACTION_DONE);
        }
        val itemRemoved = object : OnItemClicked {
            override fun onClicked(item: Any) {
                todoList.remove(item as String)
                if (todoList.isEmpty())
                    binding.noToDoTv.show()
            }
        }
        itemAdapter = AddedTodoItemAdapter(requireContext(), itemRemoved)
        binding.todoListRv.apply {
            binding.noToDoTv.show()
            layoutManager = LinearLayoutManager(context)
            adapter = itemAdapter
        }
    }

    private fun initPicker() {
        binding.timeET.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.timeET.clearFocus()
                timePicker()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add_menu -> {
                insertData()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun timePicker() {
        var str: String;
        requireActivity().showDateAndTimePicker { date: Date ->
            Timber.e(date.time.toString())
            when {
                date.time < Calendar.getInstance().timeInMillis -> {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.toast_cant_back_past_error),
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.timeET.requestFocus()
                }
                date.time < Calendar.getInstance().timeInMillis + MIN_5_MS.toLong() -> {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.toast_min_diff_error),
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.timeET.requestFocus()
                }

            }
            str = UtilityHelper.epoch2DateString(date.time, "hh:mm aa dd/MM/yyyy")
            binding.timeET.setText(str)
        }
    }

    private fun insertData() {

        val title = binding.titleET.text.toString()
        val id = UUID.randomUUID().toString()
        val time = UtilityHelper.dateStringToEpoch(
            binding.timeET.text.toString().trim(),
            "hh:mm aa dd/MM/yyyy"
        ).toString()

        Timber.e("After Conversion: $time")

        val todoDbData =
            TodoData(id, title, time, todoList)
        val validate = checkData(todoDbData)
        if (validate) {
            UtilityHelper.hideSoftKeyboard(requireActivity())
            todoViewModel.insertData(todoDbData)
            Toast.makeText(
                requireContext(),
                getString(R.string.toast_add_successful),
                Toast.LENGTH_SHORT
            ).show()
            requireActivity().onBackPressed()
        }

    }

    private fun checkData(todo: TodoData): Boolean {

        when {
            todo.title.isEmpty() -> {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.toast_title_blank_error),
                    Toast.LENGTH_SHORT
                ).show()
                binding.titleET.requestFocus()
                return false
            }
            todo.time.length == 1 -> {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.toast_pick_todo_time_error),
                    Toast.LENGTH_SHORT
                ).show()
                binding.timeET.requestFocus()
                return false
            }
            todo.description.isEmpty() -> {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.toast_add_one_task_error),
                    Toast.LENGTH_SHORT
                ).show()
                binding.addTodoItemET.requestFocus()
                return false
            }
        }
        return true
    }

}