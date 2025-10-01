package xyz.zephr.demo.ui.map

import android.Manifest
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
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
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import xyz.zephr.demo.TAG
import xyz.zephr.demo.presentation.map.viewmodel.LocationViewModel
import xyz.zephr.demo.presentation.map.viewmodel.MapViewModel
import xyz.zephr.demo.presentation.map.viewmodel.PlacesViewModel
import xyz.zephr.demo.ui.map.components.BearingChip
import xyz.zephr.demo.ui.map.components.OverlayToggleButton
import xyz.zephr.demo.ui.map.components.POIMarkersLayer

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ZephrMapScreen(
    locationViewModel: LocationViewModel = hiltViewModel(checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    }, null),
    mapViewModel: MapViewModel = hiltViewModel(checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    }, null),
    placesViewModel: PlacesViewModel = hiltViewModel(checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    }, null)
) {
    // Collect UI State
    val locationState = locationViewModel.locationState.collectAsState()
    val mapState = mapViewModel.mapState.collectAsState()
    val placesState = placesViewModel.uiState.collectAsState()

    // Camera state remembered
    val cameraPositionState = rememberCameraPositionState()

    // Marker States
    val zephrMarkerState = remember { MarkerState() }
    val androidMarkerState = remember { MarkerState() }

    // Place marker states
    val placeMarkerStates = remember {
        mutableStateMapOf<String, MarkerState>()
    }

    // Update place marker states when places change
    LaunchedEffect(placesState.value.places) {
        placesState.value.places.forEach { place ->
            if (placeMarkerStates[place.id] == null) {
                placeMarkerStates[place.id] = MarkerState(position = place.location)
            }
        }
    }

    // Update marker positions if place location changes
    LaunchedEffect(placesState.value.places) {
        placesState.value.places.forEach { place ->
            placeMarkerStates[place.id]?.let { state ->
                state.position = place.location
            }
        }
    }

    // Coroutine scope for animations
    val coroutineScope = rememberCoroutineScope()

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
                Lifecycle.Event.ON_RESUME -> locationViewModel.startLocationUpdates()
                Lifecycle.Event.ON_PAUSE -> locationViewModel.stopLocationUpdates()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            locationViewModel.stopLocationUpdates()
        }
    }

    LaunchedEffect(locationState.value.zephrLocation) {
        locationState.value.zephrLocation?.let { zephrMarkerState.position = it }
    }

    LaunchedEffect(locationState.value.androidLocation) {
        locationState.value.androidLocation?.let { androidMarkerState.position = it }
    }

    // Trigger places loading when Zephr location changes
    LaunchedEffect(locationState.value.zephrLocation) {
        locationState.value.zephrLocation?.let { location ->
            placesViewModel.initializeWithLocation(location)
        }
    }

    // Initial positioning
    val hasInitiallyPositioned = remember { mutableStateOf(false) }

    LaunchedEffect(mapState.value.mapLoaded) {
        if (!mapState.value.mapLoaded || hasInitiallyPositioned.value) return@LaunchedEffect

        // Wait until we get the first Zephr location
        var zephrLocation = locationState.value.zephrLocation
        while (zephrLocation == null) {
            kotlinx.coroutines.delay(100)
            zephrLocation = locationState.value.zephrLocation
        }

        hasInitiallyPositioned.value = true
        cameraPositionState.animate(
            update = com.google.android.gms.maps.CameraUpdateFactory.newCameraPosition(
                com.google.android.gms.maps.model.CameraPosition.Builder()
                    .bearing(locationState.value.heading)
                    .zoom(16f)
                    .target(zephrLocation)
                    .build()
            ),
            durationMs = 2000
        )
    }


    // Delegate bearing decision to VM and apply the decided bearing
    LaunchedEffect(
        locationState.value.heading,
        mapState.value.mapLoaded,
        cameraPositionState.isMoving
    ) {
        mapViewModel.onHeadingUpdate(locationState.value.heading, cameraPositionState.isMoving)
        if (!mapState.value.mapLoaded || cameraPositionState.isMoving) return@LaunchedEffect
        cameraPositionState.move(
            update = com.google.android.gms.maps.CameraUpdateFactory.newCameraPosition(
                com.google.android.gms.maps.model.CameraPosition.Builder(cameraPositionState.position)
                    .bearing(mapState.value.cameraPosition.bearing)
                    .build()
            )
        )
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        if (locationState.value.zephrLocation != null) {
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
                    onMapLoaded = {
                        Log.d(TAG, "GoogleMap onMapLoaded callback fired - map successfully loaded")
                        mapViewModel.onMapLoaded()
                    }
                ) {
                    if (mapState.value.showZephrOverlay) {
                        MapMarkers(
                            zephrMarkerState = zephrMarkerState,
                            androidMarkerState = androidMarkerState
                        )

                        // FOV Sector overlay using dedicated component
                        FovSector(
                            centerLocation = locationState.value.zephrLocation,
                            fovPoints = locationState.value.fovPoints,
                            alpha = 1f
                        )
                    } else {
                        // Still show android marker
                        MapMarker(
                            markerState = androidMarkerState,
                            type = MarkerType.ANDROID,
                            key = "android_location_marker"
                        )
                    }

                    // Place markers
                    POIMarkersLayer(
                        locationState = locationState.value,
                        places = placesState.value.places,
                        selectedPlaceId = placesState.value.selectedPlace?.id,
                        markerStates = placeMarkerStates,
                        highlightEnabled = mapState.value.showZephrOverlay
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

                OverlayToggleButton(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = innerPadding.calculateTopPadding() + 16.dp, end = 16.dp),
                    isChecked = mapState.value.showZephrOverlay,
                    onCheckedChange = { mapViewModel.toggleZephrOverlay() }
                )

                BearingChip(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = innerPadding.calculateTopPadding() + 16.dp, start = 16.dp),
                    heading = locationState.value.heading
                )

                // Zoom-to-Zephr location
                ZoomToLocationButton(
                    onClick = {
                        locationState.value.zephrLocation?.let { location ->
                            coroutineScope.launch {
                                cameraPositionState.animate(
                                    update = com.google.android.gms.maps.CameraUpdateFactory.newCameraPosition(
                                        com.google.android.gms.maps.model.CameraPosition.Builder()
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