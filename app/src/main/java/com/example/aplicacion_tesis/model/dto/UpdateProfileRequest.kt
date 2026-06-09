// com/example/aplicacion_tesis/model/dto/UpdateProfileRequest.kt
package com.example.aplicacion_tesis.model.dto

data class UpdateProfileRequest(
    val nombre: String,
    val apellidos: String,
    val correo: String,
    val nueva_password: String? = null,
    val nueva_contrasena: String? = null
)
