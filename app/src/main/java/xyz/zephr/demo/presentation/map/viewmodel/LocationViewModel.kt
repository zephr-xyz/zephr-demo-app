package xyz.zephr.demo.presentation.map.viewmodel

import android.app.Application
import android.app.Service
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import xyz.zephr.demo.TAG
import xyz.zephr.demo.presentation.map.model.LocationState
import xyz.zephr.demo.utils.FovUtils
import xyz.zephr.sdk.v2.ZephrEventListener
import xyz.zephr.sdk.v2.ZephrLocationManager
import xyz.zephr.sdk.v2.model.ZephrLocationEvent
import xyz.zephr.sdk.v2.model.ZephrPoseEvent
import javax.inject.Inject
import kotlin.math.abs

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

    private val zephrLocationFlow = MutableStateFlow<LatLng?>(null)
    private val zephrHeadingFlow = MutableStateFlow<Float?>(null)

    private var latestLocation: LatLng? = null
    private var latestHeading: Float = 0f

    private val zephrListener = object : ZephrEventListener {
        override fun onZephrLocationChanged(zephrLocationEvent: ZephrLocationEvent) {
            val status = zephrLocationEvent.status
            val location = zephrLocationEvent.location
            if (location != null) {
                Log.d(
                    TAG,
                    "GNSS Update - Status: $status, Lat: ${location.latitude}, Lng: ${location.longitude}, Alt: ${location.altitude}"
                )
                zephrLocationFlow.value = LatLng(location.latitude, location.longitude)
            } else {
                Log.d(TAG, "GNSS Update - Status: $status, Location: null")
                zephrLocationFlow.value = null
            }
        }

        override fun onPoseChanged(zephrPoseEvent: ZephrPoseEvent) {
            val headingDeg = zephrPoseEvent.headingDegWithTimestamp?.first
            if (headingDeg != null) {
                Log.d(TAG, "Bearing Update: Heading=$headingDeg° (source: timestamp)")
                zephrHeadingFlow.value = headingDeg
            } else {
                Log.d(TAG, "Bearing Update: No headingDegWithTimestamp available")
            }
        }
    }

    private val androidListener = LocationListener { location ->
        _locationState.value = _locationState.value.copy(
            androidLocation = LatLng(location.latitude, location.longitude)
        )
    }

    init {
        startCollectors()
    }

    private fun startCollectors() {
        viewModelScope.launch {
            zephrLocationFlow.collect { location ->
                latestLocation = location
                val current = _locationState.value
                val shouldEmit = when {
                    location == null -> current.zephrLocation != null
                    current.zephrLocation == null -> true
                    else -> distanceBetweenMeters(
                        location,
                        current.zephrLocation!!
                    ) >= LOCATION_THRESHOLD_METERS
                }
                if (shouldEmit) {
                    emitLocationState(force = true)
                }
            }
        }

        viewModelScope.launch {
            zephrHeadingFlow.collect { heading ->
                if (heading == null) return@collect
                val previousHeading = latestHeading
                latestHeading = heading
                if (abs(heading - previousHeading) >= HEADING_THRESHOLD_DEGREES) {
                    emitLocationState()
                }
            }
        }
    }

    private fun emitLocationState(force: Boolean = false) {
        val location = latestLocation
        val heading = latestHeading
        val current = _locationState.value

        val locationChanged = when {
            location == null && current.zephrLocation == null -> false
            location == null || current.zephrLocation == null -> true
            else -> distanceBetweenMeters(
                location,
                current.zephrLocation!!
            ) >= LOCATION_THRESHOLD_METERS
        }
        val headingChanged = abs(heading - current.heading) >= HEADING_THRESHOLD_DEGREES

        if (!force && !locationChanged && !headingChanged) return

        val fovPoints = if (location != null) {
            FovUtils.computeFovSectorPoints(
                center = location,
                bearing = heading,
                fovAngle = current.fovAngle,
                radius = current.fovRadius
            )
        } else {
            emptyList()
        }

        _locationState.value = current.copy(
            zephrLocation = location,
            heading = heading,
            fovPoints = fovPoints
        )
    }

    private fun distanceBetweenMeters(a: LatLng, b: LatLng): Float {
        val results = FloatArray(1)
        Location.distanceBetween(a.latitude, a.longitude, b.latitude, b.longitude, results)
        return results.first()
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

    companion object {
        private const val LOCATION_THRESHOLD_METERS = 0.5f
        private const val HEADING_THRESHOLD_DEGREES = 0.5f
    }
}
