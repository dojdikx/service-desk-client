package com.servicedesk.app.presentation.staff

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.servicedesk.app.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.servicedesk.app.presentation.components.PasswordTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffScreen(
    onBack: () -> Unit,
    viewModel: StaffViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.staff_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.staff_new),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
            item {
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = viewModel::onEmailChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.field_email)) },
                    singleLine = true,
                )
            }
            item {
                PasswordTextField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    label = stringResource(R.string.auth_password),
                )
            }
            item {
                OutlinedTextField(
                    value = uiState.firstName,
                    onValueChange = viewModel::onFirstNameChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    label = { Text(stringResource(R.string.staff_first_name)) },
                    singleLine = true,
                )
            }
            item {
                OutlinedTextField(
                    value = uiState.lastName,
                    onValueChange = viewModel::onLastNameChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    label = { Text(stringResource(R.string.staff_last_name)) },
                    singleLine = true,
                )
            }
            item {
                Button(
                    onClick = viewModel::register,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    enabled = !uiState.registering,
                ) {
                    if (uiState.registering) {
                        CircularProgressIndicator(strokeWidth = 2.dp)
                    } else {
                        Text(stringResource(R.string.staff_register))
                    }
                }
            }
            uiState.successMessage?.let { message ->
                item {
                    Text(text = message, color = MaterialTheme.colorScheme.primary)
                }
            }
            uiState.error?.let { message ->
                item {
                    Text(text = message, color = MaterialTheme.colorScheme.error)
                }
            }
            item {
                Text(
                    text = stringResource(R.string.staff_list),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                )
            }
            if (uiState.loading) {
                item { CircularProgressIndicator(modifier = Modifier.padding(8.dp)) }
            } else {
                items(uiState.staff) { member ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = member.email, style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = member.displayName(),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(
                                text = if (member.role == "admin") {
                                    stringResource(R.string.role_admin)
                                } else {
                                    stringResource(R.string.role_agent)
                                },
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun com.servicedesk.app.domain.staff.StaffMember.displayName(): String =
    listOfNotNull(firstName, lastName).joinToString(" ").ifBlank { email }
