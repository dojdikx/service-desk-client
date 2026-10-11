package com.servicedesk.app.data.realtime

import com.servicedesk.app.data.auth.TokenStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

@Singleton
class StaffRealtimeService @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val tokenStore: TokenStore,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true }
    private val _events = MutableSharedFlow<RealtimeEventDto>(extraBufferCapacity = 32)
    val events: SharedFlow<RealtimeEventDto> = _events.asSharedFlow()

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
                val connected = connectOnce(token)
                if (!connected) {
                    delay(3_000)
                }
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
        socket?.close(1000, "stop")
        socket = null
    }

    private suspend fun connectOnce(token: String): Boolean {
        val request = Request.Builder()
            .url(WsUrlBuilder.staff(token))
            .build()
        var closed = false
        val ws = okHttpClient.newWebSocket(
            request,
            object : WebSocketListener() {
                override fun onMessage(webSocket: WebSocket, text: String) {
                    if (text.equals("pong", ignoreCase = true)) return
                    parseEvent(text)?.let { _events.tryEmit(it) }
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
        return !closed
    }

    private fun parseEvent(raw: String): RealtimeEventDto? =
        runCatching { json.decodeFromString<RealtimeEventDto>(raw) }.getOrNull()
}
