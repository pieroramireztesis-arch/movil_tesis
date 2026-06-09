package com.example.aplicacion_tesis.ui.home.tabs.practica

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.aplicacion_tesis.R
import com.example.aplicacion_tesis.model.dto.TutorOptionDTO
import com.example.aplicacion_tesis.network.RetrofitClient
import com.example.aplicacion_tesis.network.TokenStore
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import com.example.aplicacion_tesis.model.dto.TutorExerciseDTO

class PracticaEjercicioActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID_EJERCICIO = "extra_id_ejercicio"
    }

    private lateinit var tvTituloTutor: TextView
    private lateinit var imgProblema: ImageView
    private lateinit var tvEnunciado: TextView

    private lateinit var rgAlternativas: RadioGroup
    private lateinit var rbA: RadioButton
    private lateinit var rbB: RadioButton
    private lateinit var rbC: RadioButton
    private lateinit var rbD: RadioButton
    private lateinit var rbE: RadioButton

    private lateinit var cardFeedback: LinearLayout
    private lateinit var tvFeedbackTitle: TextView
    private lateinit var tvFeedback: TextView

    private lateinit var btnSubirFoto: MaterialButton
    private lateinit var btnEnviar: MaterialButton
    private lateinit var btnBack: ImageButton

    private var ejercicioActual: TutorExerciseDTO? = null
    private var opcionesActuales: List<TutorOptionDTO> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ejercicio_practica)

        tvTituloTutor = findViewById(R.id.tvTituloTutor)
        imgProblema = findViewById(R.id.imgProblema)
        tvEnunciado = findViewById(R.id.tvEnunciado)

        rgAlternativas = findViewById(R.id.rgAlternativas)
        rbA = findViewById(R.id.rbA)
        rbB = findViewById(R.id.rbB)
        rbC = findViewById(R.id.rbC)
        rbD = findViewById(R.id.rbD)
        rbE = findViewById(R.id.rbE)

        cardFeedback = findViewById(R.id.cardFeedback)
        tvFeedbackTitle = findViewById(R.id.tvFeedbackTitle)
        tvFeedback = findViewById(R.id.tvFeedback)

        btnSubirFoto = findViewById(R.id.btnSubirFoto)
        btnEnviar = findViewById(R.id.btnEnviar)
        btnBack = findViewById(R.id.btnBackPractica)

        btnBack.setOnClickListener { finish() }
        onBackPressedDispatcher.addCallback(this) { finish() }

        tvTituloTutor.text = "Ejercicio de práctica"

        findViewById<TextView>(R.id.tvModoPracticaInfo)?.text =
            "Este ejercicio es solo para practicar. No modificará tu progreso."

        val idEjercicioHistorial = intent.getIntExtra(EXTRA_ID_EJERCICIO, -1)
        cargarEjercicioEnModoPractica(idEjercicioHistorial)

        btnSubirFoto.setOnClickListener {
            Toast.makeText(this, "Subir desarrollo (modo práctica)", Toast.LENGTH_SHORT).show()
        }

        btnEnviar.setOnClickListener {
            verificarSeleccionLocal()
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun cargarEjercicioEnModoPractica(idEjercicioHistorial: Int) {
        lifecycleScope.launch {
            try {
                val idEstudiante = TokenStore.studentId ?: 0

                val resp = RetrofitClient.tutorApi.getNextExercise(
                    idEstudiante = idEstudiante,
                    idDominio = null,
                    ajuste = null
                )

                if (!resp.status || resp.sinEjercicios == true || resp.idEjercicio == null) {
                    Toast.makeText(
                        this@PracticaEjercicioActivity,
                        resp.mensaje ?: "No se encontró ejercicio para practicar.",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                    return@launch
                }

                ejercicioActual = resp
                mostrarEjercicio(resp)

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@PracticaEjercicioActivity,
                    "Error al cargar el ejercicio.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun mostrarEjercicio(ejercicio: TutorExerciseDTO) {
        tvEnunciado.text = ejercicio.enunciado ?: "Ejercicio de álgebra"

        val urlImagen = ejercicio.imagenUrl
        if (urlImagen.isNullOrBlank()) {
            imgProblema.visibility = View.GONE
        } else {
            imgProblema.visibility = View.VISIBLE
            Glide.with(this)
                .load(urlImagen)
                .placeholder(R.drawable.donut_placeholder)
                .error(R.drawable.donut_placeholder)
                .into(imgProblema)
        }

        // 🔹 Cast seguro de la lista a List<TutorOptionDTO>
        opcionesActuales = (ejercicio.opciones as? List<TutorOptionDTO>) ?: emptyList()
        val radios = listOf(rbA, rbB, rbC, rbD, rbE)

        rgAlternativas.clearCheck()
        cardFeedback.isVisible = false

        for (i in radios.indices) {
            val rb = radios[i]
            if (i < opcionesActuales.size) {
                val opt = opcionesActuales[i]
                rb.visibility = View.VISIBLE
                rb.text = "${opt.letra}) ${opt.texto}"
                rb.tag = opt.idOpcion
            } else {
                rb.visibility = View.GONE
                rb.tag = null
            }
        }
    }

    private fun verificarSeleccionLocal() {
        val checkedId = rgAlternativas.checkedRadioButtonId
        if (checkedId == -1) {
            Toast.makeText(this, "Selecciona una respuesta.", Toast.LENGTH_SHORT).show()
            return
        }

        val ejercicio = ejercicioActual ?: return

        cardFeedback.visibility = View.VISIBLE
        tvFeedbackTitle.text = "Modo práctica: revisa esta pista"
        tvFeedback.text = ejercicio.pista
            ?: "Intenta revisar los pasos que hiciste y vuelve a intentarlo. 😊"
    }
}
