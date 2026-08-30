package com.ghostpin.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ghostpin.app.ui.home.HomeScreen
import com.ghostpin.app.ui.home.HomeViewModel
import com.ghostpin.app.ui.journey.JourneyScreen
import com.ghostpin.app.ui.journey.JourneyViewModel
import com.ghostpin.app.ui.map.MapPickerScreen
import com.ghostpin.app.ui.map.MapPickerViewModel
import com.ghostpin.app.ui.navigation.Screen
import com.ghostpin.app.ui.saved.SavedLocationsScreen
import com.ghostpin.app.ui.saved.SavedLocationsViewModel
import com.ghostpin.app.ui.settings.SettingsScreen
import com.ghostpin.app.ui.settings.SettingsViewModel
import com.ghostpin.app.ui.theme.GhostPinTheme
import com.ghostpin.app.ui.theme.MonoBorderSubtle
import com.ghostpin.app.ui.theme.MonoSurface
import com.ghostpin.app.ui.theme.MonoSurfaceVariant
import com.ghostpin.app.ui.theme.MonoTextPrimary
import com.ghostpin.app.ui.theme.MonoTextSecondary
import com.ghostpin.app.ui.theme.MonoWhite

class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // Permissions handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as GhostPinApplication

        val homeViewModel: HomeViewModel by viewModels {
            HomeViewModel.Factory(
                app.mockLocationController,
                app.locationRepository,
                app.settingsRepository
            )
        }

        val mapPickerViewModel: MapPickerViewModel by viewModels {
            MapPickerViewModel.Factory(
                app.locationRepository,
                app.settingsRepository
            )
        }

        val journeyViewModel: JourneyViewModel by viewModels {
            JourneyViewModel.Factory(
                app.journeyRepository,
                app.locationRepository
            )
        }

        val savedLocationsViewModel: SavedLocationsViewModel by viewModels {
            SavedLocationsViewModel.Factory(
                app.locationRepository,
                app.settingsRepository
            )
        }

        val settingsViewModel: SettingsViewModel by viewModels {
            SettingsViewModel.Factory(
                app.mockLocationController,
                app.settingsRepository
            )
        }

        requestRequiredPermissions()

        setContent {
            GhostPinTheme {
                GhostPinMainScaffold(
                    homeViewModel = homeViewModel,
                    mapPickerViewModel = mapPickerViewModel,
                    journeyViewModel = journeyViewModel,
                    savedLocationsViewModel = savedLocationsViewModel,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }

    private fun requestRequiredPermissions() {
        val permissionsToRequest = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val missingPermissions = permissionsToRequest.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            permissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }
}

@Composable
fun GhostPinMainScaffold(
    homeViewModel: HomeViewModel,
    mapPickerViewModel: MapPickerViewModel,
    journeyViewModel: JourneyViewModel,
    savedLocationsViewModel: SavedLocationsViewModel,
    settingsViewModel: SettingsViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MonoSurface,
                contentColor = MonoTextPrimary
            ) {
                Screen.bottomNavItems.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(screen.icon, contentDescription = screen.title)
                        },
                        label = {
                            Text(screen.title)
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MonoWhite,
                            selectedTextColor = MonoWhite,
                            unselectedIconColor = MonoTextSecondary,
                            unselectedTextColor = MonoTextSecondary,
                            indicatorColor = MonoSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToMap = { navController.navigate(Screen.MapPicker.route) },
                    onNavigateToJourney = { navController.navigate(Screen.Journey.route) },
                    onNavigateToSaved = { navController.navigate(Screen.SavedLocations.route) }
                )
            }
            composable(Screen.MapPicker.route) {
                MapPickerScreen(
                    viewModel = mapPickerViewModel,
                    onNavigateBackToHome = { navController.navigate(Screen.Home.route) }
                )
            }
            composable(Screen.Journey.route) {
                JourneyScreen(
                    viewModel = journeyViewModel,
                    onNavigateToHome = { navController.navigate(Screen.Home.route) }
                )
            }
            composable(Screen.SavedLocations.route) {
                SavedLocationsScreen(
                    viewModel = savedLocationsViewModel,
                    onLocationActivated = { navController.navigate(Screen.Home.route) }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = settingsViewModel
                )
            }
        }
    }
}
