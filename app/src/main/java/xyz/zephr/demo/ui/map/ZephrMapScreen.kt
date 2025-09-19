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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import xyz.zephr.demo.R
import xyz.zephr.demo.presentation.map.ZephrMapViewModel
import xyz.zephr.demo.utils.BitmapUtils

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
            Manifest.permission.ACCESS_COARSE_LOCATION)
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
        uiState.value.androidLocation?.let { androidMarkerState.position = it}
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        if (uiState.value.zephrLocation != null) {
            GoogleMap(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    zoomGesturesEnabled = true,
                    scrollGesturesEnabled = false,
                    rotationGesturesEnabled = false,
                    zoomControlsEnabled = false
                )
            ) {
                key("zephr_location_marker") {
                    Marker(
                        state = zephrMarkerState,
                        title = "Zephr",
                        icon = BitmapUtils.bitmapDescriptor(
                            LocalContext.current,
                            R.drawable.zephr_marker
                        ),
                        zIndex = 1.0f
                    )
                }

                key("android_location_marker") {
                    Marker(
                        state = androidMarkerState,
                        title = "Android",
                        icon = BitmapUtils.bitmapDescriptor(
                            LocalContext.current,
                            R.drawable.android_marker
                        )
                    )
                }

                LaunchedEffect(uiState.value.heading) {
                    val target = uiState.value.zephrLocation ?: cameraPositionState.position.target

                    uiState.value.zephrLocation?.let {
                        cameraPositionState.animate(
                            update = CameraUpdateFactory.newCameraPosition(
                                CameraPosition.Builder()
                                    .bearing(uiState.value.heading)
                                    .zoom(16f)
                                    .target(target)
                                    .build()
                            )
                        )
                    }
                }
            }

            Legend(innerPadding)
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
fun Legend(paddingValues: PaddingValues = PaddingValues(0.dp)) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.BottomStart
    ) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .background(Color.Black, shape = RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
        ) {
            Column {
                Text(
                    text = "Legend",
                    color = Color.White,
                    style = TextStyle(
                        fontSize = 18.sp
                    ),
                    modifier = Modifier.padding(top = 16.dp, start = 16.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.zephr_marker),
                        contentDescription = "",
                        modifier = Modifier.size(20.dp)
                    )
                    Text(text = "Zephr Solution", modifier = Modifier.padding(start = 8.dp))
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.android_marker),
                        contentDescription = "",
                        modifier = Modifier.size(20.dp)
                    )
                    Text(text = "Android Solution", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}