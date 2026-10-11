package com.servicedesk.app.presentation.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val tokenStore: TokenStore,
    private val authRepository: StaffAuthRepository,
    private val staffRealtimeService: StaffRealtimeService,
) : ViewModel() {

    private val _status = MutableStateFlow(SessionStatus.Loading)
    val status: StateFlow<SessionStatus> = _status.asStateFlow()

    init {
        viewModelScope.launch {
            tokenStore.load()
            if (tokenStore.cachedToken.isNullOrBlank()) {
                _status.value = SessionStatus.SignedOut
            } else {
                _status.value = SessionStatus.SignedIn
                staffRealtimeService.start()
            }
        }
    }

    fun onSignedIn() {
        _status.value = SessionStatus.SignedIn
        staffRealtimeService.start()
    }

    fun onSignedOut() {
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
