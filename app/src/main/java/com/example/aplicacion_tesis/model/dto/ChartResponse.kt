package com.example.aplicacion_tesis.model.dto

import com.google.gson.annotations.SerializedName

data class ChartResponse(
    val status: Boolean,
    @SerializedName("datos_chart")
    val datosChart: List<ChartPoint>? = null
)

data class ChartPoint(
    val fecha: String,
    val puntaje: Int
)