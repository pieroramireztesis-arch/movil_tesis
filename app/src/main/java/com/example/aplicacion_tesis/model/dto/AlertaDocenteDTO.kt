package com.example.aplicacion_tesis.model.dto

import com.google.gson.annotations.SerializedName

data class AlertaDocenteResponse(
    @SerializedName("status") val status: Boolean,
    @SerializedName("data")   val data:   List<AlertaDocenteDTO>? = null
)

data class AlertaDocenteDTO(
    @SerializedName("id_estudiante")        val idEstudiante:         Int,
    @SerializedName("nombre")              val nombre:               String,
    @SerializedName("tipoAlerta")          val tipoAlerta:           String,
    @SerializedName("competenciasProblema") val competenciasProblema: List<CompetenciaProblemaDTO>? = null,
    @SerializedName("erroresRecientes")    val erroresRecientes:     Int = 0,
    @SerializedName("ultimaActividad")     val ultimaActividad:      String = "Sin actividad"
)

data class CompetenciaProblemaDTO(
    @SerializedName("idCompetencia") val idCompetencia: Int,
    @SerializedName("nombre")        val nombre:        String,
    @SerializedName("promedio")      val promedio:      Int,
    @SerializedName("nivel")         val nivel:         String
)