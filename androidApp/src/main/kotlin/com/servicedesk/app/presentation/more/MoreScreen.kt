package com.servicedesk.app.presentation.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.servicedesk.app.presentation.session.CurrentStaff

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    currentStaff: CurrentStaff?,
    onOpenTicketsExport: () -> Unit,
    onOpenStaff: () -> Unit,
    onSignOut: () -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Ещё") }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 8.dp),
        ) {
            currentStaff?.let { staff ->
                ListItem(
                    headlineContent = { Text(staff.email) },
                    supportingContent = {
                        Text(
                            if (staff.isAdmin) "Администратор" else "Оператор",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    },
                )
                HorizontalDivider()
            }
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenTicketsExport),
                headlineContent = { Text("Выгрузка заявок") },
                supportingContent = { Text("Excel за период для внешней системы") },
            )
            if (currentStaff?.isAdmin == true) {
                ListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onOpenStaff),
                    headlineContent = { Text("Сотрудники") },
                    supportingContent = { Text("Список и регистрация") },
                )
            }
            Button(
                onClick = onSignOut,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Text("Выйти")
            }
        }
    }
}
