// ═══════════════════════════════════════════════════════════════════════════
//  📚 GUÍA DE ESTUDIO — CLASE APPLICATION (arranque global de la app)
// ═══════════════════════════════════════════════════════════════════════════
//  Se ejecuta UNA vez al abrir la app, antes de cualquier pantalla
//  (declarada con android:name=".App" en el AndroidManifest). Hace 3 cosas:
//  1. TokenStore.init: prepara el almacén del token JWT en SharedPreferences
//     (por eso la sesión sobrevive a cerrar la app).
//  2. Crea el canal de notificaciones "Racha Diaria" (obligatorio en
//     Android 8+ para poder notificar).
//  3. Programa RachaWorker con WorkManager cada 24 h: si hay sesión activa,
//     lanza la notificación "¿Ya practicaste hoy?". KEEP evita reprogramarlo
//     en cada arranque.
// ═══════════════════════════════════════════════════════════════════════════
package com.example.aplicacion_tesis

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.aplicacion_tesis.network.TokenStore
import com.example.aplicacion_tesis.workers.RachaWorker
import java.util.concurrent.TimeUnit

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        TokenStore.init(this)
        createNotificationChannels()
        scheduleRachaWorker()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                RachaWorker.CHANNEL_ID,
                "Racha Diaria",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Recordatorio diario para practicar álgebra en TutorMath"
            }
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    private fun scheduleRachaWorker() {
        val request = PeriodicWorkRequestBuilder<RachaWorker>(24, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            RachaWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}