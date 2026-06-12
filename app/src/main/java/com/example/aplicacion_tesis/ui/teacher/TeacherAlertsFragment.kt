package com.example.aplicacion_tesis.ui.teacher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aplicacion_tesis.R
import com.example.aplicacion_tesis.databinding.FragmentTeacherAlertsBinding
import com.example.aplicacion_tesis.model.dto.AlertSeverity
import com.example.aplicacion_tesis.model.dto.TeacherAlertItem
import com.example.aplicacion_tesis.network.RetrofitClient
import com.example.aplicacion_tesis.network.TokenStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** F2: antes era TeacherAlertsActivity. */
class TeacherAlertsFragment : Fragment() {

    private var _binding: FragmentTeacherAlertsBinding? = null
    private val binding get() = _binding!!

    private lateinit var alertsAdapter: TeacherAlertAdapter
    private val alertasActivas = mutableListOf<TeacherAlertItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTeacherAlertsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAlertsList()
        loadRealAlerts()
    }

    private fun setupAlertsList() {
        alertsAdapter = TeacherAlertAdapter { item ->
            mostrarDialogoSeguimiento(item)
        }
        binding.rvTeacherAlerts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTeacherAlerts.adapter = alertsAdapter
    }

    private fun showAlerts(items: List<TeacherAlertItem>) {
        if (_binding == null) return
        if (items.isEmpty()) {
            binding.tvAlertsEmpty.visibility   = View.VISIBLE
            binding.rvTeacherAlerts.visibility = View.GONE
        } else {
            binding.tvAlertsEmpty.visibility   = View.GONE
            binding.rvTeacherAlerts.visibility = View.VISIBLE
            alertsAdapter.updateItems(items)
        }
    }

    private fun mostrarDialogoSeguimiento(item: TeacherAlertItem) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_alerta_seguimiento, null)

        val dialog = android.app.Dialog(requireContext())
        dialog.setContentView(dialogView)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.88).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(true)

        val (chipColor, nivelTexto, nivelExplicacion) = when (item.severidad) {
            AlertSeverity.Critica     -> Triple(
                ContextCompat.getColor(requireContext(), R.color.ai_error),
                "🔴 CRÍTICA",
                "El estudiante tiene 4 o más respuestas incorrectas recientes. Requiere atención inmediata."
            )
            AlertSeverity.Advertencia -> Triple(
                ContextCompat.getColor(requireContext(), R.color.ai_warning),
                "🟡 ADVERTENCIA",
                "El estudiante tiene 2-3 respuestas incorrectas recientes. Monitorear de cerca."
            )
            AlertSeverity.Info        -> Triple(
                ContextCompat.getColor(requireContext(), R.color.ai_primary),
                "🔵 INFORMATIVA",
                "Situación a tener en cuenta. Bajo rendimiento en alguna competencia específica."
            )
        }

        dialogView.findViewById<TextView>(R.id.dialogTitle)?.apply {
            text = nivelTexto
            setTextColor(chipColor)
        }

        dialogView.findViewById<TextView>(R.id.dialogStudentName).apply {
            text = item.titulo.removePrefix("⚠ ")
            setTextColor(chipColor)
        }

        dialogView.findViewById<TextView>(R.id.dialogDescription).text =
            "${item.descripcion}\n\n${nivelExplicacion}"

        dialogView.findViewById<TextView>(R.id.dialogFecha).text =
            "📅 Última actividad: ${item.fecha}"

        dialogView.findViewById<TextView>(R.id.btnAtendida).setOnClickListener {
            alertsAdapter.removeItem(item)
            alertasActivas.remove(item)
            if (alertasActivas.isEmpty()) {
                binding.tvAlertsEmpty.visibility   = View.VISIBLE
                binding.rvTeacherAlerts.visibility = View.GONE
            }
            Toast.makeText(requireContext(), "✅ Alerta marcada como atendida", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialogView.findViewById<TextView>(R.id.btnCancelar).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun loadRealAlerts() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {

            val idDocente = TokenStore.teacherId ?: run {
                withContext(Dispatchers.Main) {
                    if (isAdded) Toast.makeText(requireContext(),
                        "No se encontró la sesión del docente", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            try {
                val resp  = RetrofitClient.docenteApi.getAlertas(idDocente)
                val datos = resp.data ?: emptyList()

                val alertas = datos.map { alerta ->
                    val descripcion = when {
                        alerta.tipoAlerta == "bajo_rendimiento" &&
                                !alerta.competenciasProblema.isNullOrEmpty() -> {
                            val comps = alerta.competenciasProblema
                                .joinToString(", ") { "${it.nombre} (${it.promedio}%)" }
                            "Bajo rendimiento en: $comps"
                        }
                        alerta.tipoAlerta == "muchos_errores" ->
                            "Tuvo ${alerta.erroresRecientes} respuestas incorrectas en las últimas 5."
                        else -> "Requiere atención del docente."
                    }

                    val severidad = when {
                        alerta.erroresRecientes >= 4 -> AlertSeverity.Critica
                        alerta.erroresRecientes >= 2 -> AlertSeverity.Advertencia
                        else                         -> AlertSeverity.Info
                    }

                    TeacherAlertItem(
                        titulo      = "⚠ ${alerta.nombre}",
                        descripcion = descripcion,
                        fecha       = alerta.ultimaActividad,
                        severidad   = severidad
                    )
                }

                withContext(Dispatchers.Main) {
                    // Ordenar: severidad (crítica→info), luego fecha reciente
                    val ordenadas = alertas.sortedWith(
                        compareByDescending<TeacherAlertItem> { item ->
                            when (item.severidad) {
                                AlertSeverity.Critica     -> 3
                                AlertSeverity.Advertencia -> 2
                                AlertSeverity.Info        -> 1
                            }
                        }.thenByDescending { item ->
                            try {
                                val partes = item.fecha.split("/")
                                if (partes.size == 3) "${partes[2]}${partes[1]}${partes[0]}"
                                else "00000000"
                            } catch (_: Exception) { "00000000" }
                        }
                    )
                    alertasActivas.clear()
                    alertasActivas.addAll(ordenadas)
                    showAlerts(ordenadas)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    if (isAdded) Toast.makeText(requireContext(),
                        "Error al cargar alertas: ${e.localizedMessage}",
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}