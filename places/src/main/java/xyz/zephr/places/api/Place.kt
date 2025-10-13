package xyz.zephr.places.api

import com.google.android.gms.maps.model.LatLng

/**
 * Represents a single place on the map. Implementations can enrich these values with additional
 * fields as long as the core contract (id, name, description, location) remains stable.
 */
data class Place(
    val id: String,
    val name: String,
    val description: String,
    val location: LatLng,
    val overtureId: String? = null,
    val isInFOV: Boolean = false
)
