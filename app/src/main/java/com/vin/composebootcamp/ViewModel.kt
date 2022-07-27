package com.vin.composebootcamp

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.min

/*
    Global viewmodel
 */
// Test / Faux viewmodel for Previews
class TestViewModel: TasksViewModel(TaskRepoDummy()) {
    override fun addTask(task: Task) {  }
    override fun updateTask(task: Task) {  }
    override fun fetchTask(id: Int) {
        task = Task(id, "Task #${(Math.random() * 12).toInt()}", "Lorem ipsum holy crap", Math.random() >= 0.5f, Date().time + (Math.random() * 4096).toInt(), Date().time + (Math.random() * 4096).toInt())
    }
    override fun fetchAllTasks() {
        tasks = List((Math.random() * 4).toInt() + 4) {
            Task(it, "Task #${(Math.random() * 12).toInt()}", "Lorem ipsum holy crap", Math.random() >= 0.5f, Date().time + (Math.random() * 4096).toInt(), Date().time + (Math.random() * 4096).toInt())
        }
    }
}

// The real deal
open class TasksViewModel (private val repo: TaskRepoInterface) : ViewModel() {
    // App state
    var isSplashDone: Boolean by mutableStateOf(false)
    var showAddTaskDialogue: Boolean by mutableStateOf(false)

    // DB (live) data
    var tasks by mutableStateOf(emptyList<Task>())
    var task by mutableStateOf(Task(0, "", "", false, Date().time, Date().time))

    open fun addTask(task: Task) {
        // since we're using coroutines...
        viewModelScope.launch(Dispatchers.IO) {
            repo.addTask(task)
        }
    }
    open fun updateTask(task: Task){
        viewModelScope.launch(Dispatchers.IO) {
            repo.updateTask(task)
        }
    }
    open fun fetchTask(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.getTask(id).collectLatest { // collect from Flow<Task>
                task = it
            }
        }
    }
    open fun fetchAllTasks() {
        viewModelScope.launch(Dispatchers.Main) {
            repo.getAllTasks().collectLatest {
                tasks = it
            }
            /*
            repo.getAllTasks().flatMapConcat { gotList ->
                flow { // Perform additional processing & split to 3 similar tasks... for FUN
                    val weirdList = mutableListOf<Task>()
                    for (task in gotList) {
                        weirdList.add(task)
                        weirdList.add(task.copy(title = task.title + " imposter #1", desc = "this is sus:\n" + task.title))
                        weirdList.add(task.copy(title = task.title + " imposter #2", desc = "this is sus:\n" + task.title))
                    }
                    emit(weirdList)
                }
            }.collect { tasks = it }
            */
            /*
            repo.getAllTasks().collect { // collect from Flow<List<Task>>
                Log.e("YEAH", "FETCH: COLLECT EXECUTED ON TASKS `${it.size}`")

                //tasks = it
                // Weird pulsating list
                val result = it
                tasks = emptyList()
                for (i in (0 .. result.size*10)) {
                    val weirdList = result.subList(0, min(i, result.size)).toMutableList()

                    // Random element... because it's FUN
                    repeat ((Math.random() * 8).toInt()) {
                        weirdList.add((Math.random() * weirdList.size).toInt(), Task(result.size + it, LoremIpsum(4).values.joinToString(), LoremIpsum(16).values.joinToString(), it and 1 == 0, (Math.random() * 4096*4).toLong(), (Math.random() * 4096*4).toLong()))
                    }

                    tasks = weirdList.toList()

                    // Delay for demonstration purposes
                    delay(50)
                }
                tasks = result
            }
             */
        }
    }
}

class TaskViewModelFactory(private val repo: TaskRepoInterface) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TasksViewModel::class.java)) {
            return TasksViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class :: ${modelClass::class.java.simpleName}")
    }
}