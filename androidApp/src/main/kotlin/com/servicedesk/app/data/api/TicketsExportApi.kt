package com.servicedesk.app.data.api

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST

interface TicketsExportApi {
    @POST("api/tickets/export")
    suspend fun export(@Body body: TicketsExportRequestDto): TicketsExportResponseDto
}

@Serializable
data class TicketsExportRequestDto(
    val startDate: String,
    val endDate: String,
    val channels: List<String> = listOf("max"),
)

@Serializable
data class TicketsExportResponseDto(
    val excelPath: String,
)
