package com.example.aplicacion_tesis.network

import com.example.aplicacion_tesis.model.dto.TeacherDashboardResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface TeacherService {

    // Llama a: GET /dashboard/docente/{idDocente}
    @GET("dashboard/docente/{idDocente}")
    suspend fun getDashboard(
        @Path("idDocente") idDocente: Int
    ): TeacherDashboardResponse
}
