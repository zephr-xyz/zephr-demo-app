package xyz.zephr.demo.data.api

import retrofit2.http.GET
import retrofit2.http.Query
import xyz.zephr.demo.data.model.PlacesResponse

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
