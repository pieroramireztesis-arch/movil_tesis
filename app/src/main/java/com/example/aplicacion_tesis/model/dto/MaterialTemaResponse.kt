package com.example.aplicacion_tesis.model.dto

data class MaterialTemaResponse(
    val idMaterial: Int,
    val titulo: String,
    val tipo: String,           // "video" o "pdf"
    val url: String?,
    val tiempoEstimado: Int?    // por ejemplo minutos para el video
)
