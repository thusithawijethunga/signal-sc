package com.widhura.signalxp

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import com.widhura.signalxp.data.WidgetPreferences
import com.widhura.signalxp.ui.MainActivity
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

            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("OPEN_FROM_WIDGET", true)
            }
            val pendingIntent = PendingIntent.getActivity(
                context, appWidgetId, launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            val pair = WidgetPreferences.getPair(context)
            val signalNo = WidgetPreferences.getSignalNo(context)

            if (pair.isEmpty() || signalNo == 0) {
                views.setTextViewText(R.id.widget_signal_no, "Signal Xpress")
                views.setTextViewText(R.id.widget_pair, "--")
                views.setTextViewText(R.id.widget_entry, "Waiting for signal\u2026")
                views.setTextViewText(R.id.widget_tp1, "")
                views.setTextViewText(R.id.widget_pips, "")
                views.setTextViewText(R.id.widget_result, "")
                views.setTextViewText(R.id.widget_time, "")
                appWidgetManager.updateAppWidget(appWidgetId, views)
                return
            }

            val direction = WidgetPreferences.getDirection(context)
            val entry = WidgetPreferences.getEntry(context)
            val tp1 = WidgetPreferences.getTp1(context)
            val tp2 = WidgetPreferences.getTp2(context)
            val sl = WidgetPreferences.getSl(context)
            val pips = WidgetPreferences.getPips(context)
            val result = WidgetPreferences.getResult(context)
            val hitLevel = WidgetPreferences.getHitLevel(context)
            val date = WidgetPreferences.getDate(context)

            val dirLabel = direction.uppercase()
            val isBuy = dirLabel == "BUY"
            val pairLine = if (dirLabel.isNotEmpty()) "$pair $dirLabel" else pair

            views.setTextViewText(R.id.widget_signal_no, "#$signalNo")
            views.setTextViewText(R.id.widget_pair, pairLine)

            val entryText = buildString {
                append("Entry: ")
                append(entry.ifEmpty { "--" })
            }
            views.setTextViewText(R.id.widget_entry, entryText)

            val tpParts = mutableListOf<String>()
            if (tp1.isNotEmpty()) tpParts.add("TP1 $tp1")
            if (tp2.isNotEmpty()) tpParts.add("TP2 $tp2")
            if (sl.isNotEmpty()) tpParts.add("SL $sl")
            views.setTextViewText(R.id.widget_tp1, tpParts.joinToString("   "))

            val pipsText = if (pips != 0) {
                val prefix = if (pips > 0) "+" else ""
                "${prefix}${pips} pips"
            } else "-- pips"
            views.setTextViewText(R.id.widget_pips, pipsText)
            views.setTextColor(R.id.widget_pips, when {
                pips > 0 -> Color.parseColor("#10B981")
                pips < 0 -> Color.parseColor("#EF4444")
                else -> Color.parseColor("#C9D1D9")
            })

            val statusText = when (result.uppercase()) {
                "WIN" -> "WIN"
                "LOSS" -> "LOSS"
                "BE" -> "BE"
                else -> if (hitLevel != "NONE" && hitLevel.isNotEmpty()) hitLevel else "RUN"
            }
            views.setTextViewText(R.id.widget_result, statusText)
            views.setInt(R.id.widget_result, "setBackgroundResource", R.drawable.widget_badge_bg)
            views.setTextColor(R.id.widget_result, when (result.uppercase()) {
                "WIN" -> Color.parseColor("#34D399")
                "LOSS" -> Color.parseColor("#F87171")
                "BE" -> Color.parseColor("#FBBF24")
                else -> Color.parseColor("#7DD3FC")
            })

            val timeAgo = try {
                val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(date)
                if (parsed != null) {
                    val diff = System.currentTimeMillis() - parsed.time
                    when {
                        diff < TimeUnit.MINUTES.toMillis(1) -> "just now"
                        diff < TimeUnit.HOURS.toMillis(1) -> "${diff / TimeUnit.MINUTES.toMillis(1)}m ago"
                        diff < TimeUnit.DAYS.toMillis(1) -> "${diff / TimeUnit.HOURS.toMillis(1)}h ago"
                        else -> date
                    }
                } else date
            } catch (e: Exception) {
                date
            }
            views.setTextViewText(R.id.widget_time, timeAgo)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
