package com.servicedesk.app.presentation.tickets

import com.servicedesk.app.data.api.TicketShortDto

fun TicketShortDto.matchesSearchQuery(query: String): Boolean {
    val q = query.trim()
    if (q.isBlank()) return true
    val haystack = listOfNotNull(
        clientName,
        contactPhone,
        reason,
        lastMessage,
        serialNumber,
        status,
        assigneeName,
    ).joinToString(" ")
    return haystack.contains(q, ignoreCase = true)
}
