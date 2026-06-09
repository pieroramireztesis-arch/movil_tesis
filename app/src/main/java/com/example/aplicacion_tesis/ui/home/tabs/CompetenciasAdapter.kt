package com.example.aplicacion_tesis.ui.home.tabs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.aplicacion_tesis.model.dto.ProgresoPorCompetenciaItemDTO

class CompetenciasAdapter :
    RecyclerView.Adapter<CompetenciasAdapter.ViewHolder>() {

    private val items = mutableListOf<ProgresoPorCompetenciaItemDTO>()

    fun setItems(newItems: List<ProgresoPorCompetenciaItemDTO>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(android.R.id.text1)
        val tvSubtitle: TextView = view.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvTitle.text = item.nombre
        holder.tvSubtitle.text = "${item.porcentaje}% de avance"
    }

    override fun getItemCount(): Int = items.size
}
