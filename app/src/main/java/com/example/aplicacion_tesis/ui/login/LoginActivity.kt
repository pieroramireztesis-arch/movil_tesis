package com.example.aplicacion_tesis.ui.login

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.aplicacion_tesis.databinding.ActivityLoginBinding
import com.example.aplicacion_tesis.network.TokenStore
import com.example.aplicacion_tesis.ui.HomeActivity
import com.example.aplicacion_tesis.ui.teacher.TeacherHomeActivity
import com.example.aplicacion_tesis.ui.onboarding.OnboardingActivity
import com.example.aplicacion_tesis.network.NetworkHelper
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Splash: el Manifest arranca esta Activity con el tema del escudo
        // (windowBackground); aquí se vuelve al tema normal ANTES de
        // dibujar el layout. El escudo se ve solo mientras la app carga.
        setTheme(com.example.aplicacion_tesis.R.style.Theme_Aplicacion_Tesis)
        super.onCreate(savedInstanceState)

        // T3-A: onboarding — solo la primera vez que se instala la app
        if (!getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getBoolean("onboarding_complete", false)) {
            startActivity(Intent(this, OnboardingActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // T2-F: aviso si la sesión expiró (redirigido desde HomeActivity/TeacherHomeActivity)
        if (intent.getBooleanExtra("session_expired", false)) {
            com.google.android.material.snackbar.Snackbar.make(
                binding.root, "Tu sesión expiró. Vuelve a ingresar.", com.google.android.material.snackbar.Snackbar.LENGTH_LONG
            ).show()
        }

        // T1-A: sesión persistente — si ya hay token válido, saltar el login
        val savedToken = TokenStore.token
        val savedUserId = TokenStore.userId
        if (!savedToken.isNullOrBlank() && savedUserId != null && savedUserId > 0) {
            val role = TokenStore.userRole
            val intent = if (role == "docente") {
                Intent(this, TeacherHomeActivity::class.java)
            } else {
                Intent(this, HomeActivity::class.java)
            }.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK }
            startActivity(intent)
            return
        }

        startFloatAnim(binding.mathS1, -30f, 3000L, 0L)
        startFloatAnim(binding.mathS2, -22f, 3600L, 350L)
        startFloatAnim(binding.mathS3, -28f, 4200L, 700L)
        startFloatAnim(binding.mathS4, -18f, 2800L, 150L)

        // T1-F: validación inline por campo
        binding.btnLogin.setOnClickListener {
            val correo = binding.etCorreo.text?.toString()?.trim().orEmpty()
            val pass = binding.etPassword.text?.toString()?.trim().orEmpty()

            if (correo.isBlank()) {
                binding.etCorreo.error = "Ingresa tu correo"
                binding.etCorreo.requestFocus()
                return@setOnClickListener
            }
            if (pass.isBlank()) {
                binding.etPassword.error = "Ingresa tu contraseña"
                binding.etPassword.requestFocus()
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

    private fun startFloatAnim(view: View?, dy: Float, duration: Long, delay: Long) {
        view ?: return
        ObjectAnimator.ofFloat(view, "translationY", 0f, dy).apply {
            this.duration = duration
            startDelay = delay
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }
}
