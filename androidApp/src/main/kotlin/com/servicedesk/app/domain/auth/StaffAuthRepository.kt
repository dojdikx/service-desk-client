package com.servicedesk.app.domain.auth

interface StaffAuthRepository {
    suspend fun signIn(email: String, password: String)
    suspend fun signUp(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
    )
    suspend fun signOut()
}
