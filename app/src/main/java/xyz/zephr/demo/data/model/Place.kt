package xyz.zephr.demo.data.model

import com.google.android.gms.maps.model.LatLng

data class Place(
    val id: String,
    val name: String,
    val description: String,
    val location: LatLng,
    val overtureId: String? = null,
    val isInFOV: Boolean = false
)
