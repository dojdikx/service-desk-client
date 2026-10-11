package com.servicedesk.app.data.realtime

import com.servicedesk.app.data.auth.TokenStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class TicketRealtimeSession(
    private val ticketId: Int,
    private val okHttpClient: OkHttpClient,
    private val tokenStore: TokenStore,
    private val onEvent: (RealtimeEventDto) -> Unit,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true }
    private var socket: WebSocket? = null
    private var job: Job? = null

    fun start() {
        if (job?.isActive == true) return
        job = scope.launch {
            while (isActive) {
                val token = tokenStore.token()
                if (token.isNullOrBlank()) {
                    delay(2_000)
                    continue
                }
                val closed = connect(token)
                if (closed) delay(2_500)
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
        socket?.close(1000, "leave")
        socket = null
    }

    private suspend fun connect(token: String): Boolean {
        var closed = false
        val request = Request.Builder()
            .url(WsUrlBuilder.ticket(ticketId, token))
            .build()
        val ws = okHttpClient.newWebSocket(
            request,
            object : WebSocketListener() {
                override fun onMessage(webSocket: WebSocket, text: String) {
                    if (text.equals("pong", ignoreCase = true)) return
                    parseEvent(text)?.let(onEvent)
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    closed = true
                    webSocket.close(code, reason)
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    closed = true
                }
            },
        )
        socket = ws
        while (!closed && scope.isActive) {
            delay(1_000)
        }
        socket = null
        return closed
    }

    private fun parseEvent(raw: String): RealtimeEventDto? =
        runCatching { json.decodeFromString<RealtimeEventDto>(raw) }.getOrNull()
}
