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
import com.example.aplicacion_tesis.R
import com.example.aplicacion_tesis.databinding.ActivityTeacherHomeBinding
import com.example.aplicacion_tesis.model.dto.TeacherDashboardData
import com.example.aplicacion_tesis.network.RetrofitClient
import com.example.aplicacion_tesis.network.TokenStore
import com.example.aplicacion_tesis.ui.login.LoginActivity
import kotlinx.coroutines.launch

class TeacherHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeacherHomeBinding
    private enum class TeacherTab { INICIO, INFORMES, ALERTAS, PERFIL }

    // ✅ Vistas de frecuencia
    private lateinit var cardFrecuencia1: LinearLayout
    private lateinit var cardFrecuencia2: LinearLayout
    private lateinit var cardFrecuencia3: LinearLayout
    private lateinit var tvFrecuencia1Nombre: TextView
    private lateinit var tvFrecuencia1Detalle: TextView
    private lateinit var tvFrecuencia1Total: TextView
    private lateinit var tvFrecuencia2Nombre: TextView
    private lateinit var tvFrecuencia2Detalle: TextView
    private lateinit var tvFrecuencia2Total: TextView
    private lateinit var tvFrecuencia3Nombre: TextView
    private lateinit var tvFrecuencia3Detalle: TextView
    private lateinit var tvFrecuencia3Total: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // SALUDO
        val rawName   = TokenStore.userName.orEmpty()
        val cleanName = rawName
            .replace("Estudiante", "", ignoreCase = true)
            .replace("Alumno", "", ignoreCase = true)
            .trim()
            .ifBlank { "Profesor" }
        binding.tvGreeting.text = "¡Hola, $cleanName!"

        // ✅ Inicializar vistas de frecuencia
        bindFrecuenciaViews()

        // TABS
        binding.tabTeacherInicio.setOnClickListener {
            selectTeacherTab(TeacherTab.INICIO)
        }
        binding.tabTeacherInformes.setOnClickListener {
            selectTeacherTab(TeacherTab.INFORMES)
            startActivity(Intent(this, TeacherReportsActivity::class.java))
            finish()
        }
        binding.tabTeacherAlertas.setOnClickListener {
            selectTeacherTab(TeacherTab.ALERTAS)
            startActivity(Intent(this, TeacherAlertsActivity::class.java))
            finish()
        }
        binding.tabTeacherPerfil.setOnClickListener {
            selectTeacherTab(TeacherTab.PERFIL)
            startActivity(Intent(this, TeacherProfileActivity::class.java))
        }

        selectTeacherTab(TeacherTab.INICIO)

        binding.btnVerInformes.setOnClickListener {
            startActivity(Intent(this, TeacherReportsActivity::class.java))
            finish()
        }

        // ✅ "Ver informes ›" en actividad reciente
        try {
            val tvVerToda = findViewById<TextView>(R.id.tvVerTodaActividad)
            tvVerToda?.setOnClickListener {
                startActivity(Intent(this, TeacherReportsActivity::class.java))
                finish()
            }
        } catch (_: Exception) {}

        val idDocente = TokenStore.teacherId
        if (idDocente == null || idDocente <= 0) {
            Toast.makeText(this, "No se encontró el ID del docente.", Toast.LENGTH_LONG).show()
            return
        }

        loadDashboard(idDocente)
        loadFrecuenciaUso(idDocente)  // ✅ Carga el ranking de frecuencia
    }

    override fun onStart() {
        super.onStart()
        val idUsuario = TokenStore.userId
        if (idUsuario == null || idUsuario <= 0) {
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
    }

    // =============================================
    // BIND VISTAS DE FRECUENCIA
    // =============================================
    private fun bindFrecuenciaViews() {
        try {
            cardFrecuencia1      = findViewById(R.id.cardFrecuencia1)
            cardFrecuencia2      = findViewById(R.id.cardFrecuencia2)
            cardFrecuencia3      = findViewById(R.id.cardFrecuencia3)
            tvFrecuencia1Nombre  = findViewById(R.id.tvFrecuencia1Nombre)
            tvFrecuencia1Detalle = findViewById(R.id.tvFrecuencia1Detalle)
            tvFrecuencia1Total   = findViewById(R.id.tvFrecuencia1Total)
            tvFrecuencia2Nombre  = findViewById(R.id.tvFrecuencia2Nombre)
            tvFrecuencia2Detalle = findViewById(R.id.tvFrecuencia2Detalle)
            tvFrecuencia2Total   = findViewById(R.id.tvFrecuencia2Total)
            tvFrecuencia3Nombre  = findViewById(R.id.tvFrecuencia3Nombre)
            tvFrecuencia3Detalle = findViewById(R.id.tvFrecuencia3Detalle)
            tvFrecuencia3Total   = findViewById(R.id.tvFrecuencia3Total)

            cardFrecuencia1.visibility = View.GONE
            cardFrecuencia2.visibility = View.GONE
            cardFrecuencia3.visibility = View.GONE
        } catch (_: Exception) {}
    }

    // =============================================
    // CARGAR DASHBOARD PRINCIPAL
    // =============================================
    private fun loadDashboard(idDocente: Int) {
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.docenteApi.getDashboard(idDocente)

                if (resp.status && resp.data != null) {
                    val data: TeacherDashboardData = resp.data

                    binding.tvEstudiantesActivos.text = data.estudiantesActivos.toString()
                    binding.tvProgresoPromedio.text   = String.format("%.1f%%", data.progresoPromedio)
                    binding.tvTemaDificil.text        = data.temaMasDificultad ?: "--"

                    val lista = data.actividadReciente ?: emptyList()

                    fun setCard(
                        index: Int,
                        card: View,
                        tituloView: TextView,
                        tiempoView: TextView
                    ) {
                        if (index < lista.size) {
                            val item = lista[index]
                            card.visibility   = View.VISIBLE
                            tituloView.text   = "${item.nombreEstudiante} ha ${item.tipo} \"${item.tema}\""
                            tiempoView.text   = item.fecha.orEmpty()
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
                    Toast.makeText(this@TeacherHomeActivity,
                        resp.message ?: "No se pudo cargar el panel.",
                        Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@TeacherHomeActivity,
                    "Error al cargar dashboard: ${e.localizedMessage}",
                    Toast.LENGTH_LONG).show()
            }
        }
    }

    // =============================================
    // ✅ CARGAR RANKING DE FRECUENCIA DE USO
    // =============================================
    private fun loadFrecuenciaUso(idDocente: Int) {
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.docenteApi.getFrecuenciaUso(idDocente)
                val tvVacia = try { findViewById<TextView>(R.id.tvFrecuenciaVacia) } catch (_: Exception) { null }
                val div1 = try { findViewById<View>(R.id.divFrecuencia1) } catch (_: Exception) { null }
                val div2 = try { findViewById<View>(R.id.divFrecuencia2) } catch (_: Exception) { null }

                if (resp.status && !resp.data.isNullOrEmpty()) {
                    val ranking = resp.data
                    tvVacia?.visibility = View.GONE

                    fun llenarFila(
                        card: LinearLayout,
                        tvNombre: TextView,
                        tvDetalle: TextView,
                        tvTotal: TextView,
                        divider: View?,
                        index: Int
                    ) {
                        val item = ranking.getOrNull(index)
                        if (item != null) {
                            card.visibility    = View.VISIBLE
                            divider?.visibility = View.VISIBLE
                            tvNombre.text      = item.nombre
                            tvDetalle.text     = "${item.ejerciciosRespondidos} ejerc. · ${item.materialesVistos} mat. · ${item.ultimaActividad}"
                            tvTotal.text       = item.totalInteracciones.toString()
                        } else {
                            card.visibility    = View.GONE
                            divider?.visibility = View.GONE
                        }
                    }

                    llenarFila(cardFrecuencia1, tvFrecuencia1Nombre, tvFrecuencia1Detalle, tvFrecuencia1Total, null, 0)
                    llenarFila(cardFrecuencia2, tvFrecuencia2Nombre, tvFrecuencia2Detalle, tvFrecuencia2Total, div1, 1)
                    llenarFila(cardFrecuencia3, tvFrecuencia3Nombre, tvFrecuencia3Detalle, tvFrecuencia3Total, div2, 2)

                } else {
                    cardFrecuencia1.visibility = View.GONE
                    cardFrecuencia2.visibility = View.GONE
                    cardFrecuencia3.visibility = View.GONE
                    tvVacia?.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // =============================================
    // TABS
    // =============================================
    private fun selectTeacherTab(tab: TeacherTab) {
        fun activate(textView: TextView, indicator: View, active: Boolean) {
            textView.setTextColor(ContextCompat.getColor(this,
                if (active) R.color.ai_primary else R.color.ai_text_muted))
            textView.setTypeface(null, if (active) Typeface.BOLD else Typeface.NORMAL)
            indicator.visibility = if (active) View.VISIBLE else View.GONE
        }

        when (tab) {
            TeacherTab.INICIO -> {
                activate(binding.tvTabTeacherInicio,   binding.indicatorTeacherInicio,   true)
                activate(binding.tvTabTeacherInformes, binding.indicatorTeacherInformes, false)
                activate(binding.tvTabTeacherAlertas,  binding.indicatorTeacherAlertas,  false)
                activate(binding.tvTabTeacherPerfil,   binding.indicatorTeacherPerfil,   false)
            }
            TeacherTab.INFORMES -> {
                activate(binding.tvTabTeacherInicio,   binding.indicatorTeacherInicio,   false)
                activate(binding.tvTabTeacherInformes, binding.indicatorTeacherInformes, true)
                activate(binding.tvTabTeacherAlertas,  binding.indicatorTeacherAlertas,  false)
                activate(binding.tvTabTeacherPerfil,   binding.indicatorTeacherPerfil,   false)
            }
            TeacherTab.ALERTAS -> {
                activate(binding.tvTabTeacherInicio,   binding.indicatorTeacherInicio,   false)
                activate(binding.tvTabTeacherInformes, binding.indicatorTeacherInformes, false)
                activate(binding.tvTabTeacherAlertas,  binding.indicatorTeacherAlertas,  true)
                activate(binding.tvTabTeacherPerfil,   binding.indicatorTeacherPerfil,   false)
            }
            TeacherTab.PERFIL -> {
                activate(binding.tvTabTeacherInicio,   binding.indicatorTeacherInicio,   false)
                activate(binding.tvTabTeacherInformes, binding.indicatorTeacherInformes, false)
                activate(binding.tvTabTeacherAlertas,  binding.indicatorTeacherAlertas,  false)
                activate(binding.tvTabTeacherPerfil,   binding.indicatorTeacherPerfil,   true)
            }
        }
    }
}