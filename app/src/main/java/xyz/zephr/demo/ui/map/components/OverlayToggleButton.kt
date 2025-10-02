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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.zephr.demo.R

@Composable
fun OverlayToggleButton(
    modifier: Modifier = Modifier,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = modifier
            .height(26.dp)
            .background(
                color = colorResource(id = R.color.chip_background),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = if (isChecked) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
            contentDescription = stringResource(id = R.string.toggle_zephr_content_description),
            tint = colorResource(id = R.color.chip_text),
            modifier = Modifier
                .size(14.dp)
        )
        Text(
            text = if (isChecked) stringResource(id = R.string.toggle_hide_zephr) else stringResource(
                id = R.string.toggle_show_zephr
            ),
            color = colorResource(id = R.color.chip_text),
            fontSize = 11.sp,
            modifier = Modifier.width(64.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = colorResource(id = R.color.chip_text)),
            modifier = Modifier
                .scale(0.65f)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OverlayToggleButtonPreview() {
    OverlayToggleButton(isChecked = true, onCheckedChange = {})
}
