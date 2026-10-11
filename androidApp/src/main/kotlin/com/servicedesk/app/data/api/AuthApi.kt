package com.servicedesk.app.data.api

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {
    @POST("api/auth/register")
    suspend fun registerStaff(@Body body: RegisterRequestDto): RegisterStaffResponseDto

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequestDto): AuthResponseDto

    @POST("api/auth/logout")
    suspend fun logout()

    @GET("api/auth/me")
    suspend fun me(): MeResponseDto
}

@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String,
)

@Serializable
data class RegisterRequestDto(
    val email: String,
    val password: String,
    val firstName: String? = null,
    val lastName: String? = null,
)

@Serializable
data class StaffDto(
    val id: Int,
    val email: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val role: String,
)

@Serializable
data class AuthResponseDto(
    val token: String,
    val staff: StaffDto,
)

@Serializable
data class MeResponseDto(
    val staff: StaffDto,
    val authenticated: Boolean = true,
)

@Serializable
data class RegisterStaffResponseDto(
    val staff: StaffDto,
)
