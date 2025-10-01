package xyz.zephr.demo.data.model

import com.google.gson.annotations.SerializedName

data class PlacesResponse(
    @SerializedName("type")
    val type: String,
    @SerializedName("features")
    val features: List<PlaceFeature>,
    @SerializedName("count")
    val count: Int? = null
)

data class PlaceFeature(
    @SerializedName("type")
    val type: String,
    @SerializedName("properties")
    val properties: PlaceProperties,
    @SerializedName("geometry")
    val geometry: PlaceGeometry
)

data class PlaceGeometry(
    @SerializedName("type")
    val type: String,
    @SerializedName("coordinates")
    val coordinates: List<Double> // [longitude, latitude]
)

data class PlaceProperties(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("category")
    val category: String?,
    @SerializedName("primary_category")
    val primaryCategory: String?,
    @SerializedName("alternate_categories")
    val alternateCategories: String?,
    @SerializedName("address")
    val address: String?,
    @SerializedName("full_address")
    val fullAddress: String?,
    @SerializedName("locality")
    val locality: String?,
    @SerializedName("region")
    val region: String?,
    @SerializedName("postcode")
    val postcode: String?,
    @SerializedName("overture_id")
    val overtureId: String?,
    @SerializedName("augmentations")
    val augmentations: Any?,
    @SerializedName("last_enriched_at")
    val lastEnrichedAt: String?,
    @SerializedName("overture_compat")
    val overtureCompat: OvertureCompat?
)

data class OvertureCompat(
    @SerializedName("id")
    val id: String?,
    @SerializedName("categories")
    val categories: CompatCategories?,
    @SerializedName("names")
    val names: CompatNames?,
    @SerializedName("addresses")
    val addresses: List<CompatAddress>?,
    @SerializedName("confidence")
    val confidence: Double?
)

data class CompatCategories(
    @SerializedName("primary")
    val primary: String?,
    @SerializedName("alternate")
    val alternate: List<String>?
)

data class CompatNames(
    @SerializedName("primary")
    val primary: String?
)

data class CompatAddress(
    @SerializedName("freeform")
    val freeform: String?,
    @SerializedName("locality")
    val locality: String?,
    @SerializedName("region")
    val region: String?,
    @SerializedName("postcode")
    val postcode: String?
)
