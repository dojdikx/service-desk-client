package com.servicedesk.app.presentation.tickets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.servicedesk.app.data.api.UserShortDto
import com.servicedesk.app.data.api.UsersApi
import com.servicedesk.app.domain.tickets.TicketsRepository
import com.servicedesk.app.presentation.auth.toAuthMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateTicketUiState(
    val isLoading: Boolean = true,
    val users: List<UserShortDto> = emptyList(),
    val selectedUserId: Int? = null,
    val reason: String = "",
    val isSaving: Boolean = false,
    val error: String? = null,
    val completed: Boolean = false,
)

@HiltViewModel
class CreateTicketViewModel @Inject constructor(
    private val usersApi: UsersApi,
    private val ticketsRepository: TicketsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateTicketUiState())
    val uiState: StateFlow<CreateTicketUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching { usersApi.list().users }
                .onSuccess { users ->
                    _uiState.update {
                        it.copy(isLoading = false, users = users, selectedUserId = users.firstOrNull()?.id)
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.toAuthMessage("Не удалось загрузить клиентов"),
                        )
                    }
                }
        }
    }

    fun onUserSelected(userId: Int) {
        _uiState.update { it.copy(selectedUserId = userId, error = null) }
    }

    fun onReasonChange(value: String) {
        _uiState.update { it.copy(reason = value, error = null) }
    }

    fun create() {
        val state = _uiState.value
        val userId = state.selectedUserId
        if (userId == null) {
            _uiState.update { it.copy(error = "Выберите клиента") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            runCatching {
                ticketsRepository.createTicket(
                    userId = userId,
                    reason = state.reason.trim().takeIf { it.isNotEmpty() },
                )
            }
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false, completed = true) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = e.toAuthMessage("Не удалось создать заявку"),
                        )
                    }
                }
        }
    }
}
