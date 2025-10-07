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

    fun destinationPoint(
        start: LatLng,
        distanceMeters: Double,
        bearingDegrees: Double
    ): LatLng {
        val earthRadius = 6371000.0
        val angularDistance = distanceMeters / earthRadius
        val bearingRad = Math.toRadians(bearingDegrees)
        return calculateDestinationPoint(start, angularDistance, bearingRad)
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
