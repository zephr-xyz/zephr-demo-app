package xyz.zephr.demo.ui.map

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import xyz.zephr.demo.ui.theme.ZephrOrange
import xyz.zephr.demo.utils.FovUtils
import kotlin.math.cos
import kotlin.math.sin

/**
 * Draws a screen-fixed FOV wedge anchored at the user's location while the map rotates underneath.
 */
@Composable
fun FovSector(
    cameraPositionState: CameraPositionState,
    centerLocation: LatLng?,
    radiusMeters: Float,
    fovAngle: Float,
    alpha: Float,
    modifier: Modifier = Modifier,
    fillColor: Color = ZephrOrange,
    strokeColor: Color = ZephrOrange,
    strokeWidth: Dp = 1.dp
) {
    if (centerLocation == null || alpha <= 0f) return

    val projection = cameraPositionState.projection ?: return

    // Track the current camera snapshot so this composable recomposes when zoom/bearing updates.
    val cameraPositionSnapshot = cameraPositionState.position
    cameraPositionSnapshot.zoom // read to stay subscribed to camera updates

    val density = LocalDensity.current
    val strokeWidthPx = with(density) { strokeWidth.toPx() }

    Canvas(modifier = modifier) {
        val centerPoint = projection.toScreenLocation(centerLocation)
        val center = Offset(centerPoint.x.toFloat(), centerPoint.y.toFloat())

        val radiusReferencePoint = FovUtils.destinationPoint(
            start = centerLocation,
            distanceMeters = radiusMeters.toDouble(),
            bearingDegrees = 0.0
        )
        val radiusPoint = projection.toScreenLocation(radiusReferencePoint)
        val radiusVector = Offset(
            (radiusPoint.x - centerPoint.x).toFloat(),
            (radiusPoint.y - centerPoint.y).toFloat()
        )
        val radius = radiusVector.getDistance()
        if (radius <= 0f || !radius.isFinite()) return@Canvas

        val rectTopLeft = Offset(center.x - radius, center.y - radius)
        val rectSize = Size(radius * 2, radius * 2)

        val startAngle = -fovAngle / 2f - 90f
        val sweepAngle = fovAngle

        drawArc(
            color = fillColor.copy(alpha = 0.4f * alpha),
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = true,
            topLeft = rectTopLeft,
            size = rectSize
        )

        drawArc(
            color = strokeColor.copy(alpha = 0.6f * alpha),
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = rectTopLeft,
            size = rectSize,
            style = Stroke(width = strokeWidthPx)
        )

        val startAngleRad = Math.toRadians(startAngle.toDouble())
        val endAngleRad = Math.toRadians((startAngle + sweepAngle).toDouble())
        val startEdge = Offset(
            x = center.x + radius * cos(startAngleRad).toFloat(),
            y = center.y + radius * sin(startAngleRad).toFloat()
        )
        val endEdge = Offset(
            x = center.x + radius * cos(endAngleRad).toFloat(),
            y = center.y + radius * sin(endAngleRad).toFloat()
        )

        drawLine(
            color = strokeColor.copy(alpha = 0.6f * alpha),
            start = center,
            end = startEdge,
            strokeWidth = strokeWidthPx
        )

        drawLine(
            color = strokeColor.copy(alpha = 0.6f * alpha),
            start = center,
            end = endEdge,
            strokeWidth = strokeWidthPx
        )
    }
}

private fun Float.isFinite(): Boolean = !isNaN() && !isInfinite()
