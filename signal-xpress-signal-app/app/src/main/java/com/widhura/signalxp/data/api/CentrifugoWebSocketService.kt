package com.widhura.signalxp.data.api

import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
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
    private val gson = Gson()

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
                val event = gson.fromJson(json, SignalRealtimeEvent::class.java)
                onSignalUpdate(event)
            } catch (e: Exception) {
                Log.e(TAG, "Signal parse error: ${e.message}")
            }
        }

        subscribeChannel("trading:trades") { json ->
            try {
                val event = gson.fromJson(json, TradeRealtimeEvent::class.java)
                onTradeUpdate(event)
            } catch (e: Exception) {
                Log.e(TAG, "Trade parse error: ${e.message}")
            }
        }

        subscribeChannel("trading:news") { json ->
            try {
                val event = gson.fromJson(json, NewsRealtimeEvent::class.java)
                onNewsUpdate(event)
            } catch (e: Exception) {
                Log.e(TAG, "News parse error: ${e.message}")
            }
        }

        subscribeChannel("trading:community") { json ->
            try {
                val event = gson.fromJson(json, CommunityRealtimeEvent::class.java)
                onCommunityUpdate(event)
            } catch (e: Exception) {
                Log.e(TAG, "Community parse error: ${e.message}")
            }
        }

        subscribeChannel("notifications:broadcast") { json ->
            try {
                val event = gson.fromJson(json, NotificationEvent::class.java)
                onNotification(event)
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
    val id: Long = 0,
    val no: Int = 0,
    val pair: String = "",
    val direction: String = "",
    val entry1: Double = 0.0,
    val entry2: Double = 0.0,
    val sl: Double = 0.0,
    val tp1: Double = 0.0,
    val tp2: Double = 0.0,
    val tp3: Double = 0.0,
    val tp4: Double = 0.0,
    val pips: Double = Double.NaN,
    val profit: Double = Double.NaN,
    val result: String = "RUNNING",
    val channel: String = "VIP",
    val date: String = "",
    val hitLevel: String = "",
    val status: String = "active",
    val thumbsCount: Int = -1,
    val fireCount: Int = -1,
    val rocketCount: Int = -1,
    val brokenHeartCount: Int = -1,
    val type: String = "signal",
    val action: String = ""
) {
    val eventType: String get() = type
}

data class TradeRealtimeEvent(
    val id: Long = 0,
    val no: Int = 0,
    val pair: String = "",
    val direction: String = "",
    val result: String = "RUNNING",
    val pips: Double = Double.NaN,
    val profit: Double = Double.NaN,
    val hitLevel: String = "",
    val type: String = "trade",
    val action: String = ""
) {
    val eventType: String get() = type
}

data class NewsRealtimeEvent(
    val id: Long = 0,
    val currency: String = "",
    val title: String = "",
    val impact: String = "",
    val forecast: String = "",
    val previous: String = "",
    val actual: String = ""
)

data class CommunityRealtimeEvent(
    val id: Long = 0,
    val author_name: String = "",
    val post_type: String = "",
    val content: String = "",
    val pair: String = "",
    val profit_amount: Double = 0.0,
    val pips_gain: Int = 0
) {
    val authorName: String get() = author_name
    val postType: String get() = post_type
    val profitAmount: Double get() = profit_amount
    val pipsGain: Int get() = pips_gain
}

data class NotificationEvent(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val type: String = "info",
    val signal_id: Long = 0,
    val trade_id: Long = 0,
    val result: String = ""
) {
    val signalId: Long get() = signal_id
    val tradeId: Long get() = trade_id
}
