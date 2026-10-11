package com.servicedesk.app.data.api

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MessagesApi {
    @GET("api/messages/tickets")
    suspend fun dialogTicketIds(): TicketDialogsResponseDto

    @GET("api/messages/ticket/{ticketId}/preview")
    suspend fun preview(@Path("ticketId") ticketId: Int): TicketMessagesResponseDto

    @GET("api/messages/ticket/{ticketId}/history")
    suspend fun history(
        @Path("ticketId") ticketId: Int,
        @Query("before_id") beforeId: Int,
        @Query("limit") limit: Int = 30,
    ): TicketMessagesResponseDto

    @POST("api/messages/send")
    suspend fun send(@Body body: SendMessageRequestDto): SendMessageResponseDto

    @POST("api/messages/{messageId}/retry")
    suspend fun retry(@Path("messageId") messageId: Int): SendMessageResponseDto
}

@Serializable
data class MediaItemDto(
    val mediaType: String,
    val mediaUrl: String,
    val mediaFileName: String? = null,
    val mediaCaption: String? = null,
)

@Serializable
data class SendMessageRequestDto(
    val ticketId: Int,
    val text: String = "",
    val replyToId: Int? = null,
    val quoteText: String? = null,
    val quoteOffset: Int? = null,
    val mediaType: String? = null,
    val mediaUrl: String? = null,
    val mediaCaption: String? = null,
    val mediaFileName: String? = null,
    val mediaItems: List<MediaItemDto>? = null,
)

@Serializable
data class SendMessageResponseDto(
    val status: String,
    val ticketId: Int,
    val messageId: Int,
    val text: String,
)

@Serializable
data class MessageDto(
    val id: Int,
    val ticketId: Int,
    val messageText: String,
    val messageType: String,
    val timestamp: String,
    val mediaType: String? = null,
    val mediaUrl: String? = null,
    val mediaCaption: String? = null,
    val mediaFileName: String? = null,
    val mediaItems: List<MediaItemDto>? = null,
    val deliveryStatus: String? = null,
    val deliveryError: String? = null,
    val replyToId: Int? = null,
    val quoteText: String? = null,
    val quoteOffset: Int? = null,
)

@Serializable
data class TicketMessagesResponseDto(
    val ticketId: Int,
    val messages: List<MessageDto>,
)

@Serializable
data class TicketDialogItemDto(
    val ticketId: Int,
)

@Serializable
data class TicketDialogsResponseDto(
    val tickets: List<TicketDialogItemDto>,
)
