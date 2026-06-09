package com.example.aplicacion_tesis.model.dto

data class TutorAnswerRequest(
    val idEstudiante:        Int,
    val idEjercicio:         Int,
    val idOpcionSeleccionada: Int,
    val tiempoRespuesta:     Int,
    val usoPista:            Boolean = false,
    val ajuste:              String? = null,
    // ✅ NUEVO — "repaso" o "evaluacion"
    val modo:                String  = "repaso",
    // ✅ NUEVO — solo para evaluación
    val idEvaluacion:        Int?    = null
)