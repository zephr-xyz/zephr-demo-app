package xyz.zephr.places.network

import retrofit2.http.GET
import retrofit2.http.Query
import xyz.zephr.places.network.model.PlacesResponse

interface PlacesApiService {
    @GET("places/search")
    suspend fun getPlaces(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("distance") distance: Double,
        @Query("confidence") minConfidence: Double? = null,
        @Query("limit") limit: Int? = null
    ): PlacesResponse
}
