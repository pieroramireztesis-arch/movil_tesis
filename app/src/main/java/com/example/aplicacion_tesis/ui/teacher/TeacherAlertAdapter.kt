package com.example.aplicacion_tesis.ui.teacher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.aplicacion_tesis.R
import com.example.aplicacion_tesis.model.dto.AlertSeverity
import com.example.aplicacion_tesis.model.dto.TeacherAlertItem

class TeacherAlertAdapter(
    private val onDismiss: (TeacherAlertItem) -> Unit
) : RecyclerView.Adapter<TeacherAlertAdapter.AlertViewHolder>() {

    private val items = mutableListOf<TeacherAlertItem>()

    fun updateItems(newItems: List<TeacherAlertItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun removeItem(item: TeacherAlertItem) {
        val idx = items.indexOf(item)
        if (idx >= 0) {
            items.removeAt(idx)
            notifyItemRemoved(idx)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_teacher_alert, parent, false)
        return AlertViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        holder.bind(items[position], onDismiss)
    }

    override fun getItemCount(): Int = items.size

    class AlertViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle:       TextView = itemView.findViewById(R.id.tvAlertTitle)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvAlertDescription)
        private val tvDate:        TextView = itemView.findViewById(R.id.tvAlertDate)
        private val tvChip:        TextView = itemView.findViewById(R.id.tvAlertChip)

        fun bind(item: TeacherAlertItem, onDismiss: (TeacherAlertItem) -> Unit) {
            tvTitle.text       = item.titulo
            tvDescription.text = item.descripcion
            tvDate.text        = item.fecha

            tvChip.text = when (item.severidad) {
                AlertSeverity.Critica     -> "Crítica"
                AlertSeverity.Advertencia -> "Advertencia"
                AlertSeverity.Info        -> "Info"
            }

            val bg = when (item.severidad) {
                AlertSeverity.Critica     -> R.drawable.bg_alert_critical
                AlertSeverity.Advertencia -> R.drawable.bg_alert_warning
                AlertSeverity.Info        -> R.drawable.bg_alert_info
            }
            tvChip.background = ContextCompat.getDrawable(itemView.context, bg)

            // ✅ Barra lateral de color
            val barColor = when (item.severidad) {
                AlertSeverity.Critica     -> android.graphics.Color.parseColor("#E74C3C")
                AlertSeverity.Advertencia -> android.graphics.Color.parseColor("#F39C12")
                AlertSeverity.Info        -> android.graphics.Color.parseColor("#1565C0")
            }
            try {
                itemView.findViewById<View>(R.id.viewSeveridadBar)
                    ?.setBackgroundColor(barColor)
            } catch (_: Exception) {}

            // Click en la tarjeta → dialog de seguimiento
            itemView.setOnClickListener { onDismiss(item) }
        }
    }
}