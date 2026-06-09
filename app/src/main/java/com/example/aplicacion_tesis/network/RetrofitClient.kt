package com.example.aplicacion_tesis.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // ======== 🔥 API LOCAL (emulador) ========
    // 10.0.2.2 = localhost del PC desde el emulador Android
    const val BASE_URL = "http://10.0.2.2:3008/"

    /*
    // ======== API EN LA NUBE (Railway) ========
    // Descomenta esto y comenta la sección LOCAL antes de generar el APK final:
    const val BASE_URL = "https://api-tesis-af92.onrender.com/"
    */

    // ⚠️ WEB_BASE_URL eliminado: nunca fue utilizado por ningún fragmento.
    //    Si en el futuro se necesita (ej: abrir el portal web desde la app),
    //    añadirlo como: "https://tesis-algebra.onrender.com/"

    // ⚠️  Level.BODY registra contraseñas y tokens JWT en Logcat.
    //     En producción usa Level.NONE. Para depurar localmente puedes cambiar a Level.BASIC.
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.NONE
    }

    private val okHttp = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val original = chain.request()
            val builder = original.newBuilder()
                .header("Accept", "application/json")

            TokenStore.token?.let {
                if (it.isNotBlank())
                    builder.header("Authorization", "Bearer $it")
            }

            chain.proceed(builder.build())
        }
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttp)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // ============================================
    // REGISTRA TODOS LOS SERVICIOS DEL SISTEMA
    // ============================================

    val api: ApiService = retrofit.create(ApiService::class.java)
    val authApi: AuthService = retrofit.create(AuthService::class.java)
    val estudianteApi: EstudianteService = retrofit.create(EstudianteService::class.java)
    val docenteApi: DocenteService = retrofit.create(DocenteService::class.java)
    val dominioApi: DominioService = retrofit.create(DominioService::class.java)
    val progresoApi: ProgresoApiService = retrofit.create(ProgresoApiService::class.java)
    val historialMaterialApi: HistorialMaterialService = retrofit.create(HistorialMaterialService::class.java)
    val tutorApi: TutorApiService = retrofit.create(TutorApiService::class.java)




}
