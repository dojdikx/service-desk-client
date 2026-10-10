package com.servicedesk.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.servicedesk.app.presentation.components.MainBottomBar
import com.servicedesk.app.presentation.placeholders.AnalyticsPlaceholderScreen
import com.servicedesk.app.presentation.placeholders.DialogsPlaceholderScreen
import com.servicedesk.app.presentation.placeholders.MorePlaceholderScreen
import com.servicedesk.app.presentation.placeholders.TicketsPlaceholderScreen

object MainRoutes {
    const val TICKETS = "tickets"
    const val DIALOGS = "dialogs"
    const val ANALYTICS = "analytics"
    const val MORE = "more"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: MainRoutes.TICKETS

    Scaffold(
        bottomBar = {
            MainBottomBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        },
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = MainRoutes.TICKETS,
            modifier = Modifier.padding(paddingValues),
        ) {
            composable(MainRoutes.TICKETS) { TicketsPlaceholderScreen() }
            composable(MainRoutes.DIALOGS) { DialogsPlaceholderScreen() }
            composable(MainRoutes.ANALYTICS) { AnalyticsPlaceholderScreen() }
            composable(MainRoutes.MORE) { MorePlaceholderScreen() }
        }
    }
}
