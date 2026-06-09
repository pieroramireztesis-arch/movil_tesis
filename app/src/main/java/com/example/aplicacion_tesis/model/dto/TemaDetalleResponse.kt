package com.example.aplicacion_tesis.model.dto

data class TemaDetalleResponse(
    val status: Boolean,
    val data: TemaDetalleDTO?
)

data class TemaDetalleDTO(
    val idTema: Int,
    val nombre: String?,
    val descripcionTema: String?,
    val area: String?,                          // 👈 FALTABA ESTE
    val materiales: List<MaterialDTO>
)

data class MaterialDTO(
    val idMaterial:     Int,
    val tipo:           String,
    val titulo:         String?,
    val url:            String?,
    val tiempoEstimado: Int?,
    val nivel:          Int? = null  // ✅ NUEVO
)
