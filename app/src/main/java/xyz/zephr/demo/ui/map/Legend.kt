package xyz.zephr.demo.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.zephr.demo.models.map.LegendContent

@Composable
fun Legend(
    modifier: Modifier = Modifier,
    legendBoxPadding: PaddingValues = PaddingValues(all = 16.dp),
    legendTitlePadding: PaddingValues = PaddingValues(top = 16.dp, start = 16.dp),
    legendItemPadding: PaddingValues = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp),
    legendLastItemPadding: PaddingValues = PaddingValues(
        start = 16.dp,
        end = 16.dp,
        top = 8.dp,
        bottom = 16.dp
    ),
    content: LegendContent = LegendContent.default()
) {
    Box(
        modifier = modifier
            .background(Color.Black, shape = RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(legendBoxPadding)) {
            Text(
                text = content.titleText,
                color = Color.White,
                style = TextStyle(
                    fontSize = 18.sp
                ),
                modifier = Modifier.padding(legendTitlePadding)
            )
            content.items.forEachIndexed { index, item ->
                val padding =
                    if (index == content.items.lastIndex) legendLastItemPadding else legendItemPadding
                LegendItemComposable(
                    item = item,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 600)
@Composable
fun LegendPreview() {
    Legend(content = LegendContent.preview())
}
