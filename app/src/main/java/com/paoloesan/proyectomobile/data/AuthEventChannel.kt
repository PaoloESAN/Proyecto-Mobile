package com.paoloesan.proyectomobile.data

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed class AuthEvent {
    object NavigateToResetPassword : AuthEvent()
    data class NavigateToChat(val transactionId: Int) : AuthEvent()
    data class NavigateToTransactionStatus(val transactionId: Int) : AuthEvent()
}

object AuthEventChannel {
    private val _events = MutableSharedFlow<AuthEvent>(replay = 1, extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    var hasPendingEvent: Boolean = false
        private set

    fun sendEvent(event: AuthEvent) {
        hasPendingEvent = true
        _events.tryEmit(event)
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun clearEvents() {
        hasPendingEvent = false
        _events.resetReplayCache()
    }
}
