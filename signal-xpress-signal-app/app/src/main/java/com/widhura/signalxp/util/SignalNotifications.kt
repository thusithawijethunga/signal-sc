package com.widhura.signalxp.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.widhura.signalxp.R
import com.widhura.signalxp.data.api.NotificationEvent

/**
 * Local Android (system-tray) notifications for real-time backend events.
 *
 * The in-app banner only works while the app is in the foreground. This posts
 * a heads-up system notification so TP/SL/BE hits are seen even when the user
 * is on another tab or the app is backgrounded (process alive — theSocket
 * connection lives in the app process; true killed-app push needs FCM).
 *
 * Posted ONLY from the `notifications:broadcast` channel (one per admin
 * action) to avoid triple-posting for signal + trade + broadcast events.
 * Reaction broadcasts are skipped — they would spam the tray.
 */
object SignalNotifications {

    const val CHANNEL_ID = "signal_hits"
    private const val CHANNEL_NAME = "Signal Hits"

    // Dedupe replayed broadcasts (Centrifugo resends on resubscribe)
    private val recentIds = ArrayDeque<String>()
    private const val MAX_RECENT = 30

    fun showIfImportant(context: Context, event: NotificationEvent, signalNo: Int = 0) {
        // Too noisy for the system tray — in-app banner still shows these.
        if (event.type == "signal_reaction") return
        if (event.title.isBlank() && event.body.isBlank()) return

        synchronized(recentIds) {
            if (event.id.isNotBlank() && recentIds.contains(event.id)) return
            if (event.id.isNotBlank()) {
                recentIds.addLast(event.id)
                while (recentIds.size > MAX_RECENT) recentIds.removeFirst()
            }
        }

        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        ensureChannel(context)

        val tapIntent = Intent(context, com.widhura.signalxp.ui.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("signal_id", event.signalId)
            putExtra("signal_no", signalNo)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            (event.id.hashCode()),
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(event.title.ifBlank { "Signal Update" })
            .setContentText(event.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(event.body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context)
                .notify(event.id.hashCode(), notification)
        } catch (_: SecurityException) {
            // Permission revoked between check and post — ignore.
        }
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < 26) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Take-profit, stop-loss and signal updates"
            }
        )
    }
}
