package com.widhura.signalxp.data.api

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.widhura.signalxp.data.AppDatabase
import com.widhura.signalxp.util.SignalNotifications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Fallback delivery path that works in ANY app state (foreground, background,
 * or process killed): the Laravel backend sends an FCM data message for every
 * signal broadcast, and Play services wakes us to show it — no WebSocket
 * or foreground service required.
 *
 * Dedupe with live WebSocket events is handled by
 * [SignalNotifications.showIfImportant] (same signal_no key + notification id).
 */
class SignalFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FcmPush"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        if (data.isEmpty()) {
            Log.d(TAG, "Ignoring FCM message without data payload")
            return
        }
        Log.d(TAG, "FCM data message: type=${data["type"]} title=${data["title"]}")
        scope.launch {
            try {
                val event = NotificationEvent(
                    id = data["id"],
                    title = data["title"],
                    body = data["body"],
                    type = data["type"],
                    signal_id = data["signal_id"]?.toLongOrNull(),
                    trade_id = data["trade_id"]?.toLongOrNull(),
                    signal_no = data["signal_no"]?.toIntOrNull(),
                    result = data["result"],
                )
                var signalNo = event.signalNo
                if (signalNo == 0 && event.signalId != 0L) {
                    try {
                        signalNo = AppDatabase.getDatabase(applicationContext)
                            .signalDao().getSignalById(event.signalId)?.no ?: 0
                    } catch (e: Exception) {
                        Log.d(TAG, "signalNo lookup failed: ${e.message}")
                    }
                }
                SignalNotifications.showIfImportant(applicationContext, event, signalNo)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to handle FCM message: ${e.message}", e)
            }
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "FCM token rotated, uploading")
        FcmTokenRegistrar.upload(applicationContext, token)
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
