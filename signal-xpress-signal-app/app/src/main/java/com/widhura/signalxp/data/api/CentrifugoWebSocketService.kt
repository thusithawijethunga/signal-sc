package com.widhura.signalxp.data.api

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class CentrifugoWebSocketService(
    private val onSignalUpdate: (SignalRealtimeEvent) -> Unit,
    private val onTradeUpdate: (TradeRealtimeEvent) -> Unit,
    private val onNewsUpdate: (NewsRealtimeEvent) -> Unit,
    private val onCommunityUpdate: (CommunityRealtimeEvent) -> Unit,
    private val onNotification: (NotificationEvent) -> Unit,
    private val onConnectionChange: (Boolean) -> Unit
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .connectTimeout(15, TimeUnit.SECONDS)
        .build()

    private var wsUrl: String = "wss://backend.signalxpress.com/connection/websocket"
    private var token: String = ""
    private var isConnected = false
    private var reconnectAttempt = 0
    private var shouldReconnect = true

    fun connect(url: String, authToken: String) {
        wsUrl = url
        token = authToken
        shouldReconnect = true
        reconnectAttempt = 0
        doConnect()
    }

    private fun doConnect() {
        try {
            val request = Request.Builder()
                .url(wsUrl)
                .build()

            webSocket = client.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    Log.d("Centrifugo", "WebSocket connected")
                    isConnected = true
                    reconnectAttempt = 0
                    onConnectionChange(true)
                    sendConnect()
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    handleMessage(text)
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    Log.d("Centrifugo", "WebSocket closing: $code $reason")
                    webSocket.close(1000, null)
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    Log.d("Centrifugo", "WebSocket closed: $code $reason")
                    isConnected = false
                    onConnectionChange(false)
                    scheduleReconnect()
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    Log.e("Centrifugo", "WebSocket failure: ${t.message}")
                    isConnected = false
                    onConnectionChange(false)
                    scheduleReconnect()
                }
            })
        } catch (e: Exception) {
            Log.e("Centrifugo", "Connect error: ${e.message}")
            scheduleReconnect()
        }
    }

    private fun sendConnect() {
        val connectMsg = JSONObject().apply {
            put("id", 1)
            put("connect", JSONObject().apply {
                put("token", token)
            })
        }
        webSocket?.send(connectMsg.toString())
    }

    private fun subscribe(channel: String, subId: Int) {
        val subMsg = JSONObject().apply {
            put("id", subId)
            put("subscribe", JSONObject().apply {
                put("channel", channel)
            })
        }
        webSocket?.send(subMsg.toString())
    }

    private fun handleMessage(text: String) {
        try {
            val json = JSONObject(text)

            // Handle connect response
            if (json.has("connect")) {
                val connectData = json.getJSONObject("connect")
                if (connectData.has("result")) {
                    Log.d("Centrifugo", "Connected successfully, subscribing to channels...")
                    subscribe("trading:signals", 2)
                    subscribe("trading:trades", 3)
                    subscribe("trading:news", 4)
                    subscribe("trading:community", 5)
                    subscribe("notifications:broadcast", 6)
                } else if (connectData.has("error")) {
                    Log.e("Centrifugo", "Connect error: ${connectData.getJSONObject("error")}")
                }
            }

            // Handle subscribe response
            if (json.has("subscribe")) {
                val subData = json.getJSONObject("subscribe")
                Log.d("Centrifugo", "Subscribed: $subData")
            }

            // Handle push messages (publications)
            if (json.has("push")) {
                val push = json.getJSONObject("push")
                val pub = push.getJSONObject("pub")
                val channel = pub.getString("channel")
                val data = pub.getJSONObject("data")

                when (channel) {
                    "trading:signals" -> handleSignalData(data)
                    "trading:trades" -> handleTradeData(data)
                    "trading:news" -> handleNewsData(data)
                    "trading:community" -> handleCommunityData(data)
                    "notifications:broadcast" -> handleNotificationData(data)
                }
            }

            // Handle pong
            if (json.has("pong")) {
                Log.d("Centrifugo", "Pong received")
            }
        } catch (e: Exception) {
            Log.e("Centrifugo", "Parse error: ${e.message}")
        }
    }

    private fun handleSignalData(data: JSONObject) {
        try {
            val event = SignalRealtimeEvent(
                id = data.optLong("id", 0),
                no = data.optInt("no", 0),
                pair = data.optString("pair", ""),
                direction = data.optString("direction", ""),
                entry1 = data.optDouble("entry1", 0.0),
                entry2 = data.optDouble("entry2", 0.0),
                sl = data.optDouble("sl", 0.0),
                tp1 = data.optDouble("tp1", 0.0),
                tp2 = data.optDouble("tp2", 0.0),
                tp3 = data.optDouble("tp3", 0.0),
                tp4 = data.optDouble("tp4", 0.0),
                pips = data.optDouble("pips", 0.0),
                profit = data.optDouble("profit", 0.0),
                result = data.optString("result", "RUNNING"),
                channel = data.optString("channel", "VIP"),
                date = data.optString("date", ""),
                hitLevel = data.optString("hit_level", ""),
                status = data.optString("status", "active"),
                thumbsCount = data.optInt("thumbs_count", 0),
                fireCount = data.optInt("fire_count", 0),
                rocketCount = data.optInt("rocket_count", 0),
                brokenHeartCount = data.optInt("broken_heart_count", 0),
                eventType = data.optString("type", "signal"),
                action = data.optString("action", "")
            )
            onSignalUpdate(event)
        } catch (e: Exception) {
            Log.e("Centrifugo", "Signal parse error: ${e.message}")
        }
    }

    private fun handleTradeData(data: JSONObject) {
        try {
            val event = TradeRealtimeEvent(
                id = data.optLong("id", 0),
                no = data.optInt("no", 0),
                pair = data.optString("pair", ""),
                direction = data.optString("direction", ""),
                result = data.optString("result", "RUNNING"),
                pips = data.optDouble("pips", 0.0),
                profit = data.optDouble("profit", 0.0),
                hitLevel = data.optString("hit_level", ""),
                eventType = data.optString("type", "trade"),
                action = data.optString("action", "")
            )
            onTradeUpdate(event)
        } catch (e: Exception) {
            Log.e("Centrifugo", "Trade parse error: ${e.message}")
        }
    }

    private fun handleNewsData(data: JSONObject) {
        try {
            val event = NewsRealtimeEvent(
                id = data.optLong("id", 0),
                currency = data.optString("currency", ""),
                title = data.optString("title", ""),
                impact = data.optString("impact", ""),
                forecast = data.optString("forecast", ""),
                previous = data.optString("previous", ""),
                actual = data.optString("actual", "")
            )
            onNewsUpdate(event)
        } catch (e: Exception) {
            Log.e("Centrifugo", "News parse error: ${e.message}")
        }
    }

    private fun handleCommunityData(data: JSONObject) {
        try {
            val event = CommunityRealtimeEvent(
                id = data.optLong("id", 0),
                authorName = data.optString("author_name", ""),
                postType = data.optString("post_type", ""),
                content = data.optString("content", ""),
                pair = data.optString("pair", ""),
                profitAmount = data.optDouble("profit_amount", 0.0),
                pipsGain = data.optInt("pips_gain", 0)
            )
            onCommunityUpdate(event)
        } catch (e: Exception) {
            Log.e("Centrifugo", "Community parse error: ${e.message}")
        }
    }

    private fun handleNotificationData(data: JSONObject) {
        try {
            val event = NotificationEvent(
                id = data.optString("id", ""),
                title = data.optString("title", ""),
                body = data.optString("body", ""),
                type = data.optString("type", "info"),
                signalId = data.optLong("signal_id", 0),
                tradeId = data.optLong("trade_id", 0),
                result = data.optString("result", "")
            )
            onNotification(event)
        } catch (e: Exception) {
            Log.e("Centrifugo", "Notification parse error: ${e.message}")
        }
    }

    private fun scheduleReconnect() {
        if (!shouldReconnect) return
        reconnectAttempt++
        val delayMs = minOf(1000L * reconnectAttempt, 30000L)
        Log.d("Centrifugo", "Reconnecting in ${delayMs}ms (attempt $reconnectAttempt)")
        scope.launch {
            delay(delayMs)
            doConnect()
        }
    }

    fun disconnect() {
        shouldReconnect = false
        webSocket?.close(1000, "Disconnecting")
        webSocket = null
        isConnected = false
    }

    fun destroy() {
        disconnect()
        scope.cancel()
    }

    fun sendPing() {
        val pingMsg = JSONObject().apply { put("ping", 1) }
        webSocket?.send(pingMsg.toString())
    }
}

// ── Real-time Event Data Classes ──────────────────────

data class SignalRealtimeEvent(
    val id: Long,
    val no: Int,
    val pair: String,
    val direction: String,
    val entry1: Double,
    val entry2: Double,
    val sl: Double,
    val tp1: Double,
    val tp2: Double,
    val tp3: Double,
    val tp4: Double,
    val pips: Double,
    val profit: Double,
    val result: String,
    val channel: String,
    val date: String,
    val hitLevel: String,
    val status: String,
    val thumbsCount: Int,
    val fireCount: Int,
    val rocketCount: Int,
    val brokenHeartCount: Int,
    val eventType: String,
    val action: String
)

data class TradeRealtimeEvent(
    val id: Long,
    val no: Int,
    val pair: String,
    val direction: String,
    val result: String,
    val pips: Double,
    val profit: Double,
    val hitLevel: String,
    val eventType: String,
    val action: String
)

data class NewsRealtimeEvent(
    val id: Long,
    val currency: String,
    val title: String,
    val impact: String,
    val forecast: String,
    val previous: String,
    val actual: String
)

data class CommunityRealtimeEvent(
    val id: Long,
    val authorName: String,
    val postType: String,
    val content: String,
    val pair: String,
    val profitAmount: Double,
    val pipsGain: Int
)

data class NotificationEvent(
    val id: String,
    val title: String,
    val body: String,
    val type: String,
    val signalId: Long = 0,
    val tradeId: Long = 0,
    val result: String = ""
)
