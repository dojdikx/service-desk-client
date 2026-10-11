package com.servicedesk.app.presentation.tickets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.servicedesk.app.data.api.AuthApi
import com.servicedesk.app.data.api.StaffApi
import com.servicedesk.app.data.api.StaffBriefDto
import com.servicedesk.app.data.api.TicketColumnDto
import com.servicedesk.app.data.api.TicketShortDto
import com.servicedesk.app.data.realtime.RealtimeEventTypes
import com.servicedesk.app.data.realtime.StaffRealtimeService
import com.servicedesk.app.domain.tickets.TicketsRepository
import com.servicedesk.app.R
import com.servicedesk.app.locale.AppStrings
import com.servicedesk.app.presentation.auth.toAuthMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TicketsUiState(
    val isLoading: Boolean = true,
    val tickets: List<TicketShortDto> = emptyList(),
    val columns: List<TicketColumnDto> = emptyList(),
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
    private val appStrings: AppStrings,
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
                val columns = ticketsRepository.loadColumns().columns
                val tickets = if (_uiState.value.filters.isActive) {
                    ticketsRepository.filterTickets(_uiState.value.filters.toQuery())
                } else {
                    ticketsRepository.loadTickets()
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        tickets = tickets,
                        columns = columns,
                        isAdmin = isAdmin,
                        staff = staff,
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.toAuthMessage(appStrings, R.string.tickets_error_load),
                    )
                }
            }
        }
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
            _uiState.update { it.copy(error = appStrings.get(R.string.tickets_filter_required)) }
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

    fun kanbanColumns(): List<Pair<TicketColumnDto, List<TicketShortDto>>> {
        val state = _uiState.value
        return state.columns.map { column ->
            column to state.tickets.filter { it.status == column.status }
        }
    }
}
