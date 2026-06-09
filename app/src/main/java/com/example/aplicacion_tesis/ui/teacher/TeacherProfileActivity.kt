package com.example.aplicacion_tesis.ui.teacher

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.aplicacion_tesis.R
import com.example.aplicacion_tesis.databinding.ActivityTeacherProfileBinding
import com.example.aplicacion_tesis.model.dto.UpdateProfileRequest
import com.example.aplicacion_tesis.network.RetrofitClient
import com.example.aplicacion_tesis.network.TokenStore
import com.example.aplicacion_tesis.ui.login.LoginActivity
import kotlinx.coroutines.launch

class TeacherProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeacherProfileBinding

    private enum class TeacherTab { INICIO, INFORMES, ALERTAS, PERFIL }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ---------- TABS SUPERIORES ----------
        binding.tabTeacherInicio.setOnClickListener {
            selectTeacherTab(TeacherTab.INICIO)
            startActivity(Intent(this, TeacherHomeActivity::class.java))
            finish()
        }

        binding.tabTeacherInformes.setOnClickListener {
            selectTeacherTab(TeacherTab.INFORMES)
            startActivity(Intent(this, TeacherReportsActivity::class.java))
            finish()
        }

        binding.tabTeacherAlertas.setOnClickListener {
            selectTeacherTab(TeacherTab.ALERTAS)
            startActivity(Intent(this, TeacherAlertsActivity::class.java))
            finish()
        }

        binding.tabTeacherPerfil.setOnClickListener {
            // Ya estamos en Perfil
            selectTeacherTab(TeacherTab.PERFIL)
        }

        // Al entrar, PERFIL seleccionado
        selectTeacherTab(TeacherTab.PERFIL)

        // ---------- CARGAR PERFIL ----------
        cargarPerfilDocente()

        // ---------- GUARDAR CAMBIOS ----------
        binding.btnGuardarTeacher.setOnClickListener {
            guardarPerfilDocente()
        }

        // ---------- CERRAR SESIÓN ----------
        binding.btnLogoutTeacher.setOnClickListener {
            TokenStore.clear()

            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }

    // ============================================================
    //                 CARGAR PERFIL DEL DOCENTE
    // ============================================================
    private fun cargarPerfilDocente() {
        val idUsuario = TokenStore.userId
        if (idUsuario == null || idUsuario <= 0) {
            Toast.makeText(this, "No se encontró el usuario en sesión", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnGuardarTeacher.isEnabled = false

        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.getUsuario(idUsuario)
                val perfil = res.data ?: throw IllegalStateException(res.message ?: "Respuesta vacía")

                val nombreCompleto = listOf(perfil.nombre, perfil.apellidos)
                    .filter { it.isNotBlank() }
                    .joinToString(" ")

                val nombreTitulo = if (nombreCompleto.isBlank()) "Profesor" else nombreCompleto

                // Encabezado
                binding.tvNombreTeacherTitulo.text = nombreTitulo
                binding.tvCorreoTeacherTitulo.text = perfil.correo

                // Formulario
                binding.etNombreTeacher.setText(nombreCompleto)
                binding.etCorreoTeacher.setText(perfil.correo)

                TokenStore.setUserName(nombreTitulo)

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@TeacherProfileActivity,
                    "No se pudo cargar el perfil",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                binding.btnGuardarTeacher.isEnabled = true
            }
        }
    }

    // ============================================================
    //                 GUARDAR PERFIL DEL DOCENTE
    // ============================================================
    private fun guardarPerfilDocente() {
        val idUsuario = TokenStore.userId
        if (idUsuario == null || idUsuario <= 0) {
            Toast.makeText(this, "No se encontró el usuario en sesión", Toast.LENGTH_SHORT).show()
            return
        }

        val nombreCompleto = binding.etNombreTeacher.text.toString().trim()
        val correoNuevo = binding.etCorreoTeacher.text.toString().trim()
        val passNuevaRaw = binding.etPasswordTeacher.text.toString().trim()

        if (nombreCompleto.isBlank() || correoNuevo.isBlank()) {
            Toast.makeText(this, "Completa nombre y correo", Toast.LENGTH_SHORT).show()
            return
        }

        val partes = nombreCompleto.split(" ", limit = 2)
        val nombreNuevo = partes.getOrNull(0) ?: ""
        val apellidosNuevos = partes.getOrNull(1) ?: ""

        val body = UpdateProfileRequest(
            nombre = nombreNuevo,
            apellidos = apellidosNuevos,
            correo = correoNuevo,
            nueva_password = if (passNuevaRaw.isNotBlank()) passNuevaRaw else null,
            nueva_contrasena = null
        )

        binding.btnGuardarTeacher.isEnabled = false
        binding.btnGuardarTeacher.text = "Guardando..."

        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.updatePerfil(idUsuario, body)

                if (res.status) {
                    Toast.makeText(
                        this@TeacherProfileActivity,
                        res.message ?: "Perfil actualizado",
                        Toast.LENGTH_SHORT
                    ).show()

                    binding.tvNombreTeacherTitulo.text = nombreCompleto
                    binding.tvCorreoTeacherTitulo.text = correoNuevo
                    TokenStore.setUserName(nombreCompleto)
                    binding.etPasswordTeacher.setText("")
                } else {
                    Toast.makeText(
                        this@TeacherProfileActivity,
                        res.message ?: "No se pudo guardar",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@TeacherProfileActivity,
                    "Error al actualizar: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                binding.btnGuardarTeacher.isEnabled = true
                binding.btnGuardarTeacher.text = "Guardar Cambios"
            }
        }
    }

    // ============================================================
    //                 ESTILOS DE LAS TABS SUPERIORES
    // ============================================================
    private fun selectTeacherTab(tab: TeacherTab) {

        fun activate(textView: TextView, indicator: View, active: Boolean) {
            if (active) {
                textView.setTextColor(ContextCompat.getColor(this, R.color.ai_primary))
                textView.setTypeface(null, Typeface.BOLD)
                indicator.visibility = View.VISIBLE
            } else {
                textView.setTextColor(ContextCompat.getColor(this, R.color.ai_text_muted))
                textView.setTypeface(null, Typeface.NORMAL)
                indicator.visibility = View.GONE
            }
        }

        when (tab) {
            TeacherTab.INICIO -> {
                activate(binding.tvTabTeacherInicio, binding.indicatorTeacherInicio, true)
                activate(binding.tvTabTeacherInformes, binding.indicatorTeacherInformes, false)
                activate(binding.tvTabTeacherAlertas, binding.indicatorTeacherAlertas, false)
                activate(binding.tvTabTeacherPerfil, binding.indicatorTeacherPerfil, false)
            }
            TeacherTab.INFORMES -> {
                activate(binding.tvTabTeacherInicio, binding.indicatorTeacherInicio, false)
                activate(binding.tvTabTeacherInformes, binding.indicatorTeacherInformes, true)
                activate(binding.tvTabTeacherAlertas, binding.indicatorTeacherAlertas, false)
                activate(binding.tvTabTeacherPerfil, binding.indicatorTeacherPerfil, false)
            }
            TeacherTab.ALERTAS -> {
                activate(binding.tvTabTeacherInicio, binding.indicatorTeacherInicio, false)
                activate(binding.tvTabTeacherInformes, binding.indicatorTeacherInformes, false)
                activate(binding.tvTabTeacherAlertas, binding.indicatorTeacherAlertas, true)
                activate(binding.tvTabTeacherPerfil, binding.indicatorTeacherPerfil, false)
            }
            TeacherTab.PERFIL -> {
                activate(binding.tvTabTeacherInicio, binding.indicatorTeacherInicio, false)
                activate(binding.tvTabTeacherInformes, binding.indicatorTeacherInformes, false)
                activate(binding.tvTabTeacherAlertas, binding.indicatorTeacherAlertas, false)
                activate(binding.tvTabTeacherPerfil, binding.indicatorTeacherPerfil, true)
            }
        }
    }
}
