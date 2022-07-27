package com.vin.composebootcamp

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

/*
    Define bottom navigation bar's 'states'
 */
sealed class Screen(val dest: String, val title: String, @StringRes val navName: Int = R.string.nav_default, val icon: ImageVector = Icons.Filled.Circle) {
    // Title / splash screen
    object SplashScreen: Screen("splash", "Welcome!", R.string.nav_home, Icons.Filled.Home)
    // List screen
    object ListScreen: Screen("list", "List screen", R.string.nav_list, Icons.Filled.Description)
}

// List for nav bar
val MAIN_NAV_ITEMS = listOf(
    Screen.SplashScreen,
    Screen.ListScreen
)