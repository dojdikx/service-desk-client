package com.servicedesk.app.presentation.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.servicedesk.app.locale.AppThemeMode
import com.servicedesk.app.locale.ThemeStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ThemeSettingsViewModel @Inject constructor(
    private val themeStore: ThemeStore,
) : ViewModel() {

    val mode: StateFlow<AppThemeMode> = themeStore.mode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppThemeMode.SYSTEM,
    )

    fun setMode(mode: AppThemeMode) {
        viewModelScope.launch { themeStore.setMode(mode) }
    }
}
