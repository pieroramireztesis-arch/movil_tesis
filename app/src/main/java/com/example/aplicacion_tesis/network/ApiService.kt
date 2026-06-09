package com.example.aplicacion_tesis.network

import com.example.aplicacion_tesis.model.dto.*
import retrofit2.http.*

interface ApiService {

    // ---------- DASHBOARD MINI ----------
    // Usado en: InicioFragment → solo para el saludo (nombre del estudiante).
    // El progreso y competencias vienen de ProgresoApiService.
    @GET("dashboard/mini/{id}")
    suspend fun getMiniDashboard(
        @Path("id") idEstudiante: Int
    ): MiniDashboardDTO

    // ---------- PERFIL ----------
    // Usado en: ProfileFragment, TeacherProfileActivity
    @GET("usuarios/{id}")
    suspend fun getUsuario(
        @Path("id") idUsuario: Int
    ): ApiDataResponse<UsuarioDTO>

    @PUT("usuarios/{id}/perfil")
    suspend fun updatePerfil(
        @Path("id") idUsuario: Int,
        @Body body: UpdateProfileRequest
    ): SimpleResponse

    // ❌ MÉTODOS ELIMINADOS (código muerto):
    //   responderTutor()   → usar TutorApiService.sendAnswer()    (tipo de retorno correcto: RespuestaTutorDTO)
    //   getNextExercise()  → usar TutorApiService.getNextExercise() (tiene parámetros modo + idEvaluacion)
    //   register()         → usar AuthService.register()
}
