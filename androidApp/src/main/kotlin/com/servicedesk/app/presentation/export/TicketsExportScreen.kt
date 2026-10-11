package com.servicedesk.app.presentation.export

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.servicedesk.app.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.servicedesk.app.presentation.analytics.openAnalyticsExcel
import com.servicedesk.app.ui.theme.OnSurfaceMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketsExportScreen(
    onBack: () -> Unit,
    viewModel: TicketsExportViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(uiState.exportedFile) {
        val file = uiState.exportedFile ?: return@LaunchedEffect
        runCatching { openAnalyticsExcel(context, file) }
            .onFailure {
                snackbarHostState.showSnackbar(
                    context.getString(R.string.export_file_saved, file.name),
                )
            }
        viewModel.clearExportedFile()
    }

    LaunchedEffect(uiState.error) {
        val message = uiState.error ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.export_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
        ) {
            Text(
                text = stringResource(R.string.export_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceMuted,
            )
            OutlinedTextField(
                value = uiState.startDate,
                onValueChange = viewModel::onStartDateChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                label = { Text(stringResource(R.string.date_from)) },
                singleLine = true,
            )
            OutlinedTextField(
                value = uiState.endDate,
                onValueChange = viewModel::onEndDateChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                label = { Text(stringResource(R.string.date_to)) },
                singleLine = true,
            )
            Button(
                onClick = viewModel::export,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                enabled = !uiState.loading,
            ) {
                if (uiState.loading) {
                    CircularProgressIndicator(strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.export_generate))
                }
            }
        }
    }
}
