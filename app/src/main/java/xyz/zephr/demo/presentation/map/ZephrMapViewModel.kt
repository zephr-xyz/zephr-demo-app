package xyz.zephr.demo.presentation.map

import android.app.Application
import android.app.Service
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import xyz.zephr.demo.TAG
import xyz.zephr.sdk.v2.ZephrEventListener
import xyz.zephr.sdk.v2.ZephrLocationManager
import xyz.zephr.sdk.v2.model.ZephrGnssEvent
import xyz.zephr.sdk.v2.model.ZephrPoseEvent

class ZephrMapViewModel(
    application: Application
): AndroidViewModel(application) {
    private val locationManager by lazy {
        application.getSystemService(Service.LOCATION_SERVICE) as LocationManager
    }

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val zephrListener = object : ZephrEventListener {
        override fun onZephrGnssReceived(zephrGnssEvent: ZephrGnssEvent) {
            val status = zephrGnssEvent.status
            val location = zephrGnssEvent.location
            if (location != null) {
                Log.d(
                    TAG,
                    "GNSS Update - Status: $status, Lat: ${location.latitude}, Lng: ${location.longitude}, Alt: ${location.altitude}"
                )
                _uiState.value = _uiState.value.copy(zephrLocation = LatLng(location.latitude, location.longitude))
            } else {
                Log.d(TAG, "GNSS Update - Status: $status, Location: null")
            }
        }

        override fun onPoseChanged(
            zephrPoseEvent: ZephrPoseEvent
        ) {
            Log.d(
                TAG,
                "Pose Update - yaw: ${zephrPoseEvent.yprWithTimestamp?.first?.get(0)} pitch: ${zephrPoseEvent.yprWithTimestamp?.first?.get(1)} roll: ${zephrPoseEvent.yprWithTimestamp?.first?.get(2)}"
            )

            zephrPoseEvent.headingDegWithTimestamp?.let {
                _uiState.value = _uiState.value.copy(heading = it.first)
            }
        }
    }

    private val androidListener =
        LocationListener { p0 -> _uiState.value = _uiState.value.copy(androidLocation = LatLng(p0.latitude, p0.longitude)) }

    fun start() {
        try {
            ZephrLocationManager.start(application)
            ZephrLocationManager.requestLocationUpdates(zephrListener)

            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0L,
                0f,
                androidListener
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "Location updates stopped, likely due to revoked permission. Error: ${e.message}")
        }
    }

    fun stop() {
        ZephrLocationManager.stop(application)
        ZephrLocationManager.removeLocationUpdates(zephrListener)
        locationManager.removeUpdates(androidListener)
    }

    fun onMapLoaded() {
        _uiState.value = _uiState.value.copy(mapLoaded = true)
    }
}

data class MapUiState(
    val androidLocation: LatLng? = null,
    val zephrLocation: LatLng? = null,
    val heading: Float = 0f,
    val mapLoaded: Boolean = false
)