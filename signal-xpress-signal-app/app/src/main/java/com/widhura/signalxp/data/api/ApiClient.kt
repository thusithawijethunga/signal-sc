package com.widhura.signalxp.data.api

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

object ApiClient {

    // Was private - exposed so raw OkHttp callers outside Retrofit/ApiService
    // (e.g. NotificationForegroundService's websocket token fetch) build
    // requests against the same host instead of hardcoding a divergent URL.
    const val BASE_URL = "https://backend.signalxpress.com/api/"
    private const val PREFS_NAME = "signal_xpress_auth"
    private const val KEY_TOKEN = "api_token"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_ROLE = "user_role"

    private var retrofit: Retrofit? = null
    private var apiService: ApiService? = null

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getToken(context: Context): String? {
        return getPrefs(context).getString(KEY_TOKEN, null)
    }

    fun saveAuth(context: Context, token: String, userId: Long, name: String, email: String, role: String) {
        getPrefs(context).edit().apply {
            putString(KEY_TOKEN, token)
            putLong(KEY_USER_ID, userId)
            putString(KEY_USER_NAME, name)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_ROLE, role)
            apply()
        }
    }

    fun clearAuth(context: Context) {
        getPrefs(context).edit().clear().apply()
    }

    fun isLoggedIn(context: Context): Boolean {
        return getToken(context) != null
    }

    fun getCurrentUserName(context: Context): String {
        return getPrefs(context).getString(KEY_USER_NAME, "Trader") ?: "Trader"
    }

    fun getCurrentUserEmail(context: Context): String {
        return getPrefs(context).getString(KEY_USER_EMAIL, "") ?: ""
    }

    fun getCurrentUserId(context: Context): Long {
        return getPrefs(context).getLong(KEY_USER_ID, 0)
    }

    fun getCurrentUserRole(context: Context): String {
        return getPrefs(context).getString(KEY_USER_ROLE, "viewer") ?: "viewer"
    }

    fun getApiService(context: Context): ApiService {
        if (apiService != null) return apiService!!

        val appContext = context.applicationContext

        val authInterceptor = Interceptor { chain ->
            val token = getToken(appContext)
            val request = chain.request().newBuilder()
                .header("Accept", "application/json")
                .header("User-Agent", "SignalXpress/1.0 (Android ${android.os.Build.VERSION.RELEASE}; ${android.os.Build.MODEL})")
                .header("X-Requested-With", "XMLHttpRequest")
            if (token != null) {
                request.header("Authorization", "Bearer $token")
            }
            chain.proceed(request.build())
        }

        // The backend rotates api_token on every login and nulls it on logout,
        // so a stored token can die at any time. On the first 401 for a call
        // that carried a token: drop the token and tell the app to log out
        // instead of retrying with a dead credential forever.
        // Skips auth/login + auth/register (a 401 there is just bad credentials).
        val authExpired = AtomicBoolean(false)
        val authenticator = Authenticator { _: Route?, response: Response ->
            val hadToken = response.request.header("Authorization") != null
            val path = response.request.url.encodedPath
            val isAuthCall = path.endsWith("auth/login") || path.endsWith("auth/register")
            if (hadToken && !isAuthCall && authExpired.compareAndSet(false, true)) {
                clearAuth(appContext)
                AuthExpiredBus.emit()
            }
            null // don't retry — the credential is dead
        }

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .authenticator(authenticator)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        // Lenient numbers: backend sends decimals as JSON strings ("4571.00")
        val moshi = Moshi.Builder()
            .add(LenientNumberFactory())
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        apiService = retrofit!!.create(ApiService::class.java)
        return apiService!!
    }
}
