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
import net.molteno.linus.prescient.api.models.SolarRegionObservation
import net.molteno.linus.prescient.ui.theme.PrescientTheme
import net.molteno.linus.prescient.utils.drawOrthoCircle
import net.molteno.linus.prescient.utils.orthographicProject
import timber.log.Timber
import java.time.LocalDate
import kotlin.math.sqrt

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
                0f to Color(0XFFfcb62f),
                0.58055f to Color(0xFFfca105),
                0.79028f to Color(0xFFfd8d01),
                0.89514f to Color(0xFFfd7501),
                0.94757f to Color(0xFFfd6200),
                0.97378f to Color(0xFFf95601),
                0.98689f to Color(0xFFd34701),
                1f to Color(0xFF7b2b02),
            )
        )
        translate(center.x, center.y) {
            regions.forEachIndexed { index, region ->
                val outerPath = Path()
                val radius = (22f * sqrt(region.metadata.area.toFloat())) / 100f
                if (index == 1) Timber.d("radius: $radius")

                drawOrthoCircle(
                    region.longitude,
                    region.latitude,
                    radius,
                    sunRadius
                ).map { outerPath.lineTo(-it.x, -it.y) }

                if (selectedRegion == region.region) {
                    drawPath(outerPath, colorSunSpotSelected)
                    val center = -orthographicProject(region.longitude.toDouble(), region.latitude.toDouble(), sunRadius)
                    val crossSize = 40f
                    drawLine(Color.Black, start = center - Offset(crossSize, 0f), end = center + Offset(crossSize, 0f))
                    drawLine(Color.Black, start = center - Offset(0f, crossSize), end = center + Offset(0f, crossSize))
                } else {
                    drawPath(outerPath, colorSunSpotCenter)
                }
            }
        }
    }
}

@Preview
@Composable
fun SunPreview() {
    val regions by remember { mutableStateOf<List<SolarRegionObservation>>(emptyList()) }

    LaunchedEffect(Unit) {
        launch(Dispatchers.IO) {
//            regions = noaaApi.fetchSolarRegions()
//                .mapNotNull { it.toSolarRegionObservation() }
//                .filter { it.observedDate == LocalDate.now().minusDays(1) }
        }
    }

    PrescientTheme {
        Text(text = "${regions.count()} regions ${LocalDate.now().minusDays(1)}")
        Sun(regions = regions)
    }
}