package com.example.aplicacion_tesis.model.dto

import com.google.gson.annotations.SerializedName

data class ProgresoResumenDTO(
    @SerializedName("status")                  val status: Boolean = false,
    @SerializedName("nivelPorcentaje")         val nivelPorcentaje: Int = 0,
    @SerializedName("ejerciciosDesarrollados") val ejerciciosDesarrollados: Int = 0,
    @SerializedName("leccionesVistas")         val leccionesVistas: Int = 0,
    @SerializedName("resumenTexto")            val resumenTexto: String = ""
)