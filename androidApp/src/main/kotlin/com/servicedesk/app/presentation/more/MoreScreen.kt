package com.servicedesk.app.presentation.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.servicedesk.app.R
import com.servicedesk.app.locale.AppLanguage
import com.servicedesk.app.presentation.locale.LocaleSettingsViewModel
import com.servicedesk.app.presentation.session.CurrentStaff

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    currentStaff: CurrentStaff?,
    onOpenTicketsExport: () -> Unit,
    onOpenStaff: () -> Unit,
    onSignOut: () -> Unit,
    localeViewModel: LocaleSettingsViewModel = hiltViewModel(),
) {
    val language by localeViewModel.language.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.more_title)) }) },
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
                            if (staff.isAdmin) {
                                stringResource(R.string.role_admin)
                            } else {
                                stringResource(R.string.role_agent)
                            },
                            style = MaterialTheme.typography.bodySmall,
                        )
                    },
                )
                HorizontalDivider()
            }

            Text(
                text = stringResource(R.string.more_language),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 8.dp),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                FilterChip(
                    selected = language == AppLanguage.RUSSIAN,
                    onClick = { localeViewModel.setLanguage(AppLanguage.RUSSIAN) },
                    label = { Text(stringResource(R.string.language_russian)) },
                    modifier = Modifier.padding(end = 8.dp),
                )
                FilterChip(
                    selected = language == AppLanguage.CHINESE,
                    onClick = { localeViewModel.setLanguage(AppLanguage.CHINESE) },
                    label = { Text(stringResource(R.string.language_chinese)) },
                )
            }

            HorizontalDivider(modifier = Modifier.padding(top = 12.dp))

            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenTicketsExport),
                headlineContent = { Text(stringResource(R.string.more_export)) },
                supportingContent = { Text(stringResource(R.string.more_export_subtitle)) },
            )
            if (currentStaff?.isAdmin == true) {
                ListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onOpenStaff),
                    headlineContent = { Text(stringResource(R.string.more_staff)) },
                    supportingContent = { Text(stringResource(R.string.more_staff_subtitle)) },
                )
            }
            Button(
                onClick = onSignOut,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Text(stringResource(R.string.action_sign_out))
            }
        }
    }
}
