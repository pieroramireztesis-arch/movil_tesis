package com.example.aplicacion_tesis.model.dto

import com.google.gson.annotations.SerializedName


data class EstudianteFromUserData(
    @SerializedName("id_estudiante")
    val idEstudiante: Int,

    @SerializedName("grado")
    val grado: String,

    @SerializedName("estado_estudiante")
    val estadoEstudiante: String,

    @SerializedName("id_usuario")
    val idUsuario: Int
)

data class EstudianteFromUserResponse(
    val status: Boolean,
    val data: EstudianteFromUserData?
)
