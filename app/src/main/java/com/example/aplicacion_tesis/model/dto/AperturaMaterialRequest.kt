package com.example.aplicacion_tesis.model.dto

import com.google.gson.annotations.SerializedName

data class AperturaMaterialRequest(
    @SerializedName("idEstudiante") val idEstudiante: Int,
    @SerializedName("idMaterial")   val idMaterial:   Int
)