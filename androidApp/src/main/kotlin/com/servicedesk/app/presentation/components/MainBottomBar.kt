package com.servicedesk.app.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.servicedesk.app.MainRoutes

@Composable
fun MainBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == MainRoutes.TICKETS,
            onClick = { onNavigate(MainRoutes.TICKETS) },
            icon = { Icon(Icons.Default.ConfirmationNumber, contentDescription = null) },
            label = { Text("Тикеты") },
        )
        NavigationBarItem(
            selected = currentRoute == MainRoutes.DIALOGS,
            onClick = { onNavigate(MainRoutes.DIALOGS) },
            icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null) },
            label = { Text("Диалоги") },
        )
        NavigationBarItem(
            selected = currentRoute == MainRoutes.ANALYTICS,
            onClick = { onNavigate(MainRoutes.ANALYTICS) },
            icon = { Icon(Icons.Default.Analytics, contentDescription = null) },
            label = { Text("Аналитика") },
        )
        NavigationBarItem(
            selected = currentRoute == MainRoutes.MORE,
            onClick = { onNavigate(MainRoutes.MORE) },
            icon = { Icon(Icons.Default.MoreHoriz, contentDescription = null) },
            label = { Text("Ещё") },
        )
    }
}
