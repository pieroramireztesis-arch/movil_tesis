package com.example.aplicacion_tesis.ui.components

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.max

class DonutChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Para ProgresoFragment: un solo porcentaje global
    private var globalPercentage: Float = 0f
    private var usandoGlobal: Boolean = false

    // Para InicioFragment: 4 segmentos por competencia
    private var segments: FloatArray = floatArrayOf(0f, 0f, 0f, 0f)

    // =============================================
    // COLORES UNIFICADOS — igual en barras y donut
    // Competencia 1 → Azul
    // Competencia 2 → Verde
    // Competencia 3 → Naranja
    // Competencia 4 → Morado
    // =============================================
    private val COLOR_C1 = Color.parseColor("#0A6FD4") // Azul   — Cantidad
    private val COLOR_C2 = Color.parseColor("#27AE60") // Verde  — Regularidad
    private val COLOR_C3 = Color.parseColor("#E67E22") // Naranja— Forma
    private val COLOR_C4 = Color.parseColor("#7B1FA2") // Morado — Datos
    private val COLOR_BG = Color.parseColor("#DCE8F5") // Fondo gris-azulado

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 30f
        color = COLOR_BG
        strokeCap = Paint.Cap.BUTT
    }

    // Paint para el donut de progreso general (un solo color azul)
    private val globalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 30f
        color = COLOR_C1
        strokeCap = Paint.Cap.ROUND
    }

    // Paints para los 4 segmentos (InicioFragment)
    private val segmentPaints = listOf(
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 30f
            color = COLOR_C1
            strokeCap = Paint.Cap.BUTT
        },
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 30f
            color = COLOR_C2
            strokeCap = Paint.Cap.BUTT
        },
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 30f
            color = COLOR_C3
            strokeCap = Paint.Cap.BUTT
        },
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 30f
            color = COLOR_C4
            strokeCap = Paint.Cap.BUTT
        }
    )

    private val rect = RectF()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val padding = 28f
        val size = minOf(width, height).toFloat() - padding * 2
        val left = (width - size) / 2f
        val top  = (height - size) / 2f
        rect.set(left, top, left + size, top + size)

        // Siempre dibuja el fondo completo gris
        canvas.drawArc(rect, -90f, 360f, false, bgPaint)

        if (usandoGlobal) {
            // MODO PROGRESO GENERAL: un solo arco azul
            val sweep = (globalPercentage / 100f * 360f).coerceIn(0f, 360f)
            if (sweep > 0f) {
                canvas.drawArc(rect, -90f, sweep, false, globalPaint)
            }
        } else {
            // MODO 4 COMPETENCIAS: 4 segmentos de colores
            val total = segments.sum()
            if (total > 0f) {
                var startAngle = -90f
                for (i in segments.indices) {
                    val angle = (segments[i] / total) * 360f
                    if (angle > 0f) {
                        canvas.drawArc(rect, startAngle, angle, false, segmentPaints[i])
                        startAngle += angle
                    }
                }
            }
        }
    }

    /**
     * Para ProgresoFragment — muestra UN solo arco azul con el % general.
     * El resto del anillo queda en gris (fondo).
     */
    fun setPercentage(pct: Float) {
        globalPercentage = pct.coerceIn(0f, 100f)
        usandoGlobal = true
        segments = floatArrayOf(0f, 0f, 0f, 0f)
        invalidate()
    }

    /**
     * Para InicioFragment — muestra 4 segmentos, uno por competencia.
     * Cada segmento proporcional al porcentaje de esa competencia.
     */
    fun setData(p1: Int, p2: Int, p3: Int, p4: Int) {
        usandoGlobal = false
        segments = floatArrayOf(
            max(0, p1).toFloat(),
            max(0, p2).toFloat(),
            max(0, p3).toFloat(),
            max(0, p4).toFloat()
        )
        invalidate()
    }
}