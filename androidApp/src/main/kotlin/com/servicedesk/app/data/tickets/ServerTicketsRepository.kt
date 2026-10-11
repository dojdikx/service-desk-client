package com.servicedesk.app.data.tickets

import com.servicedesk.app.data.api.TicketColumnsResponseDto
import com.servicedesk.app.data.api.IssueTypesResponseDto
import com.servicedesk.app.data.api.TicketCreateRequestDto
import com.servicedesk.app.data.api.TicketDetailDto
import com.servicedesk.app.data.api.TicketPatchRequestDto
import com.servicedesk.app.data.api.TicketShortDto
import com.servicedesk.app.data.api.TicketsApi
import com.servicedesk.app.domain.tickets.TicketFilterQuery
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

    override suspend fun filterTickets(query: TicketFilterQuery): List<TicketShortDto> =
        ticketsApi.filter(
            startDate = query.startDate,
            endDate = query.endDate,
            tagStr = query.tagStr,
            serialNumber = query.serialNumber,
            manager = query.manager,
        ).tickets

    override suspend fun createTicket(userId: Int, reason: String?): TicketDetailDto =
        ticketsApi.create(TicketCreateRequestDto(userId = userId, reason = reason))

    override suspend fun getTicket(id: Int): TicketDetailDto = ticketsApi.get(id)

    override suspend fun patchTicket(id: Int, body: TicketPatchRequestDto): TicketDetailDto =
        ticketsApi.patch(id, body)

    override suspend fun loadIssueTypes(lang: String): IssueTypesResponseDto =
        ticketsApi.issueTypes(lang)
}
