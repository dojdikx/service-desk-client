package com.servicedesk.app.data.search

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

private val Context.searchHistoryDataStore by preferencesDataStore(name = "ticket_search_history")

@Singleton
class SearchHistoryStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val dataStore = context.searchHistoryDataStore

    suspend fun history(): List<String> {
        val raw = dataStore.data.map { it[KEY_HISTORY] }.first()
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            json.decodeFromString(ListSerializer(String.serializer()), raw)
        }.getOrElse { emptyList() }
    }

    suspend fun addQuery(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return
        val updated = history()
            .filterNot { it.equals(trimmed, ignoreCase = true) }
            .let { listOf(trimmed) + it }
            .take(MAX_ITEMS)
        dataStore.edit { prefs ->
            prefs[KEY_HISTORY] = json.encodeToString(ListSerializer(String.serializer()), updated)
        }
    }

    suspend fun clear() {
        dataStore.edit { it.remove(KEY_HISTORY) }
    }

    private companion object {
        val KEY_HISTORY = stringPreferencesKey("queries")
        const val MAX_ITEMS = 10
    }
}
