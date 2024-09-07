package net.molteno.linus.prescient.earth;

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.Clock
import net.molteno.linus.prescient.earth.api.EarthApi
import net.molteno.linus.prescient.earth.utils.subsolarPoint
import javax.inject.Inject

@SuppressLint("MissingPermission")
@HiltViewModel
class EarthViewModel @Inject constructor (@ApplicationContext private val context: Context, private val earthApi: EarthApi): ViewModel() {
    private var locationProvider = LocationServices.getFusedLocationProviderClient(context)

    val currentLocation = MutableStateFlow<Location?>(null)
    val subsolarPoint = subsolarPoint(Clock.System.now())

    private fun areLocationPermissionsGranted(): Boolean =
        ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    fun locationUpdated() {
        getLatestLocation()
    }

    private fun getLatestLocation() {
        if (areLocationPermissionsGranted()) {
            locationProvider.lastLocation.addOnSuccessListener { location ->
                currentLocation.value = location
            }
        }
    }

    init {
        getLatestLocation()
    }
}
