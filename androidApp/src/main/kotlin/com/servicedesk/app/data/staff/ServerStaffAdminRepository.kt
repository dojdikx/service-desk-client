package com.servicedesk.app.data.staff

import com.servicedesk.app.data.api.AuthApi
import com.servicedesk.app.data.api.RegisterRequestDto
import com.servicedesk.app.data.api.StaffApi
import com.servicedesk.app.data.api.StaffBriefDto
import com.servicedesk.app.domain.staff.StaffAdminRepository
import com.servicedesk.app.domain.staff.StaffMember
import com.servicedesk.app.domain.staff.StaffRegisterRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServerStaffAdminRepository @Inject constructor(
    private val staffApi: StaffApi,
    private val authApi: AuthApi,
) : StaffAdminRepository {

    override suspend fun listStaff(): List<StaffMember> =
        staffApi.list().staff.map { it.toDomain() }

    override suspend fun registerStaff(request: StaffRegisterRequest): StaffMember {
        val response = authApi.registerStaff(
            RegisterRequestDto(
                email = request.email,
                password = request.password,
                firstName = request.firstName.takeIf { it.isNotBlank() },
                lastName = request.lastName.takeIf { it.isNotBlank() },
            ),
        )
        return response.staff.toDomain()
    }

    private fun StaffBriefDto.toDomain() = StaffMember(
        id = id,
        email = email,
        firstName = firstName,
        lastName = lastName,
        role = role,
    )

    private fun com.servicedesk.app.data.api.StaffDto.toDomain() = StaffMember(
        id = id,
        email = email,
        firstName = firstName,
        lastName = lastName,
        role = role,
    )
}
