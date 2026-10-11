package com.servicedesk.app.data.auth

import com.servicedesk.app.data.api.AuthApi
import com.servicedesk.app.data.api.LoginRequestDto
import com.servicedesk.app.R
import com.servicedesk.app.domain.auth.StaffAuthRepository
import com.servicedesk.app.locale.AppStrings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServerStaffAuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val tokenStore: TokenStore,
    private val appStrings: AppStrings,
) : StaffAuthRepository {

    override suspend fun signIn(email: String, password: String) {
        val response = authApi.login(LoginRequestDto(email = email, password = password))
        tokenStore.save(response.token, response.staff.email)
    }

    override suspend fun signUp(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
    ) {
        throw UnsupportedOperationException(appStrings.get(R.string.auth_signup_disabled))
    }

    override suspend fun signOut() {
        runCatching { authApi.logout() }
        tokenStore.clear()
    }
}
