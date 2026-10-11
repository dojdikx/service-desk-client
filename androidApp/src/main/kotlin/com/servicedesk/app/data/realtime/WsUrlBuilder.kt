package com.servicedesk.app.data.realtime

import com.servicedesk.app.BuildConfig
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object WsUrlBuilder {
    fun ticket(ticketId: Int, token: String): String {
        val base = httpToWs(BuildConfig.SERVER_BASE_URL.trimEnd('/'))
        val encoded = URLEncoder.encode(token, StandardCharsets.UTF_8.toString())
        return "$base/api/ws/tickets/$ticketId?token=$encoded"
    }

    fun staff(token: String): String {
        val base = httpToWs(BuildConfig.SERVER_BASE_URL.trimEnd('/'))
        val encoded = URLEncoder.encode(token, StandardCharsets.UTF_8.toString())
        return "$base/api/ws/staff?token=$encoded"
    }

    private fun httpToWs(url: String): String =
        when {
            url.startsWith("https://") -> "wss://" + url.removePrefix("https://")
            url.startsWith("http://") -> "ws://" + url.removePrefix("http://")
            else -> url
        }
}
