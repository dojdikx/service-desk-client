package com.servicedesk.app.data.api

import kotlinx.serialization.Serializable
import retrofit2.http.GET

interface StaffApi {
    @GET("api/staff")
    suspend fun list(): StaffListResponseDto
}

@Serializable
data class StaffBriefDto(
    val id: Int,
    val email: String,
    val firstName: String? = null,
    val lastName: String? = null,
)

@Serializable
data class StaffListResponseDto(
    val staff: List<StaffBriefDto>,
)
