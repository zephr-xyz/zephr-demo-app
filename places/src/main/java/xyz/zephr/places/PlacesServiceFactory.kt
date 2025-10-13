package xyz.zephr.places

import com.google.gson.Gson
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import xyz.zephr.places.api.PlacesRepository
import xyz.zephr.places.network.PlacesApiService
import xyz.zephr.places.repository.DefaultPlacesRepository

/**
 * Builds a ready-to-use [PlacesRepository] backed by the Zephr Places API.
 */
fun createPlacesRepository(
    okHttpClient: OkHttpClient,
    gson: Gson,
    baseUrl: String = BuildConfig.API_BASE_URL
): PlacesRepository {
    val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl.ensureTrailingSlash())
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(okHttpClient)
        .build()

    val apiService = retrofit.create(PlacesApiService::class.java)
    return DefaultPlacesRepository(apiService)
}

private fun String.ensureTrailingSlash(): String =
    if (endsWith("/")) this else "$this/"
