package com.example.aplicacion_tesis.ui.home.tabs.practica

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.aplicacion_tesis.ui.home.tabs.practica.PracticaEjercicioActivity

class EjercicioPracticaActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID_EJERCICIO = "id_ejercicio"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Leer el id que te envían (si es que alguien usa esta Activity antigua)
        val idEjercicio = intent.getIntExtra(EXTRA_ID_EJERCICIO, -1)

        // Redirigir a la nueva pantalla de práctica
        val intentNuevo = Intent(this, PracticaEjercicioActivity::class.java).apply {
            putExtra(PracticaEjercicioActivity.EXTRA_ID_EJERCICIO, idEjercicio)
        }

        startActivity(intentNuevo)

        // Cerrar esta Activity (ya no se usa directamente)
        finish()
    }
}
