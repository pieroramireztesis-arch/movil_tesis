package com.example.aplicacion_tesis.ui.home

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.aplicacion_tesis.ui.home.tabs.InicioFragment
import com.example.aplicacion_tesis.ui.home.tabs.DominioFragment
import com.example.aplicacion_tesis.ui.home.tabs.TutorFragment
import com.example.aplicacion_tesis.ui.home.tabs.ProgresoFragment
import com.example.aplicacion_tesis.ui.home.tabs.ProfileFragment

class HomePagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    private val fragments: List<Fragment> = listOf(
        InicioFragment(),
        DominioFragment(),
        TutorFragment(),
        ProgresoFragment(),
        ProfileFragment()
    )

    val titles: List<String> = listOf(
        "Inicio", "Dominio", "Tutor", "Progreso", "Perfil"
    )

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]

    // ✅ IDs estables — ViewPager2 NO recrea el Fragment al volver
    override fun getItemId(position: Int): Long = position.toLong()

    override fun containsItem(itemId: Long): Boolean =
        itemId in 0 until fragments.size
}