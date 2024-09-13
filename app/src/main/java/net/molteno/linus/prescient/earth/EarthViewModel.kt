package net.molteno.linus.prescient.earth

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import mil.nga.sf.MultiPolygon
import net.molteno.linus.prescient.earth.api.EarthApi
import net.molteno.linus.prescient.earth.api.models.WeatherForecast
import net.molteno.linus.prescient.earth.utils.subsolarPoint
import javax.inject.Inject

@SuppressLint("MissingPermission")
@HiltViewModel
class EarthViewModel @Inject constructor (@ApplicationContext private val context: Context, private val earthApi: EarthApi): ViewModel() {
    private var locationProvider = LocationServices.getFusedLocationProviderClient(context)

    val currentLocation = MutableStateFlow<Location?>(null)
    val forecast = MutableStateFlow<WeatherForecast?>(null)
    val coastlines = MutableStateFlow<List<MultiPolygon>?>(null)
    val subsolarPoint = MutableStateFlow(subsolarPoint(Clock.System.now()))

    private fun areLocationPermissionsGranted(): Boolean =
        ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    fun locationUpdated() {
        getLatestLocation()
    }

    private fun getLatestLocation() {
        if (areLocationPermissionsGranted()) {
            val req = CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                .setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
                .setDurationMillis(10_000)
                .setMaxUpdateAgeMillis(10_000)
                .build()

            locationProvider.getCurrentLocation(req, null)
                .addOnSuccessListener { location ->
                    currentLocation.value = location
                    location?.let {
                        viewModelScope.launch(Dispatchers.IO) {
                            forecast.value = earthApi.getForecast(it.longitude, it.latitude)
                        }
                    }
            }
        }
    }

    fun changeTime(time: Instant) {
        subsolarPoint.value = subsolarPoint(time)
    }

    init {
        getLatestLocation()
        coastlines.value = earthApi.getCoastlines()
    }
}
