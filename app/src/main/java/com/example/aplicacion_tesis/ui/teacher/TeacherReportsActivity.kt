package com.example.aplicacion_tesis.ui.teacher

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aplicacion_tesis.R
import com.example.aplicacion_tesis.model.dto.ProgresoPorCompetenciaItemDTO
import com.example.aplicacion_tesis.model.dto.TeacherActivityItem
import com.example.aplicacion_tesis.model.dto.TeacherStudentItem
import com.example.aplicacion_tesis.network.RetrofitClient
import com.example.aplicacion_tesis.network.TokenStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TeacherReportsActivity : AppCompatActivity() {

    private enum class TeacherTab { INICIO, INFORMES, ALERTAS, PERFIL }

    private lateinit var tabTeacherInicio: LinearLayout
    private lateinit var tabTeacherInformes: LinearLayout
    private lateinit var tabTeacherAlertas: LinearLayout
    private lateinit var tvTabTeacherInicio: TextView
    private lateinit var tvTabTeacherInformes: TextView
    private lateinit var tvTabTeacherAlertas: TextView
    private lateinit var indicatorTeacherInicio: View
    private lateinit var indicatorTeacherInformes: View
    private lateinit var indicatorTeacherAlertas: View

    private lateinit var etSearchStudent: EditText
    private lateinit var rvStudents: RecyclerView
    private lateinit var tvReportTitleName: TextView
    private lateinit var layoutInformeEstudiante: LinearLayout
    private lateinit var tvPuntajeGeneral: TextView
    private lateinit var tvTemasCompletados: TextView

    // ✅ Pie chart + spinner en vez de barras
    private lateinit var chartPieCompetencia: com.github.mikephil.charting.charts.PieChart
    private lateinit var spinnerCompetencia: android.widget.Spinner
    private lateinit var tvPieDetalle: TextView
    private var competenciasActuales: List<ProgresoPorCompetenciaItemDTO> = emptyList()

    private lateinit var tvFrecEjercicios: TextView
    private lateinit var tvFrecMateriales: TextView
    private lateinit var tvFrecTotal: TextView
    private lateinit var tvFrecUltimaActividad: TextView

    private lateinit var tvMatTotalMateriales: TextView
    private lateinit var tvMatTiempoTotal: TextView
    private lateinit var tvMatCompletados: TextView
    private lateinit var tvMatFrecuencia: TextView

    private lateinit var rvActivityHistory: RecyclerView
    private lateinit var tvHistorialEmpty: TextView
    private lateinit var tvVerMasHistorial: TextView
    private lateinit var tvFilterOptions: TextView
    private lateinit var historyAdapter: TeacherActivityAdapter

    private lateinit var adapter: TeacherReportStudentAdapter
    private var allStudents: List<TeacherStudentItem> = emptyList()
    private var filteredStudents: List<TeacherStudentItem> = emptyList()
    private var currentStudent: TeacherStudentItem? = null

    private var historialCompleto: List<TeacherActivityItem> = emptyList()
    private var mostrandoTodoHistorial = false
    private val HISTORIAL_LIMITE = 5
    private val ESTUDIANTES_VISIBLES = 4
    private var mostrandoTodosEstudiantes = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_reports)
        bindViews()
        setupTabs()
        setupStudentsRecycler()
        setupHistoryRecycler()
        setupSearch()
        setupButtons()
        loadTeacherStudents()
    }

    private fun bindViews() {
        tabTeacherInicio     = findViewById(R.id.tabTeacherInicio)
        tabTeacherInformes   = findViewById(R.id.tabTeacherInformes)
        tabTeacherAlertas    = findViewById(R.id.tabTeacherAlertas)
        tvTabTeacherInicio   = findViewById(R.id.tvTabTeacherInicio)
        tvTabTeacherInformes = findViewById(R.id.tvTabTeacherInformes)
        tvTabTeacherAlertas  = findViewById(R.id.tvTabTeacherAlertas)
        indicatorTeacherInicio   = findViewById(R.id.indicatorTeacherInicio)
        indicatorTeacherInformes = findViewById(R.id.indicatorTeacherInformes)
        indicatorTeacherAlertas  = findViewById(R.id.indicatorTeacherAlertas)

        etSearchStudent         = findViewById(R.id.etSearchStudent)
        rvStudents              = findViewById(R.id.rvStudents)
        tvReportTitleName       = findViewById(R.id.tvReportTitleName)
        layoutInformeEstudiante = findViewById(R.id.layoutInformeEstudiante)
        tvPuntajeGeneral        = findViewById(R.id.tvPuntajeGeneral)
        tvTemasCompletados      = findViewById(R.id.tvTemasCompletados)

        // ✅ Pie chart + spinner
        chartPieCompetencia = findViewById(R.id.chartPieCompetencia)
        spinnerCompetencia  = findViewById(R.id.spinnerCompetencia)
        tvPieDetalle        = findViewById(R.id.tvPieDetalle)

        tvFrecEjercicios      = findViewById(R.id.tvFrecEjercicios)
        tvFrecMateriales      = findViewById(R.id.tvFrecMateriales)
        tvFrecTotal           = findViewById(R.id.tvFrecTotal)
        tvFrecUltimaActividad = findViewById(R.id.tvFrecUltimaActividad)

        tvMatTotalMateriales = findViewById(R.id.tvMatTotalMateriales)
        tvMatTiempoTotal     = findViewById(R.id.tvMatTiempoTotal)
        tvMatCompletados     = findViewById(R.id.tvMatCompletados)
        tvMatFrecuencia      = findViewById(R.id.tvMatFrecuencia)

        rvActivityHistory = findViewById(R.id.rvActivityHistory)
        tvHistorialEmpty  = findViewById(R.id.tvHistorialEmpty)
        tvVerMasHistorial = findViewById(R.id.tvVerMasHistorial)
        tvFilterOptions   = findViewById(R.id.ivFilterOptions)


        try {
            findViewById<LinearLayout>(R.id.tabTeacherPerfil)
                ?.setOnClickListener {
                    startActivity(Intent(this, TeacherProfileActivity::class.java))
                }
        } catch (_: Exception) {}
    }

    private fun setupTabs() {
        selectTeacherTab(TeacherTab.INFORMES)
        tabTeacherInicio.setOnClickListener {
            startActivity(Intent(this, TeacherHomeActivity::class.java))
            finish()
        }
        tabTeacherInformes.setOnClickListener { selectTeacherTab(TeacherTab.INFORMES) }
        tabTeacherAlertas.setOnClickListener {
            startActivity(Intent(this, TeacherAlertsActivity::class.java))
            finish()
        }

        try {
            findViewById<LinearLayout>(R.id.tabTeacherPerfil)?.setOnClickListener {
                startActivity(Intent(this, TeacherProfileActivity::class.java))
            }
        } catch (_: Exception) {}
    }

    private fun setupButtons() {
        tvFilterOptions.setOnClickListener {
            val student = currentStudent
            if (student == null) {
                Toast.makeText(this, "Primero selecciona un estudiante.", Toast.LENGTH_SHORT).show()
            } else {
                abrirReporteCompleto(student)
            }
        }
        tvVerMasHistorial.setOnClickListener {
            mostrandoTodoHistorial = !mostrandoTodoHistorial
            historyAdapter.updateItems(
                if (mostrandoTodoHistorial) historialCompleto
                else historialCompleto.take(HISTORIAL_LIMITE)
            )
            tvVerMasHistorial.text = if (mostrandoTodoHistorial) "Ver menos ▲" else "Ver más actividad ▼"
        }
    }

    private fun abrirReporteCompleto(student: TeacherStudentItem) {
        startActivity(Intent(this, TeacherStudentReportActivity::class.java).apply {
            putExtra("student_id",       student.idEstudiante)
            putExtra("student_name",     student.nombre)
            putExtra("student_progress", student.progreso)
        })
    }

    private fun setupStudentsRecycler() {
        adapter = TeacherReportStudentAdapter(emptyList()) { student ->
            showStudentReport(student)
        }
        rvStudents.layoutManager = LinearLayoutManager(this)
        rvStudents.adapter = adapter
    }

    private fun setupHistoryRecycler() {
        historyAdapter = TeacherActivityAdapter()
        rvActivityHistory.layoutManager = LinearLayoutManager(this)
        rvActivityHistory.adapter = historyAdapter
    }

    private fun setupSearch() {
        etSearchStudent.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilter(s?.toString()?.trim() ?: "")
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadTeacherStudents() {
        val idDocente = TokenStore.teacherId
        if (idDocente == null || idDocente <= 0) {
            Toast.makeText(this, "No se encontró el ID del docente.", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val resp = RetrofitClient.docenteApi.getStudentsByTeacher(idDocente)
                if (resp.status && !resp.data.isNullOrEmpty()) {
                    allStudents      = resp.data
                    filteredStudents = allStudents
                    withContext(Dispatchers.Main) { mostrarEstudiantes(filteredStudents) }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@TeacherReportsActivity,
                            "No se encontraron estudiantes.", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@TeacherReportsActivity,
                        "Error al cargar estudiantes", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun mostrarEstudiantes(lista: List<TeacherStudentItem>) {
        val tvVerTodos = try { findViewById<TextView>(R.id.tvVerTodosEstudiantes) }
        catch (_: Exception) { null }

        if (lista.size <= ESTUDIANTES_VISIBLES) {
            adapter.updateItems(lista)
            tvVerTodos?.visibility = View.GONE
        } else {
            adapter.updateItems(lista.take(ESTUDIANTES_VISIBLES))
            tvVerTodos?.visibility = View.VISIBLE
            tvVerTodos?.text = "Ver todos (${lista.size}) ▼"
            tvVerTodos?.setOnClickListener {
                mostrandoTodosEstudiantes = !mostrandoTodosEstudiantes
                if (mostrandoTodosEstudiantes) {
                    adapter.updateItems(lista)
                    tvVerTodos.text = "Ver menos ▲"
                } else {
                    adapter.updateItems(lista.take(ESTUDIANTES_VISIBLES))
                    tvVerTodos.text = "Ver todos (${lista.size}) ▼"
                }
            }
        }
    }

    private fun applyFilter(query: String) {
        filteredStudents = if (query.isBlank()) allStudents
        else allStudents.filter { it.nombre.contains(query, ignoreCase = true) }
        adapter.updateItems(filteredStudents)
    }

    private fun showStudentReport(student: TeacherStudentItem) {
        currentStudent = student
        tvReportTitleName.text = "Informe del estudiante\n\"${student.nombre}\""
        layoutInformeEstudiante.visibility = View.VISIBLE
        mostrandoTodoHistorial = false
        tvVerMasHistorial.visibility = View.GONE

        tvFrecEjercicios.text      = "..."
        tvFrecMateriales.text      = "..."
        tvFrecTotal.text           = "..."
        tvFrecUltimaActividad.text = "Última actividad: cargando..."
        tvMatTotalMateriales.text  = "..."
        tvMatTiempoTotal.text      = "..."
        tvMatCompletados.text      = "..."
        tvMatFrecuencia.text       = "Último acceso: cargando..."

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val compResponse = RetrofitClient.progresoApi
                    .getPorCompetencia(idEstudiante = student.idEstudiante)
                val temas: List<ProgresoPorCompetenciaItemDTO> =
                    if (compResponse.status) compResponse.temas ?: emptyList() else emptyList()

                val resumen = RetrofitClient.progresoApi
                    .getResumen(idEstudiante = student.idEstudiante)
                val progresoReal = if (resumen.status) resumen.nivelPorcentaje else student.progreso

                val histResponse = RetrofitClient.progresoApi
                    .getHistorial(idEstudiante = student.idEstudiante)
                val histItems: List<TeacherActivityItem> =
                    if (histResponse.status && !histResponse.items.isNullOrEmpty()) {
                        histResponse.items.map { item ->
                            val fechaFormateada = formatearFecha(item.fecha)
                            val estado   = item.estado?.ifBlank { "Sin datos" } ?: "Sin datos"
                            val intentos = item.intentosIncorrectos ?: 0
                            val detalle  = buildString {
                                if (fechaFormateada.isNotBlank()) append("$fechaFormateada · ")
                                append("Estado: $estado · Intentos: $intentos")
                            }
                            TeacherActivityItem(
                                titulo  = item.titulo ?: "Ejercicio",
                                detalle = detalle,
                                tipo    = "ejercicio"
                            )
                        }
                    } else emptyList()

                var ejercicios  = 0
                var materiales  = 0
                var total       = 0
                var ultimaActiv = "Sin actividad"
                val idDocente   = TokenStore.teacherId
                if (idDocente != null && idDocente > 0) {
                    try {
                        val frecResp = RetrofitClient.docenteApi.getFrecuenciaUso(idDocente)
                        frecResp.data?.firstOrNull { it.idEstudiante == student.idEstudiante }?.let {
                            ejercicios  = it.ejerciciosRespondidos
                            materiales  = it.materialesVistos
                            total       = it.totalInteracciones
                            ultimaActiv = it.ultimaActividad
                        }
                    } catch (_: Exception) {}
                }

                var totalMat       = 0
                var tiempoTotalMat = 0
                var completadosMat = 0
                var frecuenciaMat  = "Sin actividad"
                try {
                    val matResp = RetrofitClient.historialMaterialApi
                        .getHistorialMateriales(student.idEstudiante)
                    if (matResp.status && matResp.data != null) {
                        totalMat       = matResp.data.totalMateriales
                        tiempoTotalMat = matResp.data.totalTiempoVisto
                        completadosMat = matResp.data.materialesCompletados
                        frecuenciaMat  = matResp.data.detalle
                            .firstOrNull()?.fechaAcceso ?: "Sin datos"
                    }
                } catch (_: Exception) {}

                val ejFinal   = ejercicios
                val matFinal  = materiales
                val totFinal  = total
                val ultFinal  = ultimaActiv
                val progFinal = progresoReal
                val matTotal  = totalMat
                val matTiempo = tiempoTotalMat
                val matComp   = completadosMat
                val matFrec   = frecuenciaMat

                withContext(Dispatchers.Main) {
                    tvPuntajeGeneral.text = "$progFinal%"
                    val completados = temas.count { it.porcentaje >= 70 }
                    tvTemasCompletados.text = "$completados/${temas.size}"
                    // ✅ Pie chart en vez de barras
                    updateCompetenciasBars(temas)

                    tvFrecEjercicios.text      = ejFinal.toString()
                    tvFrecMateriales.text      = matFinal.toString()
                    tvFrecTotal.text           = totFinal.toString()
                    tvFrecUltimaActividad.text = "Última actividad: $ultFinal"

                    tvMatTotalMateriales.text = matTotal.toString()
                    tvMatTiempoTotal.text     = formatearTiempo(matTiempo)
                    tvMatCompletados.text     = matComp.toString()
                    tvMatFrecuencia.text      = "Último acceso: $matFrec"

                    historialCompleto = histItems
                    if (histItems.isEmpty()) {
                        tvHistorialEmpty.visibility  = View.VISIBLE
                        rvActivityHistory.visibility = View.GONE
                        tvVerMasHistorial.visibility = View.GONE
                    } else {
                        tvHistorialEmpty.visibility  = View.GONE
                        rvActivityHistory.visibility = View.VISIBLE
                        historyAdapter.updateItems(histItems.take(HISTORIAL_LIMITE))
                        tvVerMasHistorial.visibility =
                            if (histItems.size > HISTORIAL_LIMITE) View.VISIBLE else View.GONE
                        tvVerMasHistorial.text = "Ver más actividad ▼"
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@TeacherReportsActivity,
                        "Error al cargar reporte: ${e.localizedMessage}",
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // ✅ Reemplaza barras por pie chart + spinner
    private fun updateCompetenciasBars(temas: List<ProgresoPorCompetenciaItemDTO>) {
        competenciasActuales = temas
        setupSpinnerCompetencias(temas)
        if (temas.isNotEmpty()) actualizarPieChart(temas[0])
    }

    private fun setupSpinnerCompetencias(temas: List<ProgresoPorCompetenciaItemDTO>) {
        val nombres = temas.map { abreviarNombre(it.nombre) }
        val spinnerAdapter = android.widget.ArrayAdapter(
            this, android.R.layout.simple_spinner_item, nombres
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        spinnerCompetencia.adapter = spinnerAdapter
        spinnerCompetencia.onItemSelectedListener = object :
            android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: android.widget.AdapterView<*>?, view: android.view.View?, pos: Int, id: Long
            ) { if (pos < temas.size) actualizarPieChart(temas[pos]) }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun abreviarNombre(nombre: String): String = when {
        "cantidad"    in nombre.lowercase() -> "Cantidad"
        "regularidad" in nombre.lowercase() -> "Regularidad / Equivalencia"
        "forma"       in nombre.lowercase() -> "Forma / Movimiento"
        "datos"       in nombre.lowercase() -> "Gestión de Datos"
        else -> nombre.take(30)
    }

    private fun actualizarPieChart(tema: ProgresoPorCompetenciaItemDTO) {
        val porcentaje = tema.porcentaje.coerceIn(0, 100)
        val pendiente  = 100 - porcentaje

        val colorLogrado = when {
            porcentaje >= 70 -> Color.parseColor("#27AE60")
            porcentaje >= 40 -> Color.parseColor("#F39C12")
            else             -> Color.parseColor("#E74C3C")
        }

        val entries = listOf(
            com.github.mikephil.charting.data.PieEntry(porcentaje.toFloat(), ""),
            com.github.mikephil.charting.data.PieEntry(pendiente.toFloat(),  "")
        )

        val dataSet = com.github.mikephil.charting.data.PieDataSet(entries, "").apply {
            colors     = listOf(colorLogrado, Color.parseColor("#E8E8E8"))
            setDrawValues(false)
            sliceSpace = 3f
        }

        chartPieCompetencia.apply {
            data                    = com.github.mikephil.charting.data.PieData(dataSet)
            description.isEnabled   = false
            isDrawHoleEnabled       = true
            holeRadius              = 58f
            transparentCircleRadius = 62f
            setHoleColor(Color.WHITE)
            setDrawCenterText(true)
            centerText              = "$porcentaje%"
            setCenterTextSize(28f)
            setCenterTextColor(colorLogrado)
            setCenterTextTypeface(android.graphics.Typeface.DEFAULT_BOLD)
            legend.isEnabled        = false
            setTouchEnabled(false)
            animateY(700)
            invalidate()
        }

        val (nivel, descripcion) = when {
            porcentaje >= 70 -> "✅ Logrado" to "Supera el mínimo MINEDU"
            porcentaje >= 40 -> "⚡ En Proceso" to "Necesita refuerzo"
            else             -> "⚠ Inicio" to "Requiere atención inmediata"
        }
        tvPieDetalle.text = "$nivel  ·  $descripcion"
        tvPieDetalle.setTextColor(colorLogrado)
    }

    private fun formatearTiempo(segundos: Int): String = when {
        segundos <= 0   -> "0 min"
        segundos < 60   -> "$segundos seg"
        segundos < 3600 -> "${segundos / 60} min"
        else            -> "${segundos / 3600}h ${(segundos % 3600) / 60}min"
    }

    private fun formatearFecha(fecha: String?): String {
        if (fecha.isNullOrBlank()) return ""
        return try {
            val regex = Regex("""\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}""")
            val match  = regex.find(fecha) ?: return fecha
            val (fechaParte, horaParte) = match.value.split("T")
            val (anio, mes, dia) = fechaParte.split("-")
            val meses = listOf("","Ene","Feb","Mar","Abr","May","Jun",
                "Jul","Ago","Sep","Oct","Nov","Dic")
            "$dia ${meses[mes.toInt()]} $anio · ${horaParte.substring(0, 5)}"
        } catch (_: Exception) { fecha }
    }

    private fun selectTeacherTab(tab: TeacherTab) {
        fun activate(tv: TextView, indicator: View, active: Boolean) {
            tv.setTextColor(ContextCompat.getColor(this,
                if (active) R.color.ai_primary else R.color.ai_text_muted))
            tv.setTypeface(null, if (active) Typeface.BOLD else Typeface.NORMAL)
            indicator.visibility = if (active) View.VISIBLE else View.GONE
        }
        activate(tvTabTeacherInicio,   indicatorTeacherInicio,   tab == TeacherTab.INICIO)
        activate(tvTabTeacherInformes, indicatorTeacherInformes, tab == TeacherTab.INFORMES)
        activate(tvTabTeacherAlertas,  indicatorTeacherAlertas,  tab == TeacherTab.ALERTAS)
    }
}