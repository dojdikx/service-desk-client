package com.servicedesk.app.data.messages

import com.servicedesk.app.data.api.MessageDto
import com.servicedesk.app.data.api.MessagesApi
import com.servicedesk.app.data.api.SendMessageRequestDto
import com.servicedesk.app.data.api.SendMessageResponseDto
import com.servicedesk.app.data.api.TicketsApi
import com.servicedesk.app.domain.messages.MessagesRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServerMessagesRepository @Inject constructor(
    private val messagesApi: MessagesApi,
    private val ticketsApi: TicketsApi,
) : MessagesRepository {
    override suspend fun dialogTicketIds(): List<Int> =
        messagesApi.dialogTicketIds().tickets.map { it.ticketId }

    override suspend fun preview(ticketId: Int): List<MessageDto> =
        messagesApi.preview(ticketId).messages

    override suspend fun historyBefore(ticketId: Int, beforeId: Int, limit: Int): List<MessageDto> =
        messagesApi.history(ticketId, beforeId, limit).messages

    override suspend fun send(ticketId: Int, text: String): SendMessageResponseDto =
        messagesApi.send(SendMessageRequestDto(ticketId = ticketId, text = text))

    override suspend fun markTicketRead(ticketId: Int) {
        ticketsApi.markRead(ticketId)
    }
}
