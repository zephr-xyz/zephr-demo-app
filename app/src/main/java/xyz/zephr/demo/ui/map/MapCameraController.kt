package xyz.zephr.demo.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.CameraPositionState
import kotlinx.coroutines.delay
import xyz.zephr.demo.presentation.map.MapUiState

@Composable
fun MapCameraController(
    mapLoaded: Boolean,
    cameraPositionState: CameraPositionState,
    uiState: State<MapUiState>
) {
    var hasInitiallyPositioned by remember { mutableStateOf(false) }

    LaunchedEffect(mapLoaded) {
        if (!mapLoaded || hasInitiallyPositioned) return@LaunchedEffect

        // Wait until we get the first Zephr location
        var zephrLocation = uiState.value.zephrLocation
        while (zephrLocation == null) {
            delay(100)
            zephrLocation = uiState.value.zephrLocation
        }

        hasInitiallyPositioned = true
        cameraPositionState.animate(
            update = CameraUpdateFactory.newCameraPosition(
                CameraPosition.Builder()
                    .bearing(uiState.value.heading)
                    .zoom(16f)
                    .target(zephrLocation)
                    .build()
            ),
            durationMs = 2000
        )
    }
}

