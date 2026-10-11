package com.servicedesk.app.presentation.tickets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.servicedesk.app.data.api.TicketShortDto
import com.servicedesk.app.data.search.SearchHistoryStore
import com.servicedesk.app.domain.tickets.TicketsRepository
import com.servicedesk.app.presentation.auth.toAuthMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class TicketSearchPhase {
    IDLE,
    LOADING,
    RESULTS,
    EMPTY,
    ERROR,
}

data class TicketSearchUiState(
    val query: String = "",
    val phase: TicketSearchPhase = TicketSearchPhase.IDLE,
    val history: List<String> = emptyList(),
    val results: List<TicketShortDto> = emptyList(),
    val error: String? = null,
    val ticketsLoaded: List<TicketShortDto> = emptyList(),
)

@HiltViewModel
class TicketSearchViewModel @Inject constructor(
    private val ticketsRepository: TicketsRepository,
    private val searchHistoryStore: SearchHistoryStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TicketSearchUiState())
    val uiState: StateFlow<TicketSearchUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val history = searchHistoryStore.history()
            _uiState.update { it.copy(history = history) }
            loadTicketsCache()
        }
    }

    fun onQueryChange(value: String) {
        _uiState.update { it.copy(query = value, error = null) }
    }

    fun submitSearch() {
        val query = _uiState.value.query.trim()
        if (query.isBlank()) return
        viewModelScope.launch {
            searchHistoryStore.addQuery(query)
            val history = searchHistoryStore.history()
            _uiState.update { it.copy(history = history) }
            runSearch(query)
        }
    }

    fun searchFromHistory(query: String) {
        _uiState.update { it.copy(query = query) }
        viewModelScope.launch {
            runSearch(query.trim())
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            searchHistoryStore.clear()
            _uiState.update { it.copy(history = emptyList()) }
        }
    }

    fun retryLoad() {
        viewModelScope.launch { loadTicketsCache() }
    }

    private suspend fun loadTicketsCache() {
        _uiState.update { it.copy(phase = TicketSearchPhase.LOADING, error = null) }
        runCatching { ticketsRepository.loadTickets() }
            .onSuccess { tickets ->
                _uiState.update {
                    it.copy(
                        ticketsLoaded = tickets,
                        phase = if (it.query.isBlank()) TicketSearchPhase.IDLE else it.phase,
                        error = null,
                    )
                }
            }
            .onFailure { e ->
                _uiState.update {
                    it.copy(
                        phase = TicketSearchPhase.ERROR,
                        error = e.toAuthMessage("Не удалось загрузить заявки"),
                    )
                }
            }
    }

    private suspend fun runSearch(query: String) {
        if (_uiState.value.ticketsLoaded.isEmpty()) {
            loadTicketsCache()
        }
        val tickets = _uiState.value.ticketsLoaded
        if (tickets.isEmpty() && _uiState.value.phase == TicketSearchPhase.ERROR) {
            return
        }
        val results = tickets.filter { it.matchesSearchQuery(query) }
        _uiState.update {
            it.copy(
                phase = if (results.isEmpty()) TicketSearchPhase.EMPTY else TicketSearchPhase.RESULTS,
                results = results,
            )
        }
    }
}
