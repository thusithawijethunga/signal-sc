package com.widhura.signalxp.data.api

import android.content.Context

class AuthRepository(private val context: Context) {

    private val api = ApiClient.getApiService(context)

    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val body = response.body()!!
                ApiClient.saveAuth(
                    context,
                    body.token,
                    body.user.id,
                    body.user.name,
                    body.user.email,
                    body.user.role
                )
                Result.success(body)
            } else {
                val error = parseError(response)
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Connection failed: ${e.localizedMessage ?: "Unknown error"}"))
        }
    }

    suspend fun register(name: String, email: String, password: String, passwordConfirmation: String): Result<AuthResponse> {
        return try {
            val response = api.register(
                RegisterRequest(name, email, password, passwordConfirmation)
            )
            if (response.isSuccessful) {
                val body = response.body()!!
                ApiClient.saveAuth(
                    context,
                    body.token,
                    body.user.id,
                    body.user.name,
                    body.user.email,
                    body.user.role
                )
                Result.success(body)
            } else {
                val error = parseError(response)
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Connection failed: ${e.localizedMessage ?: "Unknown error"}"))
        }
    }

    suspend fun logout(): Result<Unit> {
        return try {
            val response = api.logout()
            ApiClient.clearAuth(context)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            ApiClient.clearAuth(context)
            Result.success(Unit)
        }
    }

    suspend fun me(): Result<UserResponse> {
        return try {
            val response = api.me()
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch user"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isLoggedIn(): Boolean = ApiClient.isLoggedIn(context)

    fun clearAuth() = ApiClient.clearAuth(context)

    private fun parseError(response: retrofit2.Response<*>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            if (errorBody != null) {
                val json = org.json.JSONObject(errorBody)
                json.optString("message", "Request failed")
            } else {
                "Request failed (${response.code()})"
            }
        } catch (e: Exception) {
            "Request failed (${response.code()})"
        }
    }
}
