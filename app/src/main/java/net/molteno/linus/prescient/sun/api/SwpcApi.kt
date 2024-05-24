package net.molteno.linus.prescient.sun.api

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.molteno.linus.prescient.sun.api.models.SolarRegionObservationDto
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import javax.inject.Singleton

private const val BASE_URL = "https://services.swpc.noaa.gov/"

interface SwpcApi {
    @GET("/json/solar_regions.json")
    suspend fun fetchSolarRegions(): List<SolarRegionObservationDto>
}

@Module
@InstallIn(SingletonComponent::class)
object NoaaApiModule {
    @Provides
    @Singleton
    fun providesNoaaApi(): SwpcApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory
            .create(GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create()))
        .build()
        .create(SwpcApi::class.java)
}
