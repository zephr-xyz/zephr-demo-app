package xyz.zephr.demo.utils

import com.google.android.gms.maps.model.LatLng
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
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

    fun isPointInFov(
        userLocation: LatLng,
        targetLocation: LatLng,
        heading: Float,
        fovAngle: Float,
        radiusMeters: Double
    ): Boolean {
        val distanceMeters = haversineDistance(userLocation, targetLocation)
        if (distanceMeters > radiusMeters) return false

        val bearingToTarget = bearingBetween(userLocation, targetLocation)
        val angleDiff = angularDifferenceDegrees(heading.toDouble(), bearingToTarget)
        return angleDiff <= fovAngle / 2.0
    }

    private fun haversineDistance(start: LatLng, end: LatLng): Double {
        val earthRadius = 6371000.0

        val startLatRad = Math.toRadians(start.latitude)
        val endLatRad = Math.toRadians(end.latitude)
        val deltaLatRad = Math.toRadians(end.latitude - start.latitude)
        val deltaLngRad = Math.toRadians(end.longitude - start.longitude)

        val a = sin(deltaLatRad / 2).pow(2.0) +
                cos(startLatRad) * cos(endLatRad) * sin(deltaLngRad / 2).pow(2.0)
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        return earthRadius * c
    }

    private fun bearingBetween(start: LatLng, end: LatLng): Double {
        val startLatRad = Math.toRadians(start.latitude)
        val startLngRad = Math.toRadians(start.longitude)
        val endLatRad = Math.toRadians(end.latitude)
        val endLngRad = Math.toRadians(end.longitude)
        val deltaLng = endLngRad - startLngRad

        val y = sin(deltaLng) * cos(endLatRad)
        val x = cos(startLatRad) * sin(endLatRad) -
                sin(startLatRad) * cos(endLatRad) * cos(deltaLng)
        val bearing = Math.toDegrees(kotlin.math.atan2(y, x))
        return (bearing + 360) % 360
    }

    private fun angularDifferenceDegrees(angle1: Double, angle2: Double): Double {
        val diff = (angle2 - angle1 + 540) % 360 - 180
        return abs(diff)
    }
}
