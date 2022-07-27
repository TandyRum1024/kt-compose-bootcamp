package com.vin.composebootcamp

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextGeometricTransform
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.vin.composebootcamp.ui.theme.ComposeBootcampTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class MainActivity : ComponentActivity() {
    // Viewmodel
    private val roomDB = Room.databaseBuilder(App.context(), TaskDB::class.java, TASK_TABLE).build()
    private val roomDBRepo = TaskRepo(roomDB.taskDAO())

    override fun onCreate(savedInstanceState: Bundle?) {
        val viewModel = ViewModelProvider(this, TaskViewModelFactory(roomDBRepo)).get(TasksViewModel::class.java) //TasksViewModel(roomDBRepo)

        super.onCreate(savedInstanceState)
        setContent {
            ComposeBootcampTheme {
                MainApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun MainApp(viewModel: TasksViewModel) {
    val navController = rememberNavController()
    var topAppBarText by remember{ mutableStateOf(Screen.SplashScreen.title) }

    // Navigation host will now handle the 'drawing' of screens
    NavHost(navController = navController, startDestination = Screen.SplashScreen.dest) {
        composable(Screen.SplashScreen.dest) { SplashScreen(
            viewModel = viewModel,
            onSplashEnd = {
                Log.e("SPLASH", "SPLASH!")
                navController.popBackStack(Screen.SplashScreen.dest, true)
                navController.navigate(Screen.ListScreen.dest) { // switch to destination screen
                    // Prevent selecting navigation icon from flooding the backstack
                    popUpTo(navController.graph.findStartDestination().id) { saveState = false }
                    // Don't let the user select the same thing and open it billion times
                    launchSingleTop = true
                    // And make sure to save the state between screens
                    restoreState = false
                }
            }
        )}
        composable(Screen.ListScreen.dest) { ListScreen(viewModel = viewModel) }
    }
}

/*
    Splash screen
 */
@Composable
private fun SplashScreen(
    modifier: Modifier = Modifier,
    viewModel: TasksViewModel,
    onSplashEnd: () -> Unit = {}
) {
    LaunchedEffect(viewModel.isSplashDone) {
        if (!viewModel.isSplashDone) {
            // Splash logic
            delay(3000)
            // Splash end logic
            viewModel.isSplashDone = true
            onSplashEnd()
        }
    }

    Surface (
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colors.primary,
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center,
    ) {
            Text(
                text = "TASK\nWRAN\nGLER",
                style = MaterialTheme.typography.h2.copy(fontWeight = FontWeight(1000), color = Color.Black, textGeometricTransform = TextGeometricTransform(skewX = 0.75f)),
                modifier = Modifier.rotate(-45f).fillMaxSize().wrapContentHeight(),
                textAlign = TextAlign.Center
            )
            Text(
                text = "TASK\nWRAN\nGLER",
                style = MaterialTheme.typography.h2.copy(fontWeight = FontWeight(1000), color = Color.White),
                modifier = Modifier.rotate(-45f).fillMaxSize().wrapContentHeight(),
                textAlign = TextAlign.Center
            )
    }
}

/*
    The task list screen
*/
@Composable
private fun ListScreen(
    modifier: Modifier = Modifier,
    viewModel: TasksViewModel,
    onTaskAddButton: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        Log.e("YEAH", "FEATCHING ALL TASKS...")
        viewModel.fetchAllTasks()
    }

    Scaffold(
        topBar = { TopAppBar (title = { Text("Your tasks") }) },
        floatingActionButton = {  AddTaskFAB(viewModel = viewModel) },
    ) { innerPadding ->
        TasksList(padding = innerPadding, viewModel)
    }
    // Dialogue
    when (viewModel.dialogueState) {
        Dialogue.NONE -> {}
        Dialogue.ADD_TASK -> { AddTaskDialogue(viewModel = viewModel) }
        Dialogue.MODIFY_TASK -> { SetTaskDialogue(viewModel = viewModel, currentTask = viewModel.task) }
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 320, uiMode = UI_MODE_NIGHT_YES, name = "DefaultPreviewDark")
@Preview(showBackground = true, widthDp = 320, heightDp = 320)
@Composable
fun DefaultPreview() {
    val viewModel = TestViewModel()
    viewModel.fetchAllTasks()
    ComposeBootcampTheme {
        MainApp(viewModel = viewModel)
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 320, uiMode = UI_MODE_NIGHT_YES, name = "SplashPreviewDark")
@Preview(showBackground = true, widthDp = 320, heightDp = 320)
@Composable
fun SplashPreview() {
    val viewModel = TestViewModel()
    viewModel.fetchAllTasks()
    ComposeBootcampTheme {
        SplashScreen(viewModel = viewModel)
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 320, uiMode = UI_MODE_NIGHT_YES, name = "ListPreviewDark")
@Preview(showBackground = true, widthDp = 320, heightDp = 320)
@Composable
fun ListPreview() {
    val viewModel = TestViewModel()
    viewModel.fetchAllTasks()
    ComposeBootcampTheme {
        ListScreen(viewModel = viewModel)
    }
}