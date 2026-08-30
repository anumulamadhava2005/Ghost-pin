package com.ghostpin.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "Switchboard", Icons.Default.Home)
    data object MapPicker : Screen("map_picker", "Map", Icons.Default.Place)
    data object Journey : Screen("journey", "Journey", Icons.Default.AltRoute)
    data object SavedLocations : Screen("saved_locations", "Saved", Icons.Default.Bookmark)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)

    companion object {
        val bottomNavItems = listOf(Home, MapPicker, Journey, SavedLocations, Settings)
    }
}
