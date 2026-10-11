package com.servicedesk.app.presentation.placeholders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.servicedesk.app.BuildConfig
import com.servicedesk.app.R

@Composable
fun TicketsPlaceholderScreen() = PlaceholderScreen(
    title = stringResource(R.string.nav_tickets),
    subtitle = stringResource(R.string.placeholder_tickets_subtitle),
)

@Composable
fun DialogsPlaceholderScreen() = PlaceholderScreen(
    title = stringResource(R.string.nav_dialogs),
    subtitle = stringResource(R.string.placeholder_dialogs_subtitle),
)

@Composable
fun AnalyticsPlaceholderScreen() = PlaceholderScreen(
    title = stringResource(R.string.nav_analytics),
    subtitle = stringResource(R.string.placeholder_analytics_subtitle),
)

@Composable
fun MorePlaceholderScreen(onSignOut: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = stringResource(R.string.more_title), style = MaterialTheme.typography.headlineMedium)
        Text(
            text = stringResource(R.string.placeholder_more_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
        Button(
            onClick = onSignOut,
            modifier = Modifier.padding(top = 24.dp),
        ) {
            Text(stringResource(R.string.action_sign_out))
        }
    }
}

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
