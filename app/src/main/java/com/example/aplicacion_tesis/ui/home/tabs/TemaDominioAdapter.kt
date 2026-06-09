package com.example.aplicacion_tesis.ui.home.tabs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.aplicacion_tesis.R

class TemaDominioAdapter(
    private var temas: List<TemaDominio>,
    var filtroActual: String = "TODOS",
    private val onTemaClick: (TemaDominio) -> Unit
) : RecyclerView.Adapter<TemaDominioAdapter.TemaViewHolder>() {

    private val bgColors = intArrayOf(
        0x33F8E71C.toInt(), 0x3350E3C2.toInt(),
        0x339013FE.toInt(), 0x334A90E2.toInt()
    )
    private val iconTints = intArrayOf(
        0xFFF5A623.toInt(), 0xFF27AE60.toInt(),
        0xFF9013FE.toInt(), 0xFF2980B9.toInt()
    )

    fun submitList(newList: List<TemaDominio>) {
        temas = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        TemaViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_tema_dominio, parent, false)
        )

    override fun onBindViewHolder(holder: TemaViewHolder, position: Int) =
        holder.bind(temas[position])

    override fun getItemCount() = temas.size

    inner class TemaViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val containerIcono = v.findViewById<View>(R.id.containerIcono)
        private val imgIcono       = v.findViewById<ImageView>(R.id.imgIconoTema)
        private val tvBadge        = v.findViewById<TextView>(R.id.tvBadgeNivel)
        private val tvTitulo       = v.findViewById<TextView>(R.id.tvTituloTema)
        private val tvVistos       = v.findViewById<TextView>(R.id.tvVistos)
        private val tvPorcentaje   = v.findViewById<TextView>(R.id.tvPorcentaje)
        private val progress       = v.findViewById<ProgressBar>(R.id.progressTema)

        fun bind(tema: TemaDominio) {
            val idx = ((tema.idTema - 1).coerceAtLeast(0)) % bgColors.size

            containerIcono.setBackgroundColor(bgColors[idx])
            imgIcono.setImageResource(R.drawable.ic_person_24)
            imgIcono.setColorFilter(iconTints[idx])

            // ✅ Badge según filtro activo
            val badgeTexto = when (filtroActual) {
                "BASICO"     -> "BÁSICO"
                "INTERMEDIO" -> "INTERMEDIO"
                "AVANZADO"   -> "AVANZADO"
                else         -> "TODOS"
            }
            val badgeColor = when (filtroActual) {
                "BASICO"     -> 0xFF27AE60.toInt()
                "INTERMEDIO" -> 0xFFF5A623.toInt()
                "AVANZADO"   -> 0xFFE74C3C.toInt()
                else         -> 0xFF2980B9.toInt()
            }
            tvBadge.text = badgeTexto
            tvBadge.backgroundTintList =
                android.content.res.ColorStateList.valueOf(badgeColor)

            tvTitulo.text     = tema.nombre
            tvVistos.text     = "${tema.materialesVistos} de ${tema.totalMateriales} vistos"
            tvPorcentaje.text = "${tema.porcentajeProgreso}%"
            progress.progress = tema.porcentajeProgreso

            val colorBarra = when {
                tema.porcentajeProgreso >= 80 -> 0xFF27AE60.toInt()
                tema.porcentajeProgreso >= 40 -> 0xFFF5A623.toInt()
                else                          -> 0xFF2980B9.toInt()
            }
            progress.progressTintList =
                android.content.res.ColorStateList.valueOf(colorBarra)

            itemView.setOnClickListener { onTemaClick(tema) }
        }
    }
}