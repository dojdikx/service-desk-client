package com.servicedesk.app.domain.staff

data class StaffMember(
    val id: Int,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val role: String,
)

data class StaffRegisterRequest(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
)

interface StaffAdminRepository {
    suspend fun listStaff(): List<StaffMember>
    suspend fun registerStaff(request: StaffRegisterRequest): StaffMember
}
