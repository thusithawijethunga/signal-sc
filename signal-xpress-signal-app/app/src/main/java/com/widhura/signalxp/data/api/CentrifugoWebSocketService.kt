package com.widhura.signalxp.data.api

import android.util.Log
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.github.centrifugal.centrifuge.Client
import io.github.centrifugal.centrifuge.ConnectedEvent
import io.github.centrifugal.centrifuge.ConnectingEvent
import io.github.centrifugal.centrifuge.DisconnectedEvent
import io.github.centrifugal.centrifuge.ErrorEvent
import io.github.centrifugal.centrifuge.EventListener
import io.github.centrifugal.centrifuge.Options
import io.github.centrifugal.centrifuge.PublicationEvent
import io.github.centrifugal.centrifuge.Subscription
import io.github.centrifugal.centrifuge.SubscribedEvent
import io.github.centrifugal.centrifuge.SubscribingEvent
import io.github.centrifugal.centrifuge.SubscriptionEventListener
import io.github.centrifugal.centrifuge.SubscriptionState
import io.github.centrifugal.centrifuge.UnsubscribedEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min

class CentrifugoWebSocketService(
    private val onSignalUpdate: (SignalRealtimeEvent) -> Unit,
    private val onTradeUpdate: (TradeRealtimeEvent) -> Unit,
    private val onNewsUpdate: (NewsRealtimeEvent) -> Unit,
    private val onCommunityUpdate: (CommunityRealtimeEvent) -> Unit,
    private val onNotification: (NotificationEvent) -> Unit,
    private val onConnectionChange: (Boolean) -> Unit,
    private val onAuthFailed: (() -> Unit)? = null
) {

    companion object {
        private const val TAG = "CentrifugoClient"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var client: Client? = null
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val signalAdapter = moshi.adapter(SignalRealtimeEvent::class.java)
    private val tradeAdapter = moshi.adapter(TradeRealtimeEvent::class.java)
    private val newsAdapter = moshi.adapter(NewsRealtimeEvent::class.java)
    private val communityAdapter = moshi.adapter(CommunityRealtimeEvent::class.java)
    private val notificationAdapter = moshi.adapter(NotificationEvent::class.java)

    private var wsUrl: String = "wss://socket.signalxpress.com/connection/websocket"
    private var token: String = ""
    private var shouldReconnect = false
    private var reconnectAttempt = 0
    private var reconnectJob: kotlinx.coroutines.Job? = null

    private val channels = mutableMapOf<String, Subscription>()

    fun connect(url: String, authToken: String) {
        disconnect()
        wsUrl = url
        token = authToken
        shouldReconnect = true
        reconnectAttempt = 0
        doConnect()
    }

    private fun doConnect() {
        try {
            val opts = Options()
            client = Client(wsUrl, opts, object : EventListener() {
                override fun onConnecting(c: Client, event: ConnectingEvent) {
                    Log.i(TAG, "Connecting to $wsUrl")
                }

                override fun onConnected(c: Client, event: ConnectedEvent) {
                    Log.i(TAG, "Connected successfully")
                    reconnectAttempt = 0
                    onConnectionChange(true)
                    subscribeToAllChannels()
                }

                override fun onDisconnected(c: Client, event: DisconnectedEvent) {
                    Log.w(TAG, "Disconnected: code=${event.code}")
                    onConnectionChange(false)
                    scheduleReconnect()
                }

                override fun onError(c: Client, event: ErrorEvent) {
                    Log.e(TAG, "Error: ${event.error?.message}")
                    val msg = event.error?.message ?: ""
                    if (msg.contains("unauthorized", ignoreCase = true) ||
                        msg.contains("invalid token", ignoreCase = true)) {
                        try { onAuthFailed?.invoke() } catch (e: Exception) {
                            Log.e(TAG, "onAuthFailed callback failed: ${e.message}")
                        }
                    }
                }
            })

            client?.setToken(token)
            client?.connect()
        } catch (e: Exception) {
            Log.e(TAG, "Connect error: ${e.message}")
            scheduleReconnect()
        }
    }

    private fun subscribeToAllChannels() {
        subscribeChannel("trading:signals") { json ->
            try {
                val event = signalAdapter.fromJson(json)
                if (event != null) onSignalUpdate(event)
            } catch (e: Exception) {
                Log.e(TAG, "Signal parse error: ${e.message}")
            }
        }

        subscribeChannel("trading:trades") { json ->
            try {
                val event = tradeAdapter.fromJson(json)
                if (event != null) onTradeUpdate(event)
            } catch (e: Exception) {
                Log.e(TAG, "Trade parse error: ${e.message}")
            }
        }

        subscribeChannel("trading:news") { json ->
            try {
                val event = newsAdapter.fromJson(json)
                if (event != null) onNewsUpdate(event)
            } catch (e: Exception) {
                Log.e(TAG, "News parse error: ${e.message}")
            }
        }

        subscribeChannel("trading:community") { json ->
            try {
                val event = communityAdapter.fromJson(json)
                if (event != null) onCommunityUpdate(event)
            } catch (e: Exception) {
                Log.e(TAG, "Community parse error: ${e.message}")
            }
        }

        subscribeChannel("notifications:broadcast") { json ->
            try {
                val event = notificationAdapter.fromJson(json)
                if (event != null) onNotification(event)
            } catch (e: Exception) {
                Log.e(TAG, "Notification parse error: ${e.message}")
            }
        }
    }

    private fun subscribeChannel(channel: String, onMessage: (String) -> Unit) {
        val sub = client?.newSubscription(channel, object : SubscriptionEventListener() {
            override fun onPublication(sub: Subscription, event: PublicationEvent) {
                try {
                    val json = String(event.data, Charsets.UTF_8)
                    Log.d(TAG, "Publication on [$channel]: $json")
                    onMessage(json)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse publication on $channel", e)
                }
            }

            override fun onSubscribed(sub: Subscription, event: SubscribedEvent) {
                Log.i(TAG, "Subscribed to: $channel")
            }

            override fun onUnsubscribed(sub: Subscription, event: UnsubscribedEvent) {
                Log.i(TAG, "Unsubscribed from: $channel")
            }

            override fun onSubscribing(sub: Subscription, event: SubscribingEvent) {
                Log.i(TAG, "Subscribing to: $channel")
            }
        }) ?: return

        channels[channel] = sub
        sub.subscribe()
    }

    private fun scheduleReconnect() {
        reconnectJob?.cancel()
        if (!shouldReconnect) return

        val delayMs = min(1000L * (1 shl reconnectAttempt.coerceAtMost(5)), 30_000L)
        reconnectAttempt++
        Log.i(TAG, "Reconnecting in ${delayMs}ms (attempt $reconnectAttempt)")

        reconnectJob = scope.launch {
            delay(delayMs)
            if (shouldReconnect) {
                client?.setToken(token)
                client?.connect()
                channels.forEach { (_, sub) ->
                    if (sub.state != SubscriptionState.SUBSCRIBED) {
                        sub.subscribe()
                    }
                }
            }
        }
    }

    fun refreshToken(newToken: String) {
        token = newToken
        client?.setToken(newToken)
    }

    fun disconnect() {
        shouldReconnect = false
        reconnectJob?.cancel()
        reconnectJob = null
        channels.values.forEach { it.unsubscribe() }
        channels.clear()
        client?.disconnect()
        client = null
        onConnectionChange(false)
    }

    fun destroy() {
        disconnect()
        scope.cancel()
    }

    fun isConnected(): Boolean {
        return channels.values.any { it.state == SubscriptionState.SUBSCRIBED }
    }
}

data class SignalRealtimeEvent(
    val id: Long? = null,
    val no: Int? = null,
    val pair: String? = null,
    val direction: String? = null,
    val entry1: Double? = null,
    val entry2: Double? = null,
    val sl: Double? = null,
    val tp1: Double? = null,
    val tp2: Double? = null,
    val tp3: Double? = null,
    val tp4: Double? = null,
    val pips: Double? = null,
    val profit: Double? = null,
    val result: String? = null,
    val channel: String? = null,
    val date: String? = null,
    @Json(name = "hit_level") val hitLevel: String? = null,
    val status: String? = null,
    @Json(name = "thumbs_count") val thumbsCount: Int? = null,
    @Json(name = "fire_count") val fireCount: Int? = null,
    @Json(name = "rocket_count") val rocketCount: Int? = null,
    @Json(name = "broken_heart_count") val brokenHeartCount: Int? = null,
    val type: String? = null,
    val action: String? = null
) {
    val eventType: String get() = type ?: "signal"
}

data class TradeRealtimeEvent(
    val id: Long? = null,
    val no: Int? = null,
    val pair: String? = null,
    val direction: String? = null,
    val result: String? = null,
    val pips: Double? = null,
    val profit: Double? = null,
    @Json(name = "hit_level") val hitLevel: String? = null,
    val type: String? = null,
    val action: String? = null
) {
    val eventType: String get() = type ?: "trade"
}

data class NewsRealtimeEvent(
    val id: Long? = null,
    val currency: String? = null,
    val title: String? = null,
    val impact: String? = null,
    val forecast: String? = null,
    val previous: String? = null,
    val actual: String? = null
)

data class CommunityRealtimeEvent(
    val id: Long? = null,
    @Json(name = "author_name") val author_name: String? = null,
    @Json(name = "post_type") val post_type: String? = null,
    val content: String? = null,
    val pair: String? = null,
    @Json(name = "profit_amount") val profit_amount: Double? = null,
    @Json(name = "pips_gain") val pips_gain: Int? = null
) {
    val authorName: String get() = author_name ?: ""
    val postType: String get() = post_type ?: ""
    val profitAmount: Double get() = profit_amount ?: 0.0
    val pipsGain: Int get() = pips_gain ?: 0
}

data class NotificationEvent(
    val id: String? = null,
    val title: String? = null,
    val body: String? = null,
    val type: String? = null,
    @Json(name = "signal_id") val signal_id: Long? = null,
    @Json(name = "trade_id") val trade_id: Long? = null,
    @Json(name = "signal_no") val signal_no: Int? = null,
    val result: String? = null
) {
    val signalId: Long get() = signal_id ?: 0
    val tradeId: Long get() = trade_id ?: 0
    val signalNo: Int get() = signal_no ?: 0
}
