package net.molteno.linus.prescient.utils

import androidx.compose.ui.geometry.Offset
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

fun orthographicProject(longitude: Double, latitude: Double, radius: Float): Offset {
    var longRadians = Math.toRadians(longitude)
    val latRadians = Math.toRadians(latitude)

    if (abs(longitude) > 90) {
        longRadians =  if (longitude > 0) PI / 2 else -PI / 2
    }

    return Offset(
        x = (cos(latRadians) * sin(longRadians) * radius).toFloat(),
        y = (sin(latRadians) * radius).toFloat()
    )
}

fun drawOrthoCircle(longitude: Int, latitude: Int, dotRadius: Float, bigCircleRadius: Float): List<Offset> {
    val radiusLon = 1 / (cos(latitude * (Math.PI / 180))) * dotRadius
    val radiusLat = dotRadius

    return (0..360).toList().map { i ->
        orthographicProject(
            longitude = (longitude + radiusLon * cos(Math.toRadians(i.toDouble()))),
            latitude = (latitude + radiusLat * sin(Math.toRadians(i.toDouble()))),
            bigCircleRadius
        )
    }
}

