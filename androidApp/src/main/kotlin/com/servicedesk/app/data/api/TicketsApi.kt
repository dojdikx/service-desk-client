package com.servicedesk.app.data.api

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface TicketsApi {
    @GET("api/tickets/columns")
    suspend fun columns(): TicketColumnsResponseDto

    @GET("api/tickets")
    suspend fun list(): TicketListResponseDto

    @GET("api/tickets/{id}")
    suspend fun get(@Path("id") id: Int): TicketDetailDto

    @POST("api/tickets")
    suspend fun create(@Body body: TicketCreateRequestDto): TicketDetailDto

    @PATCH("api/tickets/{id}")
    suspend fun patch(@Path("id") id: Int, @Body body: TicketPatchRequestDto): TicketDetailDto
}

@Serializable
data class TicketColumnDto(
    val id: String,
    val status: String,
    val color: String,
    val isClosed: Boolean = false,
)

@Serializable
data class TicketColumnsResponseDto(
    val defaultStatus: String,
    val columns: List<TicketColumnDto>,
)

@Serializable
data class TicketShortDto(
    val ticketId: Int,
    val userId: Int,
    val tags: List<String>? = null,
    val createdAt: String,
    val clientName: String? = null,
    val clientAvatarUrl: String? = null,
    val lastMessage: String? = null,
    val status: String,
    val assignedTo: Int? = null,
    val assigneeName: String? = null,
    val unreadCount: Int = 0,
    val serialNumber: String? = null,
)

@Serializable
data class TicketListResponseDto(
    val tickets: List<TicketShortDto>,
)

@Serializable
data class TicketCreateRequestDto(
    val userId: Int,
    val reason: String? = null,
)

@Serializable
data class TicketPatchRequestDto(
    val status: String? = null,
    val assignedTo: Int? = null,
)

@Serializable
data class TicketDetailDto(
    val id: Int,
    val userId: Int,
    val status: String,
)
