package com.example.aplicacion_tesis.model.dto

import com.google.gson.annotations.SerializedName

data class DominioTemaDTO(
    @SerializedName("idTema")
    val idTema: Int,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("nivel")
    val nivel: String?,

    @SerializedName("totalMateriales")
    val totalMateriales: Int,

    @SerializedName("materialesVistos")
    val materialesVistos: Int,

    // NUEVOS
    @SerializedName("materialesBasico")
    val materialesBasico: Int,

    @SerializedName("materialesIntermedio")
    val materialesIntermedio: Int,

    @SerializedName("materialesAvanzado")
    val materialesAvanzado: Int,
)
