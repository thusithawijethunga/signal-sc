package com.widhura.signalxp.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.widhura.signalxp.R
import com.widhura.signalxp.data.api.NotificationEvent

object SignalNotifications {

    const val CHANNEL_SIGNAL_HITS = "signal_hits"
    const val CHANNEL_CENTRIFUGO_MESSAGES = "centrifugo_messages"
    const val CHANNEL_SERVICE = "centrifugo_service"

    private const val CHANNEL_SIGNAL_HITS_NAME = "Signal Hits"
    private const val CHANNEL_CENTRIFUGO_MESSAGES_NAME = "Live Notifications"
    private const val CHANNEL_SERVICE_NAME = "Background Connection"

    // Dedupe replayed broadcasts
    private val recentIds = ArrayDeque<String>()
    private const val MAX_RECENT = 30

    fun createAllChannels(context: Context) {
        if (Build.VERSION.SDK_INT < 26) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val signalChannel = NotificationChannel(
            CHANNEL_SIGNAL_HITS,
            CHANNEL_SIGNAL_HITS_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Take-profit, stop-loss and signal updates"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 300, 200, 300)
            setShowBadge(true)
        }

        val messageChannel = NotificationChannel(
            CHANNEL_CENTRIFUGO_MESSAGES,
            CHANNEL_CENTRIFUGO_MESSAGES_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Real-time trading notifications"
            enableVibration(true)
            setShowBadge(true)
        }

        val serviceChannel = NotificationChannel(
            CHANNEL_SERVICE,
            CHANNEL_SERVICE_NAME,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Keeps signal updates flowing in background"
            setShowBadge(false)
        }

        manager.createNotificationChannels(listOf(signalChannel, messageChannel, serviceChannel))
    }

    fun showIfImportant(context: Context, event: NotificationEvent, signalNo: Int = 0) {
        val eventType = event.type ?: ""
        val eventTitle = event.title ?: ""
        val eventBody = event.body ?: ""
        val eventId = event.id ?: ""

        if (eventType == "signal_reaction") return
        if (eventTitle.isBlank() && eventBody.isBlank()) return

        // Deduplicate by signal number — when the same signal arrives from both
        // the trading:signals and trading:trades channels we only show one.
        // Use signalNo (unique per signal) instead of eventId (a new UUID per broadcast).
        val dedupeKey = if (signalNo > 0) "sig_$signalNo" else eventId
        synchronized(recentIds) {
            if (dedupeKey.isNotBlank() && recentIds.contains(dedupeKey)) return
            if (dedupeKey.isNotBlank()) {
                recentIds.addLast(dedupeKey)
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

        createAllChannels(context)

        val tapIntent = Intent(context, com.widhura.signalxp.ui.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("signal_id", event.signalId)
            putExtra("signal_no", signalNo)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            eventId.hashCode(),
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Determine channel and style based on event type
        val (channelId, priority, emoji) = when {
            eventType.contains("signal", ignoreCase = true) ||
            eventType.contains("trade", ignoreCase = true) -> {
                val emoji = when {
                    eventBody.contains("BUY", ignoreCase = true) -> "\uD83D\uDFE2"
                    eventBody.contains("SELL", ignoreCase = true) -> "\uD83D\uDD34"
                    eventBody.contains("HOLD", ignoreCase = true) -> "\uD83D\uDFE1"
                    eventBody.contains("WIN", ignoreCase = true) -> "\uD83C\uDFC6"
                    eventBody.contains("LOSS", ignoreCase = true) -> "\uD83D\uDEA8"
                    else -> "\uD83D\uDCE1"
                }
                Triple(CHANNEL_SIGNAL_HITS, NotificationCompat.PRIORITY_HIGH, emoji)
            }
            else -> Triple(CHANNEL_CENTRIFUGO_MESSAGES, NotificationCompat.PRIORITY_DEFAULT, "\uD83D\uDD14")
        }

        val title = "${emoji} ${eventTitle.ifBlank { "Signal Update" }}"
        val body = eventBody

        // Use signalNo as notification ID so the same signal replaces its
        // previous notification instead of stacking duplicates.
        val notifId = if (signalNo > 0) signalNo else eventId.hashCode()

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(priority)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setGroup("signal_updates")
            .build()

        try {
            NotificationManagerCompat.from(context)
                .notify(notifId, notification)
        } catch (_: SecurityException) {
            // Permission revoked
        }
    }

    fun cancelAll(context: Context) {
        NotificationManagerCompat.from(context).cancelAll()
    }
}
