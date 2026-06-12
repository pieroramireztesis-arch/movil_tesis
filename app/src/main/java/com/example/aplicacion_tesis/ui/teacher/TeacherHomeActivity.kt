package com.example.aplicacion_tesis.ui.teacher

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.aplicacion_tesis.R
import com.example.aplicacion_tesis.databinding.ActivityTeacherHomeBinding
import com.example.aplicacion_tesis.network.TokenStore
import com.example.aplicacion_tesis.ui.login.LoginActivity

/**
 * F2: HOST único del panel docente.
 * Antes cada tab era una Activity separada (TeacherReports/Alerts/Profile)
 * con una barra artesanal duplicada → parpadeo y pérdida de estado en cada
 * cambio. Ahora: ViewPager2 + BottomNavigationView, igual que el estudiante.
 */
class TeacherHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeacherHomeBinding

    // Orden de los destinos = orden de TeacherPagerAdapter
    private val menuIds = listOf(
        R.id.nav_teacher_inicio, R.id.nav_teacher_informes,
        R.id.nav_teacher_alertas, R.id.nav_teacher_perfil
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = TeacherPagerAdapter(this)
        binding.viewPagerTeacher.adapter = adapter
        binding.viewPagerTeacher.offscreenPageLimit = 3

        binding.bottomNavTeacher.setOnItemSelectedListener { item ->
            val pos = menuIds.indexOf(item.itemId)
            if (pos >= 0) binding.viewPagerTeacher.setCurrentItem(pos, false)
            pos >= 0
        }

        binding.viewPagerTeacher.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    if (position in menuIds.indices) {
                        binding.bottomNavTeacher.menu.findItem(menuIds[position]).isChecked = true
                    }
                }
            })
    }

    /** Navegación programática entre tabs (ej: botón "Ver Informes" del inicio). */
    fun navigateTo(position: Int) {
        if (position in menuIds.indices) {
            binding.viewPagerTeacher.setCurrentItem(position, false)
        }
    }

    override fun onStart() {
        super.onStart()
        val idUsuario = TokenStore.userId
        if (idUsuario == null || idUsuario <= 0) {
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
    }
}