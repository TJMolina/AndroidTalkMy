package com.tjm.talkmy.ui.tasks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.tjm.talkmy.R
import com.tjm.talkmy.databinding.FragmentTasksListBinding
import com.tjm.talkmy.ui.tasks.adapter.TaskAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@AndroidEntryPoint
class TasksListFragment : Fragment() {

    private var _binding: FragmentTasksListBinding? = null
    private val binding get() = _binding!!
    private val tasksListViewModel by viewModels<TasksListViewModel>()
    private lateinit var taskAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTasksListBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tasksListViewModel.getAllPreferences()
        initUI()
    }

    private fun initUI() {
        initRecyclerView()
        observeOrder()
        initMenu()
        initListeners()
        observeHasTasks()
    }


    private fun observeHasTasks() {
        lifecycleScope.launch(Dispatchers.IO) {
            tasksListViewModel.haveTaskState.collectLatest {
                withContext(Dispatchers.Main) {
                    binding.ivNoTasks.visibility = if (it) View.GONE else View.VISIBLE
                }
            }
        }
    }

    private fun initMenu() {
        val menuHost: MenuHost = binding.topAppBarTaskList
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {}

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menuconfigs -> {
                        findNavController().navigate(
                            TasksListFragmentDirections.actionTasksListFragmentToOptionsFragment()
                        )
                        true
                    }

                    else -> false
                }
            }
        })
    }

    private fun initListeners() {
        binding.btnAddNota.setOnClickListener {
            findNavController().navigate(
                TasksListFragmentDirections.actionTasksListFragmentToEditTaskFragment(null, null)
            )
        }
    }

    private fun initRecyclerView() {
        val manager = LinearLayoutManager(requireContext())
        taskAdapter = TaskAdapter(
            editTask = { id, task -> editTask(id, task) },
            deleteTask = { id, position ->
                tasksListViewModel.deleteTask(
                    id,
                    position,
                    taskAdapter,
                    parentFragmentManager
                )
            })
        binding.rvTasksList.layoutManager = manager
        binding.rvTasksList.adapter = taskAdapter
        tasksListViewModel.getLocalTasks(taskAdapter)
    }

    private fun observeOrder() {
        lifecycleScope.launch(Dispatchers.IO) {
            tasksListViewModel.preferences.collect {
                val layoutManagerFirstToLast =
                    LinearLayoutManager(
                        requireContext(),
                        LinearLayoutManager.VERTICAL,
                        it.orderNote
                    )
                withContext(Dispatchers.Main) {
                    binding.rvTasksList.layoutManager = layoutManagerFirstToLast
                }
            }
        }
    }

    private fun editTask(id: String, task: String) {
        findNavController().navigate(
            TasksListFragmentDirections.actionTasksListFragmentToEditTaskFragment(
                taskToEdit = id,
                task = task,
                fontSize = tasksListViewModel.preferences.value.textSize
            )
        )
    }
}