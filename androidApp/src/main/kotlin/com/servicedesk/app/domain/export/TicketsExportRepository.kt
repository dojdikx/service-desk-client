package com.servicedesk.app.domain.export

import java.io.File

data class TicketsExportQuery(
    val startDate: String,
    val endDate: String,
)

interface TicketsExportRepository {
    suspend fun exportToFile(query: TicketsExportQuery, targetDir: File): File
}
