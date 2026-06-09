// EstudianteService.kt
package com.example.aplicacion_tesis.network

import com.example.aplicacion_tesis.model.dto.EstudianteFromUserResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface EstudianteService {

    @GET("estudiantes/por-usuario/{idUsuario}")
    suspend fun getEstudiantePorUsuario(
        @Path("idUsuario") idUsuario: Int
    ): Response<EstudianteFromUserResponse>
}

