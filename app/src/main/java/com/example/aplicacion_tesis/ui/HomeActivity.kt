package com.example.aplicacion_tesis.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.aplicacion_tesis.R
import com.example.aplicacion_tesis.network.TokenStore
import com.example.aplicacion_tesis.ui.home.HomePagerAdapter
import com.example.aplicacion_tesis.ui.login.LoginActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class HomeActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userId = TokenStore.userId
        if (userId == null || userId <= 0) {
            Toast.makeText(this, "Sesión inválida. Vuelve a iniciar sesión.", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
            return
        }

        setContentView(R.layout.activity_home)

        tabLayout = findViewById(R.id.tabLayoutHome)
        viewPager = findViewById(R.id.viewPager)

        val adapter = HomePagerAdapter(this)
        viewPager.adapter = adapter

        // ✅ CORREGIDO: valor fijo 4 garantiza que NUNCA destruye ningún tab
        // adapter.itemCount no funciona correctamente con ViewPager2
        viewPager.offscreenPageLimit = 4

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = adapter.titles[position]
        }.attach()
    }
}