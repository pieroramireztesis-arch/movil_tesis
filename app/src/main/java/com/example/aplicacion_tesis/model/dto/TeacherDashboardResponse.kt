package com.example.aplicacion_tesis.model.dto

import com.google.gson.annotations.SerializedName

data class TeacherDashboardResponse(
    val status: Boolean,
    val message: String? = null,
    val data: TeacherDashboardData? = null
)

data class TeacherDashboardData(

    @SerializedName("estudiantesActivos")
    val estudiantesActivos: Int,

    @SerializedName("progresoPromedio")
    val progresoPromedio: Double,

    @SerializedName("temaMasDificultad")
    val temaMasDificultad: String?,

    // La hago nullable para poder usar `?: emptyList()`
    @SerializedName("actividadReciente")
    val actividadReciente: List<ActividadDocente>?
)

data class ActividadDocente(

    @SerializedName("tipo")
    val tipo: String,

    @SerializedName("nombreEstudiante")
    val nombreEstudiante: String,

    @SerializedName("tema")
    val tema: String,

    @SerializedName("fecha")
    val fecha: String?
)
