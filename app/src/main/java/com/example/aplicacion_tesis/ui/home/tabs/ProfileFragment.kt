package com.example.aplicacion_tesis.ui.home.tabs

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.aplicacion_tesis.databinding.FragmentPerfilBinding
import com.example.aplicacion_tesis.model.dto.UpdateProfileRequest
import com.example.aplicacion_tesis.network.RetrofitClient
import com.example.aplicacion_tesis.network.TokenStore
import com.example.aplicacion_tesis.ui.login.LoginActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    // ----------- VIEW BINDING -----------
    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!
    // ------------------------------------

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Cargar datos al entrar
        cargarPerfil()

        // Botón Guardar
        binding.btnGuardar.setOnClickListener { guardarCambios() }

        // Botón Cerrar sesión
        binding.btnLogout.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Cerrar sesión")
                .setMessage("¿Seguro que quieres salir?")
                .setPositiveButton("Sí, salir") { _, _ ->
                    TokenStore.clear()
                    startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    requireActivity().finish()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    // ===============================================================
    //                    CARGAR PERFIL DESDE FLASK
    // ===============================================================
    private fun cargarPerfil() {
        val id = TokenStore.userId ?: run {
            Toast.makeText(requireContext(), "No hay usuario activo", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnGuardar.isEnabled = false

        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.getUsuario(id)
                val p = res.data ?: throw IllegalStateException(res.message ?: "Respuesta vacía")

                val nombreCompleto = listOf(p.nombre, p.apellidos)
                    .filter { it.isNotBlank() }
                    .joinToString(" ")

                binding.tvNombreTitulo.text =
                    if (nombreCompleto.isBlank()) "Usuario" else nombreCompleto
                binding.tvCorreoTitulo.text = p.correo

                // En el formulario mostramos nombre completo en un solo campo
                binding.etNombre.setText(nombreCompleto)
                binding.etCorreo.setText(p.correo)

                // Guardamos el nombre para otras pantallas (dashboard, etc.)
                TokenStore.setUserName(binding.tvNombreTitulo.text.toString())

            } catch (_: Exception) {
                Toast.makeText(
                    requireContext(),
                    "No se pudo cargar el perfil",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                binding.btnGuardar.isEnabled = true
            }
        }
    }

    // ===============================================================
    //                      GUARDAR CAMBIOS EN FLASK
    // ===============================================================
    private fun guardarCambios() {

        val idUsuario = TokenStore.userId
        if (idUsuario == null) {
            Toast.makeText(
                requireContext(),
                "No se encontró el usuario en sesión",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // 1. Leemos campos de la UI
        val nombreCompleto = binding.etNombre.text.toString().trim()
        val correoNuevo = binding.etCorreo.text.toString().trim()
        val passNuevaRaw = binding.etPassword.text.toString().trim()

        if (nombreCompleto.isBlank() || correoNuevo.isBlank()) {
            Toast.makeText(
                requireContext(),
                "Completa nombre y correo",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // 2. Separar nombre y apellidos (simple: primera palabra nombre, resto apellidos)
        val partes = nombreCompleto.split(" ", limit = 2)
        val nombreNuevo = partes.getOrNull(0) ?: ""
        val apellidosNuevo = partes.getOrNull(1) ?: ""

        // 3. Deshabilitar botón
        binding.btnGuardar.isEnabled = false
        binding.btnGuardar.text = "Guardando..."

        // 4. Body que espera tu endpoint /usuarios/{id}/perfil
        val bodyPerfil = UpdateProfileRequest(
            nombre = nombreNuevo,
            apellidos = apellidosNuevo,
            correo = correoNuevo,
            // Si el campo contraseña viene vacío, enviamos null (tu backend lo ignora)
            nueva_password = if (passNuevaRaw.isNotBlank()) passNuevaRaw else null,
            nueva_contrasena = null
        )

        // 5. Llamamos SOLO a updatePerfil (ya no existe cambiarPassword)
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val resPerfil = RetrofitClient.api.updatePerfil(idUsuario, bodyPerfil)

                if (resPerfil.status) {
                    Toast.makeText(
                        requireContext(),
                        resPerfil.message ?: "Perfil actualizado",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Limpiar contraseña por seguridad
                    binding.etPassword.setText("")

                    // Actualizar encabezado con el nuevo nombre/correo
                    binding.tvNombreTitulo.text = nombreCompleto
                    binding.tvCorreoTitulo.text = correoNuevo
                    TokenStore.setUserName(nombreCompleto)

                } else {
                    Toast.makeText(
                        requireContext(),
                        resPerfil.message ?: "No se pudo guardar",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Error al actualizar: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                binding.btnGuardar.isEnabled = true
                binding.btnGuardar.text = "Guardar Cambios"
            }
        }
    }
}
