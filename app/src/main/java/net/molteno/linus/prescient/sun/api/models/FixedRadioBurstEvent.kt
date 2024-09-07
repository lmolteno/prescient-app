package net.molteno.linus.prescient.sun.api.models

import java.time.ZonedDateTime

data class FixedRadioBurstEvent(
    override val region: Int?,
    override val eventId: Int,

    override val beginDatetime: ZonedDateTime,
    override val beginQuality: String?,

    override val maxDatetime: ZonedDateTime?,
    override val maxQuality: String?,

    override val endDatetime: ZonedDateTime?,
    override val endQuality: String?,

    override val type: SolarEventType,
    override val observatory: SolarObservatory,
    override val quality: String,

    override val statusCode: Int,
    override val statusText: String?,
    override val changeFlag: Int,

    val frequency: Int,
    /**
     * 	The peak value above pre-burst background of associated radio bursts
     * 	at frequencies 245, 410, 610, 1415, 2695, 4995, 8800 and 15400 MHz:
     * 	       1 flux unit = 10-22 Wm-2 Hz-1
     */
    val maxBrightness: Int
): SolarEventObservation
