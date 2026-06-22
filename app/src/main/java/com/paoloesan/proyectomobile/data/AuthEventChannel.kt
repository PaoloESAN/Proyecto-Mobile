package com.paoloesan.proyectomobile.data

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed class AuthEvent {
    object NavigateToResetPassword : AuthEvent()
}

object AuthEventChannel {
    private val _events = MutableSharedFlow<AuthEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun sendEvent(event: AuthEvent) {
        _events.tryEmit(event)
    }
}
