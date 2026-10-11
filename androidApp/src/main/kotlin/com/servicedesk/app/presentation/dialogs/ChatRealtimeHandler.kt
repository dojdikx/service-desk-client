package com.servicedesk.app.presentation.dialogs

import com.servicedesk.app.data.api.MessageDto
import com.servicedesk.app.data.realtime.RealtimeEventDto
import com.servicedesk.app.data.realtime.RealtimeEventTypes

internal fun applyRealtimeEvent(
    messages: List<MessageDto>,
    event: RealtimeEventDto,
    canLoadMore: Boolean,
): List<MessageDto>? {
    return when (event.type) {
        RealtimeEventTypes.MESSAGE_NEW -> {
            val incoming = event.message ?: return null
            if (messages.any { it.id == incoming.id }) {
                messages.map { if (it.id == incoming.id) incoming else it }
            } else if (canLoadMore) {
                mergePreviewWithExisting(messages, listOf(incoming))
            } else {
                messages + incoming
            }
        }
        RealtimeEventTypes.MESSAGE_DELIVERY -> {
            val messageId = event.messageId ?: return null
            val status = event.deliveryStatus ?: return null
            messages.map { msg ->
                if (msg.id == messageId) msg.copy(deliveryStatus = status) else msg
            }
        }
        else -> null
    }
}
