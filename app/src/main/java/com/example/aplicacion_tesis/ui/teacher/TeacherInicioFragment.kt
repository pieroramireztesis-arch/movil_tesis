package com.example.aplicacion_tesis.ui.teacher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import androidx.lifecycle.lifecycleScope
import com.example.aplicacion_tesis.databinding.FragmentTeacherInicioBinding
import com.example.aplicacion_tesis.model.dto.TeacherDashboardData
import com.example.aplicacion_tesis.network.NetworkHelper
import com.example.aplicacion_tesis.network.RetrofitClient
import com.example.aplicacion_tesis.network.TokenStore
import kotlinx.coroutines.launch

/** F2: antes era el contenido de TeacherHomeActivity. */
class TeacherInicioFragment : Fragment() {

    private var _binding: FragmentTeacherInicioBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTeacherInicioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // SALUDO
        val rawName   = TokenStore.userName.orEmpty()
        val cleanName = rawName
            .replace("Estudiante", "", ignoreCase = true)
            .replace("Alumno", "", ignoreCase = true)
            .trim()
            .ifBlank { "Profesor" }
        binding.tvGreeting.text = "¡Hola, $cleanName!"

        binding.cardFrecuencia1.visibility = View.GONE
        binding.cardFrecuencia2.visibility = View.GONE
        binding.cardFrecuencia3.visibility = View.GONE

        // "Ver Informes" → cambiar de tab en el host (sin recrear pantalla)
        binding.btnVerInformes.setOnClickListener { irAInformes() }
        binding.tvVerTodaActividad.setOnClickListener { irAInformes() }

        val idDocente = TokenStore.teacherId
        if (idDocente == null || idDocente <= 0) {
            Snackbar.make(requireView(), "No se encontró el perfil del docente.", Snackbar.LENGTH_LONG).show()
            return
        }

        if (!NetworkHelper.isOnline(requireContext())) {
            Snackbar.make(requireView(), "Sin conexión a internet", Snackbar.LENGTH_LONG)
                .setAction("Reintentar") {
                    loadDashboard(idDocente)
                    loadFrecuenciaUso(idDocente)
                }.show()
            return
        }

        loadDashboard(idDocente)
        loadFrecuenciaUso(idDocente)
    }

    private fun irAInformes() {
        (activity as? TeacherHomeActivity)?.navigateTo(1)
    }

    private fun loadDashboard(idDocente: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val resp = RetrofitClient.docenteApi.getDashboard(idDocente)

                if (resp.status && resp.data != null) {
                    val data: TeacherDashboardData = resp.data

                    binding.tvEstudiantesActivos.text = data.estudiantesActivos.toString()
                    binding.tvProgresoPromedio.text   = String.format("%.1f%%", data.progresoPromedio)
                    binding.tvTemaDificil.text        = data.temaMasDificultad ?: "--"

                    val lista = data.actividadReciente ?: emptyList()

                    fun setCard(index: Int, card: View, tituloView: TextView, tiempoView: TextView) {
                        if (index < lista.size) {
                            val item = lista[index]
                            card.visibility = View.VISIBLE
                            tituloView.text = "${item.nombreEstudiante} ha ${item.tipo} \"${item.tema}\""
                            tiempoView.text = formatearFecha(item.fecha)
                        } else {
                            card.visibility = View.GONE
                        }
                    }

                    setCard(0, binding.cardActividad1, binding.tvActividad1Titulo, binding.tvActividad1Tiempo)
                    setCard(1, binding.cardActividad2, binding.tvActividad2Titulo, binding.tvActividad2Tiempo)
                    setCard(2, binding.cardActividad3, binding.tvActividad3Titulo, binding.tvActividad3Tiempo)

                    if (lista.isEmpty()) {
                        binding.cardActividad1.visibility = View.VISIBLE
                        binding.tvActividad1Titulo.text   = "Aún no hay actividad registrada"
                        binding.tvActividad1Tiempo.text   = "--"
                        binding.cardActividad2.visibility = View.GONE
                        binding.cardActividad3.visibility = View.GONE
                    }

                } else {
                    if (isAdded) Snackbar.make(requireView(),
                        resp.message ?: "No se pudo cargar el panel.", Snackbar.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                if (isAdded) {
                    Snackbar.make(requireView(),
                        "Error al cargar datos del panel.", Snackbar.LENGTH_LONG)
                        .setAction("Reintentar") { loadDashboard(idDocente) }
                        .show()
                }
            }
        }
    }

    private fun loadFrecuenciaUso(idDocente: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val resp = RetrofitClient.docenteApi.getFrecuenciaUso(idDocente)

                if (resp.status && !resp.data.isNullOrEmpty()) {
                    val ranking = resp.data
                    binding.tvFrecuenciaVacia.visibility = View.GONE

                    fun llenarFila(
                        card: View, tvNombre: TextView, tvDetalle: TextView,
                        tvTotal: TextView, divider: View?, index: Int
                    ) {
                        val item = ranking.getOrNull(index)
                        if (item != null) {
                            card.visibility     = View.VISIBLE
                            divider?.visibility = View.VISIBLE
                            tvNombre.text       = item.nombre
                            tvDetalle.text      = "${item.ejerciciosRespondidos} ejerc. · ${item.materialesVistos} mat. · ${item.ultimaActividad}"
                            tvTotal.text        = item.totalInteracciones.toString()
                        } else {
                            card.visibility     = View.GONE
                            divider?.visibility = View.GONE
                        }
                    }

                    llenarFila(binding.cardFrecuencia1, binding.tvFrecuencia1Nombre, binding.tvFrecuencia1Detalle, binding.tvFrecuencia1Total, null, 0)
                    llenarFila(binding.cardFrecuencia2, binding.tvFrecuencia2Nombre, binding.tvFrecuencia2Detalle, binding.tvFrecuencia2Total, binding.divFrecuencia1, 1)
                    llenarFila(binding.cardFrecuencia3, binding.tvFrecuencia3Nombre, binding.tvFrecuencia3Detalle, binding.tvFrecuencia3Total, binding.divFrecuencia2, 2)

                } else {
                    binding.cardFrecuencia1.visibility = View.GONE
                    binding.cardFrecuencia2.visibility = View.GONE
                    binding.cardFrecuencia3.visibility = View.GONE
                    binding.tvFrecuenciaVacia.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** "2026-05-31T18:36:12.023350" → "31 May 2026 · 18:36" (legible para el docente) */
    private fun formatearFecha(fecha: String?): String {
        if (fecha.isNullOrBlank()) return "--"
        return try {
            val regex = Regex("""(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})""")
            val m = regex.find(fecha) ?: return fecha
            val (anio, mes, dia, hh, mm) = m.destructured
            val meses = listOf("", "Ene", "Feb", "Mar", "Abr", "May", "Jun",
                "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
            "$dia ${meses[mes.toInt()]} $anio · $hh:$mm"
        } catch (_: Exception) { fecha }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}