package com.example.aplicacion_tesis.ui.home.tabs

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.aplicacion_tesis.R
import com.example.aplicacion_tesis.model.dto.HistorialMaterialRequest
import com.example.aplicacion_tesis.model.dto.MaterialDTO
import com.example.aplicacion_tesis.network.RetrofitClient
import com.example.aplicacion_tesis.network.TokenStore
import kotlinx.coroutines.launch

class TemaDetalleActivity : AppCompatActivity() {

    private var idTema:            Int     = -1
    private var filtroNivel:       String  = "TODOS"
    private var matVistosInicial:  Int     = 0
    private var matTotal:          Int     = 0
    private var matVistosActual:   Int     = 0
    private var materialAbierto:   MaterialDTO? = null
    private var tiempoInicio:      Long    = 0L
    private var idEstudianteCache: Int?    = null

    private lateinit var tvToolbarTitulo:   TextView
    private lateinit var tvTituloTema:      TextView
    private lateinit var tvDescripcionTema: TextView
    private lateinit var tvProgreso:        TextView
    private lateinit var progressBarra:     ProgressBar
    private lateinit var layoutBasico:      LinearLayout
    private lateinit var layoutIntermedio:  LinearLayout
    private lateinit var layoutAvanzado:    LinearLayout
    private lateinit var tvContBasico:      TextView
    private lateinit var tvContInter:       TextView
    private lateinit var tvContAvanz:       TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tema_detalle)

        idTema           = intent.getIntExtra("ID_TEMA", -1)
        filtroNivel      = intent.getStringExtra("FILTRO_NIVEL") ?: "TODOS"
        matVistosInicial = intent.getIntExtra("MAT_VISTOS", 0)
        matTotal         = intent.getIntExtra("MAT_TOTAL", 0)
        matVistosActual  = matVistosInicial

        if (idTema <= 0) { finish(); return }

        tvToolbarTitulo   = findViewById(R.id.tvToolbarTitulo)
        tvTituloTema      = findViewById(R.id.tvTituloTema)
        tvDescripcionTema = findViewById(R.id.tvDescripcionTema)
        tvProgreso        = findViewById(R.id.tvProgresoDetalle)
        progressBarra     = findViewById(R.id.progressBarDetalle)
        layoutBasico      = findViewById(R.id.layoutSeccionBasico)
        layoutIntermedio  = findViewById(R.id.layoutSeccionIntermedio)
        layoutAvanzado    = findViewById(R.id.layoutSeccionAvanzado)
        tvContBasico      = findViewById(R.id.tvContBasico)
        tvContInter       = findViewById(R.id.tvContIntermedio)
        tvContAvanz       = findViewById(R.id.tvContAvanzado)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        actualizarProgreso()
        cargarDetalle()
    }

    // ✅ FIX: detectar regreso del material y registrar visita
    override fun onResume() {
        super.onResume()
        val mat = materialAbierto ?: return
        val tiempoVisto = ((System.currentTimeMillis() - tiempoInicio) / 1000).toInt()
        if (tiempoVisto < 2) return

        materialAbierto = null

        lifecycleScope.launch {
            try {
                val idEst = idEstudianteCache ?: obtenerIdEstudiante() ?: return@launch
                RetrofitClient.historialMaterialApi.registrarHistorial(
                    HistorialMaterialRequest(
                        idEstudiante  = idEst,
                        idMaterial    = mat.idMaterial,
                        estado        = if (tiempoVisto >= 240) "completado" else "visto",
                        tiempoVisto   = tiempoVisto,
                        vecesRevisado = 1
                    )
                )
                // ✅ Incrementar vistos localmente sin recargar todo
                matVistosActual++
                actualizarProgreso()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun actualizarProgreso() {
        val total = matTotal.takeIf { it > 0 } ?: 1
        val pct   = (matVistosActual * 100 / total).coerceIn(0, 100)
        tvProgreso.text       = "Progreso: $matVistosActual de $matTotal materiales vistos ($pct%)"
        progressBarra.progress = pct

        // Color barra según avance
        val color = when {
            pct >= 80 -> 0xFF27AE60.toInt()
            pct >= 40 -> 0xFFF5A623.toInt()
            else      -> 0xFF2980B9.toInt()
        }
        progressBarra.progressTintList =
            android.content.res.ColorStateList.valueOf(color)
    }

    private fun cargarDetalle() {
        lifecycleScope.launch {
            try {
                idEstudianteCache = obtenerIdEstudiante()
                val resp = RetrofitClient.dominioApi.obtenerDetalleTema(idTema)
                if (!resp.status || resp.data == null) return@launch

                val data = resp.data
                tvToolbarTitulo.text   = data.nombre ?: "Tema"
                tvTituloTema.text      = data.nombre ?: "Tema"
                tvDescripcionTema.text = data.descripcionTema ?: ""

                val materiales = data.materiales ?: emptyList()
                if (matTotal == 0) matTotal = materiales.size

                // ✅ Filtrar según nivel seleccionado en el tab principal
                val basicos     = materiales.filter { (it.nivel ?: 1) <= 1 }
                val intermedios = materiales.filter { it.nivel == 2 }
                val avanzados   = materiales.filter { it.nivel == 3 }

                when (filtroNivel) {
                    "BASICO" -> {
                        renderSeccion(layoutBasico,     basicos,     tvContBasico, "🟢 Básico")
                        layoutIntermedio.visibility = View.GONE
                        tvContInter.visibility      = View.GONE
                        layoutAvanzado.visibility   = View.GONE
                        tvContAvanz.visibility      = View.GONE
                    }
                    "INTERMEDIO" -> {
                        layoutBasico.visibility   = View.GONE
                        tvContBasico.visibility   = View.GONE
                        renderSeccion(layoutIntermedio, intermedios, tvContInter, "🟡 Intermedio")
                        layoutAvanzado.visibility = View.GONE
                        tvContAvanz.visibility    = View.GONE
                    }
                    "AVANZADO" -> {
                        layoutBasico.visibility     = View.GONE
                        tvContBasico.visibility     = View.GONE
                        layoutIntermedio.visibility = View.GONE
                        tvContInter.visibility      = View.GONE
                        renderSeccion(layoutAvanzado, avanzados, tvContAvanz, "🔴 Avanzado")
                    }
                    else -> {
                        // TODOS — mostrar las 3 secciones
                        renderSeccion(layoutBasico,     basicos,     tvContBasico, "🟢 Básico")
                        renderSeccion(layoutIntermedio, intermedios, tvContInter,  "🟡 Intermedio")
                        renderSeccion(layoutAvanzado,   avanzados,   tvContAvanz,  "🔴 Avanzado")
                    }
                }

            } catch (e: Exception) {
                Toast.makeText(this@TemaDetalleActivity, "Error al cargar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun renderSeccion(
        layout:  LinearLayout,
        items:   List<MaterialDTO>,
        tvCount: TextView,
        titulo:  String
    ) {
        layout.removeAllViews()
        if (items.isEmpty()) {
            layout.visibility = View.GONE
            tvCount.visibility = View.GONE
            return
        }
        layout.visibility  = View.VISIBLE
        tvCount.visibility = View.VISIBLE
        tvCount.text       = "$titulo  (${items.size})"
        items.forEach { m -> agregarItemMaterial(layout, m) }
    }

    private fun agregarItemMaterial(parent: LinearLayout, m: MaterialDTO) {
        val v = LayoutInflater.from(this)
            .inflate(R.layout.item_material, parent, false)

        val tvTitulo = v.findViewById<TextView>(R.id.tvTituloMaterial)
        val tvDesc   = v.findViewById<TextView>(R.id.tvDescripcionMaterial)

        tvTitulo.text = m.titulo ?: "Recurso"

        val tipo    = (m.tipo ?: "").uppercase()
        val minutos = m.tiempoEstimado ?: 0
        tvDesc.text = when (tipo) {
            "VIDEO"          -> "▶ Video · $minutos min"
            "PDF"            -> "📄 PDF · $minutos min"
            "LINK", "ENLACE" -> "🔗 Recurso en línea"
            else             -> "📖 Material"
        }

        v.setOnClickListener { abrirMaterial(m) }
        parent.addView(v)
    }

    private fun abrirMaterial(m: MaterialDTO) {
        val url = m.url
        if (url.isNullOrBlank()) {
            Toast.makeText(this, "URL no disponible", Toast.LENGTH_SHORT).show()
            return
        }
        materialAbierto = m
        tiempoInicio    = System.currentTimeMillis()
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    private suspend fun obtenerIdEstudiante(): Int? {
        TokenStore.studentId?.takeIf { it > 0 }?.let { return it }
        val idUsuario = TokenStore.userId?.takeIf { it > 0 } ?: return null
        val resp = RetrofitClient.estudianteApi.getEstudiantePorUsuario(idUsuario)
        if (resp.isSuccessful) {
            val body = resp.body()
            if (body?.status == true && body.data != null) {
                TokenStore.setStudentId(body.data.idEstudiante)
                return body.data.idEstudiante
            }
        }
        return null
    }
}