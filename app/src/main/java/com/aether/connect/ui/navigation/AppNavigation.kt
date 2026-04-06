package com.aether.connect.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aether.connect.service.AetherService
import com.aether.connect.ui.components.BottomNavBar
import com.aether.connect.ui.components.BottomNavItem
import com.aether.connect.ui.screens.*

@Composable
fun AppNavigation(service: AetherService) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: BottomNavItem.Home.route

    // Hide bottom bar on specialized screens
    val hideBottomBarRoutes = listOf("pairing", "casting", "remote_input")
    val showBottomBar = !hideBottomBarRoutes.any { currentRoute.startsWith(it) }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(BottomNavItem.Home.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(BottomNavItem.Home.route) {
                HomeScreen(
                    onNavigateToDevices = {
                        navController.navigate(BottomNavItem.Devices.route)
                    },
                    onNavigateToTransfer = {
                        navController.navigate(BottomNavItem.Transfer.route)
                    },
                    onNavigateToClipboard = {
                        navController.navigate(BottomNavItem.Clipboard.route)
                    },
                    onNavigateToPairing = {
                        navController.navigate("pairing")
                    }
                )
            }

            composable(BottomNavItem.Devices.route) {
                DevicesScreen(
                    onDeviceClick = { device ->
                        // Navigate to device details or select for casting/input
                    }
                )
            }

            composable(BottomNavItem.Transfer.route) {
                TransferScreen()
            }

            composable(BottomNavItem.Clipboard.route) {
                ClipboardScreen()
            }

            composable(BottomNavItem.Settings.route) {
                SettingsScreen()
            }

            composable("pairing") {
                PairingScreen()
            }

            composable(
                route = "casting/{peerId}",
                arguments = listOf(navArgument("peerId") { type = NavType.StringType })
            ) { backStackEntry ->
                val peerId = backStackEntry.arguments?.getString("peerId") ?: ""
                CastingScreen(service, peerId)
            }

            composable(
                route = "remote_input/{peerId}",
                arguments = listOf(navArgument("peerId") { type = NavType.StringType })
            ) { backStackEntry ->
                val peerId = backStackEntry.arguments?.getString("peerId") ?: ""
                RemoteInputScreen(service, peerId)
            }
        }
    }
}
