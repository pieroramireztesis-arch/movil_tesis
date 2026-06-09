package com.example.aplicacion_tesis.model.dto

import com.google.gson.annotations.SerializedName

/**
 * Respuesta del endpoint POST /tutor/responder
 */
data class TutorResponse(

    @SerializedName("correcta")
    val correcta: Boolean,

    @SerializedName("mostrarPista")
    val mostrarPista: Boolean,

    @SerializedName("mensaje")
    val mensaje: String? = null,

    @SerializedName("nuevoAjuste")
    val nuevoAjuste: String? = null,

    @SerializedName("idRespuesta")
    val idRespuesta: Int? = null,

    // Campos extra para análisis / dashboard (opcional en la app)
    @SerializedName("nivelMLCompetencia")
    val nivelMLCompetencia: String? = null,   // "bajo", "medio", "alto"

    @SerializedName("nivelCompetenciaInt")
    val nivelCompetenciaInt: Int? = null,     // 1..7 aprox

    @SerializedName("nivelGlobal")
    val nivelGlobal: String? = null          // "bajo", "medio", "alto"
)
