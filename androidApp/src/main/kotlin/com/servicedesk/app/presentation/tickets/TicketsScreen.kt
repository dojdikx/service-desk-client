package com.servicedesk.app.presentation.tickets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.servicedesk.app.data.api.TicketShortDto
import com.servicedesk.app.ui.theme.OnSurfaceMuted
import com.servicedesk.app.ui.theme.PrimaryBlue

@Composable
fun TicketsScreen(
    navController: NavController,
    onCreateTicket: () -> Unit,
    viewModel: TicketsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val refreshFlow = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow("tickets_refresh", false)
    LaunchedEffect(refreshFlow) {
        refreshFlow?.collect { shouldRefresh ->
            if (shouldRefresh) {
                viewModel.refresh()
                navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.set("tickets_refresh", false)
            }
        }
    }
    val visible = viewModel.visibleTickets()

    TicketFiltersSheet(
        visible = uiState.filtersSheetVisible,
        filters = uiState.filters,
        isAdmin = uiState.isAdmin,
        staff = uiState.staff,
        onDismiss = viewModel::closeFiltersSheet,
        onFiltersChange = viewModel::onDraftFiltersChange,
        onApply = viewModel::applyFilters,
        onClear = viewModel::clearFilters,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        Text(
            text = "Тикеты",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp, bottom = 12.dp),
        )

        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = viewModel::onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Поиск заявок") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                Text(
                    text = "MAX",
                    color = OnSurfaceMuted,
                    modifier = Modifier.padding(end = 8.dp),
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
        )

        LazyRow(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                StatusChip(
                    label = "Все",
                    selected = uiState.statusChip == TicketStatusChip.ALL,
                    onClick = { viewModel.onStatusChip(TicketStatusChip.ALL) },
                )
            }
            item {
                StatusChip(
                    label = "Первичный",
                    selected = uiState.statusChip == TicketStatusChip.PRIMARY,
                    onClick = { viewModel.onStatusChip(TicketStatusChip.PRIMARY) },
                )
            }
            item {
                StatusChip(
                    label = "В работе",
                    selected = uiState.statusChip == TicketStatusChip.IN_WORK,
                    onClick = { viewModel.onStatusChip(TicketStatusChip.IN_WORK) },
                )
            }
            item {
                FilterChip(
                    selected = uiState.filters.isActive,
                    onClick = viewModel::openFiltersSheet,
                    label = { Text("Фильтры") },
                )
            }
        }

        Button(
            onClick = onCreateTicket,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text("Новая заявка")
        }

        Spacer(modifier = Modifier.height(12.dp))

        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null -> {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(uiState.error ?: "", color = MaterialTheme.colorScheme.error)
                    Button(onClick = viewModel::refresh, modifier = Modifier.padding(top = 12.dp)) {
                        Text("Повторить")
                    }
                }
            }

            visible.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Заявок пока нет",
                        color = OnSurfaceMuted,
                    )
                }
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                ) {
                    items(visible, key = { it.ticketId }) { ticket ->
                        TicketCard(ticket = ticket, showAssignee = uiState.isAdmin)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
    )
}

@Composable
private fun TicketCard(ticket: TicketShortDto, showAssignee: Boolean) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarInitials(name = ticket.clientName ?: "?")
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = ticket.clientName ?: "Клиент",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (ticket.unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEF4444)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = ticket.unreadCount.toString(),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                )
                            }
                        }
                    }
                    Text(
                        text = ticket.lastMessage ?: ticket.serialNumber ?: "—",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
            Text(
                text = ticket.status,
                modifier = Modifier
                    .padding(top = 12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFEF3C7))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium,
            )
            if (showAssignee) {
                Text(
                    text = ticket.assigneeName ?: "Назначить сотрудника",
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFEFF6FF))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    color = if (ticket.assigneeName != null) PrimaryBlue else OnSurfaceMuted,
                )
            }
        }
    }
}

@Composable
private fun AvatarInitials(name: String) {
    val initials = name.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .map { it.first().uppercaseChar() }
        .joinToString("")
        .ifBlank { "?" }
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color(0xFFDBEAFE)),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = initials, color = PrimaryBlue, fontWeight = FontWeight.Bold)
    }
}
