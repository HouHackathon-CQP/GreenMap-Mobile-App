import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import org.maplibre.android.annotations.Icon
import org.maplibre.android.annotations.IconFactory

fun Context.toMapLibreIcon(
    @DrawableRes drawableResId: Int,
    @ColorInt tintColor: Int? = null
): Icon {
    val drawable = AppCompatResources.getDrawable(this, drawableResId)
        ?.mutate()
        ?: throw IllegalArgumentException("Drawable not found")

    tintColor?.let { drawable.setTint(it) }

    val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 64
    val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 64

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)

    return IconFactory.getInstance(this).fromBitmap(bitmap)
}

fun Context.toMapLibreIcon(
    @DrawableRes drawableResId: Int,
    tintColor: Color
): Icon = toMapLibreIcon(
    drawableResId = drawableResId,
    tintColor = tintColor.toArgb()
)


