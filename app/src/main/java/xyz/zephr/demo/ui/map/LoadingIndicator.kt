package xyz.zephr.demo.ui.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import xyz.zephr.demo.ui.theme.ZephrOrange

@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = ZephrOrange
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = color)
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 600)
@Composable
fun LoadingIndicatorPreview() {
    LoadingIndicator(modifier = Modifier.fillMaxSize(), color = ZephrOrange)
}
