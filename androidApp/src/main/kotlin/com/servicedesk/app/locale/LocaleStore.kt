package com.servicedesk.app.locale

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.localeDataStore by preferencesDataStore(name = "service_desk_locale")

@Singleton
class LocaleStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val activityRecreator: ActivityRecreator,
) {
    private val dataStore = context.localeDataStore

    val language: Flow<AppLanguage> = dataStore.data.map { prefs ->
        AppLanguage.fromTag(prefs[KEY_LANGUAGE])
    }

    suspend fun currentLanguage(): AppLanguage {
        val prefs = dataStore.data.first()
        return AppLanguage.fromTag(prefs[KEY_LANGUAGE])
    }

    suspend fun applyStoredLocale() {
        val lang = currentLanguage()
        applyLocale(lang)
    }

    suspend fun setLanguage(language: AppLanguage) {
        dataStore.edit { it[KEY_LANGUAGE] = language.tag }
        applyLocale(language)
    }

    private fun applyLocale(language: AppLanguage) {
        val tags = when (language) {
            AppLanguage.RUSSIAN -> "ru"
            AppLanguage.CHINESE -> "zh-CN"
        }
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tags))
        activityRecreator.recreate?.invoke()
    }

    private companion object {
        val KEY_LANGUAGE = stringPreferencesKey("app_language")
    }
}
