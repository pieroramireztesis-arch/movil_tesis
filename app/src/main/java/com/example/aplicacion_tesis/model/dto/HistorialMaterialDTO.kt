package com.example.aplicacion_tesis.model.dto

import com.google.gson.annotations.SerializedName

data class HistorialMaterialResponse(
    @SerializedName("status") val status: Boolean,
    @SerializedName("data")   val data:   HistorialMaterialData? = null
)

data class HistorialMaterialData(
    @SerializedName("totalMateriales")      val totalMateriales:      Int = 0,
    @SerializedName("totalTiempoVisto")     val totalTiempoVisto:     Int = 0,
    @SerializedName("materialesCompletados") val materialesCompletados: Int = 0,
    @SerializedName("detalle")              val detalle:              List<MaterialDetalleDTO> = emptyList()
)

data class MaterialDetalleDTO(
    @SerializedName("idMaterial")    val idMaterial:    Int,
    @SerializedName("titulo")        val titulo:        String,
    @SerializedName("tipo")          val tipo:          String,
    @SerializedName("tiempoVisto")   val tiempoVisto:   Int = 0,
    @SerializedName("tiempoEstimado") val tiempoEstimado: Int = 0,
    @SerializedName("vecesRevisado") val vecesRevisado: Int = 0,
    @SerializedName("fechaAcceso")   val fechaAcceso:   String? = null
)