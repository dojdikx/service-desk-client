package com.servicedesk.app.presentation.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.servicedesk.app.R
import com.servicedesk.app.data.staff.StaffDirectoryRefreshBus
import com.servicedesk.app.domain.staff.StaffAdminRepository
import com.servicedesk.app.locale.AppStrings
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
    private val appStrings: AppStrings,
    private val staffDirectoryRefreshBus: StaffDirectoryRefreshBus,
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
                        it.copy(loading = false, error = e.message ?: appStrings.get(R.string.staff_error_list))
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
            _uiState.update { it.copy(error = appStrings.get(R.string.staff_validation)) }
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
                    staffDirectoryRefreshBus.notifyUpdated()
                    _uiState.update {
                        it.copy(
                            registering = false,
                            email = "",
                            password = "",
                            firstName = "",
                            lastName = "",
                            successMessage = appStrings.get(R.string.staff_added, member.email),
                            staff = it.staff + member,
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(registering = false, error = e.message ?: appStrings.get(R.string.staff_error_register))
                    }
                }
        }
    }
}
