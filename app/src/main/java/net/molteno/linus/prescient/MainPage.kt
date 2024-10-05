package net.molteno.linus.prescient

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.molteno.linus.prescient.api.PrescientApiModule
import net.molteno.linus.prescient.api.models.SolarRegionObservation
import net.molteno.linus.prescient.earth.EarthPage
import net.molteno.linus.prescient.moon.MoonPage
import net.molteno.linus.prescient.sun.SunPage
import net.molteno.linus.prescient.sun.api.HpEntry
import net.molteno.linus.prescient.sun.api.models.SolarEventObservation
import net.molteno.linus.prescient.ui.theme.PrescientTheme
import java.time.Instant

@Composable
fun MainPage() {
    val viewModel: MainViewModel = hiltViewModel()
    val currentHp by viewModel.hp30.collectAsState()
    val solarEvents by viewModel.solarEvents.collectAsState()
    val solarRegions by viewModel.solarRegions.collectAsState()

    Surface {
        MainPageView(
            solarRegions = solarRegions,
            solarEvents = solarEvents,
            currentHp = currentHp
        )
    }
}

enum class SystemObject(val icon: String) {
    SUN("☉"),
    EARTH("\uD83D\uDF28"),
    MOON("☾")
}

@Composable
fun MainPageView(
    solarRegions: Map<Int, List<SolarRegionObservation>>?,
    solarEvents: Map<Int, List<SolarEventObservation>>?,
    currentHp: List<HpEntry>?
) {
    var selectedItem by remember { mutableStateOf(SystemObject.EARTH) }

    @Composable
    fun getMainContent(item: SystemObject) {
        AnimatedContent(
            targetState = item,
            label = "Fade between objects",
            transitionSpec = { slideInVertically { it } togetherWith slideOutVertically { it } }
        ) { obj ->
            when (obj) {
                SystemObject.SUN -> SunPage(regions = solarRegions ?: emptyMap(), solarEvents = solarEvents, currentHp = currentHp)
                SystemObject.MOON -> MoonPage()
                SystemObject.EARTH -> EarthPage()
            }
        }
    }

    Scaffold(bottomBar = {
        NavigationBar {
            SystemObject.entries.forEach { item ->
                NavigationBarItem(
                    icon = { Text(item.icon, fontWeight = FontWeight.Bold) }, // Icon(Icons.Filled.Favorite, contentDescription = item.name) },
                    label = { Text(item.name) },
                    selected = selectedItem == item,
                    onClick = { selectedItem = item }
                )
            }
        }
    }) { paddingValues ->
        Column(Modifier.padding(paddingValues)) {
            getMainContent(item = selectedItem)
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MainPagePreview() {
    val noaaApi = remember { PrescientApiModule.providesPrescientApi() }
    var regions by remember { mutableStateOf<Map<Int, List<SolarRegionObservation>>>(emptyMap()) }

    LaunchedEffect(Unit) {
        launch(Dispatchers.IO) {
            val rawRegions = noaaApi.fetchRegions(Instant.now().minusSeconds(86400), Instant.now())

            regions = rawRegions
                .filter { it.metadata.numberSpots > 0 && it.observedDate == rawRegions.maxBy { r -> r.observedDate }.observedDate }
                .groupBy { it.region }
        }
    }

    PrescientTheme {
        Surface(
            Modifier
                .height(800.dp)
                .width(400.dp)
        ) {
            MainPageView(regions, emptyMap(), null)
        }
    }
}