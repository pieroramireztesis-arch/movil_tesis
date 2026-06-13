package com.example.aplicacion_tesis.ui.login
import android.animation.ObjectAnimator
import com.example.aplicacion_tesis.ui.HomeActivity
import android.content.Intent
import android.content.res.ColorStateList
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.aplicacion_tesis.databinding.ActivityRegisterBinding
import com.example.aplicacion_tesis.model.dto.RegisterRequest
import com.example.aplicacion_tesis.network.RetrofitClient
import com.example.aplicacion_tesis.network.TokenStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startFloatAnim(binding.mathS1, -28f, 3200L, 0L)
        startFloatAnim(binding.mathS2, -20f, 3800L, 400L)
        startFloatAnim(binding.mathS3, -32f, 4500L, 800L)
        startFloatAnim(binding.mathS4, -16f, 2900L, 200L)

        // Listeners para validar y calcular fuerza
        binding.etNombre.addTextChangedListener(watcher)
        binding.etCorreo.addTextChangedListener(watcher)
        binding.etPassword.addTextChangedListener(watcher)

        // Crear cuenta
        binding.btnCrear.setOnClickListener {
            val nombreCompleto = binding.etNombre.text?.toString()?.trim().orEmpty()
            val correo = binding.etCorreo.text?.toString()?.trim().orEmpty()
            val pass   = binding.etPassword.text?.toString()?.trim().orEmpty()

            if (!validarCampos(nombreCompleto, correo, pass)) return@setOnClickListener

            val (nombre, apellidos) = splitNombre(nombreCompleto)

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val req = RegisterRequest(
                        nombre = nombre,
                        apellidos = apellidos,
                        correo = correo,
                        contrasena = pass,
                        rol = "estudiante"
                    )

                    val resp = withContext(Dispatchers.IO) {
                        RetrofitClient.authApi.register(req)
                    }

                    if (resp.status) {
                        // Fallbacks por si el backend no devuelve algo
                        val backendUser = resp.user
                        val savedName = backendUser?.let { "${it.nombre} ${it.apellidos}".trim() }
                            ?: nombreCompleto
                        val savedRole = backendUser?.rol ?: "estudiante"
                        val savedId = backendUser?.id_usuario
                            ?: resp.id_estudiante ?: resp.id_docente

                        // Guarda sesión (token puede venir null, lo aceptamos)
                        TokenStore.save(
                            tokenValue = resp.token,
                            userIdValue = savedId,
                            roleValue = savedRole,
                            nameValue = savedName
                        )

                        Toast.makeText(this@RegisterActivity, "Cuenta creada. ¡Bienvenido!", Toast.LENGTH_SHORT).show()

                        // Ir directo al Home y limpiar back stack
                        startActivity(Intent(this@RegisterActivity, HomeActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                        finish()
                    } else {
                        Toast.makeText(
                            this@RegisterActivity,
                            resp.message ?: "No se pudo crear la cuenta.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@RegisterActivity, "Error de red.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Ir a login
        binding.tvIrLogin.setOnClickListener { finish() }

        // Primera validación
        validateAndUpdate()
        updateStrength(binding.etPassword.text?.toString().orEmpty())
    }

    // ---------- Validaciones / fuerza de contraseña ----------

    private val watcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (s === binding.etPassword.text) {
                updateStrength(binding.etPassword.text?.toString().orEmpty())
            }
            validateAndUpdate()
        }
        override fun afterTextChanged(s: Editable?) {}
    }

    private fun validarCampos(nombreCompleto: String, correo: String, pass: String): Boolean {
        if (nombreCompleto.isBlank()) {
            Toast.makeText(this, "Ingresa tu nombre completo.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (nombreCompleto.trim().split(" ").filter { it.isNotBlank() }.size < 2) {
            Toast.makeText(this, "Ingresa tu nombre y apellido.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            Toast.makeText(this, "Correo inválido.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (pass.length < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres.", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun validateAndUpdate() {
        val nombreOk = binding.etNombre.text?.toString()?.trim()?.isNotEmpty() == true
        val correo = binding.etCorreo.text?.toString()?.trim().orEmpty()
        val correoOk = Patterns.EMAIL_ADDRESS.matcher(correo).matches()
        val pass = binding.etPassword.text?.toString()?.trim().orEmpty()
        val passOk = pass.length >= 6

        val enable = nombreOk && correoOk && passOk
        binding.btnCrear.isEnabled = enable
        binding.btnCrear.alpha = if (enable) 1f else 0.5f
    }

    private fun updateStrength(password: String) {
        val score = calcScore(password)         // 0..100
        val (label, colorHex) = when {
            score <= 30 -> "Débil" to "#D0021B"   // rojo
            score <= 70 -> "Media" to "#F5A623"   // naranja
            else -> "Fuerte" to "#7ED321"         // verde
        }

        val color = Color.parseColor(colorHex)
        binding.strengthBar.progress = score
        binding.strengthBar.progressTintList = ColorStateList.valueOf(color)
        binding.tvStrength.text = label
        binding.tvStrength.setTextColor(color)
    }

    private fun calcScore(p: String): Int {
        if (p.isEmpty()) return 0
        var score = 0
        // longitud
        score += if (p.length >= 12) 45 else if (p.length >= 8) 35 else p.length * 3
        // variedad
        if (p.any { it.isLowerCase() }) score += 15
        if (p.any { it.isUpperCase() }) score += 15
        if (p.any { it.isDigit() }) score += 15
        if (p.any { "!@#\$%^&*()-_=+[]{};:'\",.<>?/\\|`~".contains(it) }) score += 15
        return score.coerceIn(0, 100)
    }

    private fun splitNombre(full: String): Pair<String, String> {
        val parts = full.trim().split(" ").filter { it.isNotBlank() }
        val nombre    = parts.firstOrNull() ?: ""
        val apellidos = parts.drop(1).joinToString(" ").ifBlank { "-" }
        return nombre to apellidos
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
