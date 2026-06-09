package com.example.aplicacion_tesis.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.aplicacion_tesis.databinding.ActivityLoginBinding
import com.example.aplicacion_tesis.network.TokenStore
import com.example.aplicacion_tesis.ui.HomeActivity
import com.example.aplicacion_tesis.ui.teacher.TeacherHomeActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🔹 Iniciar sesión
        binding.btnLogin.setOnClickListener {
            val correo = binding.etCorreo.text?.toString()?.trim().orEmpty()
            val pass = binding.etPassword.text?.toString()?.trim().orEmpty()

            if (correo.isBlank() || pass.isBlank()) {
                Toast.makeText(this, "Ingresa correo y contraseña.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.login(correo, pass)
        }

        // 🔹 Ir a Registro
        binding.tvRegistro?.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // 🔹 Ir a Recuperar contraseña
        binding.tvOlvide?.setOnClickListener {
            val intent = Intent(this, RecoverActivity::class.java)
            startActivity(intent)
        }

        // 🔹 Observar el estado del login
        lifecycleScope.launch {
            viewModel.state.collectLatest { st ->
                when (st) {
                    is LoginState.Loading -> setLoading(true)

                    is LoginState.Success -> {
                        setLoading(false)

                        // Verificación mínima de que el ViewModel llenó el TokenStore
                        val role = TokenStore.userRole
                        val userId = TokenStore.userId

                        if (userId == null || userId <= 0) {
                            Toast.makeText(
                                this@LoginActivity,
                                "Error interno: no se recibió el id de usuario.",
                                Toast.LENGTH_LONG
                            ).show()
                            return@collectLatest
                        }

                        Toast.makeText(
                            this@LoginActivity,
                            "Bienvenido",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Si es docente -> TeacherHomeActivity, si no -> HomeActivity (estudiante)
                        val intent = if (role == "docente") {
                            Intent(this@LoginActivity, TeacherHomeActivity::class.java)
                        } else {
                            Intent(this@LoginActivity, HomeActivity::class.java)
                        }.apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }

                        startActivity(intent)
                    }

                    is LoginState.Error -> {
                        setLoading(false)
                        Toast.makeText(this@LoginActivity, st.message, Toast.LENGTH_SHORT).show()
                    }

                    else -> Unit
                }
            }
        }
    }

    // 🔹 Cambia el estado visual del botón mientras carga
    private fun setLoading(loading: Boolean) {
        binding.btnLogin.isEnabled = !loading
        binding.btnLogin.text = if (loading) "Verificando..." else "Iniciar Sesión"
    }
}
