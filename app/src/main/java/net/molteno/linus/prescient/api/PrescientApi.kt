package net.molteno.linus.prescient.api

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf
import net.molteno.linus.prescient.api.models.SolarRegionObservation
import okhttp3.MediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query
import java.time.Instant
import javax.inject.Singleton

private const val BASE_URL = "https://prescient.236.nz/"

interface PrescientApi {
    @Headers("Accept: application/protobuf")
    @GET("swpc/region/{region}")
    suspend fun fetchRegion(@Path("region") regionId: Int): List<SolarRegionObservation>

    @Headers("Accept: application/protobuf")
    @GET("swpc/region")
    suspend fun fetchRegions(@Query("start") start: Instant, @Query("end") end: Instant): List<SolarRegionObservation>
}

@OptIn(ExperimentalSerializationApi::class)
@Module
@InstallIn(SingletonComponent::class)
object PrescientApiModule {
    @Provides
    @Singleton
    fun providesPrescientApi(): PrescientApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(ProtoBuf {  }.asConverterFactory(MediaType.parse("application/protobuf")!!))
        .addConverterFactory(Json.asConverterFactory(MediaType.parse("application/json")!!))
        .build()
        .create(PrescientApi::class.java)
}

