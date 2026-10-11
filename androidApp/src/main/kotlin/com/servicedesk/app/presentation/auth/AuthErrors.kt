package com.servicedesk.app.presentation.auth

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.HttpException

fun Throwable.toAuthMessage(fallback: String): String {
    if (this is HttpException) {
        val body = response()?.errorBody()?.string()
        if (!body.isNullOrBlank()) {
            runCatching {
                val json = Json.parseToJsonElement(body).jsonObject
                json["message"]?.jsonPrimitive?.content
            }.getOrNull()?.let { return it }
        }
        if (code() == 401) return "Неверная почта или пароль"
        if (code() == 409) return "Этот email уже занят"
    }
    return message?.takeIf { it.isNotBlank() } ?: fallback
}
