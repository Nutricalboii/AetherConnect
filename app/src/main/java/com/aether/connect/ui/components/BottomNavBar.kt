package com.aether.connect.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.aether.connect.ui.theme.AetherCyan

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : BottomNavItem("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    object Devices : BottomNavItem("devices", "Devices", Icons.Filled.Radar, Icons.Outlined.Radar)
    object Transfer : BottomNavItem("transfer", "Transfer", Icons.Filled.SwapHoriz, Icons.Outlined.SwapHoriz)
    object Clipboard : BottomNavItem("clipboard", "Clipboard", Icons.Filled.ContentPaste, Icons.Outlined.ContentPaste)
    object Settings : BottomNavItem("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@Composable
fun BottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Devices,
        BottomNavItem.Transfer,
        BottomNavItem.Clipboard,
        BottomNavItem.Settings
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.title
                    )
                },
                label = {
                    Text(text = item.title, style = MaterialTheme.typography.labelSmall)
                },
                selected = selected,
                onClick = { onNavigate(item.route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AetherCyan,
                    selectedTextColor = AetherCyan,
                    indicatorColor = AetherCyan.copy(alpha = 0.12f)
                )
            )
        }
    }
}
