package com.example.aplicacion_tesis.ui.teacher

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * F2: pager del panel docente — 4 fragments fijos, igual que HomePagerAdapter
 * del estudiante. IDs estables para que ViewPager2 no recree fragments.
 */
class TeacherPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    private val fragments: List<Fragment> = listOf(
        TeacherInicioFragment(),
        TeacherReportsFragment(),
        TeacherAlertsFragment(),
        TeacherPerfilFragment()
    )

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun containsItem(itemId: Long): Boolean =
        itemId in 0 until fragments.size
}