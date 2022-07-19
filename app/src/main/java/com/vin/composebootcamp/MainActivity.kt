package com.vin.composebootcamp

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.vin.composebootcamp.ui.theme.ComposeBootcampTheme
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
sealed class Screen(val dest: String, val title: String, @StringRes val navName: Int = R.string.nav_default, val icon: ImageVector = Icons.Filled.Circle) {
    object MainScreen: Screen("main", "Oh my god... Dog!!!", R.string.nav_home, Icons.Filled.Home)
    object DogListScreen: Screen("doglist", "Dog list", R.string.nav_doglist, Icons.Filled.Pets)
    object DogViewScreen: Screen("dogview", "Watch and learn", R.string.nav_default, Icons.Filled.Pets)
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

    var dogList = remember { mutableStateListOf<Dog>() }

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
                        label = { Text(stringResource(id = screen.navName)) },
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
        // Context sensitive floating action button
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    DogData.updateBreeds(
                        onFail = {
                            Log.e("DOG", "FAILED BREED UPDATE. MY GOD")
                        },
                        // Add all dogs info
                        onUpdateDone = { breeds ->
                            breeds.forEach {
                                dogList.add(Dog(it, "Dog: $it"))
                            }
                        }
                    )
                }
            ) {
                Icon(Icons.Filled.ArrowCircleUp, contentDescription = null)
            }
//            when (navController.currentDestination?.route) {
//                Screen.DogListScreen.dest -> {
//                    FloatingActionButton(onClick = { DogData.updateBreeds() }) {
//                        Image(Icons.Filled.ArrowCircleUp, contentDescription = null)
//                    }
//                }
//                else -> {}
//            }
        },
    ) { innerPadding ->
        // Navigation host will now handle the 'drawing' of screens
        NavHost(navController = navController, startDestination = Screen.MainScreen.dest, modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.MainScreen.dest) { MainScreen(navController, onContinueClicked = {}) }
            composable(Screen.DogListScreen.dest) { DogListScreen(navController, dogDataList = dogList) }
            composable(Screen.DogViewScreen.dest) { DogViewScreen(navController) }
        }
    }
}

/*
    Main menu screen
 */
@Composable
private fun MainScreen(navcontroller: NavController?, modifier: Modifier = Modifier, onContinueClicked: () -> Unit) {
    Column (
        modifier = modifier.fillMaxSize(),
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
    dogDataList: List<Dog>
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun DogCard(dog: Dog, modifier: Modifier = Modifier) {
    var imageSeed: Int by rememberSaveable { mutableStateOf(0) }
    var active: Boolean by rememberSaveable { mutableStateOf(false) }
    val dogNameResId = translateDogIdToStringRes(dog.id)
    val dogName = if (dogNameResId == 0) dog.name else stringResource(dogNameResId)
    Card (
        backgroundColor = MaterialTheme.colors.primary,
        elevation = 4.dp,
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .animateContentSize(),
        onClick = { active = !active },
    ) {
        Column {
            Row ( modifier = Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically ) {
                // Dog image
                DogImage(dog.id, imageSeed, Modifier.size(64.dp))
                Spacer(modifier = Modifier.width(8.dp))
                // Dog name
                Text(dogName, modifier = Modifier.weight(0.9f), style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Black))
            }
            if (active) {
                Spacer(modifier = Modifier.height(8.dp))
                DogViewScreen(null, dog = dog, onImageReload = { imageSeed = (Math.random()*128).toInt() })
            }
        }
    }
}

@Composable
private fun DogImage(dogId: String?, seed: Int, modifier: Modifier = Modifier) {
    var imgUrl by remember { mutableStateOf("https://picsum.photos/id/237/200") }
    var imgExists by remember { mutableStateOf(false) }
    val imgSeed = remember { mutableStateOf(-42) }
    if (imgSeed.value != seed) { // changed seed, reset image
        Log.e("DOG", "DogImage(): new seed $seed")
        imgSeed.apply { value = seed }
        imgExists = false

        // Request for image URL
        if (!LocalInspectionMode.current && dogId != null) {
            val response = DogApi.api.getRandomBreedImageUrl(dogId)
            response.enqueue(object: Callback<DogImageResponse> {
                override fun onResponse(
                    call: Call<DogImageResponse>,
                    response: Response<DogImageResponse>
                ) {
                    if (response.isSuccessful() && response.body()!!.status == "success") { // HTTP Success
                        // Update image URL
                        imgUrl = response.body()!!.url
                        imgExists = true
                    } else { // HTTP Failed
                        Log.e("DOG", "DogImage() API CALL FAILED! (failed response)")
                    }
                }
                override fun onFailure(call: Call<DogImageResponse>, t: Throwable) {
                    Log.e("DOG", "DogImage() API CALL FAILED!")
                }
            })
        }
    }

    if (imgExists) {
        val imageRequest = ImageRequest.Builder(LocalContext.current)
            .data(imgUrl)
            .crossfade(true)
            .build()
        AsyncImage(
            model = imageRequest,
            placeholder = rememberVectorPainter(image = Icons.Filled.Pets),
            error = rememberVectorPainter(image = Icons.Filled.Warning),
            contentDescription = null,
            modifier = modifier  //.size(64.dp)
        )
    } else {
        Icon(
            Icons.Filled.Pets,
            contentDescription = null,
            modifier = modifier
        )
    }
}

/*
    Dog view screen
 */
@Composable
private fun DogViewScreen(
    navcontroller: NavController?,
    modifier: Modifier = Modifier,
    dog: Dog? = null,
    onImageReload: () -> Unit = {}
) {
    val dogName: String = dog?.name ?: "NONE"
    val dogImg: Any = dog?.imgUrl ?: Icons.Filled.Pets

    var imgSeed by remember { mutableStateOf(0) }

    Column (modifier = Modifier
        .height(200.dp)
        .fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        DogImage(dog?.id, imgSeed,
            Modifier
                .weight(0.8f)
                .fillMaxWidth())
        OutlinedButton(
            onClick = {
                imgSeed = (Math.random()*128).toInt()
                onImageReload()
            },
            modifier = Modifier
                .weight(0.2f)
                .fillMaxWidth()
        ) {
            Icon(Icons.Filled.ArrowDropDownCircle, contentDescription = null)
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
        DogListScreen(null, dogDataList = List(42) {
            Dog("$it", "Dog #$it")
        })
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 320, uiMode = UI_MODE_NIGHT_YES, name = "DogViewPreviewDark")
@Preview(showBackground = true, widthDp = 320, heightDp = 320)
@Composable
fun DogViewPreview() {
    ComposeBootcampTheme {
        DogViewScreen(null, dog = Dog("corgi", "Corgi"))
    }
}