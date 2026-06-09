package com.example.aplicacion_tesis.ui.home.tabs

import android.content.Intent
import android.graphics.Color
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
        private val tvTitulo: TextView = view.findViewById(R.id.tvHistorialTitulo)
        private val tvFecha:  TextView = view.findViewById(R.id.tvHistorialFecha)
        private val tvEstado: TextView = view.findViewById(R.id.tvHistorialEstado)
        private val tvModo:   TextView = view.findViewById(R.id.tvHistorialModo)

        fun bind(item: ProgresoHistorialItemDTO) {
            tvTitulo.text = item.titulo ?: ""
            tvFecha.text  = formatearFecha(item.fecha)

            val estadoRaw = item.estado ?: ""
            tvEstado.text = estadoRaw
            when {
                estadoRaw.contains("Correcto", ignoreCase = true) -> {
                    tvEstado.setTextColor(Color.parseColor("#27AE60"))
                    tvEstado.setBackgroundResource(R.drawable.bg_badge_correcto)
                }
                estadoRaw.contains("Incorrecto", ignoreCase = true) -> {
                    tvEstado.setTextColor(Color.parseColor("#E74C3C"))
                    tvEstado.setBackgroundResource(R.drawable.bg_badge_incorrecto)
                }
                else -> {
                    tvEstado.setTextColor(Color.parseColor("#7F8C8D"))
                    tvEstado.background = null
                }
            }

            val modoRaw = item.modo ?: "Revisión"
            tvModo.text = modoRaw
            when {
                modoRaw.contains("valuaci", ignoreCase = true) -> {
                    tvModo.setTextColor(Color.WHITE)
                    tvModo.setBackgroundResource(R.drawable.bg_badge_evaluacion)
                }
                else -> {
                    tvModo.setTextColor(Color.WHITE)
                    tvModo.setBackgroundResource(R.drawable.bg_badge_repaso)
                }
            }

            // ✅ Click → abrir detalle
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