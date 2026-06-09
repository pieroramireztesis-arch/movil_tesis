// app/src/main/java/com/example/aplicacion_tesis/model/dto/HistorialMaterialRequest.kt
package com.example.aplicacion_tesis.model.dto

import com.google.gson.annotations.SerializedName

data class HistorialMaterialRequest(
    @SerializedName("id_estudiante")
    val idEstudiante: Int,

    @SerializedName("id_material")
    val idMaterial: Int,

    @SerializedName("estado")
    val estado: String,          // ej: "completado" o "visto"

    @SerializedName("tiempo_visto")
    val tiempoVisto: Int,        // en segundos o 0 si no lo mides

    @SerializedName("veces_revisado")
    val vecesRevisado: Int
)
