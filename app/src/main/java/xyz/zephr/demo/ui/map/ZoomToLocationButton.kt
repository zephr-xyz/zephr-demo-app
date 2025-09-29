package xyz.zephr.demo.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import xyz.zephr.demo.R
import xyz.zephr.demo.ui.theme.ZephrOrange

@Composable
fun ZoomToLocationButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = stringResource(R.string.center_on_zephr)
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(56.dp)
            .background(ZephrOrange, shape = CircleShape)
    ) {
        Icon(
            imageVector = Icons.Filled.MyLocation,
            contentDescription = contentDescription,
            tint = Color.White
        )
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 600)
@Composable
fun ZoomToLocationButtonPreview() {
    ZoomToLocationButton(
        onClick = {},
        contentDescription = "Center on Zephr location"
    )
}
