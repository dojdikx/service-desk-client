package com.servicedesk.app.presentation.tickets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.servicedesk.app.data.api.AuthApi
import com.servicedesk.app.data.api.StaffApi
import com.servicedesk.app.data.api.StaffBriefDto
import com.servicedesk.app.data.api.TicketShortDto
import com.servicedesk.app.data.realtime.RealtimeEventTypes
import com.servicedesk.app.data.realtime.StaffRealtimeService
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
    val isAdmin: Boolean = false,
    val error: String? = null,
    val filters: TicketFiltersState = TicketFiltersState(),
    val filtersSheetVisible: Boolean = false,
    val staff: List<StaffBriefDto> = emptyList(),
)

@HiltViewModel
class TicketsViewModel @Inject constructor(
    private val ticketsRepository: TicketsRepository,
    private val authApi: AuthApi,
    private val staffApi: StaffApi,
    staffRealtimeService: StaffRealtimeService,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TicketsUiState())
    val uiState: StateFlow<TicketsUiState> = _uiState.asStateFlow()

    init {
        refresh()
        viewModelScope.launch {
            staffRealtimeService.events.collect { event ->
                if (event.type != RealtimeEventTypes.DIALOG_ACTIVITY) return@collect
                val id = event.ticketId ?: return@collect
                val unread = event.unreadCount ?: return@collect
                _uiState.update { state ->
                    state.copy(
                        tickets = state.tickets.map { ticket ->
                            if (ticket.ticketId == id) ticket.copy(unreadCount = unread) else ticket
                        },
                    )
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                val me = authApi.me()
                val isAdmin = me.staff.role == "admin"
                val staff = if (isAdmin) {
                    runCatching { staffApi.list().staff }.getOrDefault(emptyList())
                } else {
                    emptyList()
                }
                val tickets = if (_uiState.value.filters.isActive) {
                    ticketsRepository.filterTickets(_uiState.value.filters.toQuery())
                } else {
                    ticketsRepository.loadTickets()
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        tickets = tickets,
                        isAdmin = isAdmin,
                        staff = staff,
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

    fun openFiltersSheet() {
        _uiState.update { it.copy(filtersSheetVisible = true) }
    }

    fun closeFiltersSheet() {
        _uiState.update { it.copy(filtersSheetVisible = false) }
    }

    fun onDraftFiltersChange(filters: TicketFiltersState) {
        _uiState.update { it.copy(filters = filters) }
    }

    fun applyFilters() {
        val state = _uiState.value
        if (!state.filters.hasAnyField(state.isAdmin)) {
            _uiState.update { it.copy(error = "Заполните хотя бы одно поле фильтра") }
            return
        }
        _uiState.update {
            it.copy(
                filters = state.filters.copy(isActive = true),
                filtersSheetVisible = false,
                error = null,
            )
        }
        refresh()
    }

    fun clearFilters() {
        _uiState.update {
            it.copy(
                filters = TicketFiltersState(),
                filtersSheetVisible = false,
                error = null,
            )
        }
        refresh()
    }

    fun visibleTickets(): List<TicketShortDto> {
        val state = _uiState.value
        return state.tickets.filter { ticket -> matchesChip(ticket, state.statusChip) }
    }

    private fun matchesChip(ticket: TicketShortDto, chip: TicketStatusChip): Boolean =
        when (chip) {
            TicketStatusChip.ALL -> true
            TicketStatusChip.PRIMARY -> ticket.status.contains("Первичный", ignoreCase = true)
            TicketStatusChip.IN_WORK -> ticket.status.contains("В работе", ignoreCase = true)
        }
}
