package com.widhura.signalxp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.widhura.signalxp.data.AppDatabase
import com.widhura.signalxp.data.api.ApiClient
import com.widhura.signalxp.data.api.CentrifugoEventBus
import com.widhura.signalxp.data.api.CentrifugoWebSocketService
import com.widhura.signalxp.data.api.CommunityRealtimeEvent
import com.widhura.signalxp.data.api.NewsRealtimeEvent
import com.widhura.signalxp.data.api.NotificationEvent
import com.widhura.signalxp.data.api.SignalRealtimeEvent
import com.widhura.signalxp.data.api.TradeRealtimeEvent
import com.widhura.signalxp.ui.MainActivity
import com.widhura.signalxp.util.SignalNotifications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationForegroundService : Service() {

    companion object {
        private const val TAG = "NotifForegroundService"
        private const val CHANNEL_ID = "centrifugo_service"
        private const val CHANNEL_NAME = "Background Connection"
        private const val NOTIFICATION_ID = 9999
        private const val ACTION_START = "ACTION_START"
        private const val ACTION_STOP = "ACTION_STOP"
        private const val EXTRA_USER_ID = "user_id"
        private const val RETRY_DELAY_MS = 5_000L

        // Real token endpoint lives on the same backend as everything else in
        // ApiClient/ApiService (Route::get('/websocket/token', ...) in routes/api.php,
        // now behind the api.auth middleware). There is no separate
        // market.signalxpress.com mobile gateway and no X-API-KEY check on this route.
        private val WS_TOKEN_URL = ApiClient.BASE_URL + "websocket/token"

        fun start(context: Context, userId: String) {
            val intent = Intent(context, NotificationForegroundService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_USER_ID, userId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, NotificationForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private val binder = LocalBinder()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var centrifugoService: CentrifugoWebSocketService? = null
    private var wsToken: String? = null
    private var wsUrl: String? = null
    private var isActivelyConnected = false

    var onSignalUpdate: ((SignalRealtimeEvent) -> Unit)? = null
    var onTradeUpdate: ((TradeRealtimeEvent) -> Unit)? = null
    var onNewsUpdate: ((NewsRealtimeEvent) -> Unit)? = null
    var onCommunityUpdate: ((CommunityRealtimeEvent) -> Unit)? = null
    var onNotification: ((NotificationEvent) -> Unit)? = null
    var onConnectionChange: ((Boolean) -> Unit)? = null

    inner class LocalBinder : Binder() {
        fun getService(): NotificationForegroundService = this@NotificationForegroundService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        SignalNotifications.createAllChannels(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val userId = intent.getStringExtra(EXTRA_USER_ID) ?: return START_STICKY
                startForeground(NOTIFICATION_ID, buildNotification("Connecting..."))
                connectWebSocket(userId)
            }
            ACTION_STOP -> {
                disconnectWebSocket()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            // START_STICKY can redeliver a null/empty intent after the process is
            // killed and restarted by the OS. Reconnect using the last known user
            // instead of silently doing nothing until the app is reopened.
            else -> {
                val userId = ApiClient.getCurrentUserId(this).toString()
                if (ApiClient.isLoggedIn(this) && !isActivelyConnected) {
                    startForeground(NOTIFICATION_ID, buildNotification("Reconnecting..."))
                    connectWebSocket(userId)
                }
            }
        }
        return START_STICKY
    }

    private fun connectWebSocket(userId: String) {
        val authToken = ApiClient.getToken(this) ?: return

        scope.launch {
            try {
                val request = Request.Builder()
                    .url(WS_TOKEN_URL)
                    .header("Authorization", "Bearer $authToken")
                    .header("Accept", "application/json")
                    .build()

                val client = OkHttpClient()
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: run {
                        Log.e(TAG, "Token endpoint returned an empty body")
                        scheduleRetry(userId)
                        return@launch
                    }
                    val json = JSONObject(body)

                    // WebSocketController::token() returns a flat object:
                    // { "token": "...", "ws_url": "...", "channels": {...}, ... }
                    val tokenValue = json.optString("token", "")
                    val urlValue = json.optString("ws_url", "")

                    if (tokenValue.isBlank() || urlValue.isBlank()) {
                        Log.e(TAG, "Token response missing token/ws_url: $body")
                        scheduleRetry(userId)
                        return@launch
                    }

                    wsToken = tokenValue
                    wsUrl = urlValue

                    createCentrifugoService()
                    centrifugoService?.connect(wsUrl!!, wsToken!!)

                    updateNotification("Connected to Signal Xpress")
                } else {
                    Log.e(TAG, "Failed to fetch WS token: ${response.code} ${response.body?.string()}")
                    scheduleRetry(userId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to connect: ${e.message}")
                scheduleRetry(userId)
            }
        }
    }

    private fun createCentrifugoService() {
        centrifugoService?.destroy()

        centrifugoService = CentrifugoWebSocketService(
            onSignalUpdate = { event ->
                Log.d(TAG, "Signal received: ${event.pair} ${event.direction}")
                CentrifugoEventBus.emitSignal(event)
                scope.launch(Dispatchers.IO) {
                    handleRealtimeSignal(event)
                }
                onSignalUpdate?.invoke(event)
            },
            onTradeUpdate = { event ->
                Log.d(TAG, "Trade received: ${event.pair} ${event.result}")
                CentrifugoEventBus.emitTrade(event)
                scope.launch(Dispatchers.IO) {
                    handleRealtimeTrade(event)
                }
                onTradeUpdate?.invoke(event)
            },
            onNewsUpdate = { event ->
                CentrifugoEventBus.emitNews(event)
                onNewsUpdate?.invoke(event)
            },
            onCommunityUpdate = { event ->
                CentrifugoEventBus.emitCommunity(event)
                onCommunityUpdate?.invoke(event)
            },
            onNotification = { event ->
                Log.d(TAG, "Notification: ${event.title}")
                CentrifugoEventBus.emitNotification(event)
                scope.launch(Dispatchers.IO) {
                    handleNotification(event)
                }
                onNotification?.invoke(event)
            },
            onConnectionChange = { connected ->
                Log.d(TAG, "Connection changed: $connected")
                isActivelyConnected = connected
                CentrifugoEventBus.emitConnectionState(connected)
                updateNotification(if (connected) "Connected to Signal Xpress" else "Reconnecting...")
                onConnectionChange?.invoke(connected)
            },
            onAuthFailed = {
                Log.w(TAG, "Auth failed, refreshing token...")
                val userId = ApiClient.getCurrentUserId(this@NotificationForegroundService).toString()
                refreshAndReconnect(userId)
            }
        )
    }

    private suspend fun handleRealtimeSignal(event: SignalRealtimeEvent) {
        val db = AppDatabase.getDatabase(applicationContext)
        val eventNo = event.no ?: 0
        val eventId = event.id ?: 0L
        val eventAction = event.action ?: ""

        if (eventAction == "deleted") {
            if (eventId != 0L) db.signalDao().deleteSignalById(eventId)
            if (eventNo != 0) {
                db.signalDao().getSignalByNo(eventNo)?.let {
                    if (it.id != eventId) db.signalDao().deleteSignalById(it.id)
                }
            }
            return
        }

        val byId = if (eventId != 0L) db.signalDao().getSignalById(eventId) else null
        val byNo = if (eventNo != 0) db.signalDao().getSignalByNo(eventNo) else null
        val existing = byId ?: byNo

        if (eventAction == "reaction") {
            if (existing != null) {
                val updated = existing.copy(
                    thumbsCount = event.thumbsCount ?: existing.thumbsCount,
                    fireCount = event.fireCount ?: existing.fireCount,
                    rocketCount = event.rocketCount ?: existing.rocketCount,
                    brokenHeartCount = event.brokenHeartCount ?: existing.brokenHeartCount
                )
                db.signalDao().updateSignal(updated)
            }
            return
        }

        val e1 = event.entry1 ?: 0.0
        val e2 = event.entry2 ?: 0.0
        val entry = if (e2 > 0) "$e1 / $e2"
                    else if (e1 > 0) e1.toString()
                    else existing?.entry ?: ""

        val resolvedId = when {
            byId != null -> eventId
            byNo != null && eventId != 0L -> eventId
            byNo != null -> byNo.id
            else -> eventId
        }

        val entity = com.widhura.signalxp.data.SignalEntity(
            id = resolvedId,
            no = eventNo,
            date = event.date ?: existing?.date ?: "",
            pair = event.pair ?: existing?.pair ?: "",
            type = event.direction ?: existing?.type ?: "BUY",
            entry = entry,
            tp1 = if ((event.tp1 ?: 0.0) > 0) event.tp1.toString() else existing?.tp1 ?: "",
            tp2 = if ((event.tp2 ?: 0.0) > 0) event.tp2.toString() else existing?.tp2 ?: "",
            tp3 = if ((event.tp3 ?: 0.0) > 0) event.tp3.toString() else existing?.tp3 ?: "",
            tp4 = if ((event.tp4 ?: 0.0) > 0) event.tp4.toString() else existing?.tp4 ?: "",
            sl = if ((event.sl ?: 0.0) > 0) event.sl.toString() else existing?.sl ?: "",
            pips = if (event.pips != null) event.pips.toInt() else existing?.pips ?: 0,
            profit = event.profit ?: existing?.profit ?: 0.0,
            hitLevel = event.hitLevel ?: existing?.hitLevel ?: "NONE",
            status = event.status ?: existing?.status ?: "active",
            result = event.result ?: existing?.result ?: "RUNNING",
            thumbsCount = event.thumbsCount ?: existing?.thumbsCount ?: 0,
            fireCount = event.fireCount ?: existing?.fireCount ?: 0,
            rocketCount = event.rocketCount ?: existing?.rocketCount ?: 0,
            brokenHeartCount = event.brokenHeartCount ?: existing?.brokenHeartCount ?: 0,
            userReactedEmoji = existing?.userReactedEmoji
        )

        if (existing == null) {
            db.signalDao().insertSignal(entity)
        } else {
            db.signalDao().updateSignal(entity)
        }
    }

    private suspend fun handleRealtimeTrade(event: TradeRealtimeEvent) {
        val db = AppDatabase.getDatabase(applicationContext)
        val eventNo = event.no ?: 0
        val eventAction = event.action ?: ""

        if (eventAction == "deleted") {
            val existing = db.signalDao().getSignalByNo(eventNo)
            if (existing != null) db.signalDao().deleteSignalById(existing.id)
            return
        }

        val existing = db.signalDao().getSignalByNo(eventNo)
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        if (eventAction == "created") {
            val entity = com.widhura.signalxp.data.SignalEntity(
                id = existing?.id ?: 0,
                no = eventNo,
                date = existing?.date ?: todayStr,
                pair = event.pair ?: existing?.pair ?: "",
                type = event.direction ?: existing?.type ?: "BUY",
                entry = existing?.entry ?: "",
                tp1 = existing?.tp1 ?: "", tp2 = existing?.tp2 ?: "",
                tp3 = existing?.tp3 ?: "", tp4 = existing?.tp4 ?: "",
                sl = existing?.sl ?: "",
                pips = if (event.pips != null) event.pips.toInt() else existing?.pips ?: 0,
                profit = event.profit ?: existing?.profit ?: 0.0,
                hitLevel = event.hitLevel ?: existing?.hitLevel ?: "NONE",
                status = existing?.status ?: "active",
                result = event.result ?: existing?.result ?: "RUNNING"
            )
            if (existing == null) {
                db.signalDao().insertSignal(entity)
            } else {
                db.signalDao().updateSignal(entity)
            }
        } else if (existing != null) {
            val updated = existing.copy(
                result = event.result ?: existing.result,
                pips = if (event.pips != null) event.pips.toInt() else existing.pips,
                profit = event.profit ?: existing.profit,
                hitLevel = event.hitLevel ?: existing.hitLevel
            )
            db.signalDao().updateSignal(updated)
        }
    }

    private suspend fun handleNotification(event: NotificationEvent) {
        var signalNo = event.signalNo
        // Fallback: look up from DB if signal_no not included in the broadcast
        if (signalNo == 0 && event.signalId != 0L) {
            try {
                signalNo = AppDatabase.getDatabase(applicationContext)
                    .signalDao().getSignalById(event.signalId)?.no ?: 0
            } catch (e: Exception) {
                Log.d(TAG, "signalNo lookup failed: ${e.message}")
            }
        }
        SignalNotifications.showIfImportant(applicationContext, event, signalNo)
    }

    private fun refreshAndReconnect(userId: String) {
        centrifugoService?.destroy()
        centrifugoService = null
        isActivelyConnected = false
        connectWebSocket(userId)
    }

    private fun scheduleRetry(userId: String) {
        scope.launch {
            delay(RETRY_DELAY_MS)
            connectWebSocket(userId)
        }
    }

    fun setToken(token: String) {
        wsToken = token
        centrifugoService?.refreshToken(token)
    }

    fun isConnected(): Boolean = isActivelyConnected

    private fun disconnectWebSocket() {
        centrifugoService?.destroy()
        centrifugoService = null
        isActivelyConnected = false
    }

    private fun buildNotification(text: String): android.app.Notification {
        val tapIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Signal Xpress")
            .setContentText(text)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setSilent(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification(text))
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnectWebSocket()
        scope.cancel()
    }
}
