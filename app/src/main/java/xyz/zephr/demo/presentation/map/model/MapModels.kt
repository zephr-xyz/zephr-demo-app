package xyz.zephr.demo.presentation.map.model

import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow

/**
 * Interface for map engine abstraction.
 * Preparation for allowing swapping between Google Maps, Mapbox, etc.
 * Not used yet
 */
interface MapEngine {
    fun animateCamera(position: MapCameraPosition, durationMs: Int = 2000)
    fun moveCamera(position: MapCameraPosition)
    val isCameraMoving: Flow<Boolean>
}

data class LocationState(
    val zephrLocation: LatLng? = null,
    val androidLocation: LatLng? = null,
    val heading: Float = 0f,
    val fovPoints: List<LatLng> = emptyList(),
    val fovAngle: Float = 68f, // Default FOV angle in degrees
    val fovRadius: Float = 250f // Default FOV radius in meters
)

data class MapState(
    val mapLoaded: Boolean = false,
    val isFollowingZephr: Boolean = true,
    val cameraPosition: MapCameraPosition = MapCameraPosition()
)

data class MapCameraPosition(
    val target: LatLng? = null,
    val bearing: Float = 0f,
    val zoom: Float = 16f
)

