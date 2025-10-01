package xyz.zephr.demo.presentation.map.viewmodel

import android.app.Application
import android.app.Service
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import xyz.zephr.demo.TAG
import xyz.zephr.demo.presentation.map.model.LocationState
import xyz.zephr.demo.utils.FovUtils
import xyz.zephr.sdk.v2.ZephrEventListener
import xyz.zephr.sdk.v2.ZephrLocationManager
import xyz.zephr.sdk.v2.model.ZephrLocationEvent
import xyz.zephr.sdk.v2.model.ZephrPoseEvent
import javax.inject.Inject

/**
 * ViewModel for location tracking functionality.
 * Can be used independently for location-only features.
 */
@HiltViewModel
class LocationViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val locationManager by lazy {
        application.getSystemService(Service.LOCATION_SERVICE) as LocationManager
    }

    private val _locationState = MutableStateFlow(LocationState())
    val locationState: StateFlow<LocationState> = _locationState.asStateFlow()


    private val zephrListener = object : ZephrEventListener {
        override fun onZephrLocationChanged(zephrLocationEvent: ZephrLocationEvent) {
            val status = zephrLocationEvent.status
            val location = zephrLocationEvent.location
            if (location != null) {
                Log.d(
                    TAG,
                    "GNSS Update - Status: $status, Lat: ${location.latitude}, Lng: ${location.longitude}, Alt: ${location.altitude}"
                )
                _locationState.value = _locationState.value.copy(
                    zephrLocation = LatLng(location.latitude, location.longitude)
                )
                updateFovPoints()
            } else {
                Log.d(TAG, "GNSS Update - Status: $status, Location: null")
                updateFovPoints()
            }
        }

        override fun onPoseChanged(zephrPoseEvent: ZephrPoseEvent) {
            Log.d(
                TAG,
                "Pose Update - yaw: ${zephrPoseEvent.yprWithTimestamp?.first?.get(0)} pitch: ${
                    zephrPoseEvent.yprWithTimestamp?.first?.get(
                        1
                    )
                } roll: ${zephrPoseEvent.yprWithTimestamp?.first?.get(2)}"
            )

            // Use heading in degrees reported by SDK
            val headingDeg = zephrPoseEvent.headingDegWithTimestamp?.first
            if (headingDeg != null) {
                Log.d(TAG, "Bearing Update: Heading=$headingDeg° (source: timestamp)")
                _locationState.value = _locationState.value.copy(heading = headingDeg)
                updateFovPoints()
            } else {
                Log.d(TAG, "Bearing Update: No headingDegWithTimestamp available")
            }
        }
    }

    private val androidListener = LocationListener { p0 ->
        _locationState.value = _locationState.value.copy(
            androidLocation = LatLng(p0.latitude, p0.longitude)
        )
    }

    /**
     * Updates the FOV points based on current location, heading, FOV angle, and radius.
     */
    private fun updateFovPoints() {
        val currentState = _locationState.value
        val zephrLocation = currentState.zephrLocation

        if (zephrLocation != null) {
            val fovPoints = FovUtils.computeFovSectorPoints(
                center = zephrLocation,
                bearing = currentState.heading,
                fovAngle = currentState.fovAngle,
                radius = currentState.fovRadius
            )
            _locationState.value = currentState.copy(fovPoints = fovPoints)
        } else {
            // Clear FOV points if no location available
            _locationState.value = currentState.copy(fovPoints = emptyList())
        }
    }

    fun startLocationUpdates() {
        try {
            ZephrLocationManager.start(getApplication())
            ZephrLocationManager.requestLocationUpdates(zephrListener)

            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0L,
                0f,
                androidListener
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "Location updates stopped. Error: ${e.message}")
        }
    }

    fun stopLocationUpdates() {
        ZephrLocationManager.stop(getApplication())
        ZephrLocationManager.removeLocationUpdates(zephrListener)
        locationManager.removeUpdates(androidListener)
    }
}
