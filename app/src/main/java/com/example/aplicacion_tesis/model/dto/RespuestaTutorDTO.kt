package com.example.aplicacion_tesis.network

import com.example.aplicacion_tesis.model.dto.MaterialSugeridoDTO
import com.example.aplicacion_tesis.model.dto.RecursosAdicionalesDTO
import com.google.gson.annotations.SerializedName

data class RespuestaTutorDTO(
    @SerializedName("correcta")            val correcta:            Boolean,
    @SerializedName("mostrarPista")        val mostrarPista:        Boolean                = false,
    @SerializedName("mensaje")             val mensaje:             String?                = null,
    @SerializedName("nuevoAjuste")         val nuevoAjuste:         String?                = null,
    @SerializedName("idRespuesta")         val idRespuesta:         Int?                   = null,
    @SerializedName("modo")                val modo:                String?                = null,
    @SerializedName("nivelMLCompetencia")  val nivelMLCompetencia:  String?                = null,
    @SerializedName("nivelCompetenciaInt") val nivelCompetenciaInt: Int?                   = null,
    @SerializedName("nivelGlobal")         val nivelGlobal:         String?                = null,
    @SerializedName("materialSugerido")    val materialSugerido:    MaterialSugeridoDTO?    = null,
    @SerializedName("recursosAdicionales") val recursosAdicionales: RecursosAdicionalesDTO? = null
)