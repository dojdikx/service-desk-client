package com.servicedesk.app.presentation.locale

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.servicedesk.app.locale.AppLanguage
import com.servicedesk.app.locale.LocaleStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class LocaleSettingsViewModel @Inject constructor(
    private val localeStore: LocaleStore,
) : ViewModel() {

    val language: StateFlow<AppLanguage> = localeStore.language
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppLanguage.RUSSIAN)

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            localeStore.setLanguage(language)
        }
    }
}
