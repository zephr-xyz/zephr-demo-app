package xyz.zephr.demo.ui.map

import android.Manifest
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xyz.zephr.demo.R
import xyz.zephr.demo.presentation.map.ZephrMapViewModel
import xyz.zephr.demo.ui.theme.ZephrOrange
import xyz.zephr.demo.utils.BitmapUtils
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

    // Map loaded flag
    var mapLoaded by remember { mutableStateOf(false) }

    // Track if locations have been initially positioned
    var hasInitiallyPositioned by remember { mutableStateOf(false) }

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
    LaunchedEffect(uiState.value.heading, mapLoaded) {
        if (mapLoaded && !cameraPositionState.isMoving) {
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
                    onMapLoaded = { mapLoaded = true }
                ) {
                    key("zephr_location_marker") {
                        Marker(
                            state = zephrMarkerState,
                            title = stringResource(R.string.zephr_marker_title),
                            icon = BitmapUtils.bitmapDescriptor(
                                LocalContext.current,
                                R.drawable.zephr_marker
                            ),
                            zIndex = 2.0f
                        )
                    }

                    key("android_location_marker") {
                        Marker(
                            state = androidMarkerState,
                            title = stringResource(R.string.android_marker_title),
                            icon = BitmapUtils.bitmapDescriptor(
                                LocalContext.current,
                                R.drawable.android_marker
                            ),
                            zIndex = 1.0f
                        )
                    }

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
                IconButton(
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
                        .size(56.dp)
                        .background(ZephrOrange, shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.MyLocation,
                        contentDescription = stringResource(R.string.center_on_zephr),
                        tint = Color.White
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}


@Composable
@Preview
fun Legend(
    paddingValues: PaddingValues = PaddingValues(0.dp),
    legendContainerPadding: PaddingValues = PaddingValues(bottom = 12.dp),
    legendBoxPadding: PaddingValues = PaddingValues(all = 16.dp),
    legendTitlePadding: PaddingValues = PaddingValues(top = 16.dp, start = 16.dp),
    legendItemPadding: PaddingValues = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp),
    legendLastItemPadding: PaddingValues = PaddingValues(
        start = 16.dp,
        end = 16.dp,
        top = 8.dp,
        bottom = 16.dp
    )
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(legendContainerPadding),
        contentAlignment = Alignment.BottomStart
    ) {
        Box(
            modifier = Modifier
                .padding(legendBoxPadding)
                .background(Color.Black, shape = RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
        ) {
            Column {
                Text(
                    text = stringResource(R.string.legend_title),
                    color = Color.White,
                    style = TextStyle(
                        fontSize = 18.sp
                    ),
                    modifier = Modifier.padding(legendTitlePadding)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(legendItemPadding)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.zephr_marker),
                        contentDescription = "",
                        modifier = Modifier.size(20.dp)
                    )
                    Text(text = stringResource(R.string.zephr_solution), modifier = Modifier.padding(start = 8.dp))
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(legendLastItemPadding)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.android_marker),
                        contentDescription = "",
                        modifier = Modifier.size(20.dp)
                    )
                    Text(text = stringResource(R.string.android_solution), modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}