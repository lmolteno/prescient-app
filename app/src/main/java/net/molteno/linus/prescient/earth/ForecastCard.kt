package net.molteno.linus.prescient.earth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import net.molteno.linus.prescient.earth.api.models.WeatherForecast
import net.molteno.linus.prescient.ui.shared.Chart

val rainColor = Color.Blue
val windColor = Color.LightGray

@Composable
private fun Dot(modifier: Modifier = Modifier, color: Color) {
    Box(
        modifier
            .border(1.dp, MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f), shape = CircleShape)
            .background(color.copy(alpha = 0.8f), shape = CircleShape)
            .size(8.dp)
    )
}

fun LazyListScope.forecastCards(forecast: WeatherForecast?, onTimeChange: (time: Instant) -> Unit = { }) {
    item {
        val maxTemp = remember(forecast) { forecast?.measurements?.maxOfOrNull { m -> m.temperature ?: 1.0 } ?: 1.0 }
        val maxRain = remember(forecast) { forecast?.measurements?.maxOfOrNull { m -> m.rain ?: 1.0 } ?: 1.0 }
        val maxWind = remember(forecast) { forecast?.measurements?.maxOfOrNull { m -> m.windSpeed ?: 1.0 } ?: 1.0 }

        Chart(
            values = forecast?.measurements?.reversed(),
            title = "OpenMeteo Forecast",
            reversed = false,
            onCenteredElementChange = { onTimeChange(it.time) },
            renderElementDescription = { measurement ->
                val timeLocal = measurement.time.toLocalDateTime(TimeZone.currentSystemDefault())
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Dot(color = MaterialTheme.colorScheme.error)
                    Text("${measurement.temperature} ℃", style = MaterialTheme.typography.labelMedium)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Dot(color = rainColor)
                    Text("${measurement.rain} mm", style = MaterialTheme.typography.labelMedium)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Dot(color = windColor)
                    Text("${measurement.windSpeed} km/h", style = MaterialTheme.typography.labelMedium)
                }
                Text(
                    "%02d-%02d %02d:%02d".format(timeLocal.monthNumber, timeLocal.dayOfMonth, timeLocal.hour, timeLocal.minute),
                    style = MaterialTheme.typography.labelMedium
                )
            },
            renderDots = { measurement, maxHeight ->
                val hours = measurement.time.toLocalDateTime(TimeZone.currentSystemDefault()).hour
                if (hours < 6 || hours > 18) {
                    Box(Modifier.width(16.dp).height(maxHeight.dp).background(color = Color.Black.copy(alpha = 0.1f)))
                }
                measurement.temperature?.also {
                    Dot(
                        Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = ((1 - (it / maxTemp)) * (maxHeight - 8)).dp),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                measurement.rain?.also {
                    Dot(
                        Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = ((1 - (it / maxRain)) * (maxHeight - 8)).dp),
                        color = rainColor
                    )
                }
                measurement.windSpeed?.also {
                    Dot(
                        Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = ((1 - (it / maxWind)) * (maxHeight - 8)).dp),
                        color = windColor
                    )
                }
            }
        )
    }
}