package com.example.aplicacion_tesis.model.dto

import com.google.gson.annotations.SerializedName

data class FrecuenciaUsoResponse(
    @SerializedName("status")
    val status: Boolean,

    @SerializedName("data")
    val data: List<FrecuenciaEstudianteDTO>? = null,

    @SerializedName("message")
    val message: String? = null
)

data class FrecuenciaEstudianteDTO(

    @SerializedName("id_estudiante")
    val idEstudiante: Int = 0,

    @SerializedName("nombre")
    val nombre: String = "",

    @SerializedName("totalInteracciones")
    val totalInteracciones: Int = 0,

    // ✅ Nombre exacto que usa TeacherHomeActivity
    @SerializedName("ejerciciosRespondidos")
    val ejerciciosRespondidos: Int = 0,

    @SerializedName("materialesVistos")
    val materialesVistos: Int = 0,

    @SerializedName("ultimaActividad")
    val ultimaActividad: String = ""
)