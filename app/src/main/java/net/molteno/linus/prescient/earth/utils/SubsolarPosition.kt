package net.molteno.linus.prescient.earth.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

internal val Double.radians: Double get() = this * (PI / 180.0)
internal val Double.degrees: Double get() = this * (180.0 / PI)

private fun equationOfTime(d: Double, y: Int): Double {
    val factor = 6.24004077 + 0.01720197 * (365.25 * (y - 2000) + d)
    val a = -7.659 * sin(factor)
    val b = 9.863 * sin(2 * factor + 3.5932)
    return a + b
}

fun subsolarPoint(instant: Instant): LngLat {
    val date = instant.toLocalDateTime(TimeZone.UTC)
    val n = date.dayOfYear
    val declination = -23.44 * cos(((360.0 / 365) * (n + 10)).radians)
//    val declination = -asin(0.39779 * cos((0.98565.degrees * (n + 10) + 1.914.degrees * sin((0.98565.degrees * (n - 2)).radians).degrees).radians))
    val decimalHours = (date.hour + (date.minute / 60.0) + (date.second / 3600.0))
    val decimalDays = n + (decimalHours / 24.0)
    val equationOfTimeMinutes = equationOfTime(decimalDays, date.year)
    val longitude = -15 * (decimalHours - 12 + (equationOfTimeMinutes / 60))
    return LngLat(longitude, declination)
}

data class LngLat(val longitude: Double, val latitude: Double)