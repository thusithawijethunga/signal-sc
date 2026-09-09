package com.widhura.signalxp.data.api

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Registers this device's FCM token with the backend so signal broadcasts
 * can be pushed even when the app is closed or killed.
 *
 * All calls are best-effort and never throw: if Firebase isn't configured yet
 * (no google-services.json) they just log and return.
 */
object FcmTokenRegistrar {

    private const val TAG = "FcmToken"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Fetch the current FCM token and upload it (call on login / app start). */
    fun refresh(context: Context) {
        val appContext = context.applicationContext
        if (!ApiClient.isLoggedIn(appContext)) return
        try {
            FirebaseMessaging.getInstance().token
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result?.takeIf { it.isNotBlank() }?.let { upload(appContext, it) }
                            ?: Log.w(TAG, "FCM token was blank")
                    } else {
                        Log.w(TAG, "FCM token fetch failed: ${task.exception?.message}")
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "FCM unavailable (google-services.json not configured?): ${e.message}")
        }
    }

    /** Upload a known token (call from onNewToken). */
    fun upload(context: Context, token: String) {
        val appContext = context.applicationContext
        if (!ApiClient.isLoggedIn(appContext)) {
            Log.d(TAG, "Not logged in, skipping token upload")
            return
        }
        scope.launch {
            try {
                val res = ApiClient.getApiService(appContext)
                    .registerDevice(DeviceRegisterRequest(token = token))
                if (res.isSuccessful) {
                    Log.d(TAG, "FCM token registered")
                } else {
                    Log.w(TAG, "FCM token register failed: ${res.code()}")
                }
            } catch (e: Exception) {
                Log.w(TAG, "FCM token register exception: ${e.message}")
            }
        }
    }

    /** Remove the current token from the backend (call on logout, best-effort). */
    fun unregister(context: Context) {
        val appContext = context.applicationContext
        try {
            FirebaseMessaging.getInstance().token
                .addOnCompleteListener { task ->
                    val token = if (task.isSuccessful) task.result else null
                    if (token.isNullOrBlank()) return@addOnCompleteListener
                    scope.launch {
                        try {
                            ApiClient.getApiService(appContext)
                                .unregisterDevice(DeviceRegisterRequest(token = token))
                            Log.d(TAG, "FCM token unregistered")
                        } catch (e: Exception) {
                            Log.w(TAG, "FCM token unregister exception: ${e.message}")
                        }
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "FCM unavailable during unregister: ${e.message}")
        }
    }
}
