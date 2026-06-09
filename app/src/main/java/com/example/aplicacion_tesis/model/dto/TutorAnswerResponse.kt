package com.example.aplicacion_tesis.model.dto

data class TutorAnswerResponse(
    val correcta: Boolean,
    val mostrarPista: Boolean,
    val mensaje: String?,
    val nuevoAjuste: String?,
    // ID de la respuesta en BD para poder subir el desarrollo
    val idRespuesta: Int?
)
