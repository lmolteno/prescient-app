package net.molteno.linus.prescient.sun.api

import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Streaming
import timber.log.Timber
import java.time.ZonedDateTime
import kotlin.math.floor

private const val BASE_URL = "https://www-app3.gfz-potsdam.de/"

interface PotsdamApi {
    @Streaming
    @GET("kp_index/Hp30_ap30_nowcast.txt")
    suspend fun fetchHp30File(): ResponseBody
}

suspend fun fetchHp30(): List<HpEntry> {
    val potsdamApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val potsdam = potsdamApi.create(PotsdamApi::class.java)
    Timber.d("fetching hp30")
    val hp30s: MutableList<HpEntry> = mutableListOf()
    val reader = potsdam.fetchHp30File().byteStream().reader().useLines { lines ->
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
            if (hp30 > -0.5) {
                hp30s.add(HpEntry(time, hp30, ap30))
            }
        }
    }
    return hp30s
}

data class HpEntry (
    val time: ZonedDateTime,
    val hp30: Double,
    val ap30: Double
)