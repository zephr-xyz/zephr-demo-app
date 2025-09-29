package xyz.zephr.demo.ui.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapMarkers(
    zephrMarkerState: MarkerState,
    androidMarkerState: MarkerState
) {
    MapMarker(
        markerState = zephrMarkerState,
        type = MarkerType.ZEPHR,
        key = "zephr_location_marker"
    )

    MapMarker(
        markerState = androidMarkerState,
        type = MarkerType.ANDROID,
        key = "android_location_marker"
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun MapMarkersPreview() {
    val zephrPosition = LatLng(37.7749, -122.4194)
    val androidPosition = LatLng(37.7752, -122.4191) // Much closer to Zephr position

    val zephrMarkerState = remember { MarkerState(position = zephrPosition) }
    val androidMarkerState = remember { MarkerState(position = androidPosition) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(zephrPosition, 16f)
    }

    GoogleMap(
        cameraPositionState = cameraPositionState,
        modifier = Modifier.fillMaxSize()
    ) {
        MapMarkers(
            zephrMarkerState = zephrMarkerState,
            androidMarkerState = androidMarkerState
        )
    }
}
