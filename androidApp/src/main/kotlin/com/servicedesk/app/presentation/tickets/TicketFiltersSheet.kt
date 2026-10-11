package com.servicedesk.app.presentation.tickets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.servicedesk.app.data.api.StaffBriefDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketFiltersSheet(
    visible: Boolean,
    filters: TicketFiltersState,
    isAdmin: Boolean,
    staff: List<StaffBriefDto>,
    onDismiss: () -> Unit,
    onFiltersChange: (TicketFiltersState) -> Unit,
    onApply: () -> Unit,
    onClear: () -> Unit,
) {
    if (!visible) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Text(text = "Фильтры заявок", style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = filters.startDate,
                onValueChange = { onFiltersChange(filters.copy(startDate = it)) },
                label = { Text("Дата с (ГГГГ-ММ-ДД)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                singleLine = true,
            )
            OutlinedTextField(
                value = filters.endDate,
                onValueChange = { onFiltersChange(filters.copy(endDate = it)) },
                label = { Text("Дата по") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                singleLine = true,
            )
            OutlinedTextField(
                value = filters.tag,
                onValueChange = { onFiltersChange(filters.copy(tag = it)) },
                label = { Text("Тег (подстрока)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                singleLine = true,
            )
            OutlinedTextField(
                value = filters.serialNumber,
                onValueChange = { onFiltersChange(filters.copy(serialNumber = it)) },
                label = { Text("Серийный номер") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                singleLine = true,
            )
            if (isAdmin) {
                OutlinedTextField(
                    value = filters.managerId?.toString() ?: "",
                    onValueChange = { raw ->
                        val id = raw.trim().toIntOrNull()
                        onFiltersChange(filters.copy(managerId = id))
                    },
                    label = { Text("ID исполнителя") },
                    placeholder = {
                        val hint = staff.firstOrNull()?.let { "${it.id} — ${it.displayName()}" }
                        Text(hint ?: "Список: GET /api/staff")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    singleLine = true,
                )
            }
            Button(
                onClick = onApply,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            ) {
                Text("Применить")
            }
            TextButton(
                onClick = onClear,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Сбросить фильтры")
            }
        }
    }
}
