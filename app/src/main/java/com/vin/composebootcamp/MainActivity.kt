package com.vin.composebootcamp

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.materialIcon
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.*
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

@Composable
private fun FancyBox(title: String, content: String = LoremIpsum(16).values.joinToString()) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(100.dp)) {
        Surface(color = MaterialTheme.colors.primary, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp, 8.dp)) {
            Text(text = "<$title>", modifier = Modifier.padding(4.dp), textAlign = TextAlign.Center)
        }
        Surface(color = MaterialTheme.colors.secondary, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(0.dp, 0.dp, 8.dp, 8.dp)) {
            Text(text = content, modifier = Modifier.padding(4.dp), textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun OldmanGreeting(name: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(color = MaterialTheme.colors.primary, modifier = Modifier.fillMaxWidth()) {
            Text(text = "$name! You will be boiled.", modifier = Modifier.padding(vertical = 4.dp), textAlign = TextAlign.Center)
        }
        Surface(color = MaterialTheme.colors.background, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                FancyBox(title = "Test stuff", "MORB")
                FancyBox(title = "2 stuff 2")
                FancyBox(title = "3 stuff 3")
            }
        }
    }
}

@Composable
private fun Greeting(name: String) {
    var active by rememberSaveable { mutableStateOf(false) }
    //val paddingOff by animateDpAsState(if (active) 64.dp else 0.dp) // basic
    val paddingOff by animateDpAsState( // pro gamer edition
        if (active) 64.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    )
    Card(
        backgroundColor = MaterialTheme.colors.primary,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        elevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .animateContentSize(spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium))
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Hello, $name!",
                    style = MaterialTheme.typography.h4.copy(
                        fontWeight = FontWeight.Black
                    )
                )
                Text("Glad to meet you.")
                if (active) Text(LoremIpsum(24).values.joinToString())
            }
            IconButton(
                onClick = { active = !active }
            ) {
                //Text(if (active) "See less" else "See more")
                if (!active) {
                    Icon(
                        Icons.Filled.ExpandMore,
                        contentDescription = "Show more"
                    )
                }
                else {
                    Icon(
                        Icons.Filled.ExpandLess,
                        contentDescription = "Show less"
                    )
                }
            }
        }
    }
}

@Composable
private fun GreetingScreen(names: List<String> = List(1000) {"Man $it"}) {
    Surface(
        color = MaterialTheme.colors.background,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        LazyColumn {
            items(items = names) { name -> Greeting(name = name) }
        }
    }
}

@Composable
private fun OnboardingScreen(onOnboarded: () -> Unit) {
    Surface {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Welcome to the Basics Codelab!")
            Button(
                modifier = Modifier.padding(vertical = 24.dp),
                onClick = onOnboarded
            ) {
                Text("Continue")
            }
        }
    }
}

@Composable
private fun MainApp() {
    var shouldShowOnboarding by rememberSaveable { mutableStateOf(true) }

    if (shouldShowOnboarding) {
        OnboardingScreen(onOnboarded = {
            shouldShowOnboarding = false
        })
    }
    else {
        GreetingScreen()
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 320)
@Composable
fun OnboardingPreview() {
    ComposeBootcampTheme {
        OnboardingScreen({})
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 320, uiMode = UI_MODE_NIGHT_YES, name = "GreetingPreviewDark")
@Preview(showBackground = true, widthDp = 320, heightDp = 320)
@Composable
fun GreetingPreview() {
    ComposeBootcampTheme {
        GreetingScreen()
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
