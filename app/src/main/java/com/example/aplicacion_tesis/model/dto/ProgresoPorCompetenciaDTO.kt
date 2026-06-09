package com.example.aplicacion_tesis.model.dto

import com.google.gson.annotations.SerializedName

data class ProgresoPorCompetenciaDTO(
    @SerializedName("status")
    val status: Boolean,

    // ✅ Nullable con default para evitar crash si la API no devuelve temas
    @SerializedName("temas")
    val temas: List<ProgresoPorCompetenciaItemDTO>? = null
)