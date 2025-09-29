package xyz.zephr.demo.ui.map

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import xyz.zephr.demo.R
import xyz.zephr.demo.models.map.LegendItem

/**
 * Composable for rendering a single legend item with icon and text.
 */
@Composable
fun LegendItemComposable(
    item: LegendItem,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Image(
            painter = painterResource(id = item.iconRes),
            contentDescription = "",
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = item.text,
            color = item.textColor,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Preview(showBackground = false)
@Composable
fun LegendItemComposablePreview() {
    // Show on black background like the actual Legend component
    Box(
        modifier = Modifier
            .background(Color.Black)
            .padding(16.dp) // Same padding as Legend
    ) {
        LegendItemComposable(
            item = LegendItem(
                iconRes = R.drawable.zephr_marker,
                text = "Zephr Solution",
                textColor = Color.White
            )
        )
    }
}
