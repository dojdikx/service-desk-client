package com.servicedesk.app.data.realtime

import com.servicedesk.app.data.api.MessageDto
import kotlinx.serialization.Serializable

@Serializable
data class RealtimeEventDto(
    val type: String,
    val ticketId: Int? = null,
    val message: MessageDto? = null,
    val messageId: Int? = null,
    val deliveryStatus: String? = null,
    val deliveryError: String? = null,
    val unreadCount: Int? = null,
)

object RealtimeEventTypes {
    const val MESSAGE_NEW = "message.new"
    const val MESSAGE_DELIVERY = "message.delivery"
    const val DIALOG_ACTIVITY = "dialog.activity"
}
