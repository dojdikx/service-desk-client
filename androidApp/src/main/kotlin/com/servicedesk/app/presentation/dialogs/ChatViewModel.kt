package com.servicedesk.app.presentation.dialogs

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.servicedesk.app.data.auth.TokenStore
import com.servicedesk.app.data.media.readUriBytes
import com.servicedesk.app.data.api.MessageDto
import com.servicedesk.app.data.realtime.RealtimeEventTypes
import com.servicedesk.app.data.realtime.TicketRealtimeSession
import com.servicedesk.app.domain.messages.MessagesRepository
import com.servicedesk.app.domain.messages.OutboundMessageMedia
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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient

data class ChatReplyTarget(
    val messageId: Int,
    val preview: String,
)

data class ChatUiState(
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val isSending: Boolean = false,
    val ticketId: Int = 0,
    val title: String = "",
    val messages: List<MessageDto> = emptyList(),
    val draft: String = "",
    val replyTarget: ChatReplyTarget? = null,
    val error: String? = null,
    val canLoadMore: Boolean = true,
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val messagesRepository: MessagesRepository,
    private val ticketsRepository: TicketsRepository,
    private val okHttpClient: OkHttpClient,
    private val tokenStore: TokenStore,
    private val appStrings: AppStrings,
) : ViewModel() {

    private val ticketId: Int = savedStateHandle.get<Int>("ticketId")
        ?: savedStateHandle.get<String>("ticketId")?.toIntOrNull()
        ?: 0

    private val _uiState = MutableStateFlow(ChatUiState(ticketId = ticketId))
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    private var ticketRealtime: TicketRealtimeSession? = null

    init {
        if (ticketId > 0) {
            loadInitial()
            startTicketRealtime()
            startMessagePollingFallback()
        }
    }

    private fun startTicketRealtime() {
        ticketRealtime = TicketRealtimeSession(
            ticketId = ticketId,
            okHttpClient = okHttpClient,
            tokenStore = tokenStore,
            onEvent = { event ->
                viewModelScope.launch {
                    val state = _uiState.value
                    val updated = applyRealtimeEvent(state.messages, event, state.canLoadMore)
                    if (updated != null) {
                        if (event.type == RealtimeEventTypes.MESSAGE_NEW) {
                            messagesRepository.markTicketRead(ticketId)
                        }
                        _uiState.update { it.copy(messages = updated) }
                    }
                }
            },
        ).also { it.start() }
    }

    private fun startMessagePollingFallback() {
        viewModelScope.launch {
            delay(30_000)
            while (isActive) {
                syncLatestMessages()
                delay(30_000)
            }
        }
    }

    override fun onCleared() {
        ticketRealtime?.stop()
        ticketRealtime = null
        super.onCleared()
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
                    ?: appStrings.get(R.string.chat_ticket_title, ticketId)
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
                _uiState.update {
                    it.copy(isLoading = false, error = e.toAuthMessage(appStrings, R.string.chat_error_load))
                }
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
                _uiState.update {
                    it.copy(isLoadingMore = false, error = e.toAuthMessage(appStrings, R.string.chat_error_messages))
                }
            }
        }
    }

    fun setReplyTarget(message: MessageDto) {
        val preview = message.messageText.trim().ifBlank {
            message.mediaCaption?.trim() ?: message.mediaFileName ?: ""
        }.take(120)
        if (preview.isEmpty()) return
        _uiState.update {
            it.copy(replyTarget = ChatReplyTarget(messageId = message.id, preview = preview))
        }
    }

    fun clearReplyTarget() {
        _uiState.update { it.copy(replyTarget = null) }
    }

    fun send() {
        val state = _uiState.value
        val text = state.draft.trim()
        if (text.isEmpty() || state.isSending) return
        val reply = state.replyTarget
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, error = null) }
            runCatching {
                messagesRepository.send(
                    ticketId = ticketId,
                    text = text,
                    replyToId = reply?.messageId,
                    quoteText = reply?.preview,
                )
            }.onSuccess { response ->
                appendOutgoingMessage(response, text, null, reply)
            }.onFailure { e ->
                _uiState.update {
                    it.copy(isSending = false, error = e.toAuthMessage(appStrings, R.string.chat_error_send))
                }
            }
        }
    }

    fun sendPickedFile(context: Context, uri: Uri) {
        viewModelScope.launch {
            runCatching {
                context.readUriBytes(uri)
            }.onSuccess { (bytes, name, mime) ->
                sendAttachment(bytes, name, mime)
            }.onFailure { e ->
                _uiState.update {
                    it.copy(error = e.toAuthMessage(appStrings, R.string.chat_error_file_read))
                }
            }
        }
    }

    fun sendAttachment(bytes: ByteArray, fileName: String, mimeType: String) {
        if (_uiState.value.isSending) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, error = null) }
            runCatching {
                val uploaded = messagesRepository.uploadMedia(bytes, fileName, mimeType)
                val caption = _uiState.value.draft.trim()
                val media = OutboundMessageMedia(
                    mediaType = uploaded.mediaType,
                    mediaUrl = uploaded.mediaUrl,
                    fileName = uploaded.fileName,
                    caption = caption.takeIf { it.isNotEmpty() },
                )
                val reply = _uiState.value.replyTarget
                val response = messagesRepository.send(
                    ticketId = ticketId,
                    text = caption,
                    media = media,
                    replyToId = reply?.messageId,
                    quoteText = reply?.preview,
                )
                response to media
            }.onSuccess { (response, media) ->
                appendOutgoingMessage(response, _uiState.value.draft.trim(), media, _uiState.value.replyTarget)
            }.onFailure { e ->
                _uiState.update {
                    it.copy(isSending = false, error = e.toAuthMessage(appStrings, R.string.chat_error_send_file))
                }
            }
        }
    }

    fun retryMessage(messageId: Int) {
        if (_uiState.value.isSending) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, error = null) }
            runCatching {
                messagesRepository.retryMessage(messageId)
            }.onSuccess { response ->
                _uiState.update { state ->
                    state.copy(
                        isSending = false,
                        messages = state.messages.map { msg ->
                            if (msg.id == messageId) {
                                msg.copy(
                                    deliveryStatus = response.status,
                                    deliveryError = null,
                                )
                            } else {
                                msg
                            }
                        },
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(isSending = false, error = e.toAuthMessage(appStrings, R.string.chat_error_retry))
                }
            }
        }
    }

    private fun appendOutgoingMessage(
        response: com.servicedesk.app.data.api.SendMessageResponseDto,
        text: String,
        media: OutboundMessageMedia?,
        reply: ChatReplyTarget?,
    ) {
        val optimistic = MessageDto(
            id = response.messageId,
            ticketId = ticketId,
            messageText = response.text.ifBlank { text },
            messageType = "backend",
            timestamp = "",
            deliveryStatus = response.status,
            mediaType = media?.mediaType,
            mediaUrl = media?.mediaUrl,
            mediaFileName = media?.fileName,
            mediaCaption = media?.caption,
            replyToId = reply?.messageId,
            quoteText = reply?.preview,
        )
        _uiState.update {
            it.copy(
                isSending = false,
                draft = "",
                replyTarget = null,
                messages = it.messages + optimistic,
            )
        }
    }
}
