package com.example.mytodoapp.ui.fragments

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.mytodoapp.R
import com.example.mytodoapp.data.localdata.model.TodoData

import com.example.mytodoapp.databinding.FragmentTodolistBinding
import com.example.mytodoapp.ui.viewmodel.TodoViewModel
import com.example.mytodoapp.ui.adapter.TodoListAdapter
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import timber.log.Timber

import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.mytodoapp.ui.OnItemClicked
import com.example.mytodoapp.ui.activity.MainActivity
import com.example.mytodoapp.utils.*
import com.example.mytodoapp.utils.Constants.ACTION_SHOW_TODO_CHANGED
import com.google.gson.Gson
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


@AndroidEntryPoint
class TodoListFragment : Fragment(), SearchView.OnQueryTextListener {

    private lateinit var binding: FragmentTodolistBinding
    private val todoViewModel: TodoViewModel by viewModels()

    private lateinit var todoAdapter: TodoListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTodolistBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        initRecyclerView()
        initAddTodoBtn()
        getDataFromDB()
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_menu, menu)
        val search = menu.findItem(R.id.search)
        val searchView = search.actionView as? SearchView
        searchView?.apply {
            setOnQueryTextListener(this@TodoListFragment)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.syncData -> {
                if (UtilityHelper.isNetworkConnected(requireContext())) {
                    todoViewModel.getTodoFromServer()
                    getDataFromAPI()
                } else {
                    Toast.makeText(
                        context,
                        getString(R.string.no_internet_toast),
                        Toast.LENGTH_SHORT
                    ).show();
                }
            }
            R.id.settings -> findNavController().navigate(R.id.action_todoListFragment_to_settingsFragment)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initAddTodoBtn() {
        binding.addTodoBtn.setOnClickListener {
            findNavController().navigate(R.id.action_listFragment_to_addFragment)
        }
    }


    private fun initRecyclerView() {

        val itemClicked = object : OnItemClicked {
            override fun onClicked(item: Any) {
                val str = Gson().toJson(item as TodoData)
                findNavController().navigate(
                    R.id.action_todoListFragment_to_updateFragment,
                    bundleOf(Constants.KEY_TODO_DATA to str)
                )

            }
        }

        todoAdapter = TodoListAdapter(requireContext(), itemClicked)

        binding.listRecyclerView.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            adapter = todoAdapter
            itemAnimator = SlideInUpAnimator().apply {
                addDuration = 300
            }
            swipeToDelete(this)
        }
        //      todoViewModel.getTodoFromServer()
    }


    private fun checkForEmptyDb() {
        binding.apply {
            if (todoViewModel.isEmptyDb()) {
                noDataImage.show()
                noDataText.show()
            } else {
                noDataImage.hide()
                noDataText.hide()
            }
        }
    }

    private fun swipeToDelete(recyclerView: RecyclerView) {
        val swipeCallback = object : SwipeToDelete() {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val item = todoAdapter.todoList[viewHolder.adapterPosition]
                todoViewModel.deleteData(item)
                (requireActivity() as MainActivity).stopScheduleForTodo(item)
                restoreData(viewHolder.itemView, item)
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun restoreData(
        view: View,
        todoData: TodoData
    ) {
        Snackbar.make(view, getString(R.string.deleted) + todoData.title, Snackbar.LENGTH_LONG)
            .also {
                it.apply {
                    setAction(getString(R.string.undo)) {
                        todoViewModel.insertData(todoData)
                    }
                    show()
                }
            }
    }

    private fun getDataFromAPI() {
        todoViewModel.todo.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Loading -> {
                    Timber.e("Loading")
                    binding.progressbar.show()
                }
                is DataState.Success -> {
                    Timber.e(it.data.size.toString())
                    getDataFromDB()
                    binding.progressbar.hide()
                }
                is DataState.Error -> {
                    Timber.e("Error")
                    binding.progressbar.hide()
                }
            }
        }
    }

    private fun getDataFromDB() {
        todoViewModel.getAllTodoData.observe(viewLifecycleOwner) {
            Timber.e(it.size.toString())
            checkForEmptyDb()
            todoAdapter.addDataToAdapter(it)
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query != null) {
            searchDB(query)
        }
        return true
    }

    private fun searchDB(query: String) {
        val searchQuery = "%$query%"

        todoViewModel.searchData(searchQuery).observe(viewLifecycleOwner, Observer {
            it?.let {
                todoAdapter.addDataToAdapter(it)
            }
        })
    }

    override fun onQueryTextChange(query: String?): Boolean {
        if (query != null) {
            searchDB(query)
        }
        return true
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: String) {
        if (event == ACTION_SHOW_TODO_CHANGED)
            todoViewModel.filterData()
        getDataFromDB()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }
}