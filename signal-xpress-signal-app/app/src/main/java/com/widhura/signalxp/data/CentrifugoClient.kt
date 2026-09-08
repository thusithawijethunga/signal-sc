// android/app/src/main/java/com/appjuk/centrifugo/data/CentrifugoClient.kt
package com.signalxpress.app.data

import android.util.Log
import com.google.gson.Gson
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
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

sealed interface ConnectionState {
    data object Disconnected : ConnectionState
    data class Connecting(val reason: String = "") : ConnectionState
    data class Connected(val transport: String) : ConnectionState
    data class Error(val message: String) : ConnectionState
}

@Singleton
class CentrifugoClient @Inject constructor(
    private val gson: Gson,
) {
    companion object {
        private const val TAG = "CentrifugoClient"
    }

    private var client: Client? = null
    private val subscriptions = mutableMapOf<String, Subscription>()

    private var lastWsUrl: String? = null
    private var lastToken: String? = null
    private var shouldReconnect = false
    private var reconnectAttempt = 0
    private var reconnectJob: kotlinx.coroutines.Job? = null

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    // ── Connect ───────────────────────────────────────────────

    fun connect(wsUrl: String, token: String) {
        disconnect()
        lastWsUrl = wsUrl
        lastToken = token
        reconnectAttempt = 0
        shouldReconnect = true

        val opts = Options()
        client = Client(wsUrl, opts, object : EventListener() {
            override fun onConnecting(client: Client, event: ConnectingEvent) {
                Log.i(TAG, "Connecting")
                _connectionState.value = ConnectionState.Connecting("")
            }
            override fun onConnected(client: Client, event: ConnectedEvent) {
                Log.i(TAG, "Connected")
                reconnectAttempt = 0
                _connectionState.value = ConnectionState.Connected("websocket")
            }
            override fun onDisconnected(client: Client, event: DisconnectedEvent) {
                Log.i(TAG, "Disconnected")
                _connectionState.value = ConnectionState.Disconnected
                scheduleReconnect()
            }
            override fun onError(client: Client, event: ErrorEvent) {
                val cause = event.error
                val msg = cause?.message ?: "Unknown error"
                val detail = if (cause != null) android.util.Log.getStackTraceString(cause) else msg
                Log.e(TAG, "Error: $msg\n$detail")
                _connectionState.value = ConnectionState.Error(msg)
            }
        })
        client?.setToken(token)
        client?.connect()
    }

    private fun scheduleReconnect() {
        reconnectJob?.cancel()
        if (!shouldReconnect || lastWsUrl == null || lastToken == null) return

        val delayMs = min(1000L * (1 shl reconnectAttempt.coerceAtMost(5)), 30_000L)
        reconnectAttempt++
        Log.i(TAG, "Scheduling reconnect in ${delayMs}ms (attempt $reconnectAttempt)")

        reconnectJob = scope.launch {
            delay(delayMs)
            if (shouldReconnect) {
                Log.i(TAG, "Reconnecting...")
                client?.setToken(lastToken)
                client?.connect()
                subscriptions.forEach { (_, sub) ->
                    if (sub.state != SubscriptionState.SUBSCRIBED) {
                        sub.subscribe()
                    }
                }
            }
        }
    }

    // ── Subscribe — returns raw NotificationPayload ───────────

    fun subscribeToChannel(channel: String): Flow<NotificationPayload> = callbackFlow {
        val sub = client?.newSubscription(channel, object : SubscriptionEventListener() {
            override fun onPublication(sub: Subscription, event: PublicationEvent) {
                try {
                    val json = String(event.data, Charsets.UTF_8)
                    Log.d(TAG, "Publication on [$channel]: $json")
                    val payload = gson.fromJson(json, NotificationPayload::class.java)
                    trySend(payload)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse publication", e)
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
        }) ?: run {
            close(IllegalStateException("Client not initialized — call connect() first"))
            return@callbackFlow
        }
        subscriptions[channel] = sub
        sub.subscribe()
        awaitClose {
            Log.d(TAG, "Closing flow for channel: $channel")
            if (sub.state == SubscriptionState.SUBSCRIBED) sub.unsubscribe()
            subscriptions.remove(channel)
        }
    }

    // ── Generic typed subscribe — used for trading signals ────
    // NOT inline — inline functions cannot access private members
    // Uses the class-level gson instance (already injected via Hilt)

    fun <T> subscribeToChannelAs(channel: String, clazz: Class<T>): Flow<T> = callbackFlow {
        val sub = client?.newSubscription(channel, object : SubscriptionEventListener() {
            override fun onPublication(sub: Subscription, event: PublicationEvent) {
                try {
                    val json = String(event.data, Charsets.UTF_8)
                    Log.d(TAG, "Publication on [$channel]: $json")
                    trySend(gson.fromJson(json, clazz))
                } catch (e: Exception) {
                    Log.e(TAG, "Parse error on $channel", e)
                }
            }
            override fun onSubscribed(sub: Subscription, event: SubscribedEvent) {
                Log.i(TAG, "Subscribed to: $channel (typed)")
            }
            override fun onUnsubscribed(sub: Subscription, event: UnsubscribedEvent) {
                Log.i(TAG, "Unsubscribed from: $channel")
            }
            override fun onSubscribing(sub: Subscription, event: SubscribingEvent) {
                Log.i(TAG, "Subscribing to: $channel")
            }
        }) ?: run {
            close(IllegalStateException("Client not initialized — call connect() first"))
            return@callbackFlow
        }
        subscriptions[channel] = sub
        sub.subscribe()
        awaitClose {
            Log.d(TAG, "Closing typed flow for channel: $channel")
            if (sub.state == SubscriptionState.SUBSCRIBED) sub.unsubscribe()
            subscriptions.remove(channel)
        }
    }

    // Convenience reified wrapper — callers write subscribeToChannelAs<MyType>(channel)
    inline fun <reified T> subscribeToChannelAs(channel: String): Flow<T> =
        subscribeToChannelAs(channel, T::class.java)

    // ── Unsubscribe / Disconnect ──────────────────────────────

    fun unsubscribe(channel: String) {
        subscriptions[channel]?.unsubscribe()
        subscriptions.remove(channel)
    }

    fun disconnect() {
        shouldReconnect = false
        reconnectJob?.cancel()
        reconnectJob = null
        subscriptions.values.forEach { it.unsubscribe() }
        subscriptions.clear()
        client?.disconnect()
        client = null
        _connectionState.value = ConnectionState.Disconnected
    }
}
