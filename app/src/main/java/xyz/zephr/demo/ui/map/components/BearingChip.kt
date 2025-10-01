package xyz.zephr.demo.ui.map.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.zephr.demo.R

@Composable
fun BearingChip(
    modifier: Modifier = Modifier,
    heading: Float
) {
    Row(
        modifier = modifier
            .height(40.dp)
            .background(
                color = colorResource(id = R.color.chip_background),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Explore,
            contentDescription = stringResource(id = R.string.bearing_label),
            tint = colorResource(id = R.color.chip_text),
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = stringResource(id = R.string.bearing_label),
            color = colorResource(id = R.color.chip_text),
            fontSize = 14.sp,
            modifier = Modifier.width(60.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "${"%.1f".format(heading)}°",
            color = colorResource(id = R.color.chip_text),
            fontSize = 14.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BearingChipPreview() {
    BearingChip(heading = 123.4f)
}
