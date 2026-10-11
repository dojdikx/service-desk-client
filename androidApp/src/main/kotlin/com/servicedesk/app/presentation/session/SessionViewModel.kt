package com.servicedesk.app.presentation.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.servicedesk.app.data.api.AuthApi
import com.servicedesk.app.data.auth.TokenStore
import com.servicedesk.app.data.realtime.StaffRealtimeService
import com.servicedesk.app.domain.auth.StaffAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class SessionStatus {
    Loading,
    SignedOut,
    SignedIn,
}

data class CurrentStaff(
    val id: Int,
    val email: String,
    val role: String,
    val firstName: String? = null,
    val lastName: String? = null,
) {
    val isAdmin: Boolean get() = role == "admin"
}

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val tokenStore: TokenStore,
    private val authRepository: StaffAuthRepository,
    private val authApi: AuthApi,
    private val staffRealtimeService: StaffRealtimeService,
) : ViewModel() {

    private val _status = MutableStateFlow(SessionStatus.Loading)
    val status: StateFlow<SessionStatus> = _status.asStateFlow()

    private val _currentStaff = MutableStateFlow<CurrentStaff?>(null)
    val currentStaff: StateFlow<CurrentStaff?> = _currentStaff.asStateFlow()

    init {
        viewModelScope.launch {
            tokenStore.load()
            if (tokenStore.cachedToken.isNullOrBlank()) {
                _status.value = SessionStatus.SignedOut
            } else {
                restoreSession()
            }
        }
    }

    fun onSignedIn() {
        viewModelScope.launch { restoreSession() }
    }

    private suspend fun restoreSession() {
        runCatching { authApi.me() }
            .onSuccess { me ->
                _currentStaff.value = CurrentStaff(
                    id = me.staff.id,
                    email = me.staff.email,
                    role = me.staff.role,
                    firstName = me.staff.firstName,
                    lastName = me.staff.lastName,
                )
                _status.value = SessionStatus.SignedIn
                staffRealtimeService.start()
            }
            .onFailure {
                tokenStore.clear()
                _currentStaff.value = null
                _status.value = SessionStatus.SignedOut
            }
    }

    fun onSignedOut() {
        _currentStaff.value = null
        _status.value = SessionStatus.SignedOut
    }

    fun signOut() {
        viewModelScope.launch {
            staffRealtimeService.stop()
            runCatching { authRepository.signOut() }
            onSignedOut()
        }
    }
}
