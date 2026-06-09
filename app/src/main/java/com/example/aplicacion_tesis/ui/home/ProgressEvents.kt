package com.example.aplicacion_tesis.ui.home

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

object ProgressEvents {

    // Flujo que escucha ProgresoFragment
    private val _progressChanged = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val progressChanged = _progressChanged.asSharedFlow()

    // Notificar que cambió el progreso (se resolvió un ejercicio)
    fun notifyChanged() {
        scope.launch {
            _progressChanged.emit(Unit)
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
}
