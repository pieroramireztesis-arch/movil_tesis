package com.example.aplicacion_tesis.ui.onboarding

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.aplicacion_tesis.R
import com.example.aplicacion_tesis.ui.login.LoginActivity
import com.google.android.material.button.MaterialButton

class OnboardingActivity : AppCompatActivity() {

    private lateinit var pager: ViewPager2
    private lateinit var btnNext: MaterialButton
    private lateinit var tvSkip: TextView
    private lateinit var dotsContainer: LinearLayout

    private val pages = listOf(
        Page(R.drawable.ic_lightbulb,   "#312E81",
            "Tu Tutor de Álgebra IA",
            "El sistema aprende tu ritmo y te asigna ejercicios\nadaptados a tu nivel automáticamente."),
        Page(R.drawable.ic_trending_up, "#1E1B4B",
            "Practica y Sube de Nivel",
            "Avanza en las 4 competencias MINEDU con ejercicios\nque crecen junto a ti."),
        Page(R.drawable.ic_nav_school,  "#0F3460",
            "Tu Docente te Acompaña",
            "Tu profesor recibe alertas cuando necesitas apoyo\ny sigue tu progreso en tiempo real.")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        pager         = findViewById(R.id.onboardingPager)
        btnNext       = findViewById(R.id.btnOnboardingNext)
        tvSkip        = findViewById(R.id.tvOnboardingSkip)
        dotsContainer = findViewById(R.id.dotsContainer)

        pager.adapter = PagesAdapter()
        updateDots(0)

        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateDots(position)
                btnNext.text = if (position == pages.lastIndex) "Comenzar" else "Siguiente"
            }
        })

        btnNext.setOnClickListener {
            val cur = pager.currentItem
            if (cur < pages.lastIndex) pager.setCurrentItem(cur + 1, true)
            else completeOnboarding()
        }

        tvSkip.setOnClickListener { completeOnboarding() }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val cur = pager.currentItem
        if (cur > 0) pager.setCurrentItem(cur - 1, true)
        // Bloquea salir en la primera página — el usuario debe elegir Saltar o Comenzar
    }

    private fun updateDots(selected: Int) {
        dotsContainer.removeAllViews()
        val dp = resources.displayMetrics.density
        pages.indices.forEach { i ->
            val dot = View(this)
            val w = ((if (i == selected) 24 else 8) * dp).toInt()
            val h = (8 * dp).toInt()
            dot.layoutParams = LinearLayout.LayoutParams(w, h).apply {
                marginEnd = (6 * dp).toInt()
            }
            dot.background = getDrawable(
                if (i == selected) R.drawable.bg_dot_active else R.drawable.bg_dot_inactive
            )
            dotsContainer.addView(dot)
        }
    }

    private fun completeOnboarding() {
        getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .edit().putBoolean("onboarding_complete", true).apply()
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }

    data class Page(val icon: Int, val bgColor: String, val title: String, val subtitle: String)

    inner class PagesAdapter : RecyclerView.Adapter<PagesAdapter.VH>() {
        inner class VH(v: View) : RecyclerView.ViewHolder(v) {
            val root: FrameLayout = v.findViewById(R.id.pageRoot)
            val icon: ImageView   = v.findViewById(R.id.ivOnboardingIcon)
            val title: TextView   = v.findViewById(R.id.tvOnboardingTitle)
            val sub: TextView     = v.findViewById(R.id.tvOnboardingSubtitle)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
            LayoutInflater.from(parent.context).inflate(R.layout.item_onboarding, parent, false)
        )

        override fun getItemCount() = pages.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            val page = pages[position]
            holder.root.setBackgroundColor(Color.parseColor(page.bgColor))
            holder.icon.setImageResource(page.icon)
            holder.icon.setColorFilter(Color.WHITE)
            holder.title.text = page.title
            holder.sub.text   = page.subtitle
        }
    }
}