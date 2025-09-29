package xyz.zephr.demo.models.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import xyz.zephr.demo.R

/**
 * Data class containing all content for the Legend component.
 */
data class LegendContent(
    val titleText: String,
    val items: List<LegendItem>
) {
    companion object {
        /**
         * Default content using string resources
         */
        @Composable
        fun default(): LegendContent = LegendContent(
            titleText = stringResource(R.string.legend_title),
            items = listOf(
                LegendItem.fromResources(R.drawable.zephr_marker, R.string.zephr_solution),
                LegendItem.fromResources(R.drawable.android_marker, R.string.android_solution)
            )
        )

        /**
         * Preview content with hardcoded strings - for preview
         */
        fun preview(): LegendContent = LegendContent(
            titleText = "Legend",
            items = listOf(
                LegendItem(
                    iconRes = R.drawable.zephr_marker,
                    text = "Zephr Solution",
                    textColor = androidx.compose.ui.graphics.Color.White
                ),
                LegendItem(
                    iconRes = R.drawable.android_marker,
                    text = "Android Solution",
                    textColor = androidx.compose.ui.graphics.Color.White
                )
            )
        )
    }
}
