package com.example.aplicacion_tesis.model.dto

data class TemaDetalleData(
    val idTema: Int,
    val nombre: String,
    val descripcionTema: String,
    val area: String,
    val materiales: List<MaterialDidacticoDTO>
)
