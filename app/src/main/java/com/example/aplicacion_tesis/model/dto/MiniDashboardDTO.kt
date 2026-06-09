package com.example.aplicacion_tesis.model.dto

import com.google.gson.annotations.SerializedName

data class MiniDashboardDTO(

    @SerializedName("status")
    val status: Boolean,

    @SerializedName("nombreEstudiante")
    val nombreEstudiante: String? = null,

    @SerializedName("saludo")
    val saludo: String? = null,

    // Si tu API devuelve temas o porcentajes:
    @SerializedName("temas")
    val temas: List<Any>? = null
)
