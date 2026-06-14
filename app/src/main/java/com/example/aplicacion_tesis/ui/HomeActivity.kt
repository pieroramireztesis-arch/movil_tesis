package com.example.aplicacion_tesis.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import com.example.aplicacion_tesis.R
import com.example.aplicacion_tesis.network.AuthEventBus
import com.example.aplicacion_tesis.network.TokenStore
import com.example.aplicacion_tesis.ui.home.HomePagerAdapter
import com.example.aplicacion_tesis.ui.login.LoginActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var bottomNav: BottomNavigationView

    // Orden de los destinos = orden de HomePagerAdapter
    private val menuIds = listOf(
        R.id.nav_inicio, R.id.nav_dominio, R.id.nav_tutor,
        R.id.nav_progreso, R.id.nav_perfil
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userId = TokenStore.userId
        if (userId == null || userId <= 0) {
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
            return
        }

        setContentView(R.layout.activity_home)

        requestNotifPermissionIfNeeded()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                AuthEventBus.sessionExpired.collect {
                    startActivity(Intent(this@HomeActivity, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        putExtra("session_expired", true)
                    })
                }
            }
        }

        viewPager = findViewById(R.id.viewPager)
        bottomNav = findViewById(R.id.bottomNav)

        val adapter = HomePagerAdapter(this)
        viewPager.adapter = adapter

        // ✅ valor fijo 4 garantiza que NUNCA destruye ningún tab
        viewPager.offscreenPageLimit = 4

        // Bottom nav → ViewPager (sin animación de página para sentirse instantáneo)
        bottomNav.setOnItemSelectedListener { item ->
            val pos = menuIds.indexOf(item.itemId)
            if (pos >= 0) viewPager.setCurrentItem(pos, false)
            pos >= 0
        }

        // ViewPager (swipe) → marcar ítem del bottom nav
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position in menuIds.indices) {
                    bottomNav.menu.findItem(menuIds[position]).isChecked = true
                }
            }
        })
    }

    private fun requestNotifPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQ_NOTIF
                )
            }
        }
    }

    companion object {
        private const val REQ_NOTIF = 100
    }
}