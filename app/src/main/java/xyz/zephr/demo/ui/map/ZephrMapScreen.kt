package xyz.zephr.demo.ui.map

import android.Manifest
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import xyz.zephr.demo.presentation.map.ZephrMapViewModel
import kotlin.math.abs

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ZephrMapScreen(
    viewModel: ZephrMapViewModel = hiltViewModel(checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    }, null)
) {
    // Collect UI State
    val uiState = viewModel.uiState.collectAsState()

    // Camera state remembered
    val cameraPositionState = rememberCameraPositionState()

    // Marker States
    val zephrMarkerState = remember { MarkerState() }
    val androidMarkerState = remember { MarkerState() }

    // We check permissions again so we can kill the SDK if permissions are revoked
    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    val lifecycleOwner = LocalLifecycleOwner.current

    // Lifecycle: start/stop location updates when permission granted/revoked
    DisposableEffect(lifecycleOwner, permissionState.allPermissionsGranted) {
        val observer = LifecycleEventObserver { _, event ->
            if (!permissionState.allPermissionsGranted) return@LifecycleEventObserver
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.start()
                Lifecycle.Event.ON_PAUSE -> viewModel.stop()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.stop()
        }
    }

    LaunchedEffect(uiState.value.zephrLocation) {
        uiState.value.zephrLocation?.let { zephrMarkerState.position = it }
    }

    LaunchedEffect(uiState.value.androidLocation) {
        uiState.value.androidLocation?.let { androidMarkerState.position = it }
    }

    // Update map bearing based on Zephr heading
    LaunchedEffect(uiState.value.heading, uiState.value.mapLoaded) {
        if (uiState.value.mapLoaded && !cameraPositionState.isMoving) {
            // Only update bearing if camera is not currently moving (user interaction)
            val currentBearing = cameraPositionState.position.bearing
            val newBearing = uiState.value.heading
            val bearingDiff = abs(newBearing - currentBearing)

            // Only update if bearing change is significant (> 1 degree) to avoid jitter
            if (bearingDiff > 1f) {
                cameraPositionState.move(
                    update = CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder(cameraPositionState.position)
                            .bearing(newBearing)
                            .build()
                    )
                )
            }
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        if (uiState.value.zephrLocation != null) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                GoogleMap(
                    modifier = Modifier.matchParentSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(
                        zoomGesturesEnabled = true,
                        scrollGesturesEnabled = true,
                        rotationGesturesEnabled = false,
                        zoomControlsEnabled = false
                    ),
                    onMapLoaded = viewModel::onMapLoaded
                ) {
                    MapMarkers(
                        zephrMarkerState = zephrMarkerState,
                        androidMarkerState = androidMarkerState
                    )

                    MapCameraController(
                        mapLoaded = uiState.value.mapLoaded,
                        cameraPositionState = cameraPositionState,
                        uiState = uiState
                    )
                }

                // Legend overlay
                Legend(
                    paddingValues = innerPadding,
                    legendContainerPadding = PaddingValues(bottom = 12.dp),
                    legendBoxPadding = PaddingValues(all = 16.dp),
                    legendTitlePadding = PaddingValues(top = 16.dp, start = 16.dp),
                    legendItemPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp),
                    legendLastItemPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 16.dp
                    )
                )

                // Zoom-to-Zephr location
                val coroutineScope = rememberCoroutineScope()
                ZoomToLocationButton(
                    onClick = {
                        uiState.value.zephrLocation?.let { location ->
                            coroutineScope.launch {
                                cameraPositionState.animate(
                                    update = CameraUpdateFactory.newCameraPosition(
                                        CameraPosition.Builder()
                                            .target(location)
                                            .zoom(16f)
                                            .build()
                                    )
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 36.dp)
                )
            }
        } else {
            LoadingIndicator(modifier = Modifier.fillMaxSize())
        }
    }
}