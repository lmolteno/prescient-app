package net.molteno.linus.prescient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import mil.nga.sf.MultiPolygon
import net.molteno.linus.prescient.api.models.SolarRegionObservation
import net.molteno.linus.prescient.earth.api.EarthApi
import net.molteno.linus.prescient.sun.api.HpEntry
import net.molteno.linus.prescient.sun.api.SunApi
import net.molteno.linus.prescient.sun.api.models.SolarEventObservation
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(private val sunApi: SunApi, private val earthApi: EarthApi): ViewModel() {
    val solarEvents = MutableStateFlow<Map<Int, List<SolarEventObservation>>?>(null)
    val hp30 = MutableStateFlow<List<HpEntry>?>(null)
    val solarRegions = MutableStateFlow<Map<Int, List<SolarRegionObservation>>?>(null)
    val coastlines = MutableStateFlow<List<MultiPolygon>>(emptyList())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            hp30.value = sunApi.fetchHp30()
        }
        viewModelScope.launch(Dispatchers.IO) {
            solarRegions.value = sunApi.fetchSolarRegions()
        }
        viewModelScope.launch(Dispatchers.IO) {
            solarEvents.value = sunApi.fetchSolarEvents()
        }
        viewModelScope.launch(Dispatchers.IO) {
            coastlines.value = earthApi.getCoastlines()
        }
    }
}
