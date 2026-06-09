package com.example.aplicacion_tesis.model.dto

import com.google.gson.annotations.SerializedName

data class MaterialStatResumenDTO(
    @SerializedName("totalRevisiones")     val totalRevisiones:     Int    = 0,
    @SerializedName("tiempoTotalSeg")      val tiempoTotalSeg:      Int    = 0,
    @SerializedName("tiempoTotalMin")      val tiempoTotalMin:      Float  = 0f,
    @SerializedName("materialesDistintos") val materialesDistintos: Int    = 0,
)

data class MaterialStatItemDTO(
    @SerializedName("titulo")       val titulo:       String = "",
    @SerializedName("tipo")         val tipo:         String = "link",
    @SerializedName("vecesRevisado") val vecesRevisado: Int   = 0,
    @SerializedName("tiempoVisto")  val tiempoVisto:  Int    = 0,
    @SerializedName("tiempoMin")    val tiempoMin:    Float  = 0f,
)

data class MaterialStatsResponse(
    @SerializedName("status")  val status:  Boolean                   = false,
    @SerializedName("resumen") val resumen: MaterialStatResumenDTO?   = null,
    @SerializedName("detalle") val detalle: List<MaterialStatItemDTO>? = null,
    @SerializedName("message") val message: String?                   = null,
)
