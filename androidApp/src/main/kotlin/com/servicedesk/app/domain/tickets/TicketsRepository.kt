package com.servicedesk.app.domain.tickets

import com.servicedesk.app.data.api.TicketColumnsResponseDto
import com.servicedesk.app.data.api.IssueTypesResponseDto
import com.servicedesk.app.data.api.TicketCreateRequestDto
import com.servicedesk.app.data.api.TicketDetailDto
import com.servicedesk.app.data.api.TicketPatchRequestDto
import com.servicedesk.app.data.api.TicketShortDto

data class TicketFilterQuery(
    val startDate: String? = null,
    val endDate: String? = null,
    val tagStr: String? = null,
    val serialNumber: String? = null,
    val manager: Int? = null,
)

interface TicketsRepository {
    suspend fun loadColumns(): TicketColumnsResponseDto
    suspend fun loadTickets(): List<TicketShortDto>
    suspend fun filterTickets(query: TicketFilterQuery): List<TicketShortDto>
    suspend fun createTicket(userId: Int, reason: String?): TicketDetailDto
    suspend fun getTicket(id: Int): TicketDetailDto
    suspend fun patchTicket(id: Int, body: TicketPatchRequestDto): TicketDetailDto
    suspend fun loadIssueTypes(lang: String = "ru"): IssueTypesResponseDto
}
