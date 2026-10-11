package com.servicedesk.app.presentation.dialogs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.servicedesk.app.data.api.TicketShortDto
import com.servicedesk.app.domain.messages.MessagesRepository
import com.servicedesk.app.domain.tickets.TicketsRepository
import com.servicedesk.app.presentation.auth.toAuthMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DialogsUiState(
    val isLoading: Boolean = true,
    val tickets: List<TicketShortDto> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class DialogsViewModel @Inject constructor(
    private val messagesRepository: MessagesRepository,
    private val ticketsRepository: TicketsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DialogsUiState())
    val uiState: StateFlow<DialogsUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                val dialogIds = messagesRepository.dialogTicketIds().toSet()
                val all = ticketsRepository.loadTickets()
                all
                    .filter { it.ticketId in dialogIds }
                    .sortedWith(
                        compareByDescending<TicketShortDto> { it.unreadCount > 0 }
                            .thenByDescending { it.ticketId },
                    )
            }.onSuccess { list ->
                _uiState.update { it.copy(isLoading = false, tickets = list) }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(isLoading = false, error = e.toAuthMessage("Не удалось загрузить диалоги"))
                }
            }
        }
    }
}
