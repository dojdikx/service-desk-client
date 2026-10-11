package com.servicedesk.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.servicedesk.app.R
import com.servicedesk.app.domain.auth.StaffAuthRepository
import com.servicedesk.app.locale.AppStrings
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val password: String = "",
    val passwordRepeat: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: StaffAuthRepository,
    private val appStrings: AppStrings,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, error = null) }
    fun onFirstNameChange(value: String) = _uiState.update { it.copy(firstName = value, error = null) }
    fun onLastNameChange(value: String) = _uiState.update { it.copy(lastName = value, error = null) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, error = null) }
    fun onPasswordRepeatChange(value: String) =
        _uiState.update { it.copy(passwordRepeat = value, error = null) }

    fun register(onSuccess: () -> Unit) {
        val state = _uiState.value
        val email = state.email.trim()
        if (email.isBlank()) {
            _uiState.update { it.copy(error = "Укажите почту") }
            return
        }
        if (state.password.length < 6) {
            _uiState.update { it.copy(error = "Пароль не короче 6 символов") }
            return
        }
        if (state.password != state.passwordRepeat) {
            _uiState.update { it.copy(error = "Пароли не совпадают") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                authRepository.signUp(
                    email = email,
                    password = state.password,
                    firstName = state.firstName.trim(),
                    lastName = state.lastName.trim(),
                )
            }
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.toAuthMessage(appStrings, R.string.staff_error_register),
                        )
                    }
                }
        }
    }
}
