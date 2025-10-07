package xyz.zephr.demo.presentation.map.model

import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow
import xyz.zephr.demo.data.model.Place

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
    val fovAngle: Float = 68f, // Default FOV angle in degrees
    val fovRadius: Float = 50f // Default FOV radius in meters
)

data class PlacesUiState(
    val places: List<Place> = emptyList(),
    val selectedPlace: Place? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val highlightedPlaceIds: Set<String> = emptySet()
)

data class MapState(
    val mapLoaded: Boolean = false,
    val isFollowingZephr: Boolean = true,
    val cameraPosition: MapCameraPosition = MapCameraPosition(),
    val showZephrOverlay: Boolean = true
)

data class MapCameraPosition(
    val target: LatLng? = null,
    val bearing: Float = 0f,
    val zoom: Float = 16f
)

