# 📚 Guía de código — App Android del Estudiante (TutorMath)

> Guía de estudio para entender, defender y modificar este proyecto.
> Los archivos clave también tienen un bloque "GUÍA DE ESTUDIO" al inicio.

## Qué es este proyecto

App Kotlin (Views + ViewBinding, sin Compose) para el **estudiante**. Toda la
inteligencia (scoring, ML, elección de ejercicios) vive en la **API REST** —
la app es el cliente: pide, pinta, cronometra y envía.

```
Esta app ── Retrofit + JWT ──► API Flask (Railway) ──► Postgres
   │
   └── Glide ──► Cloudinary (imágenes de ejercicios/desarrollos)
```

## Mapa de paquetes (app/src/main/java/com/example/aplicacion_tesis/)

| Paquete / archivo | Qué contiene | Cuándo tocarlo |
|---|---|---|
| `App.kt` | Arranque global: TokenStore, canal de notificaciones, WorkManager | Inicialización |
| `network/RetrofitClient.kt` | ⭐ BASE_URL, interceptor JWT, manejo de 401, servicios | Endpoints nuevos |
| `network/*Service.kt` | Interfaces Retrofit (una por dominio: auth, tutor, progreso…) | Endpoints nuevos |
| `network/TokenStore.kt` | Token JWT + id del estudiante en SharedPreferences | Casi nunca |
| `network/AuthEventBus.kt` | Aviso global de sesión expirada (401) | Casi nunca |
| `model/dto/` | Data classes que espejan el JSON de la API (Gson) | Al cambiar la API |
| `ui/login/` | Login/registro + onboarding + auto-login si hay sesión | |
| `ui/HomeActivity.kt` | Contenedor del estudiante: ViewPager2 con las 5 pestañas | Navegación |
| `ui/home/tabs/TutorFragment.kt` | ⭐ La pantalla del tutor adaptativo (ver header del archivo) | El corazón |
| `ui/home/tabs/ProgresoFragment.kt` | Radar de competencias, historial, rachas | Gráficos alumno |
| `ui/home/tabs/DominioFragment.kt` | Materiales de estudio por competencia/nivel | |
| `ui/teacher/` | Vista del docente en la app (reportes por alumno) | |
| `workers/RachaWorker.kt` | Notificación diaria "¿ya practicaste?" (WorkManager) | |
| `app/build.gradle.kts` | Dependencias, versión (versionCode/Name), firma release | Al publicar |

## Flujos que debes poder explicar

1. **Sesión**: login → la API devuelve JWT → `TokenStore` lo guarda →
   el interceptor de `RetrofitClient` lo agrega a cada petición → si expira
   (8 h), llega 401 → `AuthEventBus` → vuelta al login con aviso.
2. **Ciclo del tutor**: pedir ejercicio → cronometrar → responder →
   el servidor devuelve correcta/pista/material y el nuevo nivel.
   El `tiempo_respuesta` sale del cronómetro `startTimeMillis` (¡todo camino
   que muestre un ejercicio debe reiniciarlo!).
3. **Persistencia del ejercicio**: cambio de pestaña = caché en memoria;
   app matada = SharedPreferences + rehidratación por id (GET /ejercicios/{id}).
4. **Imágenes**: llegan como URLs absolutas de Cloudinary y las carga Glide.
   `corregirUrlParaDispositivo` SOLO reescribe hosts locales (emulador).

## Compilar el APK de release (en esta PC)

```powershell
cd C:\Users\JUAN RAMIREZ\Desktop\Aplicacion_Tesis
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat assembleRelease `
  "-Djavax.net.ssl.trustStore=C:\Users\JUAN RAMIREZ\Desktop\Aplicacion_Tesis\cacerts-local-fix" `
  "-Djavax.net.ssl.trustStorePassword=changeit"
# APK: app\build\outputs\apk\release\app-release.apk
```

- La firma sale de `keystore.properties` + `tutormath-release.jks` (NO están
  en git; sin ellos no se puede publicar una actualización — no perderlos).
- Los flags `-Djavax.net.ssl...` existen porque el antivirus (Avast)
  intercepta HTTPS en esta PC y rompe la descarga de dependencias.
- Antes de repartir: subir `versionCode`/`versionName` en `build.gradle.kts`,
  subir el APK al release de GitHub y (si cambió el nombre del archivo)
  actualizar `APK_DOWNLOAD_URL` en Railway.

## Trampas conocidas (ya resueltas, no re-introducir)

- No usar BOM UTF-8 al guardar archivos (.kt ni .xml): rompe la compilación.
- Las imágenes deben ser realmente del formato de su extensión (aapt2 valida
  la firma binaria; un JPEG renombrado a .png falla).
- `TeacherReportsFragment` necesita el `import ...R` explícito.