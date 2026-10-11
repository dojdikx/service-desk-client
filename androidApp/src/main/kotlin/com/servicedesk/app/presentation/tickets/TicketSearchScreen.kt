package com.servicedesk.app.presentation.tickets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.servicedesk.app.ui.theme.OnSurfaceMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketSearchScreen(
    onBack: () -> Unit,
    onOpenTicket: (Int) -> Unit,
    viewModel: TicketSearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Поиск заявок") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            OutlinedTextField(
                value = uiState.query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Имя, телефон, серийник, причина…") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
            )
            Button(
                onClick = viewModel::submitSearch,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            ) {
                Text("Найти")
            }

            when (uiState.phase) {
                TicketSearchPhase.LOADING -> {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 32.dp))
                }

                TicketSearchPhase.ERROR -> {
                    Column(
                        modifier = Modifier.padding(top = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(uiState.error ?: "", color = MaterialTheme.colorScheme.error)
                        Button(onClick = viewModel::retryLoad, modifier = Modifier.padding(top = 8.dp)) {
                            Text("Повторить")
                        }
                    }
                }

                TicketSearchPhase.IDLE -> {
                    if (uiState.history.isNotEmpty()) {
                        Text(
                            text = "Недавние запросы",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(top = 20.dp, bottom = 8.dp),
                        )
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(uiState.history) { item ->
                                FilterChip(
                                    selected = false,
                                    onClick = { viewModel.searchFromHistory(item) },
                                    label = { Text(item) },
                                )
                            }
                        }
                        TextButton(onClick = viewModel::clearHistory) {
                            Text("Очистить историю")
                        }
                    } else {
                        Text(
                            text = "Введите запрос и нажмите «Найти»",
                            color = OnSurfaceMuted,
                            modifier = Modifier.padding(top = 24.dp),
                        )
                    }
                }

                TicketSearchPhase.EMPTY -> {
                    Text(
                        text = "Ничего не найдено по запросу «${uiState.query}»",
                        color = OnSurfaceMuted,
                        modifier = Modifier.padding(top = 24.dp),
                    )
                }

                TicketSearchPhase.RESULTS -> {
                    Text(
                        text = "Найдено: ${uiState.results.size}",
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                    )
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(uiState.results, key = { it.ticketId }) { ticket ->
                            SearchResultRow(
                                title = ticket.clientName ?: "Клиент #${ticket.ticketId}",
                                subtitle = listOfNotNull(
                                    ticket.contactPhone,
                                    ticket.serialNumber,
                                    ticket.reason,
                                    ticket.status,
                                ).joinToString(" · "),
                                onClick = { onOpenTicket(ticket.ticketId) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultRow(title: String, subtitle: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        if (subtitle.isNotBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceMuted,
                maxLines = 2,
            )
        }
    }
}
