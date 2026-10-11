package com.servicedesk.app.domain.tickets

import com.servicedesk.app.data.api.TicketColumnsResponseDto
import com.servicedesk.app.data.api.TicketShortDto

interface TicketsRepository {
    suspend fun loadColumns(): TicketColumnsResponseDto
    suspend fun loadTickets(): List<TicketShortDto>
}
