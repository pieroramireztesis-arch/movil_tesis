package com.example.aplicacion_tesis.network

import com.example.aplicacion_tesis.model.dto.AlertaDocenteResponse
import com.example.aplicacion_tesis.model.dto.FrecuenciaUsoResponse
import com.example.aplicacion_tesis.model.dto.MaterialStatsResponse
import com.example.aplicacion_tesis.model.dto.TeacherStudentsResponse
import com.example.aplicacion_tesis.model.dto.TeacherDashboardResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface DocenteService {

    // ============================================
    // 1) LISTAR ESTUDIANTES DEL DOCENTE
    // GET /docentes/{idDocente}/estudiantes
    // ============================================
    @GET("docentes/{idDocente}/estudiantes")
    suspend fun getStudentsByTeacher(
        @Path("idDocente") idDocente: Int
    ): TeacherStudentsResponse

    // ============================================
    // 2) DASHBOARD DEL PROFESOR
    // GET /docentes/{idDocente}/dashboard
    // ============================================
    @GET("docentes/{idDocente}/dashboard")
    suspend fun getDashboard(
        @Path("idDocente") idDocente: Int
    ): TeacherDashboardResponse

    // ============================================
    // 4) FRECUENCIA DE USO
    // GET /dashboard/docente/{idDocente}/frecuencia
    // ============================================
    @GET("dashboard/docente/{idDocente}/frecuencia")
    suspend fun getFrecuenciaUso(
        @Path("idDocente") idDocente: Int
    ): FrecuenciaUsoResponse

    @GET("docentes/{idDocente}/alertas")
    suspend fun getAlertas(
        @Path("idDocente") idDocente: Int
    ): AlertaDocenteResponse

    // ============================================
    // 5) ESTADÍSTICAS DE MATERIALES POR ESTUDIANTE
    // GET /dashboard/docente/{idDocente}/materiales-stats?id_estudiante=<id>
    // ============================================
    @GET("dashboard/docente/{idDocente}/materiales-stats")
    suspend fun getMaterialesStats(
        @Path("idDocente")    idDocente:    Int,
        @Query("id_estudiante") idEstudiante: Int
    ): MaterialStatsResponse

}