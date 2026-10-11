package com.servicedesk.app.presentation.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.servicedesk.app.MainRoutes
import com.servicedesk.app.presentation.components.MainBottomBar
import com.servicedesk.app.presentation.analytics.AnalyticsScreen
import com.servicedesk.app.presentation.dialogs.ChatScreen
import com.servicedesk.app.presentation.dialogs.DialogsScreen
import com.servicedesk.app.presentation.export.TicketsExportScreen
import com.servicedesk.app.presentation.more.MoreScreen
import com.servicedesk.app.presentation.staff.StaffScreen
import com.servicedesk.app.presentation.tickets.CreateTicketScreen
import com.servicedesk.app.presentation.tickets.TicketDetailScreen
import com.servicedesk.app.presentation.tickets.TicketSearchScreen
import com.servicedesk.app.presentation.tickets.TicketsScreen
import com.servicedesk.app.presentation.session.SessionViewModel

@Composable
fun MainShell(sessionViewModel: SessionViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: MainRoutes.TICKETS
    val currentStaff by sessionViewModel.currentStaff.collectAsStateWithLifecycle()
    val showBottomBar = currentRoute != MainRoutes.NEW_TICKET &&
        currentRoute != MainRoutes.TICKET_SEARCH &&
        currentRoute != MainRoutes.TICKETS_EXPORT &&
        currentRoute != MainRoutes.STAFF &&
        currentRoute?.startsWith("tickets/detail") != true &&
        currentRoute?.startsWith("dialogs/chat") != true

    Scaffold(
        bottomBar = {
            if (!showBottomBar) return@Scaffold
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
            composable(MainRoutes.TICKETS) {
                TicketsScreen(
                    navController = navController,
                    onCreateTicket = { navController.navigate(MainRoutes.NEW_TICKET) },
                    onOpenTicket = { id ->
                        navController.navigate("tickets/detail/$id")
                    },
                    onOpenSearch = { navController.navigate(MainRoutes.TICKET_SEARCH) },
                )
            }
            composable(MainRoutes.TICKET_SEARCH) {
                TicketSearchScreen(
                    onBack = { navController.popBackStack() },
                    onOpenTicket = { id -> navController.navigate("tickets/detail/$id") },
                )
            }
            composable(
                route = MainRoutes.TICKET_DETAIL,
                arguments = listOf(navArgument("ticketId") { type = NavType.IntType }),
            ) {
                TicketDetailScreen(onBack = { navController.popBackStack() })
            }
            composable(MainRoutes.NEW_TICKET) {
                CreateTicketScreen(
                    onBack = { navController.popBackStack() },
                    onCreated = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("tickets_refresh", true)
                        navController.popBackStack()
                    },
                )
            }
            composable(MainRoutes.DIALOGS) {
                DialogsScreen(
                    navController = navController,
                    onOpenChat = { id -> navController.navigate("dialogs/chat/$id") },
                )
            }
            composable(
                route = MainRoutes.CHAT,
                arguments = listOf(navArgument("ticketId") { type = NavType.IntType }),
            ) {
                ChatScreen(
                    onBack = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("dialogs_refresh", true)
                        navController.popBackStack()
                    },
                )
            }
            composable(MainRoutes.ANALYTICS) { AnalyticsScreen() }
            composable(MainRoutes.MORE) {
                MoreScreen(
                    currentStaff = currentStaff,
                    onOpenTicketsExport = { navController.navigate(MainRoutes.TICKETS_EXPORT) },
                    onOpenStaff = { navController.navigate(MainRoutes.STAFF) },
                    onSignOut = sessionViewModel::signOut,
                )
            }
            composable(MainRoutes.TICKETS_EXPORT) {
                TicketsExportScreen(onBack = { navController.popBackStack() })
            }
            composable(MainRoutes.STAFF) {
                StaffScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
