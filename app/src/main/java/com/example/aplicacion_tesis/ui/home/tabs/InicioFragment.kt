package com.example.aplicacion_tesis.ui.home.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import com.example.aplicacion_tesis.R
import com.example.aplicacion_tesis.network.NetworkHelper
import com.example.aplicacion_tesis.network.RetrofitClient
import com.example.aplicacion_tesis.network.TokenStore
import com.example.aplicacion_tesis.ui.components.DonutChartView
import com.example.aplicacion_tesis.ui.home.ProgressEvents
import kotlinx.coroutines.launch
import com.example.aplicacion_tesis.model.dto.ProgresoPorCompetenciaItemDTO
class InicioFragment : Fragment() {

    private var idEstudiante: Int? = null
    private lateinit var swipeRefresh: SwipeRefreshLayout

    // Header
    private lateinit var imgAvatar: ImageView
    private lateinit var tvSaludo: TextView
    private lateinit var tvPorcentajeCabecera: TextView
    private lateinit var progressGeneral: ProgressBar

    // Donut
    private lateinit var donutChart: DonutChartView

    // Leyenda
    private lateinit var tvTema1: TextView
    private lateinit var tvTema1Pct: TextView
    private lateinit var tvTema2: TextView
    private lateinit var tvTema2Pct: TextView
    private lateinit var tvTema3: TextView
    private lateinit var tvTema3Pct: TextView
    private lateinit var tvTema4: TextView
    private lateinit var tvTema4Pct: TextView

    // Botones
    private lateinit var btnDominio: Button
    private lateinit var btnTutor: Button
    private lateinit var btnVerReporte: Button
    private lateinit var btnComoSeCalcula: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_inicio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // === SWIPE REFRESH ===
        swipeRefresh = view.findViewById(R.id.swipeRefreshInicio)
        swipeRefresh.setColorSchemeColors(android.graphics.Color.parseColor("#818CF8"))
        swipeRefresh.setOnRefreshListener {
            if (!NetworkHelper.isOnline(requireContext())) {
                swipeRefresh.isRefreshing = false
                Snackbar.make(requireView(), "Sin conexión a internet", Snackbar.LENGTH_LONG)
                    .setAction("Reintentar") { swipeRefresh.isRefreshing = true; swipeRefresh.setOnRefreshListener { onResume() } }
                    .show()
                return@setOnRefreshListener
            }
            viewLifecycleOwner.lifecycleScope.launch {
                val id = obtenerIdEstudiante() ?: run { swipeRefresh.isRefreshing = false; return@launch }
                idEstudiante = id
                cargarMiniDashboard(id)
            }
        }

        // === FINDVIEWBYID ===
        imgAvatar = view.findViewById(R.id.imgAvatar)
        tvSaludo = view.findViewById(R.id.tvSaludo)
        tvPorcentajeCabecera = view.findViewById(R.id.tvPorcentajeCabecera)
        progressGeneral = view.findViewById(R.id.progressGeneral)

        donutChart = view.findViewById(R.id.donutChartView)

        tvTema1 = view.findViewById(R.id.tvTema1)
        tvTema1Pct = view.findViewById(R.id.tvTema1Pct)
        tvTema2 = view.findViewById(R.id.tvTema2)
        tvTema2Pct = view.findViewById(R.id.tvTema2Pct)
        tvTema3 = view.findViewById(R.id.tvTema3)
        tvTema3Pct = view.findViewById(R.id.tvTema3Pct)
        tvTema4 = view.findViewById(R.id.tvTema4)
        tvTema4Pct = view.findViewById(R.id.tvTema4Pct)

        btnDominio = view.findViewById(R.id.btnDominio)
        btnTutor = view.findViewById(R.id.btnTutor)
        btnVerReporte = view.findViewById(R.id.btnVerReporte)
        btnComoSeCalcula = view.findViewById(R.id.btnComoSeCalcula)

        // === NAV BOTONES ===
        btnDominio.setOnClickListener {
            requireActivity().findViewById<ViewPager2>(R.id.viewPager).currentItem = 1
        }
        btnTutor.setOnClickListener {
            requireActivity().findViewById<ViewPager2>(R.id.viewPager).currentItem = 2
        }
        btnVerReporte.setOnClickListener {
            requireActivity().findViewById<ViewPager2>(R.id.viewPager).currentItem = 3
        }
        btnComoSeCalcula.setOnClickListener { mostrarDialogoComoSeCalcula() }

        // Escuchar cambios del progreso (respuestas correctas del tutor)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                ProgressEvents.progressChanged.collect {
                    val idEst = idEstudiante ?: return@collect
                    cargarMiniDashboard(idEst)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!NetworkHelper.isOnline(requireContext())) {
            Snackbar.make(requireView(), "Sin conexión a internet", Snackbar.LENGTH_LONG)
                .setAction("Reintentar") { onResume() }
                .show()
            return
        }
        viewLifecycleOwner.lifecycleScope.launch {
            val id = obtenerIdEstudiante() ?: return@launch
            idEstudiante = id
            cargarMiniDashboard(id)
        }
    }

    // =============================
    // OBTENER ID ESTUDIANTE
    // =============================
    private suspend fun obtenerIdEstudiante(): Int? {
        val stored = TokenStore.studentId
        if (stored != null && stored > 0) return stored

        val idUsuario = TokenStore.userId ?: return null
        if (idUsuario <= 0) return null

        val resp = RetrofitClient.estudianteApi.getEstudiantePorUsuario(idUsuario)
        if (resp.isSuccessful) {
            val body = resp.body()
            if (body != null && body.status && body.data != null) {
                val id = body.data.idEstudiante
                TokenStore.setStudentId(id)
                return id
            }
        }
        return null
    }

    // =============================
    // NOMBRE OFICIAL DE COMPETENCIA
    // =============================
    private fun nombreCompetenciaOficial(idCompetencia: Int?): String {
        return when (idCompetencia) {
            1 -> "Problemas de cantidad"
            2 -> "Regularidad, equivalencia y cambio"
            3 -> "Forma, movimiento y localización"
            4 -> "Gestión de datos e incertidumbre"
            else -> "Competencia $idCompetencia"
        }
    }

    // =============================
    // CARGAR DASHBOARD COMPLETO
    // =============================
    private fun cargarMiniDashboard(idEst: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            // Cada llamada es independiente: un fallo en una no bloquea las demás.

            // -------- 1. Saludo --------
            runCatching { RetrofitClient.api.getMiniDashboard(idEstudiante = idEst) }
                .onSuccess { dto ->
                    val name = dto.nombreEstudiante ?: "Estudiante"
                    tvSaludo.text = "Hola, $name"
                }

            // -------- 2. Barra de progreso general --------
            runCatching { RetrofitClient.progresoApi.getResumen(idEstudiante = idEst) }
                .onSuccess { resumen ->
                    val pctGeneral = resumen.nivelPorcentaje.coerceIn(0, 100)
                    tvPorcentajeCabecera.text = "$pctGeneral%"
                    progressGeneral.progress = pctGeneral
                }

            // -------- 3. Donut por competencia --------
            runCatching { RetrofitClient.progresoApi.getPorCompetencia(idEstudiante = idEst) }
                .onSuccess { comp ->
                    val lista: List<ProgresoPorCompetenciaItemDTO> =
                        if (comp.status) comp.temas ?: emptyList() else emptyList()

                    val c1 = lista.getOrNull(0)
                    val c2 = lista.getOrNull(1)
                    val c3 = lista.getOrNull(2)
                    val c4 = lista.getOrNull(3)

                    val p1 = (c1?.porcentaje ?: 0).coerceIn(0, 100)
                    val p2 = (c2?.porcentaje ?: 0).coerceIn(0, 100)
                    val p3 = (c3?.porcentaje ?: 0).coerceIn(0, 100)
                    val p4 = (c4?.porcentaje ?: 0).coerceIn(0, 100)

                    donutChart.setData(p1, p2, p3, p4)

                    tvTema1.text = nombreCompetenciaOficial(c1?.idCompetencia ?: 1)
                    tvTema1Pct.text = "$p1%"

                    tvTema2.text = nombreCompetenciaOficial(c2?.idCompetencia ?: 2)
                    tvTema2Pct.text = "$p2%"

                    tvTema3.text = nombreCompetenciaOficial(c3?.idCompetencia ?: 3)
                    tvTema3Pct.text = "$p3%"

                    tvTema4.text = nombreCompetenciaOficial(c4?.idCompetencia ?: 4)
                    tvTema4Pct.text = "$p4%"
                }
            swipeRefresh.isRefreshing = false
        }
    }

    private fun mostrarDialogoComoSeCalcula() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_como_se_calcula, null)

        val dialog = android.app.Dialog(requireContext())
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogView)
        dialog.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            setLayout(
                (resources.displayMetrics.widthPixels * 0.92).toInt(),
                android.view.WindowManager.LayoutParams.WRAP_CONTENT
            )
        }

        dialogView.findViewById<com.google.android.material.button.MaterialButton>(
            R.id.btnCerrarCalculo
        ).setOnClickListener { dialog.dismiss() }

        dialog.show()
    }
}
