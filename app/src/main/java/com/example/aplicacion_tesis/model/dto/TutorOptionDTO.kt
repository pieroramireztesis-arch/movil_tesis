package com.example.aplicacion_tesis.model.dto

import com.google.gson.annotations.SerializedName

data class TutorOptionDTO(

    @SerializedName("idOpcion")
    val idOpcion: Int,

    @SerializedName("letra")
    val letra: String,

    @SerializedName("texto")
    val texto: String
)
