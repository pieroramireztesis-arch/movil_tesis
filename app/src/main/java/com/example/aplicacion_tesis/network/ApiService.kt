package com.example.aplicacion_tesis.network

import com.example.aplicacion_tesis.model.dto.*
import retrofit2.http.*

interface ApiService {

    // ---------- TUTOR: RESPONDER EJERCICIO ----------
    @POST("tutor/responder")
    suspend fun responderTutor(
        @Body body: TutorAnswerRequest
    ): TutorResponse

    // ---------- TUTOR: OBTENER SIGUIENTE EJERCICIO ----------
    @GET("tutor/ejercicio_siguiente")
    suspend fun getNextExercise(
        @Query("idEstudiante") idEstudiante: Int,
        @Query("idDominio") idDominio: Int?,
        @Query("ajuste") ajuste: String?
    ): TutorExerciseDTO

    // ---------- DASHBOARD MINI ----------
    @GET("dashboard/mini/{id}")
    suspend fun getMiniDashboard(
        @Path("id") idEstudiante: Int
    ): MiniDashboardDTO

    // ---------- REGISTER ----------
    @POST("auth/register")
    suspend fun register(
        @Body body: RegisterRequest
    ): AuthResponse

    // ---------- PERFIL ----------
    @GET("usuarios/{id}")
    suspend fun getUsuario(
        @Path("id") idUsuario: Int
    ): ApiDataResponse<UsuarioDTO>

    @PUT("usuarios/{id}/perfil")
    suspend fun updatePerfil(
        @Path("id") idUsuario: Int,
        @Body body: UpdateProfileRequest
    ): SimpleResponse
}
