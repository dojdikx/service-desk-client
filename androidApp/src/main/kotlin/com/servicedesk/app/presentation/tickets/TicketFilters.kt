package com.servicedesk.app.presentation.tickets

import com.servicedesk.app.data.api.StaffBriefDto
import com.servicedesk.app.domain.tickets.TicketFilterQuery

data class TicketFiltersState(
    val startDate: String = "",
    val endDate: String = "",
    val tag: String = "",
    val serialNumber: String = "",
    val managerId: Int? = null,
    val isActive: Boolean = false,
) {
    fun toQuery(): TicketFilterQuery = TicketFilterQuery(
        startDate = startDate.trim().takeIf { it.isNotEmpty() },
        endDate = endDate.trim().takeIf { it.isNotEmpty() },
        tagStr = tag.trim().takeIf { it.isNotEmpty() },
        serialNumber = serialNumber.trim().takeIf { it.isNotEmpty() },
        manager = managerId,
    )

    fun hasAnyField(isAdmin: Boolean): Boolean {
        val base = startDate.isNotBlank() || endDate.isNotBlank() ||
            tag.isNotBlank() || serialNumber.isNotBlank()
        return if (isAdmin) base || managerId != null else base
    }
}

fun StaffBriefDto.displayName(): String {
    val parts = listOfNotNull(firstName, lastName).joinToString(" ").trim()
    return if (parts.isNotEmpty()) parts else email
}
