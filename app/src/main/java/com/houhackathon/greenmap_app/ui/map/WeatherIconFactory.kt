package com.houhackathon.greenmap_app.ui.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.Typeface
import androidx.annotation.ColorInt
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.houhackathon.greenmap_app.ui.theme.WeatherCloudy
import com.houhackathon.greenmap_app.ui.theme.WeatherFog
import com.houhackathon.greenmap_app.ui.theme.WeatherOvercast
import com.houhackathon.greenmap_app.ui.theme.WeatherRainy
import com.houhackathon.greenmap_app.ui.theme.WeatherSnow
import com.houhackathon.greenmap_app.ui.theme.WeatherStorm
import com.houhackathon.greenmap_app.ui.theme.WeatherSunny
import com.houhackathon.greenmap_app.ui.theme.WeatherUnknown
import org.maplibre.android.annotations.Icon
import org.maplibre.android.annotations.IconFactory
import kotlin.math.roundToInt
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import android.graphics.Color as AndroidColor

enum class WeatherCondition {
    CLEAR, CLOUDY, RAIN, STORM, FOG, OVERCAST, SNOW, UNKNOWN
}

/**
 * Builds small weather pictogram icons (sun/cloud/rain/storm/fog/snow/overcast)
 * with condition-based colors.
 */
class WeatherIconFactory(context: Context) {

    private val appContext = context.applicationContext
    private val cache = mutableMapOf<String, Icon>()

    fun iconFor(weatherType: String?, temperature: Double?): Icon {
        val condition = weatherType.toCondition()
        val roundedTemp = temperature?.roundToInt()
        val key = "${condition.name}_${roundedTemp ?: "na"}"
        return cache.getOrPut(key) { buildIcon(condition, roundedTemp) }
    }

    private fun buildIcon(condition: WeatherCondition, temperature: Int?): Icon {
        val density = appContext.resources.displayMetrics.density
        val size = max((32 * density).toInt(), 46) // compact marker
        val radius = size / 2f
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val baseColor = colorFor(condition)
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f,
                0f,
                0f,
                size.toFloat(),
                lighten(baseColor, 0.12f),
                baseColor,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(radius, radius, radius, bgPaint)

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.White.copy(alpha = 0.2f).toArgb()
            style = Paint.Style.STROKE
            strokeWidth = 1.8f * density
        }
        canvas.drawCircle(radius, radius, radius - strokePaint.strokeWidth / 2, strokePaint)

        val symbolPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.White.toArgb()
            style = Paint.Style.FILL
        }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.White.toArgb()
            style = Paint.Style.STROKE
            strokeWidth = 2.2f * density
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        when (condition) {
            WeatherCondition.CLEAR -> drawSun(canvas, symbolPaint, linePaint, radius, density)
            WeatherCondition.CLOUDY -> drawCloud(canvas, symbolPaint, radius, density)
            WeatherCondition.OVERCAST -> drawCloud(canvas, symbolPaint, radius, density, dense = true)
            WeatherCondition.RAIN -> drawRain(canvas, symbolPaint, linePaint, radius, density)
            WeatherCondition.STORM -> drawStorm(canvas, symbolPaint, linePaint, radius, density)
            WeatherCondition.FOG -> drawFog(canvas, linePaint, radius, density)
            WeatherCondition.SNOW -> drawSnow(canvas, symbolPaint, linePaint, radius, density)
            WeatherCondition.UNKNOWN -> drawUnknown(canvas, linePaint, radius, density)
        }

        temperature?.let { temp ->
            val tempText = "${temp}°"
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.White.toArgb()
                textAlign = Paint.Align.LEFT
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = size * 0.22f
            }
            val bounds = Rect()
            textPaint.getTextBounds(tempText, 0, tempText.length, bounds)
            val paddingX = size * 0.09f
            val paddingY = size * 0.05f
            val chipCenterY = radius * 1.5f
            val chipLeft = radius - bounds.exactCenterX() - paddingX
            val chipRight = radius - bounds.exactCenterX() + bounds.width() + paddingX
            val chipTop = chipCenterY - bounds.exactCenterY() - bounds.height() / 2f - paddingY
            val chipBottom = chipCenterY - bounds.exactCenterY() + bounds.height() / 2f + paddingY
            val chipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.Black.copy(alpha = 0.35f).toArgb()
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(
                chipLeft,
                chipTop,
                chipRight,
                chipBottom,
                size * 0.12f,
                size * 0.12f,
                chipPaint
            )
            canvas.drawText(tempText, radius - bounds.exactCenterX(), chipCenterY - bounds.exactCenterY(), textPaint)
        }

        return IconFactory.getInstance(appContext).fromBitmap(bitmap)
    }

    private fun drawSun(canvas: Canvas, fill: Paint, line: Paint, center: Float, density: Float) {
        val sunRadius = center * 0.28f
        canvas.drawCircle(center, center, sunRadius, fill)
        val rayLength = center * 0.55f
        val rayInner = center * 0.38f
        for (i in 0 until 8) {
            val angle = (i * 45) * (PI / 180).toFloat()
            val sx = center + rayInner * cos(angle)
            val sy = center + rayInner * sin(angle)
            val ex = center + rayLength * cos(angle)
            val ey = center + rayLength * sin(angle)
            canvas.drawLine(sx, sy, ex, ey, line)
        }
    }

    private fun drawCloud(canvas: Canvas, fill: Paint, center: Float, density: Float, dense: Boolean = false) {
        val y = center * 1.05f
        val mainR = center * (if (dense) 0.72f else 0.68f)
        val smallR = center * (if (dense) 0.42f else 0.38f)
        canvas.drawCircle(center, y, mainR, fill)
        canvas.drawCircle(center - mainR * 0.9f, y, smallR, fill)
        canvas.drawCircle(center + mainR * 0.78f, y - smallR * 0.35f, smallR, fill)
        val rectTop = y - smallR * 0.6f
        canvas.drawRect(center - mainR, rectTop, center + mainR, y + mainR * 0.4f, fill)
    }

    private fun drawRain(canvas: Canvas, fill: Paint, line: Paint, center: Float, density: Float) {
        drawCloud(canvas, fill, center, density)
        val startY = center * 1.52f
        val drop = center * 0.24f
        val offsets = listOf(-center * 0.35f, 0f, center * 0.35f)
        offsets.forEach { offset ->
            canvas.drawLine(center + offset, startY, center + offset - drop * 0.15f, startY + drop, line)
        }
    }

    private fun drawStorm(canvas: Canvas, fill: Paint, line: Paint, center: Float, density: Float) {
        drawCloud(canvas, fill, center, density, dense = true)
        val bolt = Path().apply {
            val topX = center - center * 0.12f
            val topY = center * 1.45f
            moveTo(topX, topY)
            lineTo(topX + center * 0.24f, topY)
            lineTo(topX, topY + center * 0.48f)
            lineTo(topX + center * 0.28f, topY + center * 0.48f)
            lineTo(topX - center * 0.12f, topY + center * 0.98f)
            close()
        }
        canvas.drawPath(bolt, fill)
    }

    private fun drawFog(canvas: Canvas, line: Paint, center: Float, density: Float) {
        val startY = center * 0.9f
        val step = density * 7
        repeat(4) { idx ->
            val y = startY + step * idx
            canvas.drawLine(center * 0.45f, y, center * 1.55f, y, line)
        }
    }

    private fun drawSnow(canvas: Canvas, fill: Paint, line: Paint, center: Float, density: Float) {
        drawCloud(canvas, fill, center, density)
        val snowPaint = Paint(line).apply { strokeWidth = density * 2f }
        val y = center * 1.55f
        val xOffsets = listOf(-center * 0.4f, 0f, center * 0.4f)
        xOffsets.forEach { xOffset ->
            val cx = center + xOffset
            canvas.drawLine(cx, y - density * 3, cx, y + density * 3, snowPaint)
            canvas.drawLine(cx - density * 3, y, cx + density * 3, y, snowPaint)
        }
    }

    private fun drawUnknown(canvas: Canvas, line: Paint, center: Float, density: Float) {
        val text = "?"
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.White.toArgb()
            textAlign = Paint.Align.LEFT
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = center * 1.1f
        }
        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)
        val x = center - bounds.exactCenterX()
        val y = center - bounds.exactCenterY()
        canvas.drawText(text, x, y, paint)
    }

    private fun String?.toCondition(): WeatherCondition {
        val normalized = this?.trim()?.lowercase() ?: return WeatherCondition.UNKNOWN
        return when {
            listOf("storm", "thunder", "sấm", "dông").any { normalized.contains(it) } -> WeatherCondition.STORM
            listOf("rain", "mưa", "shower", "drizzle").any { normalized.contains(it) } -> WeatherCondition.RAIN
            listOf("overcast", "u ám", "âm u").any { normalized.contains(it) } -> WeatherCondition.OVERCAST
            listOf("cloud", "mây").any { normalized.contains(it) } -> WeatherCondition.CLOUDY
            listOf("fog", "mist", "sương", "haze").any { normalized.contains(it) } -> WeatherCondition.FOG
            listOf("snow", "tuyết").any { normalized.contains(it) } -> WeatherCondition.SNOW
            listOf("sun", "clear", "nắng").any { normalized.contains(it) } -> WeatherCondition.CLEAR
            else -> WeatherCondition.UNKNOWN
        }
    }

    @ColorInt
    private fun colorFor(condition: WeatherCondition): Int {
        val color = when (condition) {
            WeatherCondition.CLEAR -> WeatherSunny
            WeatherCondition.CLOUDY -> WeatherCloudy
            WeatherCondition.RAIN -> WeatherRainy
            WeatherCondition.STORM -> WeatherStorm
            WeatherCondition.FOG -> WeatherFog
            WeatherCondition.OVERCAST -> WeatherOvercast
            WeatherCondition.SNOW -> WeatherSnow
            WeatherCondition.UNKNOWN -> WeatherUnknown
        }
        return color.toArgb()
    }

    @ColorInt
    private fun lighten(@ColorInt color: Int, fraction: Float): Int {
        val f = fraction.coerceIn(0f, 1f)
        val r = AndroidColor.red(color)
        val g = AndroidColor.green(color)
        val b = AndroidColor.blue(color)
        val a = AndroidColor.alpha(color)
        val nr = (r + (255 - r) * f).toInt()
        val ng = (g + (255 - g) * f).toInt()
        val nb = (b + (255 - b) * f).toInt()
        return AndroidColor.argb(a, nr, ng, nb)
    }
}
