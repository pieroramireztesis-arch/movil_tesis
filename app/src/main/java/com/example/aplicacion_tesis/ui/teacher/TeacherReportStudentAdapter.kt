package com.example.aplicacion_tesis.ui.teacher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.aplicacion_tesis.R
import com.example.aplicacion_tesis.model.dto.TeacherStudentItem

class TeacherReportStudentAdapter(
    private var items: List<TeacherStudentItem>,
    private val onClick: (TeacherStudentItem) -> Unit
) : RecyclerView.Adapter<TeacherReportStudentAdapter.StudentViewHolder>() {

    inner class StudentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: CardView    = view.findViewById(R.id.cardStudentRoot)
        val tvInitials: TextView = view.findViewById(R.id.tvStudentInitials)
        val tvName: TextView     = view.findViewById(R.id.tvStudentName)
        val tvProgress: TextView = view.findViewById(R.id.tvStudentProgress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_teacher_report_student, parent, false)
        return StudentViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val item = items[position]

        holder.tvName.text     = item.nombre
        holder.tvProgress.text = "Progreso: ${item.progreso}%"

        // ✅ Iniciales desde "Apellidos, Nombre"
        // Formato esperado: "Chávez Díaz, Carlos"
        // Tomamos: primera letra de apellido + primera letra de nombre
        val iniciales = obtenerIniciales(item.nombre)
        holder.tvInitials.text = iniciales

        holder.card.setOnClickListener { onClick(item) }
    }

    fun updateItems(newItems: List<TeacherStudentItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    // ✅ Extrae iniciales del formato "Apellidos, Nombre"
    // Ej: "Chávez Díaz, Carlos" → "CC"
    // Ej: "Sánchez Poma, Camila" → "SC"
    private fun obtenerIniciales(nombreCompleto: String): String {
        return try {
            if (nombreCompleto.contains(",")) {
                // Formato "Apellidos, Nombre"
                val partes    = nombreCompleto.split(",")
                val apellido  = partes[0].trim()
                val nombre    = partes[1].trim()
                val initApell = apellido.firstOrNull()?.uppercaseChar() ?: ' '
                val initNom   = nombre.firstOrNull()?.uppercaseChar() ?: ' '
                "$initApell$initNom"
            } else {
                // Fallback: primeras 2 palabras
                nombreCompleto
                    .split(" ")
                    .filter { it.isNotBlank() }
                    .take(2)
                    .joinToString("") { it.first().uppercaseChar().toString() }
            }
        } catch (e: Exception) {
            "??"
        }
    }
}