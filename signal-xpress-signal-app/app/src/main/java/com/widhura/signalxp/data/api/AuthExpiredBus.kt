package com.widhura.signalxp.data.api

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Process-wide signal fired when the backend rejects our stored api_token
 * with 401 (rotated by another login, logged out elsewhere, or wiped).
 * Collectors (AuthViewModel, foreground service) must stop using the token.
 */
object AuthExpiredBus {
    private val _expired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val expired: SharedFlow<Unit> = _expired.asSharedFlow()

    fun emit() {
        _expired.tryEmit(Unit)
    }
}
