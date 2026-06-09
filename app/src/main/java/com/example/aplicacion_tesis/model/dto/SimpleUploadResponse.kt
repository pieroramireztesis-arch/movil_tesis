package com.example.aplicacion_tesis.model.dto

import com.google.gson.annotations.SerializedName

data class SimpleUploadResponse(

    @SerializedName("status")
    val status: Boolean,

    @SerializedName("message")
    val message: String? = null,

    // si quieres recuperar la URL que devuelve el backend
    @SerializedName("desarrolloUrl")
    val desarrolloUrl: String? = null
)
