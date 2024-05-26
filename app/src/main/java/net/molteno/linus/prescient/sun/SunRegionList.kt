package net.molteno.linus.prescient.sun

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.molteno.linus.prescient.sun.api.NoaaApiModule
import net.molteno.linus.prescient.sun.api.models.SolarRegionObservation
import net.molteno.linus.prescient.sun.api.models.toSolarRegionObservation
import net.molteno.linus.prescient.ui.theme.PrescientTheme
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SunRegionList(
    regions: Map<Int, List<SolarRegionObservation>>,
    onRegionSelection: (region: Int) -> Unit = { },
    state: LazyListState = rememberLazyListState()
) {
    LazyColumn (
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        stickyHeader {
            Surface(shadowElevation = 5.dp) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("region")
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                            Text("spots")
                        }
                    }
                }
                HorizontalDivider()
            }
        }
        items(regions.entries.toList()) {region ->
            val regionId = region.key
            val observations = region.value
            val latestObservation = observations.maxBy { it.observedDate }

            Surface(
                Modifier
                    .padding(horizontal = 5.dp)
                    .clickable { onRegionSelection(regionId) },
                shape = RoundedCornerShape(5.dp),
                tonalElevation = 1.dp,
                shadowElevation = 2.dp,
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 5.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text("AR$regionId", style = MaterialTheme.typography.labelLarge)
                            Text(
                                latestObservation.location,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Column(
                            Modifier
                                .weight(1f)
                                .padding(horizontal = 5.dp), horizontalAlignment = Alignment.End) {
                            Text("${latestObservation.numberSpots}")
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun SunRegionListPreview() {
    val noaaApi = remember { NoaaApiModule.providesNoaaApi() }
    var regions by remember {
        mutableStateOf(mapOf(
            3857 to listOf(SolarRegionObservation(
                region = 3857,
                carringtonLongitude = 100,
                observedDate = LocalDate.now(),
                spotClass = null,
                magClass = null,
                magString = null,
                location = "S08E17",
                extent = 10,
                numberSpots = 5,
                cXrayEvents = 0,
                mXrayEvents = 0,
                xXrayEvents = 1,
                protonEvents = null,
                status = "idk",
                firstDate = LocalDateTime.now(),
                xFlareProbability = 1,
                cFlareProbability = 0,
                mFlareProbability = 0,
                longitude = 17,
                latitude = -8,
                area = 100
            ))
        ) )
    }

    LaunchedEffect(Unit) {
        launch(Dispatchers.IO) {
            val rawRegions = noaaApi.fetchSolarRegions()
                .map { it.toSolarRegionObservation() }

            regions = rawRegions
                .filter { it.numberSpots > 0 && it.observedDate == rawRegions.maxBy { r -> r.observedDate }.observedDate }
                .groupBy { it.region }
        }
    }

    PrescientTheme {
        Surface(
            Modifier
                .width(300.dp)
                .height(500.dp)) {
            SunRegionList(regions = regions)
        }
    }
}