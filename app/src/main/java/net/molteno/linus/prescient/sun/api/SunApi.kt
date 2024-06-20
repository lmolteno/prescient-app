package net.molteno.linus.prescient.sun.api

import net.molteno.linus.prescient.sun.api.models.SolarEventObservation
import net.molteno.linus.prescient.sun.api.models.SolarRegionObservation
import net.molteno.linus.prescient.sun.api.models.toSolarEventObservation
import net.molteno.linus.prescient.sun.api.models.toSolarRegionObservation
import timber.log.Timber
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.floor

@Singleton
class SunApi @Inject constructor (
    private val potsdam: PotsdamApi,
    private val swpcApi: SwpcApi
) {
    suspend fun fetchHp30(): List<HpEntry> {
        val hp30s: MutableList<HpEntry> = mutableListOf()
        potsdam.fetchHp30File().byteStream().reader().useLines { lines ->
            lines.forEach { line ->
                if (line.startsWith("#")) return@forEach
                val entries = line.split(" ").filter { it.isNotBlank() }
                // YYYY MM DD hh.h hh._m days days_m Hp30 ap30 D
                if (entries.count() != 10) {
                    Timber.e("Invalid line $line")
                    return@forEach
                }
                val (year, month, day, hour) = entries.slice(0..3)
                val time = ZonedDateTime.of(
                    year.toInt(),
                    month.toInt(),
                    day.toInt(),
                    floor(hour.toDouble()).toInt(),
                    ((hour.toDouble() * 60) % 60).toInt(),
                    0,
                    0,
                    java.time.ZoneOffset.UTC
                )
                val hp30 = entries[7].toDouble()
                val ap30 = entries[8].toDouble()
                if (hp30 > -0.5) { // -1 for invalid (unpopulated
                    hp30s.add(HpEntry(time, hp30, ap30))
                }
            }
        }
        return hp30s
    }

    suspend fun fetchSolarRegions(): Map<Int, List<SolarRegionObservation>> {
        try {
            val regions = swpcApi.fetchSolarRegions().mapNotNull { it.toSolarRegionObservation() }
            val mostRecentObservations = regions.maxOf { it.observedDate}

            return regions
                .filter { it.observedDate == mostRecentObservations }
                .groupBy { it.region }
        } catch (e: Exception) {
            Timber.e(e)
            throw e
        }
    }

    suspend fun fetchSolarEvents(): Map<Int, List<SolarEventObservation>> {
        try {
            val events = swpcApi.fetchEvents()
                .mapNotNull { it.toSolarEventObservation() }
                .filter { it.beginDatetime.isAfter(ZonedDateTime.now().minusDays(7)) }
            return events.groupBy { it.eventId }
        } catch (e: Exception) {
            Timber.e(e)
            throw e
        }
    }
}