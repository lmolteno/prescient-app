package net.molteno.linus.prescient.earth

import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.location.Location
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.jamesyox.kastro.sol.SolarEvent
import dev.jamesyox.kastro.sol.SolarEventSequence
import dev.jamesyox.kastro.sol.calculateSolarState
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import net.molteno.linus.prescient.ui.theme.PrescientTheme
import net.molteno.linus.prescient.utils.steppedBy
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

val tz = TimeZone.currentSystemDefault()

val nightColor = Color.Black.copy(alpha = 0.2f)
val dayColor = Color(0x00ffffff).copy(alpha = 0.2f)
val sunRadius = 10f
val buffer = sunRadius * 1.5f

private fun Instant.decimalHours() = toLocalDateTime(tz).run { hour + (minute / 60f) + (second / 3600f) }

@Composable
fun SunChart(instant: Instant, position: Location?, modifier: Modifier = Modifier) {
    val sunStates = remember(instant.toLocalDateTime(tz).date, position) {
        if (position == null) {
            return@remember null
        }
        val start = instant.toLocalDateTime(tz).date.atStartOfDayIn(tz)

        (start..start.plus(1.days))
            .steppedBy(10.minutes)
            .map { it to it.calculateSolarState(position.latitude, position.longitude) }
    }

    val sunChanges = remember(instant.toLocalDateTime(tz).date, position) {
        return@remember if (position == null) {
            null
        } else {
            SolarEventSequence(
                start = instant.toLocalDateTime(tz).date.atStartOfDayIn(tz),
                latitude = position.latitude,
                longitude = position.longitude,
                limit = 24.hours,
                requestedSolarEvents = listOf(
                    SolarEvent.Sunrise,
                    SolarEvent.Sunset,
                    SolarEvent.AstronomicalDawn,
                    SolarEvent.AstronomicalDusk,
                )
            ).toList()
        }
    }

    val currentSun = remember(instant, position) {
        return@remember if (position == null) {
            null
        } else {
            instant.calculateSolarState(position.latitude, position.longitude)
        }
    }


    val maxAlt = remember(sunStates) { sunStates?.maxOfOrNull { it.second.altitude } ?: 1.0 }
    val minAlt = remember(sunStates) { sunStates?.minOfOrNull { it.second.altitude } ?: 0.0 }
    fun mapToHeight(altitude: Double, height: Float) =
        (((altitude - minAlt) / (maxAlt - minAlt)) * (height - buffer * 2)).toFloat() + buffer

    val strokeColor = MaterialTheme.colorScheme.onSurface
    val darkMode = LocalConfiguration.current.uiMode and UI_MODE_NIGHT_MASK

    ElevatedCard(modifier) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sun",
                style = MaterialTheme.typography.titleLarge
            )
        }
        HorizontalDivider(Modifier.padding(horizontal = 8.dp))
        Canvas(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(50.dp)
        ) {
            val path = Path()
            sunStates?.forEach { (time, state) ->
                val x =
                    if (time > instant && time.decimalHours() == 0f)
                        size.width
                    else
                        (time.decimalHours() / 24f) * size.width
                val y = size.height - mapToHeight(state.altitude, size.height)
                if (path.isEmpty) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }
            sunChanges?.let {
                val astroDawn = (it.elementAt(0).time.decimalHours() / 24f) * size.width
                val sunrise = (it.elementAt(1).time.decimalHours() / 24f) * size.width
                val sunset = (it.elementAt(2).time.decimalHours() / 24f) * size.width
                val astroDusk = (it.elementAt(3).time.decimalHours() / 24f) * size.width

                drawRect(dayColor.copy(green = 0.5f), topLeft = Offset(astroDawn, 0f), size = Size(sunrise - astroDawn, size.height))
                drawRect(dayColor.copy(green = 0.5f), topLeft = Offset(sunset, 0f), size = Size(astroDusk - sunset, size.height))

                when (darkMode) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        drawRect(dayColor,
                            topLeft = Offset(sunrise, 0f),
                            size = Size(sunset - sunrise, size.height)
                        )
                    }
                    Configuration.UI_MODE_NIGHT_NO -> {
                        drawRect(nightColor,
                            topLeft = Offset(0f, 0f),
                            size = Size(astroDawn, size.height)
                        )
                        drawRect(nightColor,
                            topLeft = Offset(astroDusk, 0f),
                            size = Size(size.width - astroDusk, size.height),
                        )
                    }
                }
            }
            sunChanges?.forEach { event ->
                val x = (event.time.decimalHours() / 24f) * size.width
                drawLine(strokeColor.copy(alpha = 0.2f), Offset(x, 0f), Offset(x, size.height))
            }
            currentSun?.let {
                val timeHours = instant.decimalHours()
                val x = (timeHours / 24f) * size.width
                val y = size.height - mapToHeight(it.altitude, size.height)
                drawCircle(strokeColor, center = Offset(x, y), radius = sunRadius)
            }
            drawPath(path, strokeColor, style = Stroke(width = 3f))
            drawRect(
                strokeColor.copy(alpha = 0.1f),
                topLeft = Offset(0f, size.height - mapToHeight(0.0, size.height)),
                size = Size(size.width, mapToHeight(0.0, size.height)),
            )
            drawLine(
                strokeColor,
                start = Offset(0f, size.height - mapToHeight(0.0, size.height)),
                end = Offset(size.width, size.height - mapToHeight(0.0, size.height)),
                pathEffect = PathEffect.dashPathEffect(arrayOf(5f, 5f).toFloatArray()),
                cap = StrokeCap.Round
            )
        }
    }

}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun SunChartPreview() {
    PrescientTheme {
        Surface(Modifier.width(200.dp)) {
            Row(Modifier.padding(10.dp)) {
                SunChart(instant = Clock.System.now(), position = Location("").apply {
                    latitude = -45.5
                    longitude = 170.0
                })
            }
        }
    }
}