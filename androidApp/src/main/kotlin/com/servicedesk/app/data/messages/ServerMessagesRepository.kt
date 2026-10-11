package com.servicedesk.app.data.messages

import com.servicedesk.app.data.api.MediaApi
import com.servicedesk.app.data.api.MessageDto
import com.servicedesk.app.data.api.MessagesApi
import com.servicedesk.app.data.api.SendMessageRequestDto
import com.servicedesk.app.data.api.SendMessageResponseDto
import com.servicedesk.app.data.api.TicketsApi
import com.servicedesk.app.domain.messages.MessagesRepository
import com.servicedesk.app.domain.messages.OutboundMessageMedia
import com.servicedesk.app.domain.messages.UploadedMedia
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

@Singleton
class ServerMessagesRepository @Inject constructor(
    private val messagesApi: MessagesApi,
    private val mediaApi: MediaApi,
    private val ticketsApi: TicketsApi,
) : MessagesRepository {
    override suspend fun dialogTicketIds(): List<Int> =
        messagesApi.dialogTicketIds().tickets.map { it.ticketId }

    override suspend fun preview(ticketId: Int): List<MessageDto> =
        messagesApi.preview(ticketId).messages

    override suspend fun historyBefore(ticketId: Int, beforeId: Int, limit: Int): List<MessageDto> =
        messagesApi.history(ticketId, beforeId, limit).messages

    override suspend fun send(ticketId: Int, text: String, media: OutboundMessageMedia?): SendMessageResponseDto =
        messagesApi.send(
            SendMessageRequestDto(
                ticketId = ticketId,
                text = text,
                mediaType = media?.mediaType,
                mediaUrl = media?.mediaUrl,
                mediaFileName = media?.fileName,
                mediaCaption = media?.caption,
            ),
        )

    override suspend fun uploadMedia(bytes: ByteArray, fileName: String, mimeType: String): UploadedMedia {
        val body = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", fileName, body)
        val response = mediaApi.upload(part)
        return UploadedMedia(
            mediaType = response.mediaType,
            mediaUrl = response.mediaUrl,
            fileName = response.fileName,
        )
    }

    override suspend fun markTicketRead(ticketId: Int) {
        ticketsApi.markRead(ticketId)
    }
}
