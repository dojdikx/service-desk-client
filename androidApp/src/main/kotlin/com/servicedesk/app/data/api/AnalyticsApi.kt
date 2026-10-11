package com.servicedesk.app.data.api

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST

interface AnalyticsApi {
    @POST("api/analytics")
    suspend fun fetchReport(@Body body: AnalyticsRequestDto): AnalyticsResponseDto

    @POST("api/analytics")
    suspend fun requestExcel(@Body body: AnalyticsRequestDto): AnalyticsExcelResponseDto
}

@Serializable
data class AnalyticsRequestDto(
    val startDate: String,
    val endDate: String,
    val channels: List<String> = listOf("max"),
    val includeStatusBreakdown: Boolean = false,
    val includePrinterModelsBreakdown: Boolean = false,
    val includeTicketsBreakdown: Boolean = false,
    val excel: Boolean = false,
)

@Serializable
data class AnalyticsPeriodDto(
    val startDate: String,
    val endDate: String,
    val channels: List<String>,
)

@Serializable
data class StatusCountDto(
    val status: String,
    val count: Int,
)

@Serializable
data class PrinterModelCountDto(
    val printerModel: String,
    val count: Int,
)

@Serializable
data class AnalyticsTicketRowDto(
    val id: Int,
    val status: String,
    val createdAt: String,
    val updatedAt: String? = null,
    val printerModel: String? = null,
    val serialNumber: String? = null,
    val assignedTo: Int? = null,
    val reason: String? = null,
)

@Serializable
data class AnalyticsResponseDto(
    val period: AnalyticsPeriodDto,
    val ticketsCreated: Int,
    val ticketsModified: Int,
    val statusBreakdown: List<StatusCountDto>? = null,
    val printerModelBreakdown: List<PrinterModelCountDto>? = null,
    val ticketsBreakdown: List<AnalyticsTicketRowDto>? = null,
)

@Serializable
data class AnalyticsExcelResponseDto(
    val excelPath: String,
)
