package com.example.aplicacion_tesis.ui.login

import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.aplicacion_tesis.databinding.ActivityRecoverBinding
import com.example.aplicacion_tesis.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecoverActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecoverBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecoverBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Cambia el subtítulo como pediste
        binding.tvSubtitulo.text =
            "Ingresa tu correo electrónico para recibir una nueva contraseña."

        binding.btnEnviar.setOnClickListener {
            val correo = binding.etCorreo.text?.toString()?.trim().orEmpty()

            if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                Toast.makeText(this, "Correo electrónico inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val resp = withContext(Dispatchers.IO) {
                        RetrofitClient.authApi.recuperar(mapOf("correo" to correo))
                    }

                    if (resp.status) {
                        Toast.makeText(
                            this@RecoverActivity,
                            "Se ha enviado una nueva contraseña a tu correo.",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this@RecoverActivity,
                            resp.message ?: "No se pudo procesar la solicitud.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@RecoverActivity, "Error de conexión.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Volver al login
        binding.tvVolver.setOnClickListener { finish() }
        binding.btnBack.setOnClickListener { finish() }
    }
}
