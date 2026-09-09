package com.widhura.signalxp.data.api

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    application: Application,
    private val authRepository: AuthRepository
) : AndroidViewModel(application) {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(authRepository.isLoggedIn())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        // Backend rejected our stored token (rotated / logged out elsewhere):
        // drop to the login screen instead of failing every call silently.
        viewModelScope.launch {
            AuthExpiredBus.expired.collect {
                _isLoggedIn.value = false
                _errorMessage.value = "Session expired. Please log in again."
            }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Please fill in all fields"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = authRepository.login(email, password)
            result.fold(
                onSuccess = {
                    _isLoggedIn.value = true
                    _successMessage.value = "Login successful"
                },
                onFailure = {
                    _errorMessage.value = it.message ?: "Login failed"
                }
            )
            _isLoading.value = false
        }
    }

    fun register(name: String, email: String, password: String, passwordConfirmation: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Please fill in all fields"
            return
        }
        if (password != passwordConfirmation) {
            _errorMessage.value = "Passwords do not match"
            return
        }
        if (password.length < 8) {
            _errorMessage.value = "Password must be at least 8 characters"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = authRepository.register(name, email, password, passwordConfirmation)
            result.fold(
                onSuccess = {
                    _isLoggedIn.value = true
                    _successMessage.value = "Registration successful"
                },
                onFailure = {
                    _errorMessage.value = it.message ?: "Registration failed"
                }
            )
            _isLoading.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _isLoggedIn.value = false
            wipeLocalHistory()
        }
    }

    /**
     * Removes every trace of the signed-in account from this device:
     * Room tables (signals, news, community, VIP), home-widget cache and
     * posted notifications. Auth prefs are already cleared by the repository.
     */
    private suspend fun wipeLocalHistory() {
        val app = getApplication<Application>()
        try {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                com.widhura.signalxp.data.AppDatabase.getDatabase(app).clearAllTables()
            }
        } catch (e: Exception) {
            android.util.Log.w("AuthViewModel", "DB wipe failed: ${e.message}")
        }
        try {
            com.widhura.signalxp.data.WidgetPreferences.clear(app)
            com.widhura.signalxp.util.SignalNotifications.cancelAll(app)
            com.widhura.signalxp.SignalWidgetProvider.triggerUpdate(app)
        } catch (e: Exception) {
            android.util.Log.w("AuthViewModel", "Prefs/notification wipe failed: ${e.message}")
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearSuccess() {
        _successMessage.value = null
    }
}
