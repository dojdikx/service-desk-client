package com.servicedesk.app.domain.messages

import com.servicedesk.app.data.api.MessageDto
import com.servicedesk.app.data.api.SendMessageResponseDto

interface MessagesRepository {
    suspend fun dialogTicketIds(): List<Int>
    suspend fun preview(ticketId: Int): List<MessageDto>
    suspend fun historyBefore(ticketId: Int, beforeId: Int, limit: Int = 30): List<MessageDto>
    suspend fun send(ticketId: Int, text: String): SendMessageResponseDto
    suspend fun markTicketRead(ticketId: Int)
}
