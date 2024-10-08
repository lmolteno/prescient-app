package net.molteno.linus.prescient.earth

import android.location.Location
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mil.nga.sf.MultiPolygon
import net.molteno.linus.prescient.earth.utils.LngLat
import net.molteno.linus.prescient.ui.theme.PrescientTheme
import net.molteno.linus.prescient.ui.theme.PurpleGrey80
import net.molteno.linus.prescient.utils.orthographicProject
import kotlin.math.cos
import kotlin.math.sin

private val ShadowColor = Color.Black.copy(alpha = 0.5f)
private val LandColor = Color(0xFF107929)
private val SeaColor = Color(0xFF0D5588)
private val PoleColor = Color(0xFFFFFFFF)

@Composable
fun Earth(coastlines: List<MultiPolygon>, modifier: Modifier = Modifier, location: Location? = null, subsolarPoint: LngLat? = null) {
    val phase = (((subsolarPoint?.longitude ?: 0.0).mod(360f) - (location?.longitude ?: 0.0).mod(360f)).toFloat() / 360f).mod(1f)
    val stroke = Stroke(2f, cap = StrokeCap.Round)
    val leftSide = phase < 0.5
    val halfPhase = if (leftSide) phase * 2f else 2 - (phase * 2f)

    Column {
        Canvas(
            modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            val landPath = Path()

            if (location != null) {
                for (coastline in coastlines) {
                    coastline.polygons.forEach { polygon ->
                        var started = false
                        polygon.exteriorRing.points.forEach { latlng ->
                            val point = orthographicProject(latlng.x - location.longitude, latlng.y, size.minDimension / 2F)
                            if (started) {
                                landPath.lineTo(-point.x, point.y)
                            } else {
                                landPath.moveTo(-point.x, point.y)
                                started = true
                            }
                        }
                    }
                }
            }

            val shadowPath = Path()
            shadowPath.moveTo(0f, size.minDimension / 2.0F)
            for (i in 1..179) {
                val angle = Math.toRadians(i.toDouble()).toFloat()
                val x = ((halfPhase - 0.5F) * sin(angle) * size.minDimension * if (leftSide) 1f else -1f)
                val y = cos(angle) * size.minDimension * 0.5F
                shadowPath.lineTo(x, y)
            }
            for (i in 180..359) {
                val angle = Math.toRadians(i.toDouble()).toFloat()
                val x = (sin(angle) * size.minDimension * 0.5F / (1 - halfPhase) * if (leftSide) 1f else -1f)
                val y = cos(angle) * size.minDimension * 0.5F
                shadowPath.lineTo(x, y)
            }
            shadowPath.close()

            rotate(-1 * (subsolarPoint?.latitude?.toFloat() ?: 0f), center) {
                translate(center.x, center.y) {
                    clipPath(shadowPath, clipOp = ClipOp.Difference) {
                        drawCircle(
                            Brush.radialGradient(
                                colors = (0..7).toList().map { SeaColor } + listOf(Color.Transparent),
                                radius = (size.minDimension / 2f) * 13 / 12,
                                center = Offset.Zero),
                            radius = (size.minDimension / 2f) * 13 / 12,
                            center = Offset.Zero
                        )
                    }
                    drawCircle(SeaColor, center = Offset.Zero,)
                }
            }

            rotate(180f, center) {
                translate(center.x, center.y) {
                    drawPath(landPath, Brush.verticalGradient(
                        0.0f to PoleColor,
                        0.15f to LandColor,
                        0.8f to LandColor,
                        0.95f to PoleColor,
                        1.0f to PoleColor,
                        startY = (-size.minDimension / 2),
                        endY = (size.minDimension / 2)
                    ))

                    if (location != null) {
                        // already rotated the whole earth (longitude = 0)
                        val projected = orthographicProject(0.0, location.latitude, size.minDimension / 2)
                        drawCircle(Color.Red, radius = 10f, center = projected)
                    }
                }
            }

            rotate(-1 * (subsolarPoint?.latitude?.toFloat() ?: 0f), center) {
                translate(center.x, center.y) {
                    drawPath(shadowPath, Color.Black, style = stroke)
                    drawPath(shadowPath, ShadowColor)
                }
            }

            if (phase == 0.5f) {
                drawCircle(ShadowColor)
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun EarthPreview() {
    var sliderPosition by remember { mutableFloatStateOf(1f) }
    PrescientTheme {
        Column(Modifier.background(PurpleGrey80)) {
            Slider(
                value = sliderPosition,
                onValueChange = { sliderPosition = it }
            )
            Text(text = sliderPosition.toString())
            Earth(emptyList())
        }
    }
}
