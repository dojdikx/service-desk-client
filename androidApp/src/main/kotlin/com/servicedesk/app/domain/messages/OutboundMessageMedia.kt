package com.servicedesk.app.domain.messages

data class OutboundMessageMedia(
    val mediaType: String,
    val mediaUrl: String,
    val fileName: String,
    val caption: String? = null,
)

data class UploadedMedia(
    val mediaType: String,
    val mediaUrl: String,
    val fileName: String,
)
