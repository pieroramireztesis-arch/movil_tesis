package com.example.aplicacion_tesis.network

import retrofit2.http.GET

// DTO de cada dominio según el JSON real del backend
data class DominioDto(
    val id: Int,
    val nombre: String,
    val nivel: Int
)

// Respuesta completa del endpoint GET /dominio/lista
data class DominioListResponse(
    val dominios: List<DominioDto>,
    val status: Boolean
)

interface DominioTutorService {

    // Coincide exactamente con GET http://.../dominio/lista
    @GET("dominio/lista")
    suspend fun dominios(): DominioListResponse
}
