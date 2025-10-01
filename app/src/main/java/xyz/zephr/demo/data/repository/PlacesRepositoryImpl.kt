package xyz.zephr.demo.data.repository

import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import xyz.zephr.demo.data.api.PlacesApiService
import xyz.zephr.demo.data.model.Place
import xyz.zephr.demo.domain.repository.PlacesRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Singleton
class PlacesRepositoryImpl @Inject constructor(
    private val placesApiService: PlacesApiService
) : PlacesRepository {

    private val _places = MutableStateFlow<List<Place>>(emptyList())
    private var currentLocation: LatLng? = null

    override fun getAllPlaces(): Flow<List<Place>> = _places.asStateFlow()

    override suspend fun getPlacesNearLocation(
        location: LatLng,
        radiusInMeters: Double
    ): List<Place> {
        return _places.value.filter { place ->
            // Simple distance calculation
            val distance = calculateDistance(location, place.location)
            distance <= radiusInMeters
        }
    }

    override suspend fun getPlaceById(id: String): Place? {
        return _places.value.find { it.id == id }
    }

    override suspend fun refreshPlaces(): Result<Unit> {
        return try {
            val location = currentLocation
            if (location != null) {
                // Fetch places from API within configured radius
                val apiPlaces = getNearbyPlaces(
                    lat = location.latitude,
                    lng = location.longitude,
                    radius = 1000.0, // 1km radius
                    limit = 50
                )

                _places.value = apiPlaces
                Result.success(Unit)
            } else {
                // Don't load any places without location
                _places.value = emptyList()
                Result.failure(Exception("No location available for fetching places"))
            }
        } catch (e: Exception) {
            _places.value = emptyList()
            Result.failure(e)
        }
    }

    override suspend fun initializeWithLocation(userLocation: LatLng) {
        currentLocation = userLocation

        try {
            val apiPlaces = getNearbyPlaces(
                lat = userLocation.latitude,
                lng = userLocation.longitude,
                radius = 1000.0, // 1km radius
                limit = 50
            )

            _places.value = apiPlaces
        } catch (_: Exception) {

            _places.value = emptyList()
        }
    }

    override suspend fun clearPlaces() {
        _places.value = emptyList()
        currentLocation = null
    }

    private suspend fun getNearbyPlaces(
        lat: Double,
        lng: Double,
        radius: Double,
        limit: Int
    ): List<Place> {
        return try {
            val response = placesApiService.getPlaces(
                lat = lat,
                lng = lng,
                distance = radius,
                minConfidence = 0.5,
                limit = limit
            )

            val places = response.features.take(limit).map { feature ->
                val props = feature.properties
                val name = props.name ?: props.overtureCompat?.names?.primary ?: "Unnamed"
                val category = props.primaryCategory ?: props.category
                ?: props.overtureCompat?.categories?.primary
                val address = props.fullAddress
                    ?: props.address
                    ?: props.overtureCompat?.addresses?.firstOrNull()?.freeform

                Place(
                    id = props.id,
                    name = name,
                    description = buildDescription(
                        category ?: "",
                        address
                    ),
                    location = LatLng(
                        feature.geometry.coordinates[1],
                        feature.geometry.coordinates[0]
                    ),
                    overtureId = props.overtureId ?: props.overtureCompat?.id,
                    isInFOV = false
                )
            }

            places
        } catch (e: Exception) {
            throw Exception("Failed to fetch places: ${e.message}")
        }
    }

    private fun buildDescription(category: String, address: String?): String {
        return if (address != null) {
            "$category - $address"
        } else {
            category
        }
    }

    private fun calculateDistance(location1: LatLng, location2: LatLng): Double {
        val earthRadius = 6371000.0 // meters

        val lat1Rad = Math.toRadians(location1.latitude)
        val lat2Rad = Math.toRadians(location2.latitude)
        val deltaLatRad = Math.toRadians(location2.latitude - location1.latitude)
        val deltaLngRad = Math.toRadians(location2.longitude - location1.longitude)

        val a = sin(deltaLatRad / 2) * sin(deltaLatRad / 2) +
                cos(lat1Rad) * cos(lat2Rad) *
                sin(deltaLngRad / 2) * sin(deltaLngRad / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }

}
