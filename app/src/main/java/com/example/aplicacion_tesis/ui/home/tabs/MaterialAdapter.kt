package com.example.aplicacion_tesis.ui.home.tabs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.aplicacion_tesis.R
import com.example.aplicacion_tesis.model.dto.MaterialDidacticoDTO

class MaterialAdapter(
    private val materiales: List<MaterialDidacticoDTO>,
    private val onItemClick: (MaterialDidacticoDTO) -> Unit
) : RecyclerView.Adapter<MaterialAdapter.MaterialViewHolder>() {

    inner class MaterialViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitulo: TextView = itemView.findViewById(R.id.tvTituloMaterial)
        private val tvDescripcion: TextView = itemView.findViewById(R.id.tvDescripcionMaterial)

        fun bind(item: MaterialDidacticoDTO) {
            tvTitulo.text = item.titulo
            // Puedes mostrar el tipo o algo más
            tvDescripcion.text = when (item.tipo.uppercase()) {
                "VIDEO" -> "Video didáctico"
                "PDF" -> "Guía en PDF"
                else -> "Recurso didáctico"
            }

            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MaterialViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_material, parent, false)
        return MaterialViewHolder(view)
    }

    override fun onBindViewHolder(holder: MaterialViewHolder, position: Int) {
        holder.bind(materiales[position])
    }

    override fun getItemCount(): Int = materiales.size
}
