package com.example.aplicacion_tesis.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.aplicacion_tesis.R
import com.example.aplicacion_tesis.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistorialDetalleActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TITULO         = "extra_titulo"
        const val EXTRA_SUBTITULO      = "extra_subtitulo"
        const val EXTRA_ID_EJERCICIO   = "extra_id_ejercicio"
        const val EXTRA_DESARROLLO_URL = "extra_desarrollo_url"
        const val EXTRA_ID_COMPETENCIA = "extra_id_competencia"
        const val EXTRA_ES_INCORRECTO  = "extra_es_incorrecto"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial_detalle)

        val btnBack           = findViewById<ImageButton>(R.id.btnBackHistorial)
        val tvTitulo          = findViewById<TextView>(R.id.tvTituloDetalle)
        val ivResultado       = findViewById<ImageView>(R.id.ivResultadoIcono)
        val tvResultado       = findViewById<TextView>(R.id.tvResultadoEstado)
        val ivDesarrollo      = findViewById<ImageView>(R.id.ivDesarrollo)
        val btnAbrirPdf       = findViewById<TextView>(R.id.btnAbrirPdf)
        val cardImagen        = findViewById<View>(R.id.cardImagen)
        val cardPdf           = findViewById<View>(R.id.cardPdf)
        val cardSinDesarrollo = findViewById<View>(R.id.cardSinDesarrollo)
        val layoutRefuerzo    = findViewById<LinearLayout>(R.id.layoutMaterialRefuerzo)
        val tvCargando        = findViewById<TextView>(R.id.tvCargandoMaterial)
        val layoutMateriales  = findViewById<LinearLayout>(R.id.layoutMateriales)

        btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        val titulo        = intent.getStringExtra(EXTRA_TITULO)         ?: ""
        val estadoRaw     = intent.getStringExtra(EXTRA_SUBTITULO)      ?: ""
        val desarrolloUrl = intent.getStringExtra(EXTRA_DESARROLLO_URL) ?: ""
        val idCompetencia = intent.getIntExtra(EXTRA_ID_COMPETENCIA, 0)
        val esIncorrecto  = intent.getBooleanExtra(EXTRA_ES_INCORRECTO, false)

        android.util.Log.d("HistorialDetalle", "desarrolloUrl=$desarrolloUrl")
        android.util.Log.d("HistorialDetalle", "idCompetencia=$idCompetencia")
        android.util.Log.d("HistorialDetalle", "esIncorrecto=$esIncorrecto")

        tvTitulo.text    = titulo
        tvResultado.text = estadoRaw

        if (esIncorrecto) {
            ivResultado.setImageResource(R.drawable.ic_cancel_24)
            ivResultado.setColorFilter(
                ContextCompat.getColor(this, R.color.ai_error),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
        } else {
            ivResultado.setImageResource(R.drawable.ic_check_circle_24)
            ivResultado.setColorFilter(
                ContextCompat.getColor(this, R.color.ai_success),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
        }

        // ✅ Mostrar desarrollo (imagen o PDF)
        when {
            desarrolloUrl.isBlank() -> {
                cardSinDesarrollo.visibility = View.VISIBLE
            }
            desarrolloUrl.endsWith(".pdf", ignoreCase = true) -> {
                cardPdf.visibility = View.VISIBLE
                btnAbrirPdf.setOnClickListener {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(desarrolloUrl)))
                }
            }
            else -> {
                // ✅ Corregir URL para el dispositivo/emulador
                val urlFinal = corregirUrl(desarrolloUrl)
                android.util.Log.d("HistorialDetalle", "URL final imagen: $urlFinal")

                cardImagen.visibility = View.VISIBLE

                // ✅ Hacerla VISIBLE antes de cargar (fix Glide con vistas GONE)
                ivDesarrollo.visibility = View.VISIBLE

                Glide.with(this)
                    .load(urlFinal)
                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .override(com.bumptech.glide.request.target.Target.SIZE_ORIGINAL)
                    .listener(object : com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable> {
                        override fun onLoadFailed(
                            e: com.bumptech.glide.load.engine.GlideException?,
                            model: Any?,
                            target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>,
                            isFirstResource: Boolean
                        ): Boolean {
                            android.util.Log.e("HistorialDetalle", "❌ Imagen falló: $urlFinal — ${e?.message}")
                            cardImagen.visibility         = View.GONE
                            cardSinDesarrollo.visibility  = View.VISIBLE
                            return false
                        }
                        override fun onResourceReady(
                            resource: android.graphics.drawable.Drawable,
                            model: Any,
                            target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>,
                            dataSource: com.bumptech.glide.load.DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            android.util.Log.d("HistorialDetalle", "✅ Imagen cargada: $urlFinal")
                            return false
                        }
                    })
                    .into(ivDesarrollo)

                // Click para ver imagen en pantalla completa
                ivDesarrollo.setOnClickListener {
                    val dialog = android.app.Dialog(
                        this,
                        android.R.style.Theme_Black_NoTitleBar_Fullscreen
                    )
                    val imgFull = ImageView(this)
                    Glide.with(this)
                        .load(urlFinal)
                        .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(imgFull)
                    imgFull.scaleType = ImageView.ScaleType.FIT_CENTER
                    imgFull.setOnClickListener { dialog.dismiss() }
                    dialog.setContentView(imgFull)
                    dialog.show()
                }
            }
        }

        // ✅ Materiales de refuerzo si es incorrecto
        if (esIncorrecto && idCompetencia > 0) {
            layoutRefuerzo.visibility = View.VISIBLE
            cargarMaterialesRefuerzo(idCompetencia, layoutMateriales, tvCargando)
        } else if (esIncorrecto && idCompetencia == 0) {
            layoutRefuerzo.visibility = View.VISIBLE
            tvCargando.text = "No se pudo identificar el tema del ejercicio."
        }
    }

    // ✅ Corrige la URL para que funcione en emulador y dispositivo real
    private fun corregirUrl(url: String?): String? {
        if (url.isNullOrBlank()) return null
        return try {
            val baseUri    = Uri.parse(RetrofitClient.BASE_URL.trimEnd('/'))
            val hostBase   = baseUri.host ?: return url
            val puertoBase = baseUri.port
            val esquema    = baseUri.scheme ?: "http"
            val path       = Uri.parse(url).path ?: return url
            if (puertoBase > 0) "$esquema://$hostBase:$puertoBase$path"
            else "$esquema://$hostBase$path"
        } catch (_: Exception) { url }
    }

    private fun cargarMaterialesRefuerzo(
        idCompetencia: Int,
        layoutMateriales: LinearLayout,
        tvCargando: TextView
    ) {
        android.util.Log.d("HistorialDetalle", "Cargando materiales para competencia $idCompetencia")

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val detalleResp = RetrofitClient.dominioApi.obtenerDetalleTema(idCompetencia)
                val materiales  = detalleResp.data?.materiales?.take(3) ?: emptyList()

                android.util.Log.d("HistorialDetalle", "Materiales recibidos: ${materiales.size}")

                withContext(Dispatchers.Main) {
                    tvCargando.visibility = View.GONE
                    if (materiales.isEmpty()) {
                        tvCargando.visibility = View.VISIBLE
                        tvCargando.text = "No hay materiales para este tema."
                        return@withContext
                    }
                    materiales.forEach { mat ->
                        agregarTarjetaMaterial(
                            layoutMateriales,
                            mat.titulo,
                            mat.tipo,
                            mat.url
                        )
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("HistorialDetalle", "Error cargando materiales: ${e.message}")
                withContext(Dispatchers.Main) {
                    tvCargando.text = "No se pudieron cargar los materiales."
                }
            }
        }
    }

    private fun agregarTarjetaMaterial(
        layout: LinearLayout,
        titulo: String?,
        tipo: String?,
        url: String?
    ) {
        val tipoStr = tipo?.lowercase() ?: ""
        val emoji = when {
            tipoStr.contains("video") -> "▶"
            tipoStr.contains("pdf")   -> "📄"
            else                      -> "🔗"
        }

        val tv = TextView(this).apply {
            text     = "$emoji  ${titulo ?: "Material de estudio"}"
            textSize = 14f
            setTextColor(ContextCompat.getColor(
                this@HistorialDetalleActivity, R.color.ai_primary))
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(32, 28, 32, 28)
            background = ContextCompat.getDrawable(
                this@HistorialDetalleActivity, R.drawable.bg_card)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = 12 }
            setOnClickListener {
                if (!url.isNullOrBlank()) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
            }
        }
        layout.addView(tv)
    }
}