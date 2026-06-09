package com.example.aplicacion_tesis.network

import com.example.aplicacion_tesis.model.dto.RegisterRequest
import com.example.aplicacion_tesis.model.dto.LoginRequest
import com.example.aplicacion_tesis.model.dto.AuthResponse   // 👈 AGREGA ESTO

import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {

    //  LOGIN contra ws_auth.login (POST /auth/login)
    @POST("auth/login")
    suspend fun login(
        @Body body: LoginRequest
    ): AuthResponse

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @POST("auth/recuperar")
    suspend fun recuperar(@Body body: Map<String, String>): AuthResponse
}
