package net.molteno.linus.prescient

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.molteno.linus.prescient.earth.Earth
import net.molteno.linus.prescient.moon.Moon
import net.molteno.linus.prescient.sun.Sun
import net.molteno.linus.prescient.sun.api.HpEntry
import net.molteno.linus.prescient.sun.api.NoaaApiModule
import net.molteno.linus.prescient.sun.api.models.SolarRegionObservation
import net.molteno.linus.prescient.sun.api.models.toSolarRegionObservation
import net.molteno.linus.prescient.ui.theme.PrescientTheme
import java.time.LocalDate

@Composable
fun MainPage() {
    val viewModel: TestViewModel = hiltViewModel()
    val currentHp by viewModel.hp30.collectAsState()
    val solarRegions by viewModel.solarRegions.collectAsState()

    MainPageView(solarRegions = solarRegions, currentHp = currentHp, phase = 0.25f)
}

enum class SystemObject {
    EARTH, MOON, SUN
}

val enterTransition = scaleIn() + fadeIn()
val exitTransition = scaleOut() + fadeOut()

@Composable
fun MainPageView(
    solarRegions: Map<Int, List<SolarRegionObservation>>?,
    currentHp: List<HpEntry>?,
    phase: Float
) {
    var selectedItem by remember { mutableStateOf(SystemObject.EARTH) }
    var topLeftItem by remember { mutableStateOf(SystemObject.SUN) }
    var topRightItem by remember { mutableStateOf(SystemObject.MOON) }

    @Composable
    fun getItem(item: SystemObject) {
        AnimatedContent(
            targetState = item,
            label = "Fade between objects",
        ) {
            when (it) {
                SystemObject.SUN -> Sun(regions = solarRegions?.mapNotNull { entry ->
                    entry.value.firstOrNull { it.observedDate == LocalDate.now().minusDays(1) }
                } ?: emptyList())

                SystemObject.EARTH -> Earth(phase = phase)
                SystemObject.MOON -> Moon(phase = phase)
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier
                .height(60.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                Modifier
                    .aspectRatio(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        val temp = selectedItem
                        selectedItem = topLeftItem
                        topLeftItem = temp
                    }) {
                getItem(item = topLeftItem)
            }
            Column(
                Modifier
                    .aspectRatio(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        val temp = selectedItem
                        selectedItem = topRightItem
                        topRightItem = temp
                    }
            ) {
                getItem(item = topRightItem)
            }
        }
        HorizontalDivider()
        getItem(item = selectedItem)
    }
}

@Composable
fun HpDisplay(hp30: HpEntry?) {
    if (hp30 == null) return
    Column {
        Text("rom ${hp30.time} to ${hp30.time.plusMinutes(30).toLocalTime()} = ${hp30.hp30}")
    }
}

@Preview
@Composable
fun MainPagePreview() {
    val noaaApi = remember { NoaaApiModule.providesNoaaApi() }
    var regions by remember { mutableStateOf<Map<Int, List<SolarRegionObservation>>>(emptyMap()) }

    LaunchedEffect(Unit) {
        launch(Dispatchers.IO) {
            regions = noaaApi.fetchSolarRegions()
                .map { it.toSolarRegionObservation() }.filter { it.numberSpots > 0 }
                .groupBy { it.region }
        }
    }

    PrescientTheme {
        Surface(
            Modifier
                .height(800.dp)
                .width(400.dp)
        ) {
            MainPageView(regions, null, 0.6f)
        }
    }
}