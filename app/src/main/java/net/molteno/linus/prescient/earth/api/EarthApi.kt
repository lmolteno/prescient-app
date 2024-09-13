package net.molteno.linus.prescient.earth.api

import com.openmeteo.api.Forecast
import com.openmeteo.api.OpenMeteo
import com.openmeteo.api.common.Response
import com.openmeteo.api.common.time.Timezone
import com.openmeteo.api.common.units.TemperatureUnit
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import net.molteno.linus.prescient.earth.api.models.Measurement
import net.molteno.linus.prescient.earth.api.models.WeatherForecast
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EarthApi @Inject constructor(private val db: EarthDb) {

    fun getCoastlines() = db.getCoastlines()

    @OptIn(Response.ExperimentalGluedUnitTimeStepValues::class)
    fun getForecast(longitude: Double, latitude: Double): WeatherForecast {
        val om = OpenMeteo(latitude.toFloat(), longitude.toFloat())
        val omForecast = om.forecast {
            hourly = Forecast.Hourly {
                listOf(temperature2m, cloudcoverLow, cloudcoverMid, cloudcoverHigh, cloudcover, rain, windspeed10m, windgusts10m)
            }
            temperatureUnit = TemperatureUnit.Celsius
            timezone = Timezone.getTimeZone(ZoneId.systemDefault())
        }.getOrThrow()

        val forecast = WeatherForecast()
        Forecast.Hourly.run {
            forecast.measurements = omForecast.hourlyValues.getValue("time").mapIndexedNotNull { index, time ->
                if (time == null || Instant.fromEpochSeconds(time.toLong()) < Clock.System.now()) {
                    null
                } else {
                    Measurement(
                        time = Instant.fromEpochSeconds(time.toLong()),
                        temperature = omForecast.hourlyValues.getValue(temperature2m)[index],
                        rain = omForecast.hourlyValues.getValue(rain)[index],
                        cloudCoverLow = omForecast.hourlyValues.getValue(cloudcoverLow)[index],
                        cloudCoverMid = omForecast.hourlyValues.getValue(cloudcoverMid)[index],
                        cloudCoverHigh = omForecast.hourlyValues.getValue(cloudcoverHigh)[index],
                        cloudCover = omForecast.hourlyValues.getValue(cloudcover)[index],
                        windSpeed = omForecast.hourlyValues.getValue(windspeed10m)[index],
                        windGust = omForecast.hourlyValues.getValue(windgusts10m)[index]
                    )
                }
            }
        }
        return forecast
    }
}