package com.example.aplicacion_tesis.network

import com.example.aplicacion_tesis.model.dto.ProgresoHistorialResponse
import com.example.aplicacion_tesis.model.dto.ProgresoPorCompetenciaDTO
import com.example.aplicacion_tesis.model.dto.ProgresoResumenDTO
import retrofit2.http.GET
import retrofit2.http.Query

interface ProgresoService {

    @GET("/progreso/resumen")
    suspend fun getResumen(
        @Query("idEstudiante") idEstudiante: Int
    ): ProgresoResumenDTO

    @GET("/progreso/por_competencia")
    suspend fun getPorCompetencia(
        @Query("idEstudiante") idEstudiante: Int
    ): ProgresoPorCompetenciaDTO

    @GET("/progreso/historial")
    suspend fun getHistorial(
        @Query("idEstudiante") idEstudiante: Int,
        @Query("limite")       limite: Int = 5,
        @Query("offset")       offset: Int = 0
    ): ProgresoHistorialResponse
}