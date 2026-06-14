package com.example.aplicacion_tesis.ui.teacher

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.aplicacion_tesis.databinding.FragmentTeacherPerfilBinding
import com.example.aplicacion_tesis.model.dto.UpdateProfileRequest
import com.example.aplicacion_tesis.network.RetrofitClient
import com.example.aplicacion_tesis.network.TokenStore
import com.example.aplicacion_tesis.ui.login.LoginActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

/** F2: antes era TeacherProfileActivity. */
class TeacherPerfilFragment : Fragment() {

    private var _binding: FragmentTeacherPerfilBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTeacherPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cargarPerfilDocente()

        binding.btnGuardarTeacher.setOnClickListener { guardarPerfilDocente() }

        binding.btnLogoutTeacher.setOnClickListener {
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

    private fun cargarPerfilDocente() {
        val idUsuario = TokenStore.userId
        if (idUsuario == null || idUsuario <= 0) {
            Toast.makeText(requireContext(), "No se encontró el usuario en sesión", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnGuardarTeacher.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.getUsuario(idUsuario)
                val perfil = res.data ?: throw IllegalStateException(res.message ?: "Respuesta vacía")

                val nombreCompleto = listOf(perfil.nombre, perfil.apellidos)
                    .filter { it.isNotBlank() }
                    .joinToString(" ")

                val nombreTitulo = if (nombreCompleto.isBlank()) "Profesor" else nombreCompleto

                binding.tvNombreTeacherTitulo.text = nombreTitulo
                binding.tvCorreoTeacherTitulo.text = perfil.correo
                binding.etNombreTeacher.setText(nombreCompleto)
                binding.etCorreoTeacher.setText(perfil.correo)

                TokenStore.setUserName(nombreTitulo)

            } catch (e: Exception) {
                e.printStackTrace()
                if (isAdded) {
                    Toast.makeText(requireContext(), "No se pudo cargar el perfil", Toast.LENGTH_SHORT).show()
                }
            } finally {
                _binding?.btnGuardarTeacher?.isEnabled = true
            }
        }
    }

    private fun guardarPerfilDocente() {
        val idUsuario = TokenStore.userId
        if (idUsuario == null || idUsuario <= 0) {
            Toast.makeText(requireContext(), "No se encontró el usuario en sesión", Toast.LENGTH_SHORT).show()
            return
        }

        val nombreCompleto = binding.etNombreTeacher.text.toString().trim()
        val correoNuevo    = binding.etCorreoTeacher.text.toString().trim()
        val passNuevaRaw   = binding.etPasswordTeacher.text.toString().trim()

        if (nombreCompleto.isBlank() || correoNuevo.isBlank()) {
            Toast.makeText(requireContext(), "Completa nombre y correo", Toast.LENGTH_SHORT).show()
            return
        }

        val partes = nombreCompleto.split(" ", limit = 2)
        val nombreNuevo     = partes.getOrNull(0) ?: ""
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

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.updatePerfil(idUsuario, body)

                if (res.status) {
                    Toast.makeText(requireContext(),
                        res.message ?: "Perfil actualizado", Toast.LENGTH_SHORT).show()

                    binding.tvNombreTeacherTitulo.text = nombreCompleto
                    binding.tvCorreoTeacherTitulo.text = correoNuevo
                    TokenStore.setUserName(nombreCompleto)
                    binding.etPasswordTeacher.setText("")
                } else {
                    Toast.makeText(requireContext(),
                        res.message ?: "No se pudo guardar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (isAdded) {
                    Toast.makeText(requireContext(),
                        "Error al actualizar: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                _binding?.btnGuardarTeacher?.isEnabled = true
                _binding?.btnGuardarTeacher?.text = "Guardar Cambios"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}