package net.molteno.linus.prescient.sun.algorithms

import kotlin.math.cos
import kotlin.math.sin

/**
 * @param jd observation time as julian date
 */

private const val CARRINGTON_EPOCH = 2398220.0
private const val JULIAN_CENTURY = 36525.0
private const val J_2000_EPOCH = 2451545.0

private fun getTimeInCenturies(jd: Double) = (jd - J_2000_EPOCH) / JULIAN_CENTURY

private fun getL0(jd: Double) {
    val theta = (jd - CARRINGTON_EPOCH) * (360.0 / 25.38)
    val i = 7.25
    val k = 73.6667 + (1.3958333 * (jd - 2396758) / JULIAN_CENTURY)

    val lambda = getApparentLongitude(jd)
}

private fun getApparentLongitude(jd: Double): Double {
    val t = getTimeInCenturies(jd)
    val geometricMeanLongitude = 280.46646 + 36000.76983 * t + 0.0003032 * (t * t)
    val meanAnomaly = 357.52911 + 35999.05030 * t - 0.0001559 * (t * t)

    val sunCentre = (1.914600 - 0.004817 * t - 0.000014 * (t * t)) * sin(Math.toRadians(meanAnomaly)) +
                    (0.019993 - 0.000101 * t) * sin(Math.toRadians(2 * meanAnomaly)) +
                    (0.000289 * sin(Math.toRadians(3 * meanAnomaly)))

    val trueLongitude = geometricMeanLongitude + sunCentre

    val omega = 125.04 - 1934.136 * t
    val apparentLongitude = trueLongitude-
            0.00569 -
            0.00478 * sin(Math.toRadians(omega)) +
            0.00256 * cos(Math.toRadians(omega))

    return apparentLongitude
}


private fun getObliquity(jd: Double): Double {
    val t = getTimeInCenturies(jd)
    val meanElongationMoonSun = 297.85036 +
                                445267.111480 * t +
                                0.0019142 * (t * t) +
                                (t * t * t) / 189474

    val meanAnomalySunEarth = 357.52772 +
            35999.05034 * t -
            0.0001603 * (t * t) -
            (t * t * t) / 300_000

    val meanAnomalyMoon = 134.96298 +
            477198.867398 * t +
            0.0086972 * (t * t) +
            (t * t * t) / 56250

    val argumentLatitudeMoon = 93.27191 +
            483202.017538 * t -
            0.0036825 * (t * t) +
            (t * t * t) / 327_270

    val omega = 125.04452 - 1934.136261 * t + 0.0020708 * (t * t) + (t * t * t) / 450000


}
