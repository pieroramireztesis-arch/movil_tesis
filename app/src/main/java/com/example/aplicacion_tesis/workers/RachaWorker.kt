package com.example.aplicacion_tesis.workers

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.aplicacion_tesis.R
import com.example.aplicacion_tesis.ui.login.LoginActivity

class RachaWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    override fun doWork(): Result {
        val token = applicationContext
            .getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            .getString("token", null)

        // Si no hay sesión activa no tiene sentido recordar
        if (token.isNullOrBlank()) return Result.success()

        val pi = PendingIntent.getActivity(
            applicationContext, 0,
            Intent(applicationContext, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(applicationContext, "racha_diaria")
            .setSmallIcon(R.drawable.ic_lightbulb)
            .setContentTitle("¿Ya practicaste hoy?")
            .setContentText("Mantén tu racha — entra a TutorMath y resuelve un ejercicio.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Mantén tu racha — entra a TutorMath y resuelve un ejercicio de álgebra. ¡Cada día cuenta!")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()

        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIF_ID, notif)

        return Result.success()
    }

    companion object {
        const val NOTIF_ID = 1001
        const val WORK_NAME = "racha_diaria_check"
        const val CHANNEL_ID = "racha_diaria"
    }
}