package com.example.aplicacion_tesis.ui.home.tabs

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aplicacion_tesis.R
import com.example.aplicacion_tesis.model.dto.ChartPoint
import com.example.aplicacion_tesis.model.dto.ProgresoPorCompetenciaItemDTO
import com.example.aplicacion_tesis.model.dto.TiempoNivelItemDTO
import com.example.aplicacion_tesis.network.NetworkHelper
import com.example.aplicacion_tesis.network.RetrofitClient
import com.example.aplicacion_tesis.network.TokenStore
import com.example.aplicacion_tesis.ui.components.DonutChartView
import com.example.aplicacion_tesis.ui.home.ProgressEvents
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.launch

class ProgresoFragment : Fragment() {

    // ── Card 1: Progreso general ──────────────────────────
    private lateinit var donutGeneral: DonutChartView
    private lateinit var tvPctGeneral: TextView
    private lateinit var tvNivelGeneral: TextView
    private lateinit var tvMensaje: TextView
    private lateinit var tvStatEjercicios: TextView
    private lateinit var tvStatMateriales: TextView

    // ── Card 2: 4 Competencias ────────────────────────────
    private lateinit var donutC1: DonutChartView
    private lateinit var donutC2: DonutChartView
    private lateinit var donutC3: DonutChartView
    private lateinit var donutC4: DonutChartView
    private lateinit var tvPctC1: TextView
    private lateinit var tvPctC2: TextView
    private lateinit var tvPctC3: TextView
    private lateinit var tvPctC4: TextView
    private lateinit var tvNombreC1: TextView
    private lateinit var tvNombreC2: TextView
    private lateinit var tvNombreC3: TextView
    private lateinit var tvNombreC4: TextView
    private lateinit var tvNivelC1: TextView
    private lateinit var tvNivelC2: TextView
    private lateinit var tvNivelC3: TextView
    private lateinit var tvNivelC4: TextView

    // ── Card Radar — Perfil de las 4 competencias ─────────
    private lateinit var radarChart: RadarChart

    // ── Card 3: Bar chart + filas de acierto ─────────────
    private lateinit var barChart: BarChart
    private lateinit var tvBarEmpty: TextView
    private lateinit var llDificultad: LinearLayout
    private lateinit var tvDificultadEmpty: TextView

    // ── Card 4: Line chart evolución ─────────────────────
    private lateinit var lineChart: LineChart
    private lateinit var tvLineEmpty: TextView

    // ── Historial ─────────────────────────────────────────
    private lateinit var rvHistorial: RecyclerView
    private lateinit var tvHistorialEmpty: TextView
    private lateinit var btnVerMas: TextView
    private lateinit var historialAdapter: HistorialAdapter

    // Colores del sistema (deben coincidir con colors.xml)
    private val COLORES = listOf("#818CF8", "#34D399", "#FB923C", "#C084FC")

    private val LIMITE_INICIAL = 5
    private var mostrandoTodo = false
    private var idEstudianteCached: Int? = null
    private lateinit var swipeRefresh: SwipeRefreshLayout


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_progreso, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        swipeRefresh = view.findViewById(R.id.swipeRefreshProgreso)
        swipeRefresh.setColorSchemeColors(android.graphics.Color.parseColor("#818CF8"))
        swipeRefresh.setOnRefreshListener { recargarProgreso() }

        // Card 1
        donutGeneral     = view.findViewById(R.id.donutGeneral)
        tvPctGeneral     = view.findViewById(R.id.tvPctGeneral)
        tvNivelGeneral   = view.findViewById(R.id.tvNivelGeneral)
        tvMensaje        = view.findViewById(R.id.tvMensajeMotivacion)
        tvStatEjercicios = view.findViewById(R.id.tvStatEjercicios)
        tvStatMateriales = view.findViewById(R.id.tvStatMateriales)

        // Card 2
        donutC1 = view.findViewById(R.id.donutC1)
        donutC2 = view.findViewById(R.id.donutC2)
        donutC3 = view.findViewById(R.id.donutC3)
        donutC4 = view.findViewById(R.id.donutC4)
        tvPctC1 = view.findViewById(R.id.tvPctC1)
        tvPctC2 = view.findViewById(R.id.tvPctC2)
        tvPctC3 = view.findViewById(R.id.tvPctC3)
        tvPctC4 = view.findViewById(R.id.tvPctC4)
        tvNombreC1 = view.findViewById(R.id.tvNombreC1)
        tvNombreC2 = view.findViewById(R.id.tvNombreC2)
        tvNombreC3 = view.findViewById(R.id.tvNombreC3)
        tvNombreC4 = view.findViewById(R.id.tvNombreC4)
        tvNivelC1 = view.findViewById(R.id.tvNivelC1)
        tvNivelC2 = view.findViewById(R.id.tvNivelC2)
        tvNivelC3 = view.findViewById(R.id.tvNivelC3)
        tvNivelC4 = view.findViewById(R.id.tvNivelC4)

        // Card Radar
        radarChart       = view.findViewById(R.id.radarCompetencias)

        // Card 3
        barChart         = view.findViewById(R.id.chartBarDificultad)
        tvBarEmpty       = view.findViewById(R.id.tvBarEmpty)
        llDificultad     = view.findViewById(R.id.llDificultad)
        tvDificultadEmpty = view.findViewById(R.id.tvDificultadEmpty)

        // Card 4
        lineChart   = view.findViewById(R.id.chartLineEvolucion)
        tvLineEmpty = view.findViewById(R.id.tvLineEmpty)

        // Historial
        rvHistorial      = view.findViewById(R.id.rvHistorialProgreso)
        tvHistorialEmpty = view.findViewById(R.id.tvHistorialEmpty)
        btnVerMas        = view.findViewById(R.id.btnVerMasHistorial)

        historialAdapter = HistorialAdapter()
        rvHistorial.apply {
            layoutManager            = LinearLayoutManager(requireContext())
            adapter                  = historialAdapter
            isNestedScrollingEnabled = false
        }

        btnVerMas.setOnClickListener {
            val idEst = idEstudianteCached ?: return@setOnClickListener
            if (mostrandoTodo) {
                mostrandoTodo = false
                viewLifecycleOwner.lifecycleScope.launch { cargarHistorial(idEst, LIMITE_INICIAL) }
            } else {
                mostrandoTodo = true
                viewLifecycleOwner.lifecycleScope.launch { cargarHistorial(idEst, 200) }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                ProgressEvents.progressChanged.collect { recargarProgreso() }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        recargarProgreso()
    }

    // ══════════════════════════════════════════════════════
    // CARGA PRINCIPAL
    // ══════════════════════════════════════════════════════
    private fun recargarProgreso() {
        if (!NetworkHelper.isOnline(requireContext())) {
            Snackbar.make(requireView(), "Sin conexión a internet", Snackbar.LENGTH_LONG)
                .setAction("Reintentar") { recargarProgreso() }
                .show()
            return
        }
        viewLifecycleOwner.lifecycleScope.launch {
            val idEst = try { obtenerIdEstudiante() } catch (e: Exception) { null }
            if (idEst == null) {
                Snackbar.make(requireView(), "No se pudo obtener el perfil del estudiante.", Snackbar.LENGTH_LONG)
                    .setAction("Reintentar") { recargarProgreso() }
                    .show()
                return@launch
            }
            idEstudianteCached = idEst
            cargarProgreso(idEst)
        }
    }

    private suspend fun obtenerIdEstudiante(): Int? {
        val stored = TokenStore.studentId
        if (stored != null && stored > 0) return stored
        val idUsuario = TokenStore.userId ?: return null
        if (idUsuario <= 0) return null
        val response = RetrofitClient.estudianteApi.getEstudiantePorUsuario(idUsuario)
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null && body.status && body.data != null) {
                TokenStore.setStudentId(body.data.idEstudiante)
                return body.data.idEstudiante
            }
        }
        return null
    }

    private suspend fun cargarProgreso(idEstudiante: Int) {
        try {
            // 1) Resumen general (donut + stats)
            val resumen = RetrofitClient.progresoApi.getResumen(idEstudiante)
            if (resumen.status) {
                val pct = resumen.nivelPorcentaje.coerceIn(0, 100)
                donutGeneral.setPercentage(pct.toFloat(), Color.parseColor("#818CF8"))
                tvPctGeneral.text     = "$pct%"
                setNivelEstado(tvNivelGeneral, pct)
                tvMensaje.text        = resumen.resumenTexto.ifBlank { "¡Sigue así, vas por buen camino!" }
                tvStatEjercicios.text = resumen.ejerciciosDesarrollados.toString()
                tvStatMateriales.text = resumen.leccionesVistas.toString()
            } else {
                donutGeneral.setPercentage(0f, Color.parseColor("#818CF8"))
                tvPctGeneral.text   = "0%"
                setNivelEstado(tvNivelGeneral, 0)
                tvMensaje.text      = "Aún no se ha registrado progreso."
                tvStatEjercicios.text = "0"
                tvStatMateriales.text = "0"
            }

            // 2) Las 4 competencias — 4 donuts circulares (% → circular)
            val comp = RetrofitClient.progresoApi.getPorCompetencia(idEstudiante)
            if (comp.status) actualizarCompetencias(comp.temas ?: emptyList())

            // 3) Dificultad — barras de cantidad + filas de % acierto
            cargarDificultad(idEstudiante)

            // 4) Evolución de puntaje — gráfico lineal (tendencia)
            cargarEvolucion(idEstudiante)

            // 5) Historial reciente
            mostrandoTodo = false
            cargarHistorial(idEstudiante, LIMITE_INICIAL)
            swipeRefresh.isRefreshing = false

        } catch (e: Exception) {
            e.printStackTrace()
            swipeRefresh.isRefreshing = false
            if (isAdded) {
                Snackbar.make(requireView(), "Error al cargar progreso.", Snackbar.LENGTH_LONG)
                    .setAction("Reintentar") { recargarProgreso() }
                    .show()
            }
        }
    }

    // ══════════════════════════════════════════════════════
    // HELPERS DE NIVEL
    // ══════════════════════════════════════════════════════
    private fun nivelTexto(pct: Int): String = when {
        pct >= 70 -> "Logrado"
        pct >= 40 -> "En Proceso"
        else      -> "Inicio"
    }

    private fun colorNivel(pct: Int): Int = when {
        pct >= 70 -> Color.parseColor("#34D399")
        pct >= 40 -> Color.parseColor("#FB923C")
        else      -> Color.parseColor("#F87171")
    }

    private fun nivelIconRes(pct: Int): Int = when {
        pct >= 70 -> R.drawable.ic_check_circle_24
        pct >= 40 -> R.drawable.ic_sync
        else      -> R.drawable.ic_flag
    }

    private fun setNivelEstado(tv: android.widget.TextView, texto: String, pct: Int) {
        val color = colorNivel(pct)
        tv.text = texto
        tv.setTextColor(color)
        tv.setCompoundDrawablesRelativeWithIntrinsicBounds(nivelIconRes(pct), 0, 0, 0)
        tv.compoundDrawablePadding = (4 * resources.displayMetrics.density).toInt()
        tv.compoundDrawablesRelative[0]?.mutate()?.setTint(color)
    }

    private fun setNivelEstado(tv: android.widget.TextView, pct: Int) =
        setNivelEstado(tv, nivelTexto(pct), pct)

    private fun abreviarCompetencia(nombre: String): String = when {
        "cantidad"    in nombre.lowercase() -> "Cantidad"
        "regularidad" in nombre.lowercase() -> "Regularidad"
        "forma"       in nombre.lowercase() -> "Forma / Mov."
        "datos"       in nombre.lowercase() -> "Gestión Datos"
        else -> nombre.take(14)
    }

    // ══════════════════════════════════════════════════════
    // SECCIÓN 2 — 4 Donuts de competencia (% → circular)
    // ══════════════════════════════════════════════════════
    private fun actualizarCompetencias(temas: List<ProgresoPorCompetenciaItemDTO>) {
        val donuts   = listOf(donutC1,   donutC2,   donutC3,   donutC4)
        val tvPcts   = listOf(tvPctC1,   tvPctC2,   tvPctC3,   tvPctC4)
        val tvNombres = listOf(tvNombreC1, tvNombreC2, tvNombreC3, tvNombreC4)
        val tvNiveles = listOf(tvNivelC1, tvNivelC2, tvNivelC3, tvNivelC4)

        donuts.forEachIndexed { i, donut ->
            val tema  = temas.getOrNull(i)
            val pct   = tema?.porcentaje?.coerceIn(0, 100) ?: 0
            val color = Color.parseColor(COLORES[i])

            donut.setPercentage(pct.toFloat(), color)
            tvPcts[i].text = "$pct%"
            tvPcts[i].setTextColor(color)

            if (tema != null) tvNombres[i].text = abreviarCompetencia(tema.nombre)

            val nivelLabel = tema?.nombreNivel?.ifBlank { nivelTexto(pct) } ?: nivelTexto(pct)
            setNivelEstado(tvNiveles[i], nivelLabel, pct)
        }

        actualizarRadarChart(temas)
    }

    // ══════════════════════════════════════════════════════
    // CARD RADAR — araña de las 4 competencias MINEDU
    // ══════════════════════════════════════════════════════
    private fun actualizarRadarChart(temas: List<ProgresoPorCompetenciaItemDTO>) {
        val etiquetas = listOf("Cantidad", "Regularidad", "Forma/Mov.", "Datos")
        val entradas  = (0..3).map { i ->
            RadarEntry(temas.getOrNull(i)?.porcentaje?.coerceIn(0, 100)?.toFloat() ?: 0f)
        }

        val dataSet = RadarDataSet(entradas, "").apply {
            color              = Color.parseColor("#818CF8")
            fillColor          = Color.parseColor("#818CF8")
            setDrawFilled(true)
            fillAlpha          = 55
            lineWidth          = 2.2f
            setDrawValues(false)
            setDrawHighlightCircleEnabled(true)
            highlightCircleStrokeColor = Color.parseColor("#818CF8")
            highlightCircleInnerRadius = 3f
        }

        radarChart.apply {
            data = RadarData(dataSet)

            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(etiquetas)
                textColor      = Color.parseColor("#94A3B8")
                textSize       = 11f
            }
            yAxis.apply {
                setDrawLabels(false)
                axisMinimum = 0f
                axisMaximum = 100f
                setLabelCount(5, true)
            }

            webColor      = Color.parseColor("#334155")
            webColorInner = Color.parseColor("#334155")
            webAlpha      = 160
            webLineWidth  = 1f

            description.isEnabled = false
            legend.isEnabled      = false
            setBackgroundColor(Color.TRANSPARENT)
            setTouchEnabled(false)
            animateXY(900, 900)
            invalidate()
        }
    }

    // ══════════════════════════════════════════════════════
    // SECCIÓN 3 — BarChart cantidades + filas de acierto
    // ══════════════════════════════════════════════════════
    private suspend fun cargarDificultad(idEstudiante: Int) {
        try {
            val resp = RetrofitClient.progresoApi.getTiempoPorNivel(idEstudiante)
            llDificultad.removeAllViews()

            if (!resp.status || resp.niveles.isEmpty()) {
                barChart.visibility       = View.GONE
                tvBarEmpty.visibility     = View.VISIBLE
                tvDificultadEmpty.visibility = View.VISIBLE
                return
            }

            tvBarEmpty.visibility        = View.GONE
            barChart.visibility          = View.VISIBLE
            tvDificultadEmpty.visibility = View.GONE

            setupBarChart(resp.niveles)
            resp.niveles.forEach { llDificultad.addView(crearFilaAcierto(it)) }

        } catch (e: Exception) {
            e.printStackTrace()
            tvBarEmpty.visibility = View.VISIBLE
        }
    }

    // BarChart: totalRespuestas por nivel (cantidades → barras)
    private fun setupBarChart(niveles: List<TiempoNivelItemDTO>) {
        val entries = niveles.mapIndexed { i, item ->
            BarEntry(i.toFloat(), item.totalRespuestas.toFloat())
        }
        val labels = niveles.map { "N${it.nivelEjercicio}" }
        // Color de barra por rendimiento (acierto), no por nivel — más informativo
        val barColors = niveles.map { item ->
            when {
                item.tasaAcierto >= 0.70f -> Color.parseColor("#34D399") // verde — logrado
                item.tasaAcierto >= 0.40f -> Color.parseColor("#FB923C") // naranja — en proceso
                else                      -> Color.parseColor("#F87171") // rojo — inicio
            }
        }

        val dataSet = BarDataSet(entries, "").apply {
            colors = barColors
            valueTextColor = Color.parseColor("#F1F5F9")
            valueTextSize  = 11f
            valueFormatter = object : ValueFormatter() {
                override fun getBarLabel(e: BarEntry) = "${e.y.toInt()} resp."
            }
        }

        barChart.apply {
            data = BarData(dataSet).apply { barWidth = 0.55f }
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(labels)
                position       = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                textColor      = Color.parseColor("#94A3B8")
                textSize       = 12f
                granularity    = 1f
            }
            axisLeft.apply {
                textColor    = Color.parseColor("#94A3B8")
                gridColor    = Color.parseColor("#334155")
                textSize     = 11f
                axisMinimum  = 0f
            }
            axisRight.isEnabled    = false
            description.isEnabled  = false
            legend.isEnabled       = false
            setBackgroundColor(Color.TRANSPARENT)
            setFitBars(true)
            animateY(800)
            invalidate()
        }
    }

    // Filas de tasa de acierto (% en texto coloreado)
    private fun crearFilaAcierto(item: TiempoNivelItemDTO): View {
        val ctx  = requireContext()
        val dp4  = (4  * resources.displayMetrics.density).toInt()
        val dp8  = (8  * resources.displayMetrics.density).toInt()
        val dp12 = (12 * resources.displayMetrics.density).toInt()

        val colorBadge = Color.parseColor(COLORES.getOrElse(item.nivelEjercicio - 1) { "#94A3B8" })
        val pct        = (item.tasaAcierto * 100).toInt()
        val colorPct   = colorNivel(pct)

        val row = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp4, 0, dp4)
            gravity = android.view.Gravity.CENTER_VERTICAL
        }

        val badge = TextView(ctx).apply {
            text = "N${item.nivelEjercicio}"
            textSize = 11f
            setTextColor(Color.WHITE)
            setBackgroundColor(colorBadge)
            setPadding(dp8, dp4, dp8, dp4)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { marginEnd = dp12 }
        }

        val tvNombre = TextView(ctx).apply {
            text = item.nombreNivel
            textSize = 13f
            setTextColor(Color.parseColor("#F1F5F9"))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f)
        }

        // Si hay pocas respuestas, el % no es estadísticamente representativo
        val muestraInsuficiente = item.totalRespuestas < 3
        val tvAcierto = TextView(ctx).apply {
            text = if (muestraInsuficiente) "$pct% · pocos datos" else "$pct% acierto"
            textSize = if (muestraInsuficiente) 11f else 13f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setTextColor(if (muestraInsuficiente) Color.parseColor("#94A3B8") else colorPct)
            gravity = android.view.Gravity.END
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.5f)
        }

        row.addView(badge)
        row.addView(tvNombre)
        row.addView(tvAcierto)
        return row
    }

    // ══════════════════════════════════════════════════════
    // SECCIÓN 4 — LineChart evolución de puntaje (tendencia → lineal)
    // ══════════════════════════════════════════════════════
    private suspend fun cargarEvolucion(idEstudiante: Int) {
        try {
            val resp   = RetrofitClient.progresoApi.getChart(idEstudiante)
            val puntos = resp.datosChart ?: emptyList()

            if (!resp.status || puntos.isEmpty()) {
                lineChart.visibility = View.GONE
                tvLineEmpty.visibility = View.VISIBLE
                return
            }

            tvLineEmpty.visibility = View.GONE
            lineChart.visibility   = View.VISIBLE
            setupLineChart(puntos)

        } catch (e: Exception) {
            e.printStackTrace()
            lineChart.visibility   = View.GONE
            tvLineEmpty.visibility = View.VISIBLE
        }
    }

    private fun setupLineChart(puntos: List<ChartPoint>) {
        val entries = puntos.mapIndexed { i, p -> Entry(i.toFloat(), p.puntaje.toFloat()) }
        val labels  = puntos.map { f ->
            val parts = f.fecha.split("-")
            if (parts.size >= 3) "${parts[1]}/${parts[2]}" else f.fecha
        }

        val dataSet = LineDataSet(entries, "Puntaje").apply {
            color            = Color.parseColor("#818CF8")
            setCircleColor(Color.parseColor("#818CF8"))
            circleRadius     = 4f
            circleHoleColor  = Color.parseColor("#1E293B")
            lineWidth        = 2.5f
            fillAlpha        = 55
            fillColor        = Color.parseColor("#818CF8")
            setDrawFilled(true)
            valueTextColor   = Color.parseColor("#94A3B8")
            valueTextSize    = 9f
            mode             = LineDataSet.Mode.CUBIC_BEZIER
        }

        lineChart.apply {
            data = LineData(dataSet)
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(labels)
                position       = XAxis.XAxisPosition.BOTTOM
                textColor      = Color.parseColor("#94A3B8")
                gridColor      = Color.parseColor("#334155")
                textSize       = 10f
                granularity    = 1f
                setDrawGridLines(false)
            }
            axisLeft.apply {
                textColor   = Color.parseColor("#94A3B8")
                gridColor   = Color.parseColor("#334155")
                textSize    = 11f
                axisMinimum = 0f
            }
            axisRight.isEnabled   = false
            description.isEnabled = false
            legend.isEnabled      = false
            setBackgroundColor(Color.TRANSPARENT)
            animateX(1000)
            invalidate()
        }
    }

    // ══════════════════════════════════════════════════════
    // HISTORIAL
    // ══════════════════════════════════════════════════════
    private suspend fun cargarHistorial(idEstudiante: Int, limite: Int) {
        try {
            val resp = RetrofitClient.progresoApi.getHistorial(idEstudiante, limite, 0)
            if (!resp.status || resp.items.isEmpty()) {
                tvHistorialEmpty.visibility = View.VISIBLE
                rvHistorial.visibility      = View.GONE
                btnVerMas.visibility        = View.GONE
                return
            }
            tvHistorialEmpty.visibility = View.GONE
            rvHistorial.visibility      = View.VISIBLE
            historialAdapter.setItems(resp.items)

            when {
                !mostrandoTodo && resp.hayMas -> {
                    btnVerMas.visibility = View.VISIBLE
                    btnVerMas.text       = "Ver todos (${resp.total - limite} más) ▼"
                }
                mostrandoTodo && resp.total > LIMITE_INICIAL -> {
                    btnVerMas.visibility = View.VISIBLE
                    btnVerMas.text       = "Ver menos ▲"
                }
                else -> btnVerMas.visibility = View.GONE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}