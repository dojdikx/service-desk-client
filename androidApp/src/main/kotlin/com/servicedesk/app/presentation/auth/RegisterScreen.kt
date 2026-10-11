package com.servicedesk.app.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.servicedesk.app.R
import com.servicedesk.app.presentation.components.PasswordTextField
import com.servicedesk.app.ui.theme.BackgroundLight
import com.servicedesk.app.ui.theme.OnSurfaceMuted
import com.servicedesk.app.ui.theme.PrimaryBlue

@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onRegistered: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
    ) {
        TextButton(onClick = onNavigateBack, enabled = !uiState.isLoading) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            Text(stringResource(R.string.action_back), modifier = Modifier.padding(start = 4.dp))
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
                Text(
                    text = stringResource(R.string.register_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(R.string.register_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceMuted,
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
                )

                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = viewModel::onEmailChange,
                    label = { Text(stringResource(R.string.auth_email)) },
                    placeholder = { Text(stringResource(R.string.register_email_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !uiState.isLoading,
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = uiState.firstName,
                    onValueChange = viewModel::onFirstNameChange,
                    label = { Text(stringResource(R.string.staff_first_name)) },
                    placeholder = { Text(stringResource(R.string.register_first_name_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !uiState.isLoading,
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = uiState.lastName,
                    onValueChange = viewModel::onLastNameChange,
                    label = { Text(stringResource(R.string.staff_last_name)) },
                    placeholder = { Text(stringResource(R.string.register_last_name_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !uiState.isLoading,
                )
                Spacer(modifier = Modifier.height(12.dp))
                PasswordTextField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChange,
                    label = stringResource(R.string.auth_password),
                    placeholder = stringResource(R.string.auth_password),
                    enabled = !uiState.isLoading,
                )
                Spacer(modifier = Modifier.height(12.dp))
                PasswordTextField(
                    value = uiState.passwordRepeat,
                    onValueChange = viewModel::onPasswordRepeatChange,
                    label = stringResource(R.string.register_password_repeat),
                    placeholder = stringResource(R.string.register_password_repeat_placeholder),
                    enabled = !uiState.isLoading,
                )

                uiState.error?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { viewModel.register(onRegistered) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(stringResource(R.string.register_create_account))
                    }
                }
                TextButton(
                    onClick = onNavigateBack,
                    enabled = !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                ) {
                    Text(stringResource(R.string.register_have_account), color = PrimaryBlue)
                }
            }
        }
    }
}
