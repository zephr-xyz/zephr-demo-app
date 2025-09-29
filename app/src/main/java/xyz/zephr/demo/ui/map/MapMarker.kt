package xyz.zephr.demo.ui.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import xyz.zephr.demo.R
import xyz.zephr.demo.utils.BitmapUtils

enum class MarkerType {
    ZEPHR,
    ANDROID
}

@Composable
fun MapMarker(
    markerState: MarkerState,
    type: MarkerType,
    key: String
) {
    val context = LocalContext.current

    val (titleResId, iconResId, zIndex) = when (type) {
        MarkerType.ZEPHR -> Triple(R.string.zephr_marker_title, R.drawable.zephr_marker, 2.0f)
        MarkerType.ANDROID -> Triple(R.string.android_marker_title, R.drawable.android_marker, 1.0f)
    }

    key(key) {
        Marker(
            state = markerState,
            title = stringResource(titleResId),
            icon = BitmapUtils.bitmapDescriptor(context, iconResId),
            zIndex = zIndex
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun MapMarkerPreview() {
    val markerState =
        remember { MarkerState(position = LatLng(37.7749, -122.4194)) } // San Francisco

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(markerState.position, 15f)
    }

    GoogleMap(
        cameraPositionState = cameraPositionState,
        modifier = Modifier.fillMaxSize()
    ) {
        MapMarker(
            markerState = markerState,
            type = MarkerType.ZEPHR,
            key = "preview_marker"
        )
    }
}
