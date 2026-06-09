package com.example.aplicacion_tesis.model.dto

import com.google.gson.annotations.SerializedName

// ── Respuesta de /tutor/evaluacion/activa ──────────────────────────
data class EvaluacionActivaResponse(
    val status:         Boolean,
    val hayEvaluacion:  Boolean = false,
    val yaCompleto:     Boolean = false,
    val evaluacion:     EvaluacionDTO? = null
)

data class EvaluacionDTO(
    @SerializedName("idEvaluacion") val idEvaluacion:  Int,
    @SerializedName("titulo")       val titulo:        String,
    @SerializedName("descripcion")  val descripcion:   String? = null,
    @SerializedName("fechaInicio")  val fechaInicio:   String? = null,
    @SerializedName("fechaFin")     val fechaFin:      String? = null
)

// ── Respuesta de /tutor/evaluacion/finalizar ───────────────────────
data class FinalizarEvaluacionResponse(
    val status:          Boolean,
    val puntajeTotal:    Int    = 0,
    val totalCorrectas:  Int    = 0,
    val totalPreguntas:  Int    = 0,
    val error:           String? = null
)