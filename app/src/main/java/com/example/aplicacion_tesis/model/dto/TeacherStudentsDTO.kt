package com.example.aplicacion_tesis.model.dto

import com.google.gson.annotations.SerializedName

data class TeacherStudentsResponse(
    val status: Boolean,
    val message: String? = null,
    val data: List<TeacherStudentItem>? = null
)

data class TeacherStudentItem(
    @SerializedName("id_estudiante")
    val idEstudiante: Int,

    val nombre: String,

    // progreso_general (0–100)
    val progreso: Int
)
