package com.example.aplicacion_tesis.model.dto

enum class AlertSeverity {
    Critica,
    Advertencia,
    Info
}

data class TeacherAlertItem(
    val titulo: String,
    val descripcion: String,
    val fecha: String,
    val severidad: AlertSeverity
)
