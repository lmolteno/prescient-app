package net.molteno.linus.prescient.earth.api.models

import kotlinx.datetime.Instant
import net.molteno.linus.prescient.ui.shared.TimeKeyed

data class WeatherForecast(
    var measurements: List<Measurement> = emptyList(),
)

data class Measurement(
    override val time: Instant,
    val temperature: Double?,
    val rain: Double?,
    val windSpeed: Double?,
    val windGust: Double?,
    val cloudCover: Double?,
    val cloudCoverHigh: Double?,
    val cloudCoverMid: Double?,
    val cloudCoverLow: Double?,
): TimeKeyed

