package com.servicedesk.app.presentation.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.servicedesk.app.domain.staff.StaffAdminRepository
import com.servicedesk.app.domain.staff.StaffMember
import com.servicedesk.app.domain.staff.StaffRegisterRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StaffUiState(
    val loading: Boolean = true,
    val staff: List<StaffMember> = emptyList(),
    val error: String? = null,
    val email: String = "",
    val password: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val registering: Boolean = false,
    val successMessage: String? = null,
)

@HiltViewModel
class StaffViewModel @Inject constructor(
    private val repository: StaffAdminRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StaffUiState())
    val uiState: StateFlow<StaffUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            runCatching { repository.listStaff() }
                .onSuccess { list -> _uiState.update { it.copy(loading = false, staff = list) } }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(loading = false, error = e.message ?: "Не удалось загрузить список")
                    }
                }
        }
    }

    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value) }
    fun onFirstNameChange(value: String) = _uiState.update { it.copy(firstName = value) }
    fun onLastNameChange(value: String) = _uiState.update { it.copy(lastName = value) }

    fun register() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.length < 6) {
            _uiState.update { it.copy(error = "Укажите email и пароль (не короче 6 символов)") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(registering = true, error = null, successMessage = null) }
            runCatching {
                repository.registerStaff(
                    StaffRegisterRequest(
                        email = state.email.trim(),
                        password = state.password,
                        firstName = state.firstName.trim(),
                        lastName = state.lastName.trim(),
                    ),
                )
            }
                .onSuccess { member ->
                    _uiState.update {
                        it.copy(
                            registering = false,
                            email = "",
                            password = "",
                            firstName = "",
                            lastName = "",
                            successMessage = "Добавлен: ${member.email}",
                            staff = it.staff + member,
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(registering = false, error = e.message ?: "Не удалось зарегистрировать")
                    }
                }
        }
    }
}
