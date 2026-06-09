package com.example.aplicacion_tesis.network

import com.example.aplicacion_tesis.model.dto.AperturaMaterialRequest
import com.example.aplicacion_tesis.model.dto.SimpleUploadResponse
import com.example.aplicacion_tesis.model.dto.TutorAnswerRequest
import com.example.aplicacion_tesis.model.dto.TutorExerciseDTO
import com.example.aplicacion_tesis.model.dto.TutorResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface TutorService {

    // GET /tutor/ejercicio_siguiente
    @GET("tutor/ejercicio_siguiente")
    suspend fun getNextExercise(
        @Query("idEstudiante") idEstudiante: Int,
        @Query("idDominio")    idDominio:    Int?    = null,
        @Query("ajuste")       ajuste:       String? = null,
        @Query("modo")         modo:         String  = "repaso"  // ← faltaba este parámetro
    ): TutorExerciseDTO

    // POST /tutor/responder
    @POST("tutor/responder")
    suspend fun sendAnswer(
        @Body body: TutorAnswerRequest
    ): TutorResponse

    // POST /tutor/subir_desarrollo
    @Multipart
    @POST("tutor/subir_desarrollo")
    suspend fun uploadDevelopment(
        @Part("idRespuesta") idRespuesta: RequestBody,
        @Part archivo: MultipartBody.Part
    ): SimpleUploadResponse

}