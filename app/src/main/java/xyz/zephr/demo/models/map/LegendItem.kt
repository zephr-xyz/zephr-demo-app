package xyz.zephr.demo.models.map

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource

/**
 * Data class representing a single legend item with icon and text.
 */
data class LegendItem(
    @param:DrawableRes val iconRes: Int,
    val text: String,
    val textColor: Color = Color.White
) {
    companion object {
        /**
         * Creates a LegendItem using string resources - for production use
         */
        @Composable
        fun fromResources(
            @DrawableRes iconRes: Int,
            @StringRes textRes: Int,
            textColor: Color = Color.White
        ): LegendItem = LegendItem(iconRes, stringResource(textRes), textColor)
    }
}

