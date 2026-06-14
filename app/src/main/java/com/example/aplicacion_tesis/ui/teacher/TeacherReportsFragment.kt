package com.example.aplicacion_tesis.ui.teacher

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aplicacion_tesis.databinding.FragmentTeacherReportsBinding
import com.example.aplicacion_tesis.model.dto.ProgresoPorCompetenciaItemDTO
import com.example.aplicacion_tesis.model.dto.TeacherActivityItem
import com.example.aplicacion_tesis.model.dto.TeacherStudentItem
import com.example.aplicacion_tesis.network.RetrofitClient
import com.example.aplicacion_tesis.network.TokenStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** F2: antes era TeacherReportsActivity. */
class TeacherReportsFragment : Fragment() {

    private var _binding: FragmentTeacherReportsBinding? = null
    private val binding get() = _binding!!

    private lateinit var historyAdapter: TeacherActivityAdapter
    private lateinit var adapter: TeacherReportStudentAdapter
    private var allStudents: List<TeacherStudentItem> = emptyList()
    private var filteredStudents: List<TeacherStudentItem> = emptyList()
    private var currentStudent: TeacherStudentItem? = null
    private var competenciasActuales: List<ProgresoPorCompetenciaItemDTO> = emptyList()

    private var historialCompleto: List<TeacherActivityItem> = emptyList()
    private var mostrandoTodoHistorial = false
    private val HISTORIAL_LIMITE = 5
    private val ESTUDIANTES_VISIBLES = 4
    private var mostrandoTodosEstudiantes = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTeacherReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupStudentsRecycler()
        setupHistoryRecycler()
        setupSearch()
        setupButtons()
        loadTeacherStudents()
    }

    private fun setupButtons() {
        binding.ivFilterOptions.setOnClickListener {
            val student = currentStudent
            if (student == null) {
                Toast.makeText(requireContext(), "Primero selecciona un estudiante.", Toast.LENGTH_SHORT).show()
            } else {
                abrirReporteCompleto(student)
            }
        }
        binding.tvVerMasHistorial.setOnClickListener {
            mostrandoTodoHistorial = !mostrandoTodoHistorial
            historyAdapter.updateItems(
                if (mostrandoTodoHistorial) historialCompleto
                else historialCompleto.take(HISTORIAL_LIMITE)
            )
            binding.tvVerMasHistorial.text =
                if (mostrandoTodoHistorial) "Ver menos ▲" else "Ver más actividad ▼"
        }
    }

    private fun abrirReporteCompleto(student: TeacherStudentItem) {
        startActivity(Intent(requireContext(), TeacherStudentReportActivity::class.java).apply {
            putExtra("student_id",       student.idEstudiante)
            putExtra("student_name",     student.nombre)
            putExtra("student_progress", student.progreso)
        })
    }

    private fun setupStudentsRecycler() {
        adapter = TeacherReportStudentAdapter(emptyList()) { student ->
            showStudentReport(student)
        }
        binding.rvStudents.layoutManager = LinearLayoutManager(requireContext())
        binding.rvStudents.adapter = adapter
    }

    private fun setupHistoryRecycler() {
        historyAdapter = TeacherActivityAdapter()
        binding.rvActivityHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvActivityHistory.adapter = historyAdapter
    }

    private fun setupSearch() {
        binding.etSearchStudent.addTextChangedListener(object : TextWatcher {
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
            Toast.makeText(requireContext(), "No se encontró el ID del docente.", Toast.LENGTH_LONG).show()
            return
        }
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val resp = RetrofitClient.docenteApi.getStudentsByTeacher(idDocente)
                if (resp.status && !resp.data.isNullOrEmpty()) {
                    allStudents      = resp.data
                    filteredStudents = allStudents
                    withContext(Dispatchers.Main) { mostrarEstudiantes(filteredStudents) }
                } else {
                    withContext(Dispatchers.Main) {
                        if (isAdded) Toast.makeText(requireContext(),
                            "No se encontraron estudiantes.", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (isAdded) Toast.makeText(requireContext(),
                        "Error al cargar estudiantes", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun mostrarEstudiantes(lista: List<TeacherStudentItem>) {
        if (_binding == null) return
        val tvVerTodos = binding.tvVerTodosEstudiantes

        if (lista.size <= ESTUDIANTES_VISIBLES) {
            adapter.updateItems(lista)
            tvVerTodos.visibility = View.GONE
        } else {
            adapter.updateItems(lista.take(ESTUDIANTES_VISIBLES))
            tvVerTodos.visibility = View.VISIBLE
            tvVerTodos.text = "Ver todos (${lista.size}) ▼"
            tvVerTodos.setOnClickListener {
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
        binding.tvReportTitleName.text = "Informe del estudiante\n\"${student.nombre}\""
        binding.layoutInformeEstudiante.visibility = View.VISIBLE
        mostrandoTodoHistorial = false
        binding.tvVerMasHistorial.visibility = View.GONE

        binding.tvFrecEjercicios.text      = "..."
        binding.tvFrecMateriales.text      = "..."
        binding.tvFrecTotal.text           = "..."
        binding.tvFrecUltimaActividad.text = "Última actividad: cargando..."
        binding.tvMatTotalMateriales.text  = "..."
        binding.tvMatTiempoTotal.text      = "..."
        binding.tvMatCompletados.text      = "..."
        binding.tvMatFrecuencia.text       = "Último acceso: cargando..."

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
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
                    if (_binding == null) return@withContext
                    binding.tvPuntajeGeneral.text = "$progFinal%"
                    val completados = temas.count { it.porcentaje >= 70 }
                    binding.tvTemasCompletados.text = "$completados/${temas.size}"
                    updateCompetenciasBars(temas)

                    binding.tvFrecEjercicios.text      = ejFinal.toString()
                    binding.tvFrecMateriales.text      = matFinal.toString()
                    binding.tvFrecTotal.text           = totFinal.toString()
                    binding.tvFrecUltimaActividad.text = "Última actividad: $ultFinal"

                    binding.tvMatTotalMateriales.text = matTotal.toString()
                    binding.tvMatTiempoTotal.text     = formatearTiempo(matTiempo)
                    binding.tvMatCompletados.text     = matComp.toString()
                    binding.tvMatFrecuencia.text      = "Último acceso: $matFrec"

                    historialCompleto = histItems
                    if (histItems.isEmpty()) {
                        binding.tvHistorialEmpty.visibility  = View.VISIBLE
                        binding.rvActivityHistory.visibility = View.GONE
                        binding.tvVerMasHistorial.visibility = View.GONE
                    } else {
                        binding.tvHistorialEmpty.visibility  = View.GONE
                        binding.rvActivityHistory.visibility = View.VISIBLE
                        historyAdapter.updateItems(histItems.take(HISTORIAL_LIMITE))
                        binding.tvVerMasHistorial.visibility =
                            if (histItems.size > HISTORIAL_LIMITE) View.VISIBLE else View.GONE
                        binding.tvVerMasHistorial.text = "Ver más actividad ▼"
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    if (isAdded) Toast.makeText(requireContext(),
                        "Error al cargar reporte: ${e.localizedMessage}",
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun updateCompetenciasBars(temas: List<ProgresoPorCompetenciaItemDTO>) {
        competenciasActuales = temas
        setupSpinnerCompetencias(temas)
        if (temas.isNotEmpty()) actualizarPieChart(temas[0])
    }

    private fun setupSpinnerCompetencias(temas: List<ProgresoPorCompetenciaItemDTO>) {
        val nombres = temas.map { abreviarNombre(it.nombre) }
        val spinnerAdapter = android.widget.ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item, nombres
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        binding.spinnerCompetencia.adapter = spinnerAdapter
        binding.spinnerCompetencia.onItemSelectedListener = object :
            android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: android.widget.AdapterView<*>?, view: View?, pos: Int, id: Long
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

        binding.chartPieCompetencia.apply {
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

        val (nivel, descripcion, iconNivel) = when {
            porcentaje >= 70 -> Triple("Logrado",    "Supera el mínimo MINEDU",    R.drawable.ic_check_circle_24)
            porcentaje >= 40 -> Triple("En Proceso", "Necesita refuerzo",           R.drawable.ic_sync)
            else             -> Triple("Inicio",     "Requiere atención inmediata", R.drawable.ic_flag)
        }
        binding.tvPieDetalle.text = "$nivel  ·  $descripcion"
        binding.tvPieDetalle.setTextColor(colorLogrado)
        binding.tvPieDetalle.setCompoundDrawablesRelativeWithIntrinsicBounds(iconNivel, 0, 0, 0)
        binding.tvPieDetalle.compoundDrawablePadding = (4 * resources.displayMetrics.density).toInt()
        binding.tvPieDetalle.compoundDrawablesRelative[0]?.mutate()?.setTint(colorLogrado)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}