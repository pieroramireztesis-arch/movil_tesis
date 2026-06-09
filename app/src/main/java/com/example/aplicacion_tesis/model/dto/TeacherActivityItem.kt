package com.example.aplicacion_tesis.model.dto

/**
 * Item para el historial de actividad del estudiante
 * (para la tarjeta "Historial de Actividad" del profesor).
 */
data class TeacherActivityItem(
    val titulo: String,      // Ej: "Completó el tema 'Ecuaciones lineales'"
    val detalle: String,     // Ej: "Hace 15 minutos"
    val tipo: String         // Ej: "progreso", "pista", "practica"
)
