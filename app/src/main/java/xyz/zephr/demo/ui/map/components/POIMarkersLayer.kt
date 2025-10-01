package xyz.zephr.demo.ui.map.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import xyz.zephr.demo.R
import xyz.zephr.demo.data.model.Place
import xyz.zephr.demo.presentation.map.model.LocationState
import xyz.zephr.demo.utils.FovUtils
import xyz.zephr.demo.utils.MapIconUtils

@Composable
fun POIMarkersLayer(
    locationState: LocationState,
    places: List<Place>,
    selectedPlaceId: String?,
    markerStates: Map<String, MarkerState>,
    highlightEnabled: Boolean
) {
    val context = LocalContext.current
    val labelBackgroundColor = colorResource(id = R.color.place_marker_label_background)
    val labelTextColor = colorResource(id = R.color.white)
    val placeDotColor = colorResource(id = R.color.place_marker_yellow)
    val selectedDotColor = colorResource(id = R.color.place_marker_red)
    val glowColor = colorResource(id = R.color.place_marker_glow_orange)

    val highlightedPlaceIds = remember(locationState, places, highlightEnabled) {
        if (!highlightEnabled) {
            emptySet<String>()
        } else {
            val userLocation = locationState.zephrLocation
            if (userLocation == null) {
                emptySet<String>()
            } else {
                places.filter { place ->
                    FovUtils.isPointInFov(
                        userLocation = userLocation,
                        targetLocation = place.location,
                        heading = locationState.heading,
                        fovAngle = locationState.fovAngle,
                        radiusMeters = locationState.fovRadius.toDouble()
                    )
                }.map { it.id }.toSet()
            }
        }
    }

    places.forEach { place ->
        val isSelected = place.id == selectedPlaceId
        val isHighlighted = highlightedPlaceIds.contains(place.id)
        val iconData = remember(place.id, place.name, isSelected, isHighlighted) {
            val dotColor = when {
                isSelected -> selectedDotColor
                isHighlighted -> glowColor
                else -> placeDotColor
            }
            MapIconUtils.createLabeledDotIcon(
                context = context,
                label = place.name,
                dotColor = dotColor,
                glowColor = if (isHighlighted) glowColor else null,
                backgroundColor = labelBackgroundColor,
                textColor = labelTextColor
            )
        }

        key(place.id) {
            val markerState =
                remember { markerStates[place.id] ?: MarkerState(position = place.location) }
            markerState.position = place.location
            Marker(
                state = markerState,
                title = place.name,
                snippet = place.description,
                icon = iconData.descriptor,
                flat = true,
                anchor = androidx.compose.ui.geometry.Offset(iconData.anchorX, iconData.anchorY),
                onClick = { false }
            )
        }
    }
}
