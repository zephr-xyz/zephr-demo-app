package xyz.zephr.demo.presentation.map.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import xyz.zephr.demo.presentation.map.model.MapCameraPosition
import xyz.zephr.demo.presentation.map.model.MapState
import javax.inject.Inject

/**
 * ViewModel for map UI state and camera control.
 * Independent of specific map engine implementation.
 */
@HiltViewModel
class MapViewModel @Inject constructor() : ViewModel() {

    private val _mapState = MutableStateFlow(MapState())
    val mapState: StateFlow<MapState> = _mapState.asStateFlow()

    fun onMapLoaded() {
        Log.d("xyz.zephr.demo", "onMapLoaded called - setting mapLoaded = true")
        _mapState.value = _mapState.value.copy(mapLoaded = true)
        Log.d(
            "xyz.zephr.demo",
            "onMapLoaded completed - mapLoaded is now: ${_mapState.value.mapLoaded}"
        )
    }

    fun toggleZephrOverlay() {
        _mapState.value = _mapState.value.copy(
            showZephrOverlay = !_mapState.value.showZephrOverlay
        )
    }

    fun updateCamera(position: MapCameraPosition) {
        val currentState = _mapState.value
        if (!currentState.mapLoaded) return

        val current = currentState.cameraPosition
        val currentTarget = current.target
        val newTarget = position.target

        val targetChanged = if (currentTarget == null || newTarget == null) {
            newTarget != null
        } else {
            distanceBetween(currentTarget, newTarget) > LOCATION_THRESHOLD_METERS
        }

        val bearingChanged =
            shortestDeltaDeg(position.bearing, current.bearing) > HEADING_THRESHOLD_DEGREES

        if (!targetChanged && !bearingChanged) return

        _mapState.value = currentState.copy(
            cameraPosition = current.copy(
                target = newTarget ?: currentTarget,
                bearing = position.bearing,
                zoom = position.zoom
            )
        )
    }

    fun onHeadingUpdate(headingDeg: Float, isCameraMoving: Boolean) {
        if (isCameraMoving) return
        updateCamera(_mapState.value.cameraPosition.copy(bearing = headingDeg))
    }

    private fun shortestDeltaDeg(target: Float, current: Float): Float {
        val diff = ((target - current + 540f) % 360f) - 180f
        return kotlin.math.abs(diff)
    }

    private fun distanceBetween(
        a: com.google.android.gms.maps.model.LatLng,
        b: com.google.android.gms.maps.model.LatLng
    ): Float {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            a.latitude,
            a.longitude,
            b.latitude,
            b.longitude,
            results
        )
        return results.first()
    }

    companion object {
        private const val HEADING_THRESHOLD_DEGREES = 0.5f
        private const val LOCATION_THRESHOLD_METERS = 0.5f
    }
}
