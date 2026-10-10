package com.servicedesk.app.presentation.placeholders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.servicedesk.app.BuildConfig

@Composable
fun TicketsPlaceholderScreen() = PlaceholderScreen(
    title = "Тикеты",
    subtitle = "Канбан и фильтры появятся в следующих коммитах",
)

@Composable
fun DialogsPlaceholderScreen() = PlaceholderScreen(
    title = "Диалоги",
    subtitle = "Список чатов с клиентами MAX",
)

@Composable
fun AnalyticsPlaceholderScreen() = PlaceholderScreen(
    title = "Аналитика",
    subtitle = "Отчёты за период",
)

@Composable
fun MorePlaceholderScreen() = PlaceholderScreen(
    title = "Ещё",
    subtitle = "Выгрузка, сотрудники, тема, выход",
)

@Composable
private fun PlaceholderScreen(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            text = "API: ${BuildConfig.SERVER_BASE_URL}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}
