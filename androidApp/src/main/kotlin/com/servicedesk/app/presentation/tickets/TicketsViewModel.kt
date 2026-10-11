package com.servicedesk.app.presentation.tickets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.servicedesk.app.data.api.AuthApi
import com.servicedesk.app.data.api.TicketShortDto
import com.servicedesk.app.domain.tickets.TicketsRepository
import com.servicedesk.app.presentation.auth.toAuthMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class TicketStatusChip {
    ALL,
    PRIMARY,
    IN_WORK,
}

data class TicketsUiState(
    val isLoading: Boolean = true,
    val tickets: List<TicketShortDto> = emptyList(),
    val statusChip: TicketStatusChip = TicketStatusChip.ALL,
    val searchQuery: String = "",
    val isAdmin: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class TicketsViewModel @Inject constructor(
    private val ticketsRepository: TicketsRepository,
    private val authApi: AuthApi,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TicketsUiState())
    val uiState: StateFlow<TicketsUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                val me = authApi.me()
                val tickets = ticketsRepository.loadTickets()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        tickets = tickets,
                        isAdmin = me.staff.role == "admin",
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.toAuthMessage("Не удалось загрузить заявки"),
                    )
                }
            }
        }
    }

    fun onStatusChip(chip: TicketStatusChip) {
        _uiState.update { it.copy(statusChip = chip) }
    }

    fun onSearchChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun visibleTickets(): List<TicketShortDto> {
        val state = _uiState.value
        return state.tickets
            .filter { ticket -> matchesChip(ticket, state.statusChip) }
            .filter { ticket -> matchesSearch(ticket, state.searchQuery) }
    }

    private fun matchesChip(ticket: TicketShortDto, chip: TicketStatusChip): Boolean =
        when (chip) {
            TicketStatusChip.ALL -> true
            TicketStatusChip.PRIMARY -> ticket.status.contains("Первичный", ignoreCase = true)
            TicketStatusChip.IN_WORK -> ticket.status.contains("В работе", ignoreCase = true)
        }

    private fun matchesSearch(ticket: TicketShortDto, query: String): Boolean {
        val q = query.trim()
        if (q.isBlank()) return true
        val haystack = listOfNotNull(
            ticket.clientName,
            ticket.lastMessage,
            ticket.serialNumber,
            ticket.status,
        ).joinToString(" ")
        return haystack.contains(q, ignoreCase = true)
    }
}
