package com.vin.composebootcamp

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.materialIcon
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vin.composebootcamp.ui.theme.ComposeBootcampTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeBootcampTheme {
                MainApp()
            }
        }
    }
}

/*
    Define bottom navigation bar's 'states'
 */
sealed class Screen(val dest: String, val title: String, @StringRes val resId: Int, val icon: ImageVector = Icons.Filled.Circle) {
    object MainScreen: Screen("main", "Oh my god... Dog!!!", R.string.nav_home, Icons.Filled.Home)
    object DogListScreen: Screen("doglist", "Dog list", R.string.nav_doglist, Icons.Filled.Pets)
}

@Composable
private fun MainApp() {
    // List for nav bar
    val navItems = listOf(
        Screen.MainScreen,
        Screen.DogListScreen,
    )
    val navController = rememberNavController()
    var topAppBarText by remember{ mutableStateOf("Oh my god! Dog!!!") }

    Scaffold(
        topBar = {
            TopAppBar (
                title = {
                    Text(topAppBarText)
                }
            )
        },
        bottomBar = {
            BottomNavigation {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                navItems.forEach { screen ->
                    BottomNavigationItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        selected = currentDestination?.route == screen.dest,
                        onClick = { // on navigation icon clicked
                            navController.navigate(screen.dest) { // switch to destination screen
                                // Prevent selecting navigation icon from flooding the backstack
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                // Don't let the user select the same thing and open it billion times
                                launchSingleTop = true
                                // And make sure to save the state between screens
                                restoreState = true
                            }
                            // Set title
                            topAppBarText = screen.title
                        },
                    )
                }
            }
        },
    ) { innerPadding ->
        // Navigation host will now handle the 'drawing' of screens
        NavHost(navController = navController, startDestination = Screen.MainScreen.dest, modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.MainScreen.dest) { MainScreen(navController, onContinueClicked = {}) }
            composable(Screen.DogListScreen.dest) { DogListScreen(navController) }
        }
//        MainScreen(
//            modifier = Modifier
//                .padding(innerPadding)
//                .fillMaxSize(),
//            onContinueClicked = { }
//        )
    }
}

/*
    Main menu screen
 */
@Composable
private fun MainScreen(navcontroller: NavController?, modifier: Modifier = Modifier, onContinueClicked: () -> Unit) {
    Column (
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(stringResource(R.string.menu_greetings))
        Button (
            onClick = onContinueClicked
        ) {
            Text(stringResource(R.string.menu_continue))
        }
    }
}

/*
    List of Dogs screen
 */
@Composable
private fun DogListScreen(
    navcontroller: NavController?,
    modifier: Modifier = Modifier,
    dogDataList: List<Dog> = List(42) {
        Dog("$it", "Dog #$it")
    }
) {
    LazyColumn (
        modifier = modifier.padding(vertical = 4.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center,
    ) {
        items(items = dogDataList) { dog ->
            DogCard(dog)
        }
    }
}

@Composable
private fun DogCard(dog: Dog, modifier: Modifier = Modifier) {
    Card (
        backgroundColor = MaterialTheme.colors.primary,
        elevation = 4.dp,
        modifier = modifier.padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Row ( modifier = Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically ) {
            // Dog image
            Image(Icons.Filled.Pets, contentDescription = null, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.width(8.dp))
            // Dog name
            Text(dog.name, modifier = Modifier.weight(0.9f), style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Black))
        }
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 320, uiMode = UI_MODE_NIGHT_YES, name = "DefaultPreviewDark")
@Preview(showBackground = true, widthDp = 320, heightDp = 320)
@Composable
fun DefaultPreview() {
    ComposeBootcampTheme {
        MainApp()
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 320, uiMode = UI_MODE_NIGHT_YES, name = "DogPreviewDark")
@Preview(showBackground = true, widthDp = 320, heightDp = 320)
@Composable
fun DogPreview() {
    ComposeBootcampTheme {
        DogListScreen(null)
    }
}