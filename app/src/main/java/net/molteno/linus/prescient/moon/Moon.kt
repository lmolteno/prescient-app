package net.molteno.linus.prescient.moon

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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.molteno.linus.prescient.ui.theme.PrescientTheme
import net.molteno.linus.prescient.ui.theme.PurpleGrey80
import kotlin.math.cos
import kotlin.math.sin


@Composable
fun Moon(phase: Float, modifier: Modifier = Modifier ) {
    val stroke = Stroke(2f, cap = StrokeCap.Round)
    val leftSide = phase < 0.5
    val halfPhase = if (leftSide) phase * 2f else 2 - (phase * 2f)
    val shadowColor = Color.Black.copy(alpha = 1f - halfPhase / 2)

    Canvas(modifier
        .fillMaxSize()
        .padding(10.dp)
    ) {
        if (phase == 0.5f) {
            drawCircle(shadowColor)
            return@Canvas
        }
        if (phase == 0.0f || phase == 1.0f) {
            drawCircle(Color.Black, style= stroke)
            drawCircle(Color.White)
            return@Canvas
        }
        drawCircle(Color.White)
        translate(center.x, center.y) {
            val moonPath = Path()
            moonPath.moveTo(0f, size.minDimension / 2.0F)
            for (i in 1..179) {
                val angle = Math.toRadians(i.toDouble()).toFloat()
                val x = ((halfPhase - 0.5F) * sin(angle) * size.minDimension * if (leftSide) 1f else -1f)
                val y = cos(angle) * size.minDimension * 0.5F
                moonPath.lineTo(x, y)
            }
            for (i in 180..359) {
                val angle = Math.toRadians(i.toDouble()).toFloat()
                val x = (sin(angle) * size.minDimension * 0.5F / (1 - halfPhase) * if (leftSide) 1f else -1f)
                val y = cos(angle) * size.minDimension * 0.5F
                moonPath.lineTo(x, y)
            }
            moonPath.close()

            drawPath(moonPath, Color.Black, style = stroke)
            drawPath(moonPath, shadowColor)

            drawArc(
                if (leftSide) Color.White else Color.Black,
                -60f - 180f,
                120f,
                false,
                topLeft = Offset(-size.minDimension / 2f, -size.minDimension / 2f),
                size = Size(size.minDimension, size.minDimension),
                style = stroke
            )

            drawArc(
                if (leftSide) Color.Black else Color.White,
                -60f,
                120f,
                false,
                topLeft = Offset(-size.minDimension / 2f, -size.minDimension / 2f),
                size = Size(size.minDimension, size.minDimension),
                style = stroke
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MoonPreview() {
    var sliderPosition by remember { mutableFloatStateOf(1f) }
    PrescientTheme {
        Column(Modifier.background(PurpleGrey80)) {
            Slider(
                value = sliderPosition,
                onValueChange = { sliderPosition = it }
            )
            Text(text = sliderPosition.toString())
            Moon(sliderPosition)
        }
    }
}
