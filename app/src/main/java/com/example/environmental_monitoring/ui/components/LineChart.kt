package com.example.environmental_monitoring.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

@Composable
fun SimpleLineChart(
    values: List<Float>,
    minThreshold: Float? = null,
    maxThreshold: Float? = null,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    thresholdColor: Color = MaterialTheme.colorScheme.error,
    modifier: Modifier = Modifier
) {
    val gridColor   = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
    val labelColor  = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        if (values.isEmpty()) return@Canvas

        val padH = 8.dp.toPx()
        val padV = 8.dp.toPx()
        val w = size.width  - padH * 2
        val h = size.height - padV * 2

        // Calcular rango incluyendo umbrales
        val allValues = buildList {
            addAll(values)
            minThreshold?.let { add(it) }
            maxThreshold?.let { add(it) }
        }
        var minV = allValues.min()
        var maxV = allValues.max()
        if (minV == maxV) { minV -= 1f; maxV += 1f }
        val margin = (maxV - minV) * 0.15f
        minV -= margin; maxV += margin
        val range = (maxV - minV).coerceAtLeast(0.001f)

        fun yFor(v: Float) = padV + h - ((v - minV) / range) * h
        fun xFor(i: Int)   = if (values.size <= 1) padH + w / 2
                             else padH + (i.toFloat() / (values.size - 1)) * w

        // Líneas de cuadrícula horizontales (5 niveles)
        val dash = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
        repeat(5) { idx ->
            val y = padV + h * idx / 4f
            drawLine(gridColor, Offset(padH, y), Offset(padH + w, y), strokeWidth = 1.dp.toPx())
        }

        // Líneas de umbral (discontinuas, rojas)
        val threshDash = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
        minThreshold?.let {
            drawLine(thresholdColor, Offset(padH, yFor(it)), Offset(padH + w, yFor(it)),
                strokeWidth = 1.5.dp.toPx(), pathEffect = threshDash)
        }
        maxThreshold?.let {
            drawLine(thresholdColor, Offset(padH, yFor(it)), Offset(padH + w, yFor(it)),
                strokeWidth = 1.5.dp.toPx(), pathEffect = threshDash)
        }

        // Zona sombreada entre umbrales (si existen los dos)
        if (minThreshold != null && maxThreshold != null) {
            val yTop    = yFor(maxThreshold)
            val yBottom = yFor(minThreshold)
            drawRect(
                color   = lineColor.copy(alpha = 0.07f),
                topLeft = Offset(padH, yTop),
                size    = androidx.compose.ui.geometry.Size(w, yBottom - yTop)
            )
        }

        // Puntos en caso de lectura única
        if (values.size == 1) {
            drawCircle(color = lineColor, radius = 5.dp.toPx(),
                center = Offset(xFor(0), yFor(values[0])))
            return@Canvas
        }

        // Línea de datos principal
        for (i in 0 until values.size - 1) {
            drawLine(
                color       = lineColor,
                start       = Offset(xFor(i),   yFor(values[i])),
                end         = Offset(xFor(i + 1), yFor(values[i + 1])),
                strokeWidth = 2.5.dp.toPx(),
                cap         = StrokeCap.Round
            )
        }

        // Punto final destacado
        val lastX = xFor(values.size - 1)
        val lastY = yFor(values.last())
        drawCircle(color = lineColor.copy(alpha = 0.3f), radius = 8.dp.toPx(),
            center = Offset(lastX, lastY))
        drawCircle(color = lineColor, radius = 4.dp.toPx(),
            center = Offset(lastX, lastY))
    }
}
