package com.example.aplicacion_tesis.network

import com.example.aplicacion_tesis.model.dto.DominioTemasResponse
import com.example.aplicacion_tesis.model.dto.TemaDetalleResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface DominioService {

    // Lista de tarjetas del Dominio
    @GET("dominio/temas/{idEstudiante}")
    suspend fun listarTemasDominio(
        @Path("idEstudiante") idEstudiante: Int
    ): DominioTemasResponse

    // Detalle de un tema
    // COINCIDE CON: GET /dominio/tema/1
    @GET("dominio/tema/{idTema}")
    suspend fun obtenerDetalleTema(
        @Path("idTema") idTema: Int
    ): TemaDetalleResponse
}
