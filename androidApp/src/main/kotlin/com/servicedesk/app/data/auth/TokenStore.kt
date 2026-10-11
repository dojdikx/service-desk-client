package com.servicedesk.app.data.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

private val Context.authDataStore by preferencesDataStore(name = "service_desk_auth")

@Singleton
class TokenStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dataStore = context.authDataStore

    @Volatile
    var cachedToken: String? = null
        private set

    @Volatile
    var cachedEmail: String? = null
        private set

    suspend fun save(token: String, email: String) {
        cachedToken = token
        cachedEmail = email
        dataStore.edit { prefs ->
            prefs[KEY_TOKEN] = token
            prefs[KEY_EMAIL] = email
        }
    }

    suspend fun load() {
        val prefs = dataStore.data.first()
        cachedToken = prefs[KEY_TOKEN]
        cachedEmail = prefs[KEY_EMAIL]
    }

    suspend fun token(): String? {
        cachedToken?.let { return it }
        load()
        return cachedToken
    }

    suspend fun clear() {
        cachedToken = null
        cachedEmail = null
        dataStore.edit { it.clear() }
    }

    private companion object {
        val KEY_TOKEN = stringPreferencesKey("jwt")
        val KEY_EMAIL = stringPreferencesKey("email")
    }
}
