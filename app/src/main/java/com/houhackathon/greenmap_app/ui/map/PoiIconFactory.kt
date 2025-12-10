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
import com.houhackathon.greenmap_app.domain.model.LocationType
import com.houhackathon.greenmap_app.ui.theme.BikeAmber
import com.houhackathon.greenmap_app.ui.theme.ChargeMint
import com.houhackathon.greenmap_app.ui.theme.ParkGreen
import com.houhackathon.greenmap_app.ui.theme.TourismCoral
import org.maplibre.android.annotations.Icon
import org.maplibre.android.annotations.IconFactory
import kotlin.math.max
import android.graphics.Color as AndroidColor

/**
 * Draws compact POI icons for bike rentals, parks, tourist spots, and charging stations.
 */
class PoiIconFactory(context: Context) {

    private val appContext = context.applicationContext
    private val cache = mutableMapOf<LocationType, Icon>()

    fun iconFor(type: LocationType): Icon {
        return cache.getOrPut(type) { buildIcon(type) }
    }

    private fun buildIcon(type: LocationType): Icon {
        val density = appContext.resources.displayMetrics.density
        val size = max((34 * density).toInt(), 48)
        val radius = size / 2f
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val simpleLabelTypes = setOf(
            LocationType.BICYCLE_RENTAL,
            LocationType.PUBLIC_PARK,
            LocationType.TOURIST_ATTRACTION
        )

        val baseColor = colorFor(type)
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f,
                0f,
                0f,
                size.toFloat(),
                lighten(baseColor, 0.15f),
                baseColor,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(radius, radius, radius, bgPaint)

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.White.copy(alpha = 0.22f).toArgb()
            style = Paint.Style.STROKE
            strokeWidth = 2f * density
        }
        canvas.drawCircle(radius, radius, radius - strokePaint.strokeWidth / 2, strokePaint)

        val symbolPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.White.toArgb()
            style = Paint.Style.STROKE
            strokeWidth = 2.4f * density
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.White.toArgb()
            style = Paint.Style.FILL
        }

        val label = labelFor(type)
        if (type in simpleLabelTypes) {
            drawCenteredLabel(canvas, label, radius, size)
        } else {
            when (type) {
                LocationType.CHARGING_STATION -> drawEv(canvas, symbolPaint, fillPaint, radius)
                else -> drawDot(canvas, fillPaint, radius)
            }
            drawBottomLabel(canvas, label, radius, size)
        }

        return IconFactory.getInstance(appContext).fromBitmap(bitmap)
    }

    private fun drawCenteredLabel(canvas: Canvas, label: String, center: Float, size: Int) {
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.White.toArgb()
            textAlign = Paint.Align.LEFT
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = size * 0.34f
        }
        val bounds = Rect()
        textPaint.getTextBounds(label, 0, label.length, bounds)
        val x = center - bounds.exactCenterX()
        val y = center - bounds.exactCenterY()
        canvas.drawText(label, x, y, textPaint)
    }

    private fun drawBottomLabel(canvas: Canvas, label: String, center: Float, size: Int) {
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.White.toArgb()
            textAlign = Paint.Align.LEFT
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = size * 0.18f
        }
        val bounds = Rect()
        textPaint.getTextBounds(label, 0, label.length, bounds)
        val y = center * 1.65f - bounds.exactCenterY()
        canvas.drawText(label, center - bounds.exactCenterX(), y, textPaint)
    }

    private fun drawEv(canvas: Canvas, stroke: Paint, fill: Paint, center: Float) {
        val bodyR = center * 0.6f
        val plug = Path().apply {
            val left = center - bodyR * 0.45f
            val right = center + bodyR * 0.45f
            val top = center - bodyR * 0.4f
            val bottom = center + bodyR * 0.5f
            moveTo(left, top)
            lineTo(right, top)
            lineTo(right, bottom)
            lineTo(left, bottom)
            close()
        }
        canvas.drawPath(plug, stroke)

        val bolt = Path().apply {
            val topX = center - bodyR * 0.15f
            val topY = center - bodyR * 0.05f
            moveTo(topX, topY)
            lineTo(topX + bodyR * 0.3f, topY)
            lineTo(topX - bodyR * 0.05f, topY + bodyR * 0.5f)
            lineTo(topX + bodyR * 0.25f, topY + bodyR * 0.5f)
            lineTo(topX - bodyR * 0.05f, topY + bodyR * 1.0f)
            close()
        }
        canvas.drawPath(bolt, fill)

        val prongHeight = bodyR * 0.4f
        val prongOffset = bodyR * 0.25f
        canvas.drawLine(center - prongOffset, center - bodyR * 0.7f, center - prongOffset, center - bodyR * 0.7f + prongHeight, stroke)
        canvas.drawLine(center + prongOffset, center - bodyR * 0.7f, center + prongOffset, center - bodyR * 0.7f + prongHeight, stroke)
    }

    private fun drawDot(canvas: Canvas, paint: Paint, center: Float) {
        canvas.drawCircle(center, center, center * 0.5f, paint)
    }

    private fun labelFor(type: LocationType): String = when (type) {
        LocationType.BICYCLE_RENTAL -> "BIKE"
        LocationType.PUBLIC_PARK -> "PARK"
        LocationType.TOURIST_ATTRACTION -> "TOUR"
        LocationType.CHARGING_STATION -> "EV"
        else -> type.displayName.take(4).uppercase()
    }

    @ColorInt
    private fun colorFor(type: LocationType): Int {
        val color = when (type) {
            LocationType.BICYCLE_RENTAL -> BikeAmber
            LocationType.PUBLIC_PARK -> ParkGreen
            LocationType.TOURIST_ATTRACTION -> TourismCoral
            LocationType.CHARGING_STATION -> ChargeMint
            else -> Color.Gray
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
