package com.example.aplicacion_tesis.ui.teacher

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aplicacion_tesis.R
import com.example.aplicacion_tesis.model.dto.AlertSeverity
import com.example.aplicacion_tesis.model.dto.TeacherAlertItem
import com.example.aplicacion_tesis.model.dto.TeacherStudentItem
import com.example.aplicacion_tesis.network.RetrofitClient
import com.example.aplicacion_tesis.network.TokenStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TeacherAlertsActivity : AppCompatActivity() {

    private enum class TeacherTab { INICIO, INFORMES, ALERTAS, PERFIL }

    private lateinit var tabTeacherInicio: LinearLayout
    private lateinit var tabTeacherInformes: LinearLayout
    private lateinit var tabTeacherAlertas: LinearLayout
    private lateinit var tabTeacherPerfil: LinearLayout

    private lateinit var tvTabTeacherInicio: TextView
    private lateinit var tvTabTeacherInformes: TextView
    private lateinit var tvTabTeacherAlertas: TextView
    private lateinit var tvTabTeacherPerfil: TextView

    private lateinit var indicatorTeacherInicio: View
    private lateinit var indicatorTeacherInformes: View
    private lateinit var indicatorTeacherAlertas: View
    private lateinit var indicatorTeacherPerfil: View

    private lateinit var rvAlerts: RecyclerView
    private lateinit var tvAlertsEmpty: View
    private lateinit var alertsAdapter: TeacherAlertAdapter
    private val alertasActivas = mutableListOf<TeacherAlertItem>()

    private var allStudents: List<TeacherStudentItem> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_alerts)

        bindViews()
        setupTabs()
        setupAlertsList()
        loadRealAlerts()
    }

    private fun bindViews() {
        tabTeacherInicio    = findViewById(R.id.tabTeacherInicio)
        tabTeacherInformes  = findViewById(R.id.tabTeacherInformes)
        tabTeacherAlertas   = findViewById(R.id.tabTeacherAlertas)
        tabTeacherPerfil    = findViewById(R.id.tabTeacherPerfil)

        tvTabTeacherInicio   = findViewById(R.id.tvTabTeacherInicio)
        tvTabTeacherInformes = findViewById(R.id.tvTabTeacherInformes)
        tvTabTeacherAlertas  = findViewById(R.id.tvTabTeacherAlertas)
        tvTabTeacherPerfil   = findViewById(R.id.tvTabTeacherPerfil)

        indicatorTeacherInicio   = findViewById(R.id.indicatorTeacherInicio)
        indicatorTeacherInformes = findViewById(R.id.indicatorTeacherInformes)
        indicatorTeacherAlertas  = findViewById(R.id.indicatorTeacherAlertas)
        indicatorTeacherPerfil   = findViewById(R.id.indicatorTeacherPerfil)

        rvAlerts      = findViewById(R.id.rvTeacherAlerts)
        tvAlertsEmpty = findViewById(R.id.tvAlertsEmpty)

        val imgInfo: View = findViewById(R.id.imgTeacherInfo)
        imgInfo.isClickable = false
        imgInfo.isFocusable = false
    }

    private fun setupTabs() {
        selectTab(TeacherTab.ALERTAS)

        tabTeacherInicio.setOnClickListener {
            startActivity(Intent(this, TeacherHomeActivity::class.java))
            finish()
        }
        tabTeacherInformes.setOnClickListener {
            startActivity(Intent(this, TeacherReportsActivity::class.java))
            finish()
        }
        tabTeacherAlertas.setOnClickListener { selectTab(TeacherTab.ALERTAS) }
        tabTeacherPerfil.setOnClickListener {
            startActivity(Intent(this, TeacherProfileActivity::class.java))
        }
    }

    private fun setupAlertsList() {
        alertsAdapter = TeacherAlertAdapter { item ->
            mostrarDialogoSeguimiento(item)
        }
        rvAlerts.layoutManager = LinearLayoutManager(this)
        rvAlerts.adapter = alertsAdapter
    }

    private fun showAlerts(items: List<TeacherAlertItem>) {
        if (items.isEmpty()) {
            tvAlertsEmpty.visibility = View.VISIBLE
            rvAlerts.visibility      = View.GONE
        } else {
            tvAlertsEmpty.visibility = View.GONE
            rvAlerts.visibility      = View.VISIBLE
            alertsAdapter.updateItems(items)
        }
    }

    private fun mostrarDialogoSeguimiento(item: TeacherAlertItem) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_alerta_seguimiento, null)

        val dialog = android.app.Dialog(this)
        dialog.setContentView(dialogView)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.88).toInt(),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(true)

        // ✅ Explicación clara del nivel de severidad
        val (chipColor, nivelTexto, nivelExplicacion) = when (item.severidad) {
            AlertSeverity.Critica     -> Triple(
                getColor(R.color.ai_error),
                "🔴 CRÍTICA",
                "El estudiante tiene 4 o más respuestas incorrectas recientes. Requiere atención inmediata."
            )
            AlertSeverity.Advertencia -> Triple(
                getColor(R.color.ai_warning),
                "🟡 ADVERTENCIA",
                "El estudiante tiene 2-3 respuestas incorrectas recientes. Monitorear de cerca."
            )
            AlertSeverity.Info        -> Triple(
                getColor(R.color.ai_primary),
                "🔵 INFORMATIVA",
                "Situación a tener en cuenta. Bajo rendimiento en alguna competencia específica."
            )
        }

        // Título del diálogo
        dialogView.findViewById<TextView>(R.id.dialogTitle)?.apply {
            text = nivelTexto
            setTextColor(chipColor)
        }

        dialogView.findViewById<TextView>(R.id.dialogStudentName).apply {
            text = item.titulo.removePrefix("⚠ ")
            setTextColor(chipColor)
        }

        // Descripción + explicación del nivel
        dialogView.findViewById<TextView>(R.id.dialogDescription).text =
            "${item.descripcion}\n\n${nivelExplicacion}"

        dialogView.findViewById<TextView>(R.id.dialogFecha).text =
            "📅 Última actividad: ${item.fecha}"

        dialogView.findViewById<TextView>(R.id.btnAtendida).setOnClickListener {
            alertsAdapter.removeItem(item)
            alertasActivas.remove(item)
            if (alertasActivas.isEmpty()) {
                tvAlertsEmpty.visibility = View.VISIBLE
                rvAlerts.visibility      = View.GONE
            }
            Toast.makeText(this, "✅ Alerta marcada como atendida", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialogView.findViewById<TextView>(R.id.btnCancelar).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun nombreCompetenciaOficial(idCompetencia: Int?): String {
        return when (idCompetencia) {
            1    -> "Resuelve problemas de cantidad"
            2    -> "Resuelve problemas de regularidad, equivalencia y cambio"
            3    -> "Resuelve problemas de forma, movimiento y localización"
            4    -> "Resuelve problemas de gestión de datos e incertidumbre"
            else -> "Competencia $idCompetencia"
        }
    }

    private fun loadRealAlerts() {
        lifecycleScope.launch(Dispatchers.IO) {

            val idDocente = TokenStore.teacherId ?: run {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@TeacherAlertsActivity,
                        "No se encontró la sesión del docente",
                        Toast.LENGTH_SHORT
                    ).show()
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
                    // ✅ Ordenar: primero por severidad (crítica→advertencia→info),
                    //            luego por fecha más reciente primero
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
                    Toast.makeText(
                        this@TeacherAlertsActivity,
                        "Error al cargar alertas: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun selectTab(tab: TeacherTab) {
        fun activate(tv: TextView, indicator: View, active: Boolean) {
            if (active) {
                tv.setTextColor(ContextCompat.getColor(this, R.color.ai_primary))
                tv.setTypeface(null, Typeface.BOLD)
                indicator.visibility = View.VISIBLE
            } else {
                tv.setTextColor(ContextCompat.getColor(this, R.color.ai_text_muted))
                tv.setTypeface(null, Typeface.NORMAL)
                indicator.visibility = View.GONE
            }
        }

        activate(tvTabTeacherInicio,   indicatorTeacherInicio,   tab == TeacherTab.INICIO)
        activate(tvTabTeacherInformes, indicatorTeacherInformes, tab == TeacherTab.INFORMES)
        activate(tvTabTeacherAlertas,  indicatorTeacherAlertas,  tab == TeacherTab.ALERTAS)
        activate(tvTabTeacherPerfil,   indicatorTeacherPerfil,   tab == TeacherTab.PERFIL)
    }
}