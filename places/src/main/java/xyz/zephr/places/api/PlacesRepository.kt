package xyz.zephr.places.api

import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow

/**
 * Contract for providing place data to the host application. Implementations can source places
 * from any backend or local provider and remain swappable at build time.
 */
interface PlacesRepository {
    fun getAllPlaces(): Flow<List<Place>>

    suspend fun getPlacesNearLocation(
        location: LatLng,
        radiusInMeters: Double
    ): List<Place>

    suspend fun getPlaceById(id: String): Place?

    suspend fun refreshPlaces(): Result<Unit>

    suspend fun initializeWithLocation(userLocation: LatLng)

    suspend fun clearPlaces()
}
