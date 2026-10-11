package com.servicedesk.app.presentation.dialogs

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.servicedesk.app.data.api.MessageDto
import com.servicedesk.app.domain.messages.MessagesRepository
import com.servicedesk.app.domain.tickets.TicketsRepository
import com.servicedesk.app.presentation.auth.toAuthMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

data class ChatUiState(
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val isSending: Boolean = false,
    val ticketId: Int = 0,
    val title: String = "",
    val messages: List<MessageDto> = emptyList(),
    val draft: String = "",
    val error: String? = null,
    val canLoadMore: Boolean = true,
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val messagesRepository: MessagesRepository,
    private val ticketsRepository: TicketsRepository,
) : ViewModel() {

    private val ticketId: Int = savedStateHandle.get<Int>("ticketId")
        ?: savedStateHandle.get<String>("ticketId")?.toIntOrNull()
        ?: 0

    private val _uiState = MutableStateFlow(ChatUiState(ticketId = ticketId))
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        if (ticketId > 0) {
            loadInitial()
            startMessagePolling()
        }
    }

    private fun startMessagePolling() {
        viewModelScope.launch {
            delay(2_000)
            while (isActive) {
                syncLatestMessages()
                delay(2_500)
            }
        }
    }

    private suspend fun syncLatestMessages() {
        val state = _uiState.value
        if (state.isLoading || state.error != null && state.messages.isEmpty()) return
        runCatching {
            messagesRepository.preview(ticketId)
        }.onSuccess { preview ->
            val merged = if (state.canLoadMore) {
                mergePreviewWithExisting(state.messages, preview)
            } else {
                preview
            }
            if (hasMessageChanges(state.messages, merged)) {
                messagesRepository.markTicketRead(ticketId)
                _uiState.update { it.copy(messages = merged) }
            }
        }
    }

    fun onDraftChange(value: String) {
        _uiState.update { it.copy(draft = value) }
    }

    fun loadInitial() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                val ticket = ticketsRepository.getTicket(ticketId)
                val title = ticket.fullName?.trim()?.takeIf { it.isNotEmpty() }
                    ?: "Заявка #$ticketId"
                messagesRepository.markTicketRead(ticketId)
                val messages = messagesRepository.preview(ticketId)
                title to messages
            }.onSuccess { (title, messages) ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        title = title,
                        messages = messages,
                        canLoadMore = messages.size >= 30,
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.toAuthMessage("Не удалось загрузить чат")) }
            }
        }
    }

    fun loadOlder() {
        val state = _uiState.value
        if (state.isLoadingMore || !state.canLoadMore || state.messages.isEmpty()) return
        val beforeId = state.messages.first().id
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            runCatching {
                messagesRepository.historyBefore(ticketId, beforeId)
            }.onSuccess { older ->
                _uiState.update {
                    it.copy(
                        isLoadingMore = false,
                        messages = older + it.messages,
                        canLoadMore = older.isNotEmpty(),
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoadingMore = false, error = e.toAuthMessage("Не удалось загрузить сообщения")) }
            }
        }
    }

    fun send() {
        val text = _uiState.value.draft.trim()
        if (text.isEmpty() || _uiState.value.isSending) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, error = null) }
            runCatching {
                messagesRepository.send(ticketId, text)
            }.onSuccess { response ->
                val optimistic = MessageDto(
                    id = response.messageId,
                    ticketId = ticketId,
                    messageText = response.text,
                    messageType = "backend",
                    timestamp = "",
                    deliveryStatus = response.status,
                )
                _uiState.update {
                    it.copy(
                        isSending = false,
                        draft = "",
                        messages = it.messages + optimistic,
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isSending = false, error = e.toAuthMessage("Не удалось отправить")) }
            }
        }
    }
}
