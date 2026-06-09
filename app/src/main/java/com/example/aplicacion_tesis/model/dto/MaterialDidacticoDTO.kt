package com.example.aplicacion_tesis.model.dto

import com.google.gson.annotations.SerializedName

data class MaterialDidacticoDTO(
    @SerializedName("idMaterial")
    val idMaterial: Int,

    val titulo: String,

    val tipo: String,           // "video", "pdf", etc.

    val url: String?,

    @SerializedName("tiempoEstimado")
    val tiempoEstimado: Int?    // puede venir null en el JSON
)
