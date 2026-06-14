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

    private var globalPercentage: Float = 0f
    private var usandoGlobal: Boolean = false
    private var segments: FloatArray = floatArrayOf(0f, 0f, 0f, 0f)

    // Colores unificados con colors.xml (dark theme)
    private val COLOR_C1 = Color.parseColor("#818CF8") // Índigo  — Cantidad
    private val COLOR_C2 = Color.parseColor("#34D399") // Esmeralda — Regularidad
    private val COLOR_C3 = Color.parseColor("#FB923C") // Naranja — Forma
    private val COLOR_C4 = Color.parseColor("#C084FC") // Violeta — Datos
    private val COLOR_BG = Color.parseColor("#475569") // Track visible en fondo oscuro (dark theme)

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 24f
        color = COLOR_BG
        strokeCap = Paint.Cap.BUTT
    }

    private val globalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 24f
        color = COLOR_C1
        strokeCap = Paint.Cap.ROUND
    }

    private val segmentPaints = listOf(
        Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 24f; color = COLOR_C1; strokeCap = Paint.Cap.BUTT },
        Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 24f; color = COLOR_C2; strokeCap = Paint.Cap.BUTT },
        Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 24f; color = COLOR_C3; strokeCap = Paint.Cap.BUTT },
        Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 24f; color = COLOR_C4; strokeCap = Paint.Cap.BUTT }
    )

    private val rect = RectF()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val padding = 20f
        val size = minOf(width, height).toFloat() - padding * 2
        val left = (width - size) / 2f
        val top  = (height - size) / 2f
        rect.set(left, top, left + size, top + size)

        canvas.drawArc(rect, -90f, 360f, false, bgPaint)

        if (usandoGlobal) {
            val sweep = (globalPercentage / 100f * 360f).coerceIn(0f, 360f)
            if (sweep > 0f) canvas.drawArc(rect, -90f, sweep, false, globalPaint)
        } else {
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

    /** Un solo arco con color personalizable — para ProgresoFragment (4 donuts de competencia) */
    fun setPercentage(pct: Float, arcColor: Int = COLOR_C1) {
        globalPercentage = pct.coerceIn(0f, 100f)
        globalPaint.color = arcColor
        usandoGlobal = true
        segments = floatArrayOf(0f, 0f, 0f, 0f)
        invalidate()
    }

    /** 4 segmentos proporcionales — para InicioFragment */
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