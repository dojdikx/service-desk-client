package com.servicedesk.app.presentation.dialogs

import com.servicedesk.app.data.api.MessageDto

internal fun mergePreviewWithExisting(existing: List<MessageDto>, preview: List<MessageDto>): List<MessageDto> {
    if (preview.isEmpty()) return existing
    if (existing.isEmpty()) return preview
    val previewMinId = preview.minOf { it.id }
    val previewMaxId = preview.maxOf { it.id }
    val head = existing.filter { it.id < previewMinId }
    val existingById = existing.associateBy { it.id }
    val tail = preview.map { fresh ->
        val old = existingById[fresh.id]
        if (old == null) {
            fresh
        } else {
            fresh.copy(
                timestamp = old.timestamp.ifBlank { fresh.timestamp },
            )
        }
    }
    val newer = existing.filter { it.id > previewMaxId }
    return (head + tail + newer).sortedBy { it.id }
}

internal fun hasMessageChanges(before: List<MessageDto>, after: List<MessageDto>): Boolean {
    if (before.size != after.size) return true
    val afterById = after.associateBy { it.id }
    return before.any { old ->
        val new = afterById[old.id] ?: return true
        old.messageText != new.messageText ||
            old.deliveryStatus != new.deliveryStatus ||
            old.messageType != new.messageType
    }
}
