package com.servicedesk.app.presentation.tickets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.servicedesk.app.R
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
    onOpenTicket: (Int) -> Unit,
    onOpenSearch: () -> Unit,
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
    val kanban = viewModel.kanbanColumns()

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
            text = stringResource(R.string.tickets_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp, bottom = 12.dp),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenSearch),
        ) {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.tickets_search_hint)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    Text(
                        text = stringResource(R.string.tickets_channel_max),
                        color = OnSurfaceMuted,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                },
                enabled = false,
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
            )
        }

        Row(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            FilterChip(
                selected = uiState.filters.isActive,
                onClick = viewModel::openFiltersSheet,
                label = { Text(stringResource(R.string.tickets_filters)) },
            )
        }

        Button(
            onClick = onCreateTicket,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(stringResource(R.string.tickets_new))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(modifier = Modifier.weight(1f)) {
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
                        Text(stringResource(R.string.action_retry))
                    }
                }
            }

            uiState.tickets.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.tickets_empty),
                        color = OnSurfaceMuted,
                    )
                }
            }

            else -> {
                TicketsKanbanBoard(
                    columns = kanban,
                    showAssignee = uiState.isAdmin,
                    onOpenTicket = onOpenTicket,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        }
    }
}
