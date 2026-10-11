package com.servicedesk.app.domain.analytics

data class AnalyticsQuery(
    val startDate: String,
    val endDate: String,
    val includeStatusBreakdown: Boolean,
    val includePrinterModelsBreakdown: Boolean,
    val includeTicketsBreakdown: Boolean,
)

data class AnalyticsReport(
    val startDate: String,
    val endDate: String,
    val channels: List<String>,
    val ticketsCreated: Int,
    val ticketsModified: Int,
    val statusBreakdown: List<StatusCount> = emptyList(),
    val printerModelBreakdown: List<PrinterModelCount> = emptyList(),
    val ticketsBreakdown: List<AnalyticsTicketRow> = emptyList(),
)

data class StatusCount(
    val status: String,
    val count: Int,
)

data class PrinterModelCount(
    val printerModel: String,
    val count: Int,
)

data class AnalyticsTicketRow(
    val id: Int,
    val status: String,
    val createdAt: String,
    val printerModel: String?,
    val serialNumber: String?,
    val reason: String?,
)
