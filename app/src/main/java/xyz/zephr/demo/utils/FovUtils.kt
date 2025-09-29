package xyz.zephr.demo.utils

import com.google.android.gms.maps.model.LatLng
import kotlin.math.cos
import kotlin.math.sin

/**
 * Utility functions for Field of View (FOV) calculations and visualization.
 */
object FovUtils {

    /**
     * Computes the points for a circular FOV sector (filled pie slice) visualization.
     *
     * Creates a filled sector showing the field of view with:
     * - Center point, arc points along the boundary, and back to center for closed shape
     * - Suitable for filled polygon rendering
     */
    fun computeFovSectorPoints(
        center: LatLng,
        bearing: Float,
        fovAngle: Float,
        radius: Float,
        arcPoints: Int = 20
    ): List<LatLng> {
        // Convert inputs to radians for trigonometric calculations
        val bearingRad = Math.toRadians(bearing.toDouble())
        val halfFovRad = Math.toRadians((fovAngle / 2).toDouble())

        // Earth's radius in meters (approximate)
        val earthRadius = 6371000.0

        // Convert radius from meters to angular distance (radians)
        val angularRadius = radius / earthRadius

        // Calculate the bearing angles for the FOV sector
        val startBearingRad = bearingRad - halfFovRad
        val endBearingRad = bearingRad + halfFovRad

        // Create sector points: start with center, add arc points, close back at center
        val sectorPoints = mutableListOf<LatLng>()

        // Start with center point
        sectorPoints.add(center)

        // Calculate angle step for smooth arc
        val angleStep = (endBearingRad - startBearingRad) / (arcPoints - 1)

        // Add points along the arc
        for (i in 0 until arcPoints) {
            val currentAngle = startBearingRad + (angleStep * i)
            val point = calculateDestinationPoint(center, angularRadius, currentAngle)
            sectorPoints.add(point)
        }

        return sectorPoints
    }

    /**
     * Calculates a destination point given a starting point, angular distance, and bearing.
     *
     * Uses the haversine formula for great circle navigation.
     */
    private fun calculateDestinationPoint(
        start: LatLng,
        angularDistance: Double,
        bearingRad: Double
    ): LatLng {
        val startLatRad = Math.toRadians(start.latitude)
        val startLngRad = Math.toRadians(start.longitude)

        val endLatRad = kotlin.math.asin(
            sin(startLatRad) * cos(angularDistance) +
                    cos(startLatRad) * sin(angularDistance) * cos(bearingRad)
        )

        val endLngRad = startLngRad + kotlin.math.atan2(
            sin(bearingRad) * sin(angularDistance) * cos(startLatRad),
            cos(angularDistance) - sin(startLatRad) * sin(endLatRad)
        )

        return LatLng(
            Math.toDegrees(endLatRad),
            Math.toDegrees(endLngRad)
        )
    }
}
