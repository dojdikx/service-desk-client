package com.servicedesk.app.data.staff

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@Singleton
class StaffDirectoryRefreshBus @Inject constructor() {
    private val _updates = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val updates: SharedFlow<Unit> = _updates.asSharedFlow()

    fun notifyUpdated() {
        _updates.tryEmit(Unit)
    }
}
