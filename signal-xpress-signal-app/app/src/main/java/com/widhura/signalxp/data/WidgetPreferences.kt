package com.widhura.signalxp.data

import android.content.Context
import android.content.SharedPreferences

object WidgetPreferences {
    private const val PREFS_NAME = "signal_widget_data"
    private const val KEY_PAIR = "pair"
    private const val KEY_DIRECTION = "direction"
    private const val KEY_ENTRY = "entry"
    private const val KEY_TP1 = "tp1"
    private const val KEY_TP2 = "tp2"
    private const val KEY_SL = "sl"
    private const val KEY_PIPS = "pips"
    private const val KEY_RESULT = "result"
    private const val KEY_HIT_LEVEL = "hit_level"
    private const val KEY_SIGNAL_NO = "signal_no"
    private const val KEY_DATE = "date"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun updateFromSignal(context: Context, signal: SignalEntity) {
        prefs(context).edit().apply {
            putString(KEY_PAIR, signal.pair)
            putString(KEY_DIRECTION, signal.type)
            putString(KEY_ENTRY, signal.entry)
            putString(KEY_TP1, signal.tp1)
            putString(KEY_TP2, signal.tp2)
            putString(KEY_SL, signal.sl)
            putInt(KEY_PIPS, signal.pips)
            putString(KEY_RESULT, signal.result)
            putString(KEY_HIT_LEVEL, signal.hitLevel)
            putInt(KEY_SIGNAL_NO, signal.no)
            putString(KEY_DATE, signal.date)
            apply()
        }
    }

    fun getPair(context: Context) = prefs(context).getString(KEY_PAIR, "") ?: ""
    fun getDirection(context: Context) = prefs(context).getString(KEY_DIRECTION, "") ?: ""
    fun getEntry(context: Context) = prefs(context).getString(KEY_ENTRY, "") ?: ""
    fun getTp1(context: Context) = prefs(context).getString(KEY_TP1, "") ?: ""
    fun getTp2(context: Context) = prefs(context).getString(KEY_TP2, "") ?: ""
    fun getSl(context: Context) = prefs(context).getString(KEY_SL, "") ?: ""
    fun getPips(context: Context) = prefs(context).getInt(KEY_PIPS, 0)
    fun getResult(context: Context) = prefs(context).getString(KEY_RESULT, "") ?: ""
    fun getHitLevel(context: Context) = prefs(context).getString(KEY_HIT_LEVEL, "") ?: ""
    fun getSignalNo(context: Context) = prefs(context).getInt(KEY_SIGNAL_NO, 0)
    fun getDate(context: Context) = prefs(context).getString(KEY_DATE, "") ?: ""
}
