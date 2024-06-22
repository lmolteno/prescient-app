package net.molteno.linus.prescient.sun

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

@Composable
fun SunRegionList(
    regions: Map<Int, List<SolarRegionObservation>>,
    selectedRegion: Int? = null,
    onRegionSelection: (region: Int?) -> Unit = { },
    state: LazyListState = rememberLazyListState(),
    internalPadding: PaddingValues = PaddingValues()
) {
    LazyColumn (
        Modifier.fillMaxSize().padding(horizontal = 16.dp),
        state = state,
        contentPadding = PaddingValues(
            top = internalPadding.calculateTopPadding() + 16.dp,
            bottom = internalPadding.calculateBottomPadding() + 16.dp,
            start = internalPadding.calculateStartPadding(LocalLayoutDirection.current),
            end = internalPadding.calculateEndPadding(LocalLayoutDirection.current),
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(regions.entries.sortedByDescending { it.value.maxBy { it.observedDate }.area }.toList()) {region ->
            val regionId = region.key
            val observations = region.value
            val latestObservation = observations.maxBy { it.observedDate }
            val selected = selectedRegion == regionId

            ElevatedCard(
                onClick = { onRegionSelection(if (selected) null else regionId) },
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (selected) 5.dp else 1.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text("region", style = MaterialTheme.typography.labelSmall)
                            Text("AR$regionId",
                                style = MaterialTheme.typography.labelLarge
                                    .copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        ScoreCard(
                            "area",
                            "${latestObservation.area}",
                            Alignment.End,
                            Modifier.width(50.dp)
                        )
                        ScoreCard(
                            "spots",
                            "${latestObservation.numberSpots}",
                            Alignment.End,
                            Modifier.width(50.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("classification", style = MaterialTheme.typography.labelMedium)
                        Spacer(Modifier.width(8.dp))
                        HorizontalDivider()
                    }
                    Row {
                        ScoreCard(
                            "magnetic",
                            "${latestObservation.magClass?.toGreek()}",
                            Alignment.Start,
                            Modifier.width(100.dp)
                        )
                        ScoreCard(
                            "spot",
                            "${latestObservation.spotClass}",
                            Alignment.Start,
                            Modifier.width(100.dp)
                        )
                    }
                    AnimatedVisibility(visible = selected) {
                        Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("events", style = MaterialTheme.typography.labelMedium)
                                Spacer(Modifier.width(8.dp))
                                HorizontalDivider()
                            }
                            Row {
                                ScoreCard(
                                    "X x-ray",
                                    "${latestObservation.xXrayEvents}",
                                    Alignment.Start,
                                    Modifier.width(50.dp)
                                )
                                ScoreCard(
                                    "M x-ray",
                                    "${latestObservation.mXrayEvents}",
                                    Alignment.Start,
                                    Modifier.width(50.dp)
                                )
                                ScoreCard(
                                    "C x-ray",
                                    "${latestObservation.cXrayEvents}",
                                    Alignment.Start,
                                    Modifier.width(50.dp)
                                )
                                ScoreCard(
                                    "proton",
                                    "${latestObservation.protonEvents}",
                                    Alignment.Start,
                                    Modifier.width(50.dp)
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("probabilities", style = MaterialTheme.typography.labelMedium)
                                Spacer(Modifier.width(8.dp))
                                HorizontalDivider()
                            }
                            Row {
                                ScoreCard(
                                    "X x-ray",
                                    "${latestObservation.xFlareProbability}%",
                                    Alignment.Start,
                                    Modifier.width(50.dp)
                                )
                                ScoreCard(
                                    "M x-ray",
                                    "${latestObservation.mFlareProbability}%",
                                    Alignment.Start,
                                    Modifier.width(50.dp)
                                )
                                ScoreCard(
                                    "C x-ray",
                                    "${latestObservation.cFlareProbability}%",
                                    Alignment.Start,
                                    Modifier.width(50.dp)
                                )
                                ScoreCard(
                                    "proton",
                                    "${latestObservation.protonProbability}%",
                                    Alignment.Start,
                                    Modifier.width(50.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreCard(label: String, value: String, alignment: Alignment.Horizontal, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = alignment) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            textAlign = if (alignment == Alignment.End) TextAlign.End else TextAlign.Start
        )
        Text(
            value,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

fun String.toGreek() =
    this.map { when (it) {
        'A' -> "α"
        'B' -> "β"
        'D' -> "δ"
        'G' -> "γ"
        else -> it
    } }.joinToString("")


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
                protonProbability = 0,
                longitude = 17,
                latitude = -8,
                area = 100
            ))
        ) )
    }

    LaunchedEffect(Unit) {
        launch(Dispatchers.IO) {
            val rawRegions = noaaApi.fetchSolarRegions()
                .mapNotNull { it.toSolarRegionObservation() }

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
            SunRegionList(regions = regions, selectedRegion = 3857)
        }
    }
}