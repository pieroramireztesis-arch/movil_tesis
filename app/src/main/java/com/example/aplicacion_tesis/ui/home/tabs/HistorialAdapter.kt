package com.example.aplicacion_tesis.ui.home.tabs

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.aplicacion_tesis.R
import com.example.aplicacion_tesis.model.dto.ProgresoHistorialItemDTO
import com.example.aplicacion_tesis.ui.home.HistorialDetalleActivity

class HistorialAdapter : RecyclerView.Adapter<HistorialAdapter.ViewHolder>() {

    private val items = mutableListOf<ProgresoHistorialItemDTO>()

    fun setItems(newItems: List<ProgresoHistorialItemDTO>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_historial_progreso, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val accentBar:   View     = view.findViewById(R.id.viewAccentBar)
        private val tvTitulo:    TextView = view.findViewById(R.id.tvHistorialTitulo)
        private val tvFecha:     TextView = view.findViewById(R.id.tvHistorialFecha)
        private val tvEstado:    TextView = view.findViewById(R.id.tvHistorialEstado)
        private val tvModo:      TextView = view.findViewById(R.id.tvHistorialModo)
        private val tvIntentos:  TextView = view.findViewById(R.id.tvHistorialIntentos)

        fun bind(item: ProgresoHistorialItemDTO) {
            tvTitulo.text = item.titulo ?: ""
            tvFecha.text  = formatearFecha(item.fecha)

            val estadoRaw = item.estado ?: ""
            val esCorrecto = estadoRaw.contains("Correcto", ignoreCase = true) &&
                             !estadoRaw.contains("In", ignoreCase = true)

            // Barra de acento lateral
            val accentColor = if (esCorrecto) Color.parseColor("#27AE60") else Color.parseColor("#E74C3C")
            accentBar.setBackgroundColor(accentColor)

            // Badge estado
            val badgeRadius = 50f * itemView.resources.displayMetrics.density
            val dp4 = (4 * itemView.resources.displayMetrics.density).toInt()
            tvEstado.text = if (esCorrecto) "Correcto" else "Incorrecto"
            tvEstado.background = GradientDrawable().apply {
                setColor(Color.parseColor(if (esCorrecto) "#1A27AE60" else "#1AE74C3C"))
                cornerRadius = badgeRadius
            }
            tvEstado.setTextColor(accentColor)
            val iconEstado = if (esCorrecto) R.drawable.ic_check_circle_24 else R.drawable.ic_cancel_24
            tvEstado.setCompoundDrawablesRelativeWithIntrinsicBounds(iconEstado, 0, 0, 0)
            tvEstado.compoundDrawablePadding = dp4
            tvEstado.compoundDrawablesRelative[0]?.mutate()?.setTint(accentColor)

            // Badge modo
            val modoRaw = item.modo ?: "Revisión"
            tvModo.text = modoRaw
            val esEval = modoRaw.contains("valuaci", ignoreCase = true)
            tvModo.background = GradientDrawable().apply {
                setColor(Color.parseColor(if (esEval) "#1AF59E0B" else "#1A818CF8"))
                cornerRadius = badgeRadius
            }
            tvModo.setTextColor(Color.parseColor(if (esEval) "#D97706" else "#6366F1"))

            // Badge intentos incorrectos (solo si > 0)
            val intentos = (item.intentosIncorrectos ?: 0).coerceAtLeast(0)
            if (intentos > 0) {
                tvIntentos.text = "$intentos ${if (intentos == 1) "intento" else "intentos"}"
                tvIntentos.background = GradientDrawable().apply {
                    setColor(Color.parseColor("#1AF87171"))
                    cornerRadius = badgeRadius
                }
                tvIntentos.setTextColor(Color.parseColor("#DC2626"))
                tvIntentos.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_bolt, 0, 0, 0)
                tvIntentos.compoundDrawablePadding = dp4
                tvIntentos.compoundDrawablesRelative[0]?.mutate()?.setTint(Color.parseColor("#DC2626"))
                tvIntentos.visibility = View.VISIBLE
            } else {
                tvIntentos.visibility = View.GONE
            }

            itemView.setOnClickListener {
                val ctx = itemView.context
                val intent = Intent(ctx, HistorialDetalleActivity::class.java).apply {
                    putExtra(HistorialDetalleActivity.EXTRA_TITULO,         item.titulo ?: "")
                    putExtra(HistorialDetalleActivity.EXTRA_SUBTITULO,      estadoRaw)
                    putExtra(HistorialDetalleActivity.EXTRA_ID_EJERCICIO,   item.idEjercicio)
                    putExtra(HistorialDetalleActivity.EXTRA_DESARROLLO_URL, item.desarrolloUrl ?: "")
                    putExtra(HistorialDetalleActivity.EXTRA_ID_COMPETENCIA, item.idCompetencia)
                    putExtra(HistorialDetalleActivity.EXTRA_ES_INCORRECTO,
                        estadoRaw.contains("Incorrecto", ignoreCase = true))
                }
                ctx.startActivity(intent)
            }
        }

        private fun formatearFecha(fecha: String?): String {
            if (fecha.isNullOrBlank()) return ""
            return try {
                val partes = fecha.split("T")
                if (partes.size < 2) return fecha
                val (anio, mes, dia) = partes[0].split("-")
                val hora = partes[1].substring(0, 5)
                val meses = listOf("","Ene","Feb","Mar","Abr","May","Jun",
                    "Jul","Ago","Sep","Oct","Nov","Dic")
                "$dia ${meses[mes.toInt()]} $anio · $hora"
            } catch (e: Exception) { fecha }
        }
    }
}