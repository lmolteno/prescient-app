package net.molteno.linus.prescient.api.models

import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

@Serializable
data class SolarRegionObservation(
    val id: Int,
    val region: Int,
    val observedDate: LocalDate, // iso 8601
    val firstDate: Instant,
    val latitude: Int,
    val longitude: Int,
    val metadata: SolarRegionObservationMetadata
)

@Serializable
data class SolarRegionObservationMetadata(
    val location: String,
    val carringtonLongitude: Int?,
    val area: Int,
    val spotClass: String?,
    val extent: Int,
    val numberSpots: Int,
    val magClass: String?,
    val magString: String?,
    val status: String?,
    val cXrayEvents: Int,
    val mXrayEvents: Int,
    val xXrayEvents: Int,
    val protonEvents: Int?,
    val cFlareProbability: Int,
    val mFlareProbability: Int,
    val xFlareProbability: Int,
    val protonProbability: Int?,
)
