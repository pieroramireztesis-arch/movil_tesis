package com.example.aplicacion_tesis.model.dto

import com.google.gson.annotations.SerializedName

data class ProgresoPorCompetenciaItemDTO(
    @SerializedName("idCompetencia")   val idCompetencia:   Int,
    @SerializedName("nombre")          val nombre:          String,
    @SerializedName("porcentaje")      val porcentaje:      Int,
    @SerializedName("nivelActual")     val nivelActual:     Int    = 0,
    @SerializedName("nombreNivel")     val nombreNivel:     String = "Sin datos",
    @SerializedName("promedioPuntaje") val promedioPuntaje: Int    = 0
)