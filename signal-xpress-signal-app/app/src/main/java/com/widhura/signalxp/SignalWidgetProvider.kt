package com.widhura.signalxp

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import com.widhura.signalxp.data.AppDatabase
import com.widhura.signalxp.ui.MainActivity
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class SignalWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_UPDATE_SIGNALS) {
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(ComponentName(context, SignalWidgetProvider::class.java))
            for (id in ids) {
                updateWidget(context, mgr, id)
            }
        }
    }

    companion object {
        const val ACTION_UPDATE_SIGNALS = "com.widhura.signalxp.UPDATE_SIGNAL_WIDGET"

        fun triggerUpdate(context: Context) {
            val intent = Intent(context, SignalWidgetProvider::class.java).apply {
                action = ACTION_UPDATE_SIGNALS
            }
            context.sendBroadcast(intent)
        }

        private fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.signal_widget_layout)

            // Tap widget → open app
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("OPEN_FROM_WIDGET", true)
            }
            val pendingIntent = PendingIntent.getActivity(
                context, appWidgetId, launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            // Read latest signal from DB (blocking is fine — runs briefly for widget update)
            val db = AppDatabase.getDatabase(context)
            val signal = runBlocking {
                db.signalDao().getLatestSignal()
            }

            if (signal == null) {
                views.setTextViewText(R.id.widget_pair, "--")
                views.setTextViewText(R.id.widget_signal_no, "Signal Xpress")
                views.setTextViewText(R.id.widget_status_badge, "Waiting…")
                views.setTextViewText(R.id.widget_direction, "")
                views.setTextViewText(R.id.widget_entry, "--")
                views.setTextViewText(R.id.widget_tp1, "--")
                views.setTextViewText(R.id.widget_tp2, "--")
                views.setTextViewText(R.id.widget_sl, "--")
                views.setTextViewText(R.id.widget_pips, "No signals yet")
                views.setTextViewText(R.id.widget_time, "")
                views.setTextViewText(R.id.widget_result, "")
                appWidgetManager.updateAppWidget(appWidgetId, views)
                return
            }

            // Signal number
            views.setTextViewText(R.id.widget_signal_no, "Signal #${signal.no}")

            // Pair
            views.setTextViewText(R.id.widget_pair, signal.pair)

            // Direction badge (BUY green / SELL red)
            val isBuy = signal.type.uppercase() == "BUY"
            views.setTextViewText(R.id.widget_direction, signal.type.uppercase())
            views.setInt(R.id.widget_direction, "setBackgroundResource",
                if (isBuy) R.drawable.widget_buy_bg else R.drawable.widget_sell_bg)

            // Entry
            views.setTextViewText(R.id.widget_entry, signal.entry.ifEmpty { "--" })

            // TP1 / TP2 / SL
            views.setTextViewText(R.id.widget_tp1, signal.tp1.ifEmpty { "--" })
            views.setTextViewText(R.id.widget_tp2, signal.tp2.ifEmpty { "--" })
            views.setTextViewText(R.id.widget_sl, signal.sl.ifEmpty { "--" })

            // Pips
            val pipsText = if (signal.pips != 0) {
                val prefix = if (signal.pips > 0) "+" else ""
                "$prefix${signal.pips} pips"
            } else "--"
            views.setTextViewText(R.id.widget_pips, pipsText)
            views.setTextColor(R.id.widget_pips, when {
                signal.pips > 0 -> Color.parseColor("#10B981")
                signal.pips < 0 -> Color.parseColor("#EF4444")
                else -> Color.parseColor("#C9D1D9")
            })

            // Status badge
            val statusText = when (signal.result.uppercase()) {
                "WIN" -> "WIN"
                "LOSS" -> "LOSS"
                "BE" -> "BE"
                else -> signal.hitLevel.let {
                    if (it == "NONE" || it.isEmpty()) "RUNNING" else it
                }
            }
            views.setTextViewText(R.id.widget_status_badge, statusText)

            // Status badge color
            val statusColor = when (signal.result.uppercase()) {
                "WIN" -> Color.parseColor("#10B981")
                "LOSS" -> Color.parseColor("#EF4444")
                "BE" -> Color.parseColor("#F59E0B")
                else -> Color.parseColor("#38BDF8")
            }
            views.setTextColor(R.id.widget_status_badge, statusColor)

            // Result text
            views.setTextViewText(R.id.widget_result, signal.result.ifEmpty { "RUNNING" })
            views.setTextColor(R.id.widget_result, when (signal.result.uppercase()) {
                "WIN" -> Color.parseColor("#10B981")
                "LOSS" -> Color.parseColor("#EF4444")
                "BE" -> Color.parseColor("#F59E0B")
                else -> Color.parseColor("#38BDF8")
            })

            // Time ago
            val timeAgo = try {
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(signal.date)
                if (date != null) {
                    val diff = System.currentTimeMillis() - date.time
                    when {
                        diff < TimeUnit.MINUTES.toMillis(1) -> "just now"
                        diff < TimeUnit.HOURS.toMillis(1) -> "${diff / TimeUnit.MINUTES.toMillis(1)}m ago"
                        diff < TimeUnit.DAYS.toMillis(1) -> "${diff / TimeUnit.HOURS.toMillis(1)}h ago"
                        else -> signal.date
                    }
                } else signal.date
            } catch (e: Exception) {
                signal.date
            }
            views.setTextViewText(R.id.widget_time, timeAgo)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
