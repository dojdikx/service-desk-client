package com.servicedesk.app.locale

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class AppThemeMode(val storageValue: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark"),
    ;

    companion object {
        fun fromStorage(value: String?): AppThemeMode =
            entries.find { it.storageValue == value } ?: SYSTEM
    }
}

private val Context.themeDataStore by preferencesDataStore(name = "service_desk_theme")

@Singleton
class ThemeStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dataStore = context.themeDataStore

    val mode: Flow<AppThemeMode> = dataStore.data.map { prefs ->
        AppThemeMode.fromStorage(prefs[KEY_THEME_MODE])
    }

    suspend fun setMode(mode: AppThemeMode) {
        dataStore.edit { it[KEY_THEME_MODE] = mode.storageValue }
    }

    private companion object {
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
    }
}
