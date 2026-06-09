package com.example.aplicacion_tesis.ui.home.tabs

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aplicacion_tesis.R
import com.example.aplicacion_tesis.network.RetrofitClient
import com.example.aplicacion_tesis.network.TokenStore
import com.example.aplicacion_tesis.ui.components.DonutChartView
import com.example.aplicacion_tesis.ui.home.ProgressEvents
import kotlinx.coroutines.launch
import android.widget.AdapterView
import android.widget.Spinner
import com.example.aplicacion_tesis.model.dto.ProgresoPorCompetenciaItemDTO
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
class ProgresoFragment : Fragment() {

    private lateinit var donutProgresoGeneral: DonutChartView
    private lateinit var tvPorcentajeGeneral:  TextView
    private lateinit var tvTotalEjercicios:    TextView
    private lateinit var tvTotalLecciones:     TextView
    private lateinit var tvMensajeMotivacion:  TextView

    private lateinit var chartPieCompetencia:   PieChart
    private lateinit var spinnerCompetencia:    Spinner
    private lateinit var tvPieDetalle:          TextView
    private var competenciasActuales: List<ProgresoPorCompetenciaItemDTO> = emptyList()

    private lateinit var rvHistorial:      RecyclerView
    private lateinit var tvHistorialEmpty: TextView
    private lateinit var btnVerMas:        TextView
    private lateinit var historialAdapter: HistorialAdapter

    private val COLOR_C1 = Color.parseColor("#0A6FD4")
    private val COLOR_C2 = Color.parseColor("#27AE60")
    private val COLOR_C3 = Color.parseColor("#E67E22")
    private val COLOR_C4 = Color.parseColor("#7B1FA2")

    // Paginación
    private val LIMITE_INICIAL     = 5
    private var mostrandoTodo      = false
    private var totalHistorial     = 0
    private var idEstudianteCached: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_progreso, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        donutProgresoGeneral = view.findViewById(R.id.donutProgresoGeneral)
        tvPorcentajeGeneral  = view.findViewById(R.id.tvPorcentajeGeneral)
        tvTotalEjercicios    = view.findViewById(R.id.tvTotalEjercicios)
        tvTotalLecciones     = view.findViewById(R.id.tvTotalLecciones)
        tvMensajeMotivacion  = view.findViewById(R.id.tvMensajeMotivacion)

        chartPieCompetencia = view.findViewById(R.id.chartPieCompetenciaEstudiante)
        spinnerCompetencia  = view.findViewById(R.id.spinnerCompetenciaEstudiante)
        tvPieDetalle        = view.findViewById(R.id.tvPieDetalleEstudiante)

        rvHistorial      = view.findViewById(R.id.rvHistorialProgreso)
        tvHistorialEmpty = view.findViewById(R.id.tvHistorialEmpty)
        btnVerMas        = view.findViewById(R.id.btnVerMasHistorial)

        historialAdapter = HistorialAdapter()
        rvHistorial.apply {
            layoutManager            = LinearLayoutManager(requireContext())
            adapter                  = historialAdapter
            isNestedScrollingEnabled = false
        }

        // ✅ Botón Ver más / Ver menos
        btnVerMas.setOnClickListener {
            val idEst = idEstudianteCached ?: return@setOnClickListener
            if (mostrandoTodo) {
                mostrandoTodo = false
                viewLifecycleOwner.lifecycleScope.launch {
                    cargarHistorial(idEst, limite = LIMITE_INICIAL)
                }
            } else {
                mostrandoTodo = true
                viewLifecycleOwner.lifecycleScope.launch {
                    cargarHistorial(idEst, limite = 200)
                }
            }
        }

        // Escuchar eventos de progreso (cuando el estudiante responde)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                ProgressEvents.progressChanged.collect {
                    recargarProgreso()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Único punto de carga — arranque inicial y al volver al tab
        recargarProgreso()
    }

    // =============================================
    // OBTENER ID ESTUDIANTE
    // =============================================
    private suspend fun obtenerIdEstudiante(): Int? {
        val stored = TokenStore.studentId
        if (stored != null && stored > 0) return stored

        val idUsuario = TokenStore.userId ?: return null
        if (idUsuario <= 0) return null

        val response = RetrofitClient.estudianteApi.getEstudiantePorUsuario(idUsuario)
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null && body.status && body.data != null) {
                val idEst = body.data.idEstudiante
                TokenStore.setStudentId(idEst)
                return idEst
            }
        }
        return null
    }

    // =============================================
    // RECARGAR PROGRESO
    // =============================================
    private fun recargarProgreso() {
        viewLifecycleOwner.lifecycleScope.launch {
            val idEst = try { obtenerIdEstudiante() } catch (e: Exception) { null }
            if (idEst == null) {
                Toast.makeText(requireContext(),
                    "No se pudo obtener el estudiante.", Toast.LENGTH_SHORT).show()
                return@launch
            }
            idEstudianteCached = idEst
            cargarProgreso(idEst)
        }
    }

    // =============================================
    // CARGAR DATOS DE LA API
    // =============================================
    private suspend fun cargarProgreso(idEstudiante: Int) {
        try {
            // 1) Resumen general
            val resumen = RetrofitClient.progresoApi.getResumen(idEstudiante)
            if (resumen.status) {
                val pct = resumen.nivelPorcentaje.coerceIn(0, 100)
                donutProgresoGeneral.setPercentage(pct.toFloat())
                tvPorcentajeGeneral.text = "$pct%"
                tvTotalEjercicios.text   = resumen.ejerciciosDesarrollados.toString()
                tvTotalLecciones.text    = resumen.leccionesVistas.toString()
                tvMensajeMotivacion.text =
                    resumen.resumenTexto.ifBlank { "¡Sigue así, vas por buen camino!" }
            } else {
                donutProgresoGeneral.setPercentage(0f)
                tvPorcentajeGeneral.text = "0%"
                tvTotalEjercicios.text   = "0"
                tvTotalLecciones.text    = "0"
                tvMensajeMotivacion.text = "Aún no se ha registrado progreso."
            }

            // 2) Por competencia
            val comp = RetrofitClient.progresoApi.getPorCompetencia(idEstudiante)
            if (comp.status) {
                val temas: List<ProgresoPorCompetenciaItemDTO> = comp.temas ?: emptyList()
                setupPieChartCompetencias(temas)
            }

            // 3) Historial — primeros 5 al cargar
            mostrandoTodo = false
            cargarHistorial(idEstudiante, limite = LIMITE_INICIAL)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(),
                "Error al cargar progreso: ${e.localizedMessage}",
                Toast.LENGTH_LONG).show()
        }
    }

    // =============================================
    // PIECHART — Rendimiento por competencia
    // =============================================
    private fun setupPieChartCompetencias(temas: List<ProgresoPorCompetenciaItemDTO>) {
        competenciasActuales = temas
        val nombres = temas.map { abreviarCompetencia(it.nombre) }
        val adapter = android.widget.ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item, nombres
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        spinnerCompetencia.adapter = adapter
        spinnerCompetencia.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: android.view.View?, pos: Int, id: Long) {
                if (pos < temas.size) actualizarPieEstudiante(temas[pos])
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }
        if (temas.isNotEmpty()) actualizarPieEstudiante(temas[0])
    }

    private fun actualizarPieEstudiante(tema: ProgresoPorCompetenciaItemDTO) {
        val pct      = tema.porcentaje.coerceIn(0, 100)
        val pendiente = 100 - pct
        val color = when {
            pct >= 70 -> Color.parseColor("#27AE60")
            pct >= 40 -> Color.parseColor("#F39C12")
            else      -> Color.parseColor("#E74C3C")
        }
        val entries = listOf(
            PieEntry(pct.toFloat(), ""),
            PieEntry(pendiente.toFloat(), "")
        )
        val dataSet = PieDataSet(entries, "").apply {
            colors        = listOf(color, Color.parseColor("#E8E8E8"))
            setDrawValues(false)
            sliceSpace    = 3f
        }
        chartPieCompetencia.apply {
            data                    = PieData(dataSet)
            description.isEnabled   = false
            isDrawHoleEnabled       = true
            holeRadius              = 58f
            transparentCircleRadius = 62f
            setHoleColor(Color.WHITE)
            setDrawCenterText(true)
            centerText              = "$pct%"
            setCenterTextSize(28f)
            setCenterTextColor(color)
            setCenterTextTypeface(android.graphics.Typeface.DEFAULT_BOLD)
            legend.isEnabled        = false
            setTouchEnabled(false)
            animateY(700)
            invalidate()
        }
        val (nivel, desc) = when {
            pct >= 70 -> "✅ Logrado"     to "Supera el mínimo MINEDU"
            pct >= 40 -> "⚡ En Proceso"  to "Necesita refuerzo"
            else      -> "⚠ Inicio"      to "Requiere atención inmediata"
        }
        tvPieDetalle.text = "$nivel  ·  $desc"
        tvPieDetalle.setTextColor(color)
    }

    private fun abreviarCompetencia(nombre: String): String = when {
        "cantidad"    in nombre.lowercase() -> "Cantidad"
        "regularidad" in nombre.lowercase() -> "Regularidad / Equivalencia"
        "forma"       in nombre.lowercase() -> "Forma / Movimiento"
        "datos"       in nombre.lowercase() -> "Gestión de Datos"
        else -> nombre.take(30)
    }

    // =============================================
    // CARGAR HISTORIAL CON PAGINACIÓN
    // =============================================
    private suspend fun cargarHistorial(idEstudiante: Int, limite: Int) {
        try {
            val resp = RetrofitClient.progresoApi.getHistorial(
                idEstudiante = idEstudiante,
                limite       = limite,
                offset       = 0
            )

            if (!resp.status || resp.items.isEmpty()) {
                tvHistorialEmpty.visibility = View.VISIBLE
                rvHistorial.visibility      = View.GONE
                btnVerMas.visibility        = View.GONE
                return
            }

            totalHistorial = resp.total
            tvHistorialEmpty.visibility = View.GONE
            rvHistorial.visibility      = View.VISIBLE

            historialAdapter.setItems(resp.items)

            // ✅ Botón Ver más / Ver menos
            when {
                !mostrandoTodo && resp.hayMas -> {
                    val restantes = resp.total - limite
                    btnVerMas.visibility = View.VISIBLE
                    btnVerMas.text       = "Ver todos ($restantes más) ▼"
                }
                mostrandoTodo && resp.total > LIMITE_INICIAL -> {
                    btnVerMas.visibility = View.VISIBLE
                    btnVerMas.text       = "Ver menos ▲"
                }
                else -> {
                    btnVerMas.visibility = View.GONE
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}