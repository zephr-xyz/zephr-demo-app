package xyz.zephr.demo.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.RectF
import android.text.TextPaint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import kotlin.math.ceil

/**
 * Utilities for generating map marker icons. These helpers are tightly coupled to the
 * Google Maps SDK because they return [com.google.android.gms.maps.model.BitmapDescriptor]
 * instances and call [com.google.android.gms.maps.MapsInitializer].
 */
object MapIconUtils {

    data class MarkerIconData(
        val descriptor: com.google.android.gms.maps.model.BitmapDescriptor,
        val anchorX: Float,
        val anchorY: Float
    )

    /**
     * Creates a circular dot icon for map markers
     * @param context Android context
     * @param radiusDp Radius of the dot in dp
     * @param color Compose Color
     * @return BitmapDescriptor for the dot icon
     */
    fun createDotIcon(
        context: Context,
        radiusDp: androidx.compose.ui.unit.Dp,
        color: Color
    ): com.google.android.gms.maps.model.BitmapDescriptor {
        // Ensure Maps SDK is initialized before creating BitmapDescriptor
        com.google.android.gms.maps.MapsInitializer.initialize(
            context.applicationContext,
            com.google.android.gms.maps.MapsInitializer.Renderer.LATEST,
            null
        )

        val radiusPx = (radiusDp.value * context.resources.displayMetrics.density).toInt()
        val diameter = radiusPx * 2
        val bmp = createBitmap(diameter, diameter)
        val canvas = Canvas(bmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = color.toArgb()
        canvas.drawCircle(radiusPx.toFloat(), radiusPx.toFloat(), radiusPx.toFloat(), paint)
        return BitmapDescriptorFactory.fromBitmap(bmp)
    }

    /**
     * Creates a glowing dot icon: a solid inner circle with a soft outer glow.
     */
    fun createGlowingDotIcon(
        context: Context,
        innerRadiusDp: androidx.compose.ui.unit.Dp,
        innerColor: Color,
        glowRadiusDp: androidx.compose.ui.unit.Dp,
        glowColor: Color,
        glowAlphaPrimary: Float = 0.28f,
        glowAlphaSecondary: Float = 0.16f
    ): com.google.android.gms.maps.model.BitmapDescriptor {
        // Ensure Maps SDK is initialized before creating BitmapDescriptor
        com.google.android.gms.maps.MapsInitializer.initialize(
            context.applicationContext,
            com.google.android.gms.maps.MapsInitializer.Renderer.LATEST,
            null
        )

        val density = context.resources.displayMetrics.density
        val innerRadiusPx = (innerRadiusDp.value * density)
        val glowRadiusPx = (glowRadiusDp.value * density)

        // Ensure glow radius is at least the inner radius
        val outerRadiusPx = maxOf(glowRadiusPx, innerRadiusPx * 1.5f)
        val diameter = (outerRadiusPx * 2).toInt()
        val bmp = createBitmap(diameter, diameter)
        val canvas = Canvas(bmp)

        // Clear canvas to transparent
        canvas.drawColor(android.graphics.Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        val center = outerRadiusPx
        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Draw soft outer glow (two passes for a simple gradient effect)
        glowPaint.color = glowColor.copy(alpha = glowAlphaPrimary).toArgb()
        canvas.drawCircle(center, center, outerRadiusPx, glowPaint)

        glowPaint.color = glowColor.copy(alpha = glowAlphaSecondary).toArgb()
        canvas.drawCircle(center, center, outerRadiusPx * 0.7f, glowPaint)

        // Draw the solid inner dot
        innerPaint.color = innerColor.toArgb()
        canvas.drawCircle(center, center, innerRadiusPx, innerPaint)

        return BitmapDescriptorFactory.fromBitmap(bmp)
    }

    /**
     * Creates a blue circle bitmap descriptor for the user location
     * @param context Android context
     * @param radiusDp Radius of the circle in dp (default MEDIUM size)
     * @return BitmapDescriptor of a blue circle
     */
    fun createBlueCircleBitmapDescriptor(
        context: Context,
        radiusDp: androidx.compose.ui.unit.Dp = Sizes.MEDIUM
    ): com.google.android.gms.maps.model.BitmapDescriptor {
        return createDotIcon(
            context = context,
            radiusDp = radiusDp,
            color = Color.Blue
        )
    }

    // Predefined sizes for common use cases
    object Sizes {
        val SMALL = 4.dp
        val MEDIUM = 6.dp
        val LARGE = 8.dp
        val EXTRA_LARGE = 12.dp
    }

    fun createLabeledDotIcon(
        context: Context,
        label: String,
        dotColor: Color,
        glowColor: Color? = null,
        backgroundColor: Color,
        textColor: Color,
        textSizeSp: Float = 12f,
        dotRadiusDp: androidx.compose.ui.unit.Dp = Sizes.MEDIUM
    ): MarkerIconData {
        com.google.android.gms.maps.MapsInitializer.initialize(
            context.applicationContext,
            com.google.android.gms.maps.MapsInitializer.Renderer.LATEST,
            null
        )

        val density = context.resources.displayMetrics.density
        val dotRadiusPx = dotRadiusDp.value * density
        val dotDiameterPx = dotRadiusPx * 2f
        val spacingPx = 4f * density
        val edgePaddingPx = 6f * density
        val labelPaddingHorizontalPx = 6f * density
        val labelPaddingVerticalPx = 4f * density

        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor.toArgb()
            textSize = textSizeSp * density
        }

        val trimmedLabel = if (label.length > 24) label.take(21) + "…" else label
        val textWidth = textPaint.measureText(trimmedLabel)
        val fontMetrics = textPaint.fontMetrics
        val textHeight = fontMetrics.bottom - fontMetrics.top

        val labelWidth = textWidth + labelPaddingHorizontalPx * 2f
        val contentWidth = edgePaddingPx * 2f + dotDiameterPx + spacingPx + labelWidth
        val contentHeight = maxOf(dotDiameterPx, textHeight + labelPaddingVerticalPx * 2f)

        val bitmapWidth = ceil(contentWidth).toInt().coerceAtLeast(1)
        val bitmapHeight = ceil(contentHeight).toInt().coerceAtLeast(1)

        val bmp = createBitmap(bitmapWidth, bitmapHeight)
        val canvas = Canvas(bmp)
        canvas.drawColor(android.graphics.Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        val centerY = bitmapHeight / 2f
        val dotCenterX = edgePaddingPx + dotRadiusPx

        // Draw label background
        val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = backgroundColor.toArgb()
        }
        val rectLeft = dotCenterX + dotRadiusPx + spacingPx
        val rectRight = rectLeft + labelWidth
        val textBaseline = centerY - (fontMetrics.ascent + fontMetrics.descent) / 2f
        val backgroundRect = RectF(
            rectLeft,
            textBaseline + fontMetrics.ascent - labelPaddingVerticalPx,
            rectRight,
            textBaseline + fontMetrics.descent + labelPaddingVerticalPx
        )
        val cornerRadius = contentHeight / 2f
        canvas.drawRoundRect(backgroundRect, cornerRadius, cornerRadius, backgroundPaint)

        // Draw dot
        glowColor?.let {
            val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = it.copy(alpha = 0.35f).toArgb()
            }
            canvas.drawCircle(dotCenterX, centerY, dotRadiusPx * 1.8f, glowPaint)
        }

        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = dotColor.toArgb()
        }
        canvas.drawCircle(dotCenterX, centerY, dotRadiusPx, dotPaint)

        // Draw text
        val textX = rectLeft + labelPaddingHorizontalPx
        canvas.drawText(trimmedLabel, textX, textBaseline, textPaint)

        val descriptor = BitmapDescriptorFactory.fromBitmap(bmp)
        val anchorX = dotCenterX / bitmapWidth.toFloat()
        val anchorY = 0.5f

        return MarkerIconData(descriptor, anchorX, anchorY)
    }
}
