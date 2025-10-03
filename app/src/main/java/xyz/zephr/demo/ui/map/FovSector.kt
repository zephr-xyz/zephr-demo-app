package xyz.zephr.demo.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.LatLng
import xyz.zephr.demo.ui.theme.ZephrOrange

// Map provider abstraction - can be swapped out
interface MapSectorRenderer {
    @Composable
    fun RenderSector(points: List<LatLng>, fillColor: Color, strokeColor: Color, strokeWidth: Float)
}

// Google Maps implementation
class GoogleMapsSectorRenderer : MapSectorRenderer {
    @Composable
    override fun RenderSector(
        points: List<LatLng>,
        fillColor: Color,
        strokeColor: Color,
        strokeWidth: Float
    ) {
        com.google.maps.android.compose.Polygon(
            points = points,
            fillColor = fillColor,
            strokeColor = strokeColor,
            strokeWidth = strokeWidth
        )
    }
}

/**
 * A composable component that renders a Field of View (FOV) sector on the map.
 *
 * Displays a filled circular sector showing the user's field of view as a "pie slice"
 * emanating from their location. The sector appears immediately when location data
 * is available and disappears when location is lost.
 *
 * @param centerLocation The center point of the FOV (user location)
 * @param fovPoints List of LatLng points defining the sector shape (center + arc points)
 * @param alpha The opacity multiplier for the sector (0.0 to 1.0)
 * @param mapRenderer The map provider implementation (default: Google Maps)
 */
@Composable
fun FovSector(
    centerLocation: LatLng?,
    fovPoints: List<LatLng>,
    alpha: Float,
    mapRenderer: MapSectorRenderer = GoogleMapsSectorRenderer()
) {
    // Only render if we have valid parameters and visible alpha
    if (centerLocation != null && fovPoints.isNotEmpty() && alpha > 0f) {
        mapRenderer.RenderSector(
            points = fovPoints,
            fillColor = ZephrOrange.copy(alpha = 0.4f * alpha),
            strokeColor = ZephrOrange.copy(alpha = 0.6f * alpha),
            strokeWidth = 1f
        )
    }
}

