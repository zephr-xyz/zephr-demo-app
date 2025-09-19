package xyz.zephr.demo.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import kotlin.math.roundToInt

object BitmapUtils {
    fun bitmapDescriptor(
        context: Context,
        vectorResId: Int
    ): BitmapDescriptor? {
        val drawable = ContextCompat.getDrawable(context, vectorResId) ?: return null
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        val bm = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)

        val canvas = Canvas(bm)
        drawable.draw(canvas)

        val scaledBm = bitmapSizeByScale(bm)
        return BitmapDescriptorFactory.fromBitmap(scaledBm)
    }

    private fun bitmapSizeByScale(bitmapIn: Bitmap, scale: Float = 0.15f): Bitmap {
        val scale = scale.coerceIn(0f, 1f)
        return bitmapIn.scale(
            (bitmapIn.width * scale).roundToInt(),
            (bitmapIn.height * scale).roundToInt(),
            false
        )
    }
}