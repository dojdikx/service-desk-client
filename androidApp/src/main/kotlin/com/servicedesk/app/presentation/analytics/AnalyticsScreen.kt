package com.servicedesk.app.presentation.analytics

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.servicedesk.app.domain.analytics.AnalyticsReport
import com.servicedesk.app.ui.theme.OnSurfaceMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(viewModel: AnalyticsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(uiState.excelFile) {
        val file = uiState.excelFile ?: return@LaunchedEffect
        runCatching { openAnalyticsExcel(context, file) }
            .onFailure {
                snackbarHostState.showSnackbar("Файл: ${file.name} (кэш приложения)")
            }
        viewModel.clearExcelFile()
    }

    LaunchedEffect(uiState.excelError) {
        val message = uiState.excelError ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Аналитика") })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = "Период",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            item {
                OutlinedTextField(
                    value = uiState.startDate,
                    onValueChange = viewModel::onStartDateChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("С") },
                    placeholder = { Text("ГГГГ-ММ-ДД") },
                    singleLine = true,
                )
            }
            item {
                OutlinedTextField(
                    value = uiState.endDate,
                    onValueChange = viewModel::onEndDateChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("По") },
                    placeholder = { Text("ГГГГ-ММ-ДД") },
                    singleLine = true,
                )
            }
            item {
                Text(
                    text = "Детализация",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            item {
                ToggleRow(
                    label = "По статусам",
                    checked = uiState.includeStatusBreakdown,
                    onCheckedChange = viewModel::onIncludeStatusChange,
                )
            }
            item {
                ToggleRow(
                    label = "По моделям принтеров",
                    checked = uiState.includePrinterModels,
                    onCheckedChange = viewModel::onIncludePrinterChange,
                )
            }
            item {
                ToggleRow(
                    label = "Список заявок",
                    checked = uiState.includeTickets,
                    onCheckedChange = viewModel::onIncludeTicketsChange,
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = viewModel::loadReport,
                        modifier = Modifier.weight(1f),
                        enabled = uiState.phase != AnalyticsPhase.LOADING,
                    ) {
                        Text("Показать")
                    }
                    OutlinedButton(
                        onClick = {
                            viewModel.downloadExcel()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.excelLoading,
                    ) {
                        if (uiState.excelLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(4.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text("Excel")
                        }
                    }
                }
            }

            if (uiState.phase == AnalyticsPhase.LOADING) {
                item {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
                }
            }

            uiState.error?.let { message ->
                if (uiState.phase == AnalyticsPhase.ERROR) {
                    item {
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }

            val report = uiState.report
            if (report != null) {
                item { SummaryCard(report) }
                if (report.statusBreakdown.isNotEmpty()) {
                    item {
                        Text(
                            text = "Статусы",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                    items(report.statusBreakdown) { row ->
                        BreakdownRow(title = row.status, count = row.count)
                    }
                }
                if (report.printerModelBreakdown.isNotEmpty()) {
                    item {
                        Text(
                            text = "Модели",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                    items(report.printerModelBreakdown) { row ->
                        BreakdownRow(title = row.printerModel, count = row.count)
                    }
                }
                if (report.ticketsBreakdown.isNotEmpty()) {
                    item {
                        Text(
                            text = "Заявки",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                    items(report.ticketsBreakdown) { ticket ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "#${ticket.id} · ${ticket.status}",
                                    style = MaterialTheme.typography.titleSmall,
                                )
                                ticket.serialNumber?.let {
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OnSurfaceMuted,
                                    )
                                }
                                ticket.reason?.takeIf { it.isNotBlank() }?.let {
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(top = 4.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (uiState.phase == AnalyticsPhase.IDLE) {
                item {
                    Text(
                        text = "Выберите период и нажмите «Показать» или «Excel»",
                        color = OnSurfaceMuted,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(text = label, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
private fun SummaryCard(report: AnalyticsReport) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "${report.startDate} — ${report.endDate}",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Создано: ${report.ticketsCreated}",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = "Изменено: ${report.ticketsModified}",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun BreakdownRow(title: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = title, modifier = Modifier.weight(1f))
        Text(text = count.toString(), style = MaterialTheme.typography.titleSmall)
    }
}

fun openAnalyticsExcel(context: android.content.Context, file: java.io.File) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file,
    )
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Открыть Excel"))
}
