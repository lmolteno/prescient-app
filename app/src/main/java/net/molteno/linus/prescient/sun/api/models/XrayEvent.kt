package net.molteno.linus.prescient.sun.api.models

import java.time.ZonedDateTime

data class XrayEvent(
    override val region: Int,
    override val eventId: Int,

    override val beginDatetime: ZonedDateTime,
    override val beginQuality: String,

    override val maxDatetime: ZonedDateTime?,
    override val maxQuality: String,

    override val endDatetime: ZonedDateTime?,
    override val endQuality: String,

    override val type: SolarEventType,
    override val observatory: SolarObservatory,
    override val quality: String,

    override val statusCode: Int,
    override val statusText: String,
    override val changeFlag: Int,

    /** MHz */
    val frequency: Int,
    val xRayClass: String
): SolarEvent