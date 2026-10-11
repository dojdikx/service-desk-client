package com.servicedesk.app.presentation.auth

import androidx.annotation.StringRes
import com.servicedesk.app.R
import com.servicedesk.app.locale.AppStrings
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.HttpException

fun Throwable.toAuthMessage(appStrings: AppStrings, @StringRes fallbackRes: Int): String {
    if (this is HttpException) {
        val body = response()?.errorBody()?.string()
        if (!body.isNullOrBlank()) {
            runCatching {
                val json = Json.parseToJsonElement(body).jsonObject
                json["message"]?.jsonPrimitive?.content
            }.getOrNull()?.let { return it }
        }
        if (code() == 401) return appStrings.get(R.string.auth_error_invalid_credentials)
        if (code() == 409) return appStrings.get(R.string.auth_error_email_taken)
    }
    return message?.takeIf { it.isNotBlank() } ?: appStrings.get(fallbackRes)
}
