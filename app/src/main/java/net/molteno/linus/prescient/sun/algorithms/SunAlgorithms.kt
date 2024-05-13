package net.molteno.linus.prescient.sun.algorithms

import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

private const val CARRINGTON_EPOCH = 2398220.0
private const val JULIAN_CENTURY = 36525.0
private const val J_2000_EPOCH = 2451545.0


private fun fromArcseconds(arcseconds: Double) = arcseconds / 3600.0
@Suppress("SameParameterValue")
private fun fromDms(degrees: Double, minutes: Double, arcseconds: Double) = degrees + (minutes / 60.0) + (arcseconds / 3600.0)
private fun getTimeInCenturies(jd: Double) = (jd - J_2000_EPOCH) / JULIAN_CENTURY

/**
 * @param jd observation time as julian date
 */
private fun getSolarParameters(jd: Double): Triple<Double, Double, Double> {
    val theta = (jd - CARRINGTON_EPOCH) * (360.0 / 25.38)
    val i = 7.25
    val k = 73.6667 + (1.3958333 * (jd - 2396758) / JULIAN_CENTURY)

    val apparentLongitude = getApparentLongitude(jd)
    val correctedApparentLongitude = apparentLongitude + getNutationLongitude(jd)
    val obliquity = getObliquity(jd)

    val x = atan(-cos(Math.toRadians(correctedApparentLongitude)) * tan(obliquity))
    val y = atan(-cos(Math.toRadians(apparentLongitude - k)) * tan(i))

    val eta = Math.toDegrees(atan(tan(Math.toRadians(apparentLongitude - k)) * cos(Math.toRadians(i))))
    val l0 = eta - theta
    val p = x + y
    val b0 = Math.toDegrees(asin(sin(Math.toRadians(apparentLongitude - k)) * sin(Math.toRadians(i))))
    return Triple(l0, p, b0)
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

    val omega = 125.04452 - 1934.136261 * t + 0.0020708 * (t * t) + (t * t * t) / 450_000

    val meanLongitudeSun = 280.4665 + 36000.76983 * t
    val meanLongitudeMoon = 218.3165 + 481267.881286 * t

    // nutation in obliquity
    val deltaEpsilon = fromArcseconds(9.20) * cos(Math.toRadians(omega)) +
            fromArcseconds(0.57) * cos(Math.toRadians(2 * meanLongitudeSun)) +
            fromArcseconds(0.10) * cos(Math.toRadians(2 * meanLongitudeMoon)) -
            fromArcseconds(0.09) * cos(Math.toRadians(2 * omega))

    val epsilonNaught = fromDms(23.0, 26.0, 21.448) -
            fromArcseconds(46.8150 * t) -
            fromArcseconds(0.00059 * (t * t)) +
            fromArcseconds(0.001813 * (t * t * t))

    return epsilonNaught + deltaEpsilon
}

private fun getNutationLongitude(jd: Double): Double {
    val t = getTimeInCenturies(jd)

    val omega = 125.04452 - 1934.136261 * t + 0.0020708 * (t * t) + (t * t * t) / 450_000

    val meanLongitudeSun = 280.4665 + 36000.76983 * t
    val meanLongitudeMoon = 218.3165 + 481267.881286 * t

    return  fromArcseconds(-17.20) * sin(Math.toRadians(omega)) +
            fromArcseconds(1.32) * sin(Math.toRadians(2 * meanLongitudeSun)) -
            fromArcseconds(0.23) * sin(Math.toRadians(2 * meanLongitudeMoon)) +
            fromArcseconds(0.21) * sin(Math.toRadians(2 * omega))
}
