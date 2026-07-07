// ═══════════════════════════════════════════════════════════════════════════
//  📚 GUÍA DE ESTUDIO — CAPA DE RED (Retrofit + JWT)
// ═══════════════════════════════════════════════════════════════════════════
//  Único punto de la app que habla con el servidor. Cómo funciona:
//
//  · BASE_URL decide contra QUÉ servidor corre la app: producción (Railway)
//    o desarrollo local (10.0.2.2 = "localhost de la PC" visto desde el
//    emulador). ⚠️ Cambiar antes de compilar el APK que se reparte.
//
//  · El interceptor de OkHttp (abajo) se ejecuta en CADA petición:
//    1. Agrega el header "Authorization: Bearer <token>" leyendo TokenStore
//       (el token JWT que devolvió /auth/login y que vive en SharedPreferences).
//    2. Si el servidor responde 401 (token expirado — duran 8 h), limpia el
//       token y avisa por AuthEventBus → HomeActivity redirige al Login con
//       el mensaje de sesión expirada. Así ninguna pantalla necesita manejar
//       la expiración por su cuenta.
//
//  · Los "servicios" del final (authApi, tutorApi, progresoApi...) son
//    interfaces de Retrofit: cada método anotado (@GET/@POST) se convierte
//    en una llamada HTTP. Para consumir un endpoint nuevo de la API:
//    agregar el método en la interfaz correspondiente (p. ej.
//    TutorApiService.kt) + su DTO en model/dto/ y listo.
//
//  · Gson convierte JSON ⇄ data class automáticamente: los nombres de los
//    campos del DTO deben coincidir con las claves del JSON (o usar
//    @SerializedName).
// ═══════════════════════════════════════════════════════════════════════════
package com.example.aplicacion_tesis.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // ======== PRODUCCIÓN (Railway) ========
    const val BASE_URL = "https://apitesis-production-68de.up.railway.app/"

    // ======== DESARROLLO LOCAL (emulador Android) ========
    // Para volver a probar contra tu PC local, comenta la línea de arriba
    // y descomenta esta:
    // const val BASE_URL = "http://10.0.2.2:3008/"

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

            val response = chain.proceed(builder.build())
            if (response.code == 401) {
                TokenStore.clear()
                AuthEventBus.notifyExpired()
            }
            response
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
