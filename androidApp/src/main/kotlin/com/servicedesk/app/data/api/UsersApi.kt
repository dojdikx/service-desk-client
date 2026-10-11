package com.servicedesk.app.data.api

import kotlinx.serialization.Serializable
import retrofit2.http.GET

interface UsersApi {
    @GET("api/users")
    suspend fun list(): UserListResponseDto
}

@Serializable
data class UserShortDto(
    val id: Int,
    val name: String,
    val phone: String,
    val city: String,
)

@Serializable
data class UserListResponseDto(
    val users: List<UserShortDto>,
)
