package com.houhackathon.greenmap_app.ui.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.Typeface
import androidx.annotation.ColorInt
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.houhackathon.greenmap_app.ui.theme.AqiGood
import com.houhackathon.greenmap_app.ui.theme.AqiHazardous
import com.houhackathon.greenmap_app.ui.theme.AqiModerate
import com.houhackathon.greenmap_app.ui.theme.AqiUnhealthy
import com.houhackathon.greenmap_app.ui.theme.AqiUnhealthySensitive
import com.houhackathon.greenmap_app.ui.theme.AqiUnknown
import com.houhackathon.greenmap_app.ui.theme.AqiVeryHazardous
import com.houhackathon.greenmap_app.ui.theme.AqiVeryUnhealthy
import org.maplibre.android.annotations.Icon
import org.maplibre.android.annotations.IconFactory
import kotlin.math.max
import android.graphics.Color as AndroidColor

/**
 * Builds and caches AQI icons that display the numeric index and a color
 * representing the severity category.
 */
class AqiIconFactory(context: Context) {

    private val appContext = context.applicationContext
    private val cache = mutableMapOf<String, Icon>()

    fun iconFor(aqi: Int?, category: VietnamAqiCategory?): Icon {
        val key = "${aqi ?: -1}_${category?.name ?: "unknown"}"
        return cache.getOrPut(key) { buildIcon(aqi, category) }
    }

    private fun buildIcon(aqi: Int?, category: VietnamAqiCategory?): Icon {
        val density = appContext.resources.displayMetrics.density
        val size = max((30 * density).toInt(), 48) // smaller footprint
        val radius = size / 2f
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val baseColor = colorFor(category)
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f,
                0f,
                0f,
                size.toFloat(),
                lighten(baseColor, 0.16f),
                baseColor,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(radius, radius, radius, fillPaint)

        var strokeWidth = 2 * density
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.White.copy(alpha = 0.2f).toArgb()
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
        }
        canvas.drawCircle(radius, radius, radius - strokeWidth / 2, strokePaint)

        val innerRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.White.copy(alpha = 0.12f).toArgb()
            style = Paint.Style.STROKE
            strokeWidth = density * 1.5f
        }
        canvas.drawCircle(radius, radius, radius * 0.78f, innerRingPaint)

        val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f,
                0f,
                0f,
                radius,
                Color.White.copy(alpha = 0.22f).toArgb(),
                Color.Transparent.toArgb(),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(radius, radius, radius, highlightPaint)

        val label = aqi?.coerceAtMost(500)?.toString() ?: "--"
        val tag = "AQI"

        val tagPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.White.copy(alpha = 0.78f).toArgb()
            textAlign = Paint.Align.LEFT
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = size * 0.16f
        }
        val tagBounds = Rect()
        tagPaint.getTextBounds(tag, 0, tag.length, tagBounds)
        val tagX = radius - tagBounds.exactCenterX()
        val tagY = radius * 0.62f - tagBounds.exactCenterY()
        canvas.drawText(tag, tagX, tagY, tagPaint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.White.toArgb()
            textAlign = Paint.Align.LEFT
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setShadowLayer(density * 2f, 0f, density * 1.2f, Color.Black.copy(alpha = 0.25f).toArgb())
        }
        val bounds = Rect()
        var textSize = size * 0.32f
        do {
            textPaint.textSize = textSize
            textPaint.getTextBounds(label, 0, label.length, bounds)
            textSize -= 1f
        } while ((bounds.width() > size * 0.7f || bounds.height() > size * 0.55f) && textSize > size * 0.22f)

        val x = radius - bounds.exactCenterX()
        val y = radius * 1.12f - bounds.exactCenterY()
        canvas.drawText(label, x, y, textPaint)

        return IconFactory.getInstance(appContext).fromBitmap(bitmap)
    }

    @ColorInt
    private fun colorFor(category: VietnamAqiCategory?): Int {
        val color = when (category) {
            VietnamAqiCategory.GOOD -> AqiGood
            VietnamAqiCategory.MODERATE -> AqiModerate
            VietnamAqiCategory.UNHEALTHY_FOR_SENSITIVE -> AqiUnhealthySensitive
            VietnamAqiCategory.UNHEALTHY -> AqiUnhealthy
            VietnamAqiCategory.VERY_UNHEALTHY -> AqiVeryUnhealthy
            VietnamAqiCategory.HAZARDOUS -> AqiHazardous
            VietnamAqiCategory.VERY_HAZARDOUS -> AqiVeryHazardous
            null -> AqiUnknown
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
