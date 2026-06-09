package com.example.aplicacion_tesis.ui.home.tabs

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aplicacion_tesis.R
import com.example.aplicacion_tesis.model.dto.DominioTemaDTO
import com.example.aplicacion_tesis.network.RetrofitClient
import com.example.aplicacion_tesis.network.TokenStore
import kotlinx.coroutines.launch

class DominioFragment : Fragment() {

    private lateinit var rvTemas:             RecyclerView
    private lateinit var chipTodos:           TextView
    private lateinit var chipBasico:          TextView
    private lateinit var chipIntermedio:      TextView
    private lateinit var chipAvanzado:        TextView
    private lateinit var tvTotalMateriales:   TextView
    private lateinit var tvTotalVistos:       TextView
    private lateinit var tvPorcentajeGeneral: TextView

    private lateinit var adapter: TemaDominioAdapter

    private var listaCompleta: List<DominioTemaDTO> = emptyList()
    private var filtroActual:  String               = "TODOS"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_dominio, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvTemas             = view.findViewById(R.id.rvTemasDominio)
        chipTodos           = view.findViewById(R.id.chipTodos)
        chipBasico          = view.findViewById(R.id.chipBasico)
        chipIntermedio      = view.findViewById(R.id.chipIntermedio)
        chipAvanzado        = view.findViewById(R.id.chipAvanzado)
        tvTotalMateriales   = view.findViewById(R.id.tvTotalMateriales)
        tvTotalVistos       = view.findViewById(R.id.tvTotalVistos)
        tvPorcentajeGeneral = view.findViewById(R.id.tvPorcentajeGeneral)

        rvTemas.layoutManager = GridLayoutManager(requireContext(), 2)

        adapter = TemaDominioAdapter(emptyList()) { tema ->
            startActivity(
                Intent(requireContext(), TemaDetalleActivity::class.java).apply {
                    putExtra("ID_TEMA",         tema.idTema)
                    putExtra("NOMBRE_TEMA",     tema.nombre)
                    putExtra("FILTRO_NIVEL",    filtroActual) // ✅ pasa el filtro actual
                    putExtra("MAT_VISTOS",      tema.materialesVistos)
                    putExtra("MAT_TOTAL",       tema.totalMateriales)
                }
            )
        }
        rvTemas.adapter = adapter

        configurarChips()
    }

    override fun onResume() {
        super.onResume()
        cargarTemasDesdeApi()
    }

    private fun configurarChips() {
        listOf(
            chipTodos       to "TODOS",
            chipBasico      to "BASICO",
            chipIntermedio  to "INTERMEDIO",
            chipAvanzado    to "AVANZADO"
        ).forEach { (chip, filtro) ->
            chip.setOnClickListener {
                filtroActual = filtro
                actualizarEstilosChips()
                aplicarFiltro()
            }
        }
        actualizarEstilosChips()
    }

    private fun actualizarEstilosChips() {
        listOf(
            chipTodos      to "TODOS",
            chipBasico     to "BASICO",
            chipIntermedio to "INTERMEDIO",
            chipAvanzado   to "AVANZADO"
        ).forEach { (tv, filtro) ->
            val sel = filtroActual == filtro
            tv.background = ContextCompat.getDrawable(
                requireContext(),
                if (sel) R.drawable.bg_chip_selected else R.drawable.bg_chip_unselected
            )
            tv.setTextColor(
                if (sel) android.graphics.Color.WHITE
                else ContextCompat.getColor(requireContext(), R.color.ai_text_dark)
            )
        }
    }

    private fun aplicarFiltro() {
        val filtrada = when (filtroActual) {
            "BASICO"     -> listaCompleta.filter { it.materialesBasico > 0 }
            "INTERMEDIO" -> listaCompleta.filter { it.materialesIntermedio > 0 }
            "AVANZADO"   -> listaCompleta.filter { it.materialesAvanzado > 0 }
            else         -> listaCompleta
        }
        // ✅ Primero actualiza el filtro, LUEGO la lista
        adapter.filtroActual = filtroActual
        adapter.submitList(filtrada.map { it.toTemaDominio() })
    }

    private fun actualizarHeader() {
        val totalMat    = listaCompleta.sumOf { it.totalMateriales }
        val totalVistos = listaCompleta.sumOf { it.materialesVistos }
        val pct         = if (totalMat > 0) totalVistos * 100 / totalMat else 0

        tvTotalMateriales.text   = totalMat.toString()
        tvTotalVistos.text       = totalVistos.toString()
        tvPorcentajeGeneral.text = "$pct%"
    }

    private fun cargarTemasDesdeApi() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val idEst = obtenerIdEstudiante() ?: return@launch
                val resp  = RetrofitClient.dominioApi.listarTemasDominio(idEst)

                if (resp.status && resp.data != null) {
                    listaCompleta = resp.data
                    actualizarHeader()
                    aplicarFiltro()
                } else {
                    Toast.makeText(requireContext(), "No se pudieron cargar los temas.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al cargar el dominio.", Toast.LENGTH_SHORT).show()
            }
        }
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

    private fun DominioTemaDTO.toTemaDominio() = TemaDominio(
        idTema           = idTema,
        nombre           = nombre,
        nivel            = nivel ?: "SIN NIVEL",
        totalMateriales  = totalMateriales,
        materialesVistos = materialesVistos,
        materialesBasico      = materialesBasico,
        materialesIntermedio  = materialesIntermedio,
        materialesAvanzado    = materialesAvanzado
    )
}