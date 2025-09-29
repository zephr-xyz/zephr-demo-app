package xyz.zephr.demo.presentation.map.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    fun onHeadingUpdate(headingDeg: Float, isCameraMoving: Boolean) {
        val currentState = _mapState.value
        if (!currentState.mapLoaded || isCameraMoving) return

        val target = headingDeg
        val current = currentState.cameraPosition.bearing
        val delta = shortestDeltaDeg(target, current)

        if (delta > 1.0f) {
            _mapState.value = currentState.copy(
                cameraPosition = currentState.cameraPosition.copy(bearing = target)
            )
        }
    }

    private fun shortestDeltaDeg(target: Float, current: Float): Float {
        val diff = ((target - current + 540f) % 360f) - 180f
        return kotlin.math.abs(diff)
    }
}
