package com.example.aplicacion_tesis.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class ConfettiView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private data class Particle(
        var x: Float,
        var y: Float,
        val vx: Float,
        val vy: Float,
        val vr: Float,       // velocidad de rotación
        var angle: Float,
        val size: Float,
        val color: Int,
        val shape: Int,      // 0=rect, 1=circle, 2=line
        var alpha: Float = 1f
    )

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val particles = mutableListOf<Particle>()
    private var startTime = 0L
    private val duration = 2800L   // ms que dura la lluvia
    private val fadeStart = 2000L  // ms en los que empieza el fade

    private val palette = intArrayOf(
        Color.parseColor("#818CF8"), // indigo
        Color.parseColor("#34D399"), // emerald
        Color.parseColor("#FBBF24"), // amber
        Color.parseColor("#F472B6"), // pink
        Color.parseColor("#60A5FA"), // blue
        Color.parseColor("#A78BFA"), // violet
        Color.parseColor("#FB923C"), // orange
    )

    fun launch() {
        particles.clear()
        startTime = System.currentTimeMillis()
        repeat(90) { spawnParticle() }
        visibility = VISIBLE
        postInvalidateOnAnimation()
    }

    private fun spawnParticle() {
        val w = width.toFloat().coerceAtLeast(1f)
        particles.add(
            Particle(
                x      = Random.nextFloat() * w,
                y      = Random.nextFloat() * -200f,
                vx     = Random.nextFloat() * 4f - 2f,
                vy     = Random.nextFloat() * 5f + 3f,
                vr     = Random.nextFloat() * 8f - 4f,
                angle  = Random.nextFloat() * 360f,
                size   = Random.nextFloat() * 10f + 5f,
                color  = palette[Random.nextInt(palette.size)],
                shape  = Random.nextInt(3)
            )
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val elapsed = System.currentTimeMillis() - startTime
        if (elapsed > duration) {
            visibility = GONE
            particles.clear()
            return
        }

        val globalAlpha = if (elapsed > fadeStart) {
            1f - (elapsed - fadeStart).toFloat() / (duration - fadeStart)
        } else 1f

        for (p in particles) {
            p.x    += p.vx
            p.y    += p.vy
            p.angle += p.vr

            val alpha = (globalAlpha * 255).toInt().coerceIn(0, 255)
            paint.color = p.color
            paint.alpha = alpha

            canvas.save()
            canvas.translate(p.x, p.y)
            canvas.rotate(p.angle)

            when (p.shape) {
                0 -> canvas.drawRect(-p.size / 2, -p.size / 4, p.size / 2, p.size / 4, paint)
                1 -> canvas.drawCircle(0f, 0f, p.size / 2.5f, paint)
                else -> {
                    paint.strokeWidth = 3f
                    paint.style = Paint.Style.STROKE
                    canvas.drawLine(-p.size / 2, 0f, p.size / 2, 0f, paint)
                    paint.style = Paint.Style.FILL
                }
            }

            canvas.restore()
        }

        // Eliminar partículas que salieron de pantalla por abajo
        particles.removeAll { it.y > height + 50 }
        // Reponer para mantener densidad durante los primeros 1200ms
        if (elapsed < 1200L && particles.size < 90) spawnParticle()

        postInvalidateOnAnimation()
    }
}