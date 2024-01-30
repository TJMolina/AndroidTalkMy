package com.tjm.talkmy.ui.tasks

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
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

    override fun onResume() {
        super.onResume()
        tasksListViewModel.getLocalTasks()
        initObserverTasks()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        initListeners()
        initRecyclerView()
    }


    private fun initListeners() {
        binding.btnAddNota.setOnClickListener {
            findNavController().navigate(
                TasksListFragmentDirections.actionTasksListFragmentToEditTaskFragment(null)
            )
        }
    }

    private fun initRecyclerView() {
        val manager = LinearLayoutManager(requireContext())
        taskAdapter = TaskAdapter(editTask = { id -> editTask(id) },
            deleteTask = { id, position ->
                tasksListViewModel.deleteTask(
                    id,
                    position,
                    taskAdapter
                )
            })
        binding.rvTasksList.layoutManager = manager
        binding.rvTasksList.adapter = taskAdapter
    }

    private fun editTask(id: String) {
        findNavController().navigate(
            TasksListFragmentDirections.actionTasksListFragmentToEditTaskFragment(id)
        )
    }

    private fun initObserverTasks() {
        lifecycleScope.launch(Dispatchers.IO) {
            tasksListViewModel.getTaskState.collectLatest { value ->
                withContext(Dispatchers.Main) {
                    if (value.isLoading) {

                    } else if (value.error.isNotBlank()) {

                    } else {
                        value.tasksList.let {
                            taskAdapter.reloadList(it)
                        }
                    }
                }
            }
        }
    }

}