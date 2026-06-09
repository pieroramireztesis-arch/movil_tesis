package com.example.aplicacion_tesis.model.dto

import com.google.gson.annotations.SerializedName

// ---------- LOGIN ----------
data class LoginRequest(
    @SerializedName("correo")
    val correo: String,

    @SerializedName("contrasena")
    val password: String
)

// ---------- REGISTER ----------
data class RegisterRequest(
    val nombre: String,
    val apellidos: String,
    val correo: String,
    val contrasena: String,
    val rol: String = "estudiante"
)

// ---------- RESPUESTA COMÚN ----------
data class AuthResponse(
    val status: Boolean,
    val message: String? = null,
    @SerializedName("data") val user: UserInfo? = null,
    val id_estudiante: Int? = null,
    val id_docente: Int? = null,
    val token: String? = null
)

// Datos del usuario que vienen dentro de "data"
data class UserInfo(
    @SerializedName("id_usuario") val id_usuario: Int,
    val nombre: String,
    val apellidos: String,
    val correo: String,
    val rol: String,
    @SerializedName("id_docente") val id_docente: Int? = null,
    @SerializedName("id_estudiante") val id_estudiante: Int? = null
)

// ---------- PERFIL ----------
data class UsuarioDTO(
    val id_usuario: Int,
    val nombre: String,
    val apellidos: String,
    val correo: String,
    val estado_usuario: String? = null,
    val rol: String? = null
)

data class CambiarPasswordRequest(
    val id_usuario: Int,
    val nueva_password: String
)

data class RecoverRequest(val correo: String)

data class ApiDataResponse<T>(
    val status: Boolean,
    val message: String? = null,
    val data: T? = null
)
