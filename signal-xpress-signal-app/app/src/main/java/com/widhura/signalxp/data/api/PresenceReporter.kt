package com.widhura.signalxp.data.api

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Reports this install to the admin presence board — no permissions needed,
 * only basic Build info + app version:
 *
 * - heartbeat("online" | "background"): on login, on foreground/background
 *   switch, and every ~60s while the foreground service is alive.
 * - markOffline(): on logout (before the api_token is cleared) and on
 *   explicit service stop. Killed apps simply stop heartbeating and expire
 *   off the board via the server TTL.
 */
object PresenceReporter {

    private const val TAG = "Presence"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Stable per-install id. ANDROID_ID needs no permission. */
    @SuppressLint("HardwareIds")
    fun deviceKey(context: Context): String {
        return try {
            Settings.Secure.getString(
                context.applicationContext.contentResolver,
                Settings.Secure.ANDROID_ID
            ) ?: "unknown"
        } catch (e: Exception) {
            Log.w(TAG, "ANDROID_ID unavailable: ${e.message}")
            "unknown"
        }
    }

    fun collectDeviceInfo(context: Context): PresenceDeviceInfo {
        val appVersion = try {
            val pm = context.applicationContext.packageManager
            @Suppress("DEPRECATION")
            pm.getPackageInfo(context.applicationContext.packageName, 0).versionName ?: ""
        } catch (e: Exception) {
            ""
        }
        return PresenceDeviceInfo(
            brand = Build.MANUFACTURER ?: "",
            model = Build.MODEL ?: "",
            android = Build.VERSION.RELEASE ?: "",
            sdk = Build.VERSION.SDK_INT,
            appVersion = appVersion
        )
    }

    /** Fire-and-forget heartbeat (service loop / state switches). */
    fun heartbeat(context: Context, state: String) {
        val appContext = context.applicationContext
        if (!ApiClient.isLoggedIn(appContext)) return
        scope.launch {
            heartbeatSuspend(appContext, state)
        }
    }

    suspend fun heartbeatSuspend(context: Context, state: String) {
        val appContext = context.applicationContext
        if (!ApiClient.isLoggedIn(appContext)) return
        try {
            val res = ApiClient.getApiService(appContext).presenceHeartbeat(
                PresenceHeartbeatRequest(
                    deviceKey = deviceKey(appContext),
                    state = state,
                    device = collectDeviceInfo(appContext)
                )
            )
            if (!res.isSuccessful) {
                Log.w(TAG, "heartbeat failed: ${res.code()}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "heartbeat exception: ${e.message}")
        }
    }

    /** Explicit goodbye — call BEFORE the auth token is cleared. */
    suspend fun markOffline(context: Context) {
        val appContext = context.applicationContext
        try {
            ApiClient.getApiService(appContext).presenceOffline(
                PresenceOfflineRequest(deviceKey = deviceKey(appContext))
            )
        } catch (e: Exception) {
            Log.w(TAG, "offline exception: ${e.message}")
        }
    }

    fun currentState(isForeground: Boolean): String =
        if (isForeground) "online" else "background"
}
