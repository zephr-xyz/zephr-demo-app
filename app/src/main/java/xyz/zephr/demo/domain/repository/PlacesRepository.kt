package xyz.zephr.demo.domain.repository

import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow
import xyz.zephr.demo.data.model.Place

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
