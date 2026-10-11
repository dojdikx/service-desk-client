package com.servicedesk.app.data.tickets

import com.servicedesk.app.data.api.TicketColumnsResponseDto
import com.servicedesk.app.data.api.TicketShortDto
import com.servicedesk.app.data.api.TicketsApi
import com.servicedesk.app.domain.tickets.TicketsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServerTicketsRepository @Inject constructor(
    private val ticketsApi: TicketsApi,
) : TicketsRepository {
    override suspend fun loadColumns(): TicketColumnsResponseDto = ticketsApi.columns()

    override suspend fun loadTickets(): List<TicketShortDto> =
        ticketsApi.list().tickets
}
