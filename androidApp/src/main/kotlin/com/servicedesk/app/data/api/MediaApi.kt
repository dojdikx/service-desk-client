package com.servicedesk.app.data.api

import kotlinx.serialization.Serializable
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface MediaApi {
    @Multipart
    @POST("api/media/upload")
    suspend fun upload(@Part file: MultipartBody.Part): MediaUploadResponseDto
}

@Serializable
data class MediaUploadResponseDto(
    val storageKey: String,
    val mediaUrl: String,
    val mediaType: String,
    val fileName: String,
    val contentType: String,
    val sizeBytes: Long,
)
