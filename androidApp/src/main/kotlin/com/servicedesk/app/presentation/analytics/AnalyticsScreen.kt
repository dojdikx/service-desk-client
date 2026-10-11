package com.servicedesk.app.presentation.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.servicedesk.app.ui.theme.parseHexColor
import com.servicedesk.app.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.servicedesk.app.domain.analytics.AnalyticsReport
import com.servicedesk.app.presentation.export.savedDownloadMessage
import com.servicedesk.app.ui.theme.OnSurfaceMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(viewModel: AnalyticsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(uiState.excelFile) {
        val file = uiState.excelFile ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(savedDownloadMessage(context, file))
        viewModel.clearExcelFile()
    }

    LaunchedEffect(uiState.excelError) {
        val message = uiState.excelError ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.analytics_title)) })
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
                    text = stringResource(R.string.analytics_period),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            item {
                OutlinedTextField(
                    value = uiState.startDate,
                    onValueChange = viewModel::onStartDateChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.date_from)) },
                    placeholder = { Text(stringResource(R.string.date_format_hint)) },
                    singleLine = true,
                )
            }
            item {
                OutlinedTextField(
                    value = uiState.endDate,
                    onValueChange = viewModel::onEndDateChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.date_to)) },
                    placeholder = { Text(stringResource(R.string.date_format_hint)) },
                    singleLine = true,
                )
            }
            item {
                Text(
                    text = stringResource(R.string.analytics_details),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            item {
                ToggleRow(
                    label = stringResource(R.string.analytics_by_status),
                    checked = uiState.includeStatusBreakdown,
                    onCheckedChange = viewModel::onIncludeStatusChange,
                )
            }
            item {
                ToggleRow(
                    label = stringResource(R.string.analytics_by_models),
                    checked = uiState.includePrinterModels,
                    onCheckedChange = viewModel::onIncludePrinterChange,
                )
            }
            item {
                ToggleRow(
                    label = stringResource(R.string.analytics_tickets_list),
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
                        Text(stringResource(R.string.action_show))
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
                            Text(stringResource(R.string.action_excel))
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
                if (uiState.statusItems.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.analytics_statuses),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                    items(uiState.statusItems, key = { it.status }) { row ->
                        StatusBreakdownRow(item = row)
                    }
                }
                if (report.printerModelBreakdown.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.analytics_models),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                    val printerTotal = report.printerModelBreakdown.sumOf { it.count }.coerceAtLeast(1)
                    items(report.printerModelBreakdown, key = { it.printerModel }) { row ->
                        PrinterBreakdownRow(
                            title = row.printerModel,
                            count = row.count,
                            percent = (row.count * 100) / printerTotal,
                        )
                    }
                }
                if (report.ticketsBreakdown.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.analytics_tickets),
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
                        text = stringResource(R.string.analytics_idle),
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
                text = stringResource(R.string.analytics_created, report.ticketsCreated),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = stringResource(R.string.analytics_modified, report.ticketsModified),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun StatusBreakdownRow(item: StatusBreakdownItem) {
    val color = parseHexColor(item.colorHex)
    val track = MaterialTheme.colorScheme.surfaceVariant
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color),
            )
            Text(
                text = item.status,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp, end = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = item.count.toString(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "(${item.percent}%)",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceMuted,
                modifier = Modifier.padding(start = 6.dp),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(track),
        ) {
            if (item.percent > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(item.percent / 100f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(color),
                )
            }
        }
    }
}

@Composable
private fun PrinterBreakdownRow(title: String, count: Int, percent: Int) {
    val barColor = MaterialTheme.colorScheme.primary
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = count.toString(), style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "$percent%",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceMuted,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                if (percent > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(percent / 100f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(barColor),
                    )
                }
            }
        }
    }
}

