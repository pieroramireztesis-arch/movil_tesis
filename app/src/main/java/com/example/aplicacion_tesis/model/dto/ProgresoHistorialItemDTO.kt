package com.example.aplicacion_tesis.model.dto

import com.google.gson.annotations.SerializedName

data class ProgresoHistorialItemDTO(
    @SerializedName("idProgreso")          val idProgreso:          Int     = 0,
    @SerializedName("idEjercicio")         val idEjercicio:         Int     = 0,
    @SerializedName("titulo")              val titulo:              String? = null,
    @SerializedName("subtitulo")           val subtitulo:           String? = null,
    @SerializedName("fecha")               val fecha:               String? = null,
    @SerializedName("estado")              val estado:              String? = null,
    @SerializedName("modo")                val modo:                String? = null,
    @SerializedName("intentosIncorrectos") val intentosIncorrectos: Int     = 0,
    @SerializedName("desarrolloUrl")       val desarrolloUrl:       String? = null,
    @SerializedName("idCompetencia")       val idCompetencia:       Int     = 0  // ✅ NUEVO
)

data class ProgresoHistorialResponse(
    @SerializedName("status") val status: Boolean = false,
    @SerializedName("items")  val items:  List<ProgresoHistorialItemDTO> = emptyList(),
    @SerializedName("total")  val total:  Int     = 0,
    @SerializedName("hayMas") val hayMas: Boolean = false,
    @SerializedName("offset") val offset: Int     = 0,
    @SerializedName("limite") val limite: Int     = 5
)