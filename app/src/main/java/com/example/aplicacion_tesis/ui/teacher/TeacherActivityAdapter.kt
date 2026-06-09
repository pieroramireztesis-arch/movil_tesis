package com.example.aplicacion_tesis.ui.teacher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.aplicacion_tesis.R
import com.example.aplicacion_tesis.model.dto.TeacherActivityItem

class TeacherActivityAdapter(
    private var items: List<TeacherActivityItem> = emptyList()
) : RecyclerView.Adapter<TeacherActivityAdapter.ActivityVH>() {

    inner class ActivityVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvActivityTitle)
        private val tvSubtitle: TextView = itemView.findViewById(R.id.tvActivitySubtitle)
        private val imgIcon: ImageView = itemView.findViewById(R.id.imgIcon)

        fun bind(item: TeacherActivityItem) {
            tvTitle.text = item.titulo
            tvSubtitle.text = item.detalle

            val iconRes = when (item.tipo) {
                "progreso" -> android.R.drawable.presence_online
                "pista" -> android.R.drawable.ic_menu_help
                "practica" -> android.R.drawable.ic_media_play
                else -> android.R.drawable.ic_menu_info_details
            }
            imgIcon.setImageResource(iconRes)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity_history, parent, false)
        return ActivityVH(view)
    }

    override fun onBindViewHolder(holder: ActivityVH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<TeacherActivityItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
