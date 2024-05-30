package net.molteno.linus.prescient.sun

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.molteno.linus.prescient.sun.api.NoaaApiModule
import net.molteno.linus.prescient.sun.api.models.SolarRegionObservation
import net.molteno.linus.prescient.sun.api.models.toSolarRegionObservation
import net.molteno.linus.prescient.ui.theme.PrescientTheme
import java.time.LocalDate
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

fun orthographicProject(longitude: Double, latitude: Double, radius: Float): Offset {
    val longRadians = Math.toRadians(longitude)
    val latRadians = Math.toRadians(latitude)

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

val colorSunOuter = Color(0xFF9E3E15)
val colorSunMid = Color(0xFFB96F1F)
val colorSunCenter = Color(0xFFF8AC1F)
val colorSunSpotSelected = Color(0xFFD24407)
val colorSunSpotCenter = Color(0xFF000000)

@Composable
fun Sun(
    regions: List<SolarRegionObservation>,
    selectedRegion: Int? = null
) {
    Canvas(modifier = Modifier
        .fillMaxSize()
        .padding(10.dp)
    ) {
        val sunRadius = this.size.minDimension / 2f
        drawCircle(
            Brush.radialGradient(
                0f to colorSunCenter,
                0.8f to colorSunMid,
                1f to colorSunOuter
            )
        )
        translate(center.x, center.y) {
            regions.forEach { region ->
//                val innerPath = Path()
                val outerPath = Path()
//                drawOrthoCircle(
//                    region.longitude,
//                    region.latitude,
//                    0.015f * region.area,
//                    sunRadius
//                ).map {
//                    innerPath.lineTo(-it.x, -it.y)
//                }
                drawOrthoCircle(
                    region.longitude,
                    region.latitude,
                    (max(50f * sqrt(region.area.toFloat()), 10f) * sunRadius) / 100_000f,
                    sunRadius
                ).map {
                    outerPath.lineTo(-it.x, -it.y)
                }
                if (selectedRegion == region.region) {
                    drawPath(outerPath, colorSunSpotSelected)
                    val center = -orthographicProject(region.longitude.toDouble(), region.latitude.toDouble(), sunRadius)
                    val crossSize = 40f
                    drawLine(Color.Black, start = center - Offset(crossSize, 0f), end = center + Offset(crossSize, 0f))
                    drawLine(Color.Black, start = center - Offset(0f, crossSize), end = center + Offset(0f, crossSize))
                } else {
                    drawPath(outerPath, colorSunSpotCenter)
                }
//                drawPath(innerPath, colorSunSpotCenter)
            }
        }
    }
}

@Preview
@Composable
fun SunPreview() {
    val noaaApi = remember { NoaaApiModule.providesNoaaApi() }
    var regions by remember { mutableStateOf<List<SolarRegionObservation>>(emptyList()) }

    LaunchedEffect(Unit) {
        launch(Dispatchers.IO) {
            regions = noaaApi.fetchSolarRegions()
                .mapNotNull { it.toSolarRegionObservation() }
                .filter { it.observedDate == LocalDate.now().minusDays(1) }
        }
    }

    PrescientTheme {
        Text(text = "${regions.count()} regions ${LocalDate.now().minusDays(1)}")
        Sun(regions = regions)
    }
}