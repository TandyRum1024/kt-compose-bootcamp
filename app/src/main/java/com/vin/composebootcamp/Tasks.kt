package com.vin.composebootcamp

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import com.vin.composebootcamp.ui.theme.ComposeBootcampTheme
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun TasksList(
    padding: PaddingValues = PaddingValues(2.dp),
    viewModel: TasksViewModel
) {
    /*
    val lifecycleOwner = LocalLifecycleOwner.current
    val flowLifecycleAware = remember(viewModel!!.liveTasks, lifecycleOwner) {
        viewModel!!.liveTasks.flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
    }
    val tasks: List<Task> by flowLifecycleAware.collectAsState(initial = emptyList())
    */
//    val tasks: List<Task> by viewModel!!.liveTasks.collectAsState(initial = emptyList())
    val tasks: List<Task> = viewModel.tasks

    Column {
        Text("${tasks.size} task(s) total...")
        LazyColumn (contentPadding = padding, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            items(tasks) { task ->
                TaskCard(task = task, viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TaskCard(padding: PaddingValues = PaddingValues(0.dp), task: Task, viewModel: TasksViewModel) {
    var isActive by rememberSaveable { mutableStateOf(false) }
    Card (
        elevation = if (isActive) 0.5.dp else 0.2.dp,
        backgroundColor = MaterialTheme.colors.primary,
        modifier = Modifier
            .padding(horizontal = 0.dp, vertical = 4.dp)
            .animateContentSize(),
        onClick = { isActive = !isActive; viewModel.fetchTask(task.id) }
    ) {
        Column {
            Row (horizontalArrangement = Arrangement.SpaceBetween) {
                // Left side
                Surface (color = Color.Black.copy(0.5f), modifier = Modifier.weight(0.2f, fill = false)) {
                    Checkbox (checked = task.isDone, onCheckedChange = {
                        val updatedTask = task.copy(isDone = !task.isDone)
                        viewModel.updateTask(updatedTask)
                    })
                }
                // Right side
                Column (horizontalAlignment = Alignment.End, modifier = Modifier
                    .weight(0.8f, fill = true)
                    .padding(horizontal = 8.dp)) {
                    Text(text = task.title, style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Black))
                    Text(text = "Added on ${SimpleDateFormat("yy/MM/dd").format(Date(task.dateAdded))}")
                }
            }
            // Description & Edit button
            if (isActive) {
                Surface (color = MaterialTheme.colors.background, modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text(text = task.desc, style = TextStyle(fontWeight = FontWeight.Black), modifier = Modifier.padding(8.dp))
                        Button(onClick = { viewModel.dialogueState = Dialogue.MODIFY_TASK }) {
                            Text(text = "EDIT")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddTaskFAB(viewModel: TasksViewModel) {
    FloatingActionButton(
        onClick = {
            viewModel.dialogueState = Dialogue.ADD_TASK
        }
    ) {
        Icon(Icons.Filled.Add, contentDescription = null)
    }
}

enum class Dialogue {
    NONE, ADD_TASK, MODIFY_TASK
}

@Composable
fun AddTaskDialogue(viewModel: TasksViewModel) {
    // Temp. variable
    var title by remember { mutableStateOf("") }
    var titleError by remember { mutableStateOf(false) }
    var desc by remember { mutableStateOf("") }
    var descError by remember { mutableStateOf(false) }
    var currentTime = Date().time

    AlertDialog(
        onDismissRequest = { viewModel.dialogueState = Dialogue.NONE },
        confirmButton = {
            TextButton(
                onClick = {
                    if (!title.isEmpty() && !desc.isEmpty()) {
                        val newTask = Task(0, title, desc, false, currentTime, currentTime)
                        viewModel.addTask(newTask)
                        viewModel.dialogueState = Dialogue.NONE
                    }
                    else {
                        titleError = title.isEmpty()
                        descError = desc.isEmpty()
                    }
                }
            ) {
                Text("ADD")
            }
        },
        dismissButton = {
            TextButton( onClick = { viewModel.dialogueState = Dialogue.NONE } ) {
                Text("CANCEL")
            }
        },
        title = { Text("Add new Task") },
        text = {
            Column (modifier = Modifier.padding(4.dp)) {
                Text("Adding task on: ${SimpleDateFormat("yy/MM/dd HH:mm:ss").format(Date(currentTime))}")

                Spacer(modifier = Modifier.size(width = 0.dp, height = 8.dp))

                // Title field
                TextField(value = title, onValueChange = {title = it}, isError = titleError, placeholder = {
                    Text("New task", style = TextStyle(fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold))
                })

                Spacer(modifier = Modifier.size(width = 0.dp, height = 4.dp))

                // Description field
                TextField(value = desc, onValueChange = {desc = it}, isError = descError, placeholder = {
                    Text("Enter description here...", style = TextStyle(fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold))
                })
            }
        }
    )
}

@Composable
fun SetTaskDialogue(viewModel: TasksViewModel, currentTask: Task) {
    // Temp. variable
    var title by remember { mutableStateOf(currentTask.title) }
    var titleError by remember { mutableStateOf(false) }
    var desc by remember { mutableStateOf(currentTask.desc) }
    var descError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { viewModel.dialogueState = Dialogue.NONE },
        confirmButton = {
            TextButton(
                onClick = {
                    if (!title.isEmpty() && !desc.isEmpty()) {
                        val newTask = currentTask.copy(title=title, desc=desc)
                        viewModel.updateTask(newTask)
                        viewModel.dialogueState = Dialogue.NONE
                    }
                    else {
                        titleError = title.isEmpty()
                        descError = desc.isEmpty()
                    }
                }
            ) {
                Text("EDIT")
            }
        },
        dismissButton = {
            Row {
                TextButton( onClick = { viewModel.dialogueState = Dialogue.NONE } ) {
                    Text("CANCEL")
                }
                TextButton( onClick = { viewModel.dialogueState = Dialogue.NONE; viewModel.deleteTask(currentTask) } ) {
                    Text("DELETE", style = TextStyle(color = Color.Red))
                }
            }
        },
        title = { Text("Edit Task") },
        text = {
            Column (modifier = Modifier.padding(4.dp)) {
                Text("Task was created on: ${SimpleDateFormat("yy/MM/dd HH:mm:ss").format(Date(currentTask.dateAdded))}")

                Spacer(modifier = Modifier.size(width = 0.dp, height = 8.dp))

                // Title field
                TextField(value = title, onValueChange = {title = it}, isError = titleError, placeholder = {
                    Text("Task title", style = TextStyle(fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold))
                })

                Spacer(modifier = Modifier.size(width = 0.dp, height = 4.dp))

                // Description field
                TextField(value = desc, onValueChange = {desc = it}, isError = descError, placeholder = {
                    Text("Enter description here...", style = TextStyle(fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold))
                })
            }
        }
    )
}

@Preview(showBackground = true, widthDp = 320, heightDp = 320)
@Composable
fun AddTaskPreview() {
    val viewModel = TestViewModel()
    viewModel.fetchAllTasks()
    ComposeBootcampTheme {
        AddTaskDialogue(viewModel = viewModel)
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 320)
@Composable
fun TaskListPreview() {
    val viewModel = TestViewModel()
    viewModel.fetchAllTasks()
    ComposeBootcampTheme {
        TasksList(viewModel = viewModel)
    }
}