package net.molteno.linus.prescient.sun.api

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Streaming
import timber.log.Timber
import java.time.ZonedDateTime
import javax.inject.Singleton
import kotlin.math.floor

private const val BASE_URL = "https://www-app3.gfz-potsdam.de/"

interface PotsdamApi {
    @Streaming
    @GET("kp_index/Hp30_ap30_nowcast.txt")
    suspend fun fetchHp30File(): ResponseBody
}

@Module
@InstallIn(SingletonComponent::class)
object PotsdamApiModule {
    @Provides
    @Singleton
    fun providesPotsdamApi(): PotsdamApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(PotsdamApi::class.java)
}


data class HpEntry (
    val time: ZonedDateTime,
    val hp30: Double,
    val ap30: Double
)