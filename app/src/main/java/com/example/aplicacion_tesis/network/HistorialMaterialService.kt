package com.example.aplicacion_tesis.network

import com.example.aplicacion_tesis.model.dto.HistorialMaterialRequest
import com.example.aplicacion_tesis.model.dto.HistorialMaterialResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface HistorialMaterialService {

    @POST("historial")
    suspend fun registrarHistorial(
        @Body body: HistorialMaterialRequest
    ): retrofit2.Response<Unit>

    @GET("historial/materiales")
    suspend fun getHistorialMateriales(
        @Query("idEstudiante") idEstudiante: Int
    ): HistorialMaterialResponse
}