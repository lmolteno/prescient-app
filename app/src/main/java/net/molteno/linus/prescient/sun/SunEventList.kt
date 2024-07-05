package net.molteno.linus.prescient.sun

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.molteno.linus.prescient.sun.api.models.GenericSolarEvent
import net.molteno.linus.prescient.sun.api.models.SolarEventObservation
import net.molteno.linus.prescient.sun.api.models.SolarEventType
import net.molteno.linus.prescient.sun.api.models.SolarObservatory
import net.molteno.linus.prescient.ui.theme.PrescientTheme
import java.time.ZonedDateTime

val X_RAY_EVENT_TYPES = listOf(SolarEventType.XRayEvent, SolarEventType.XRayFlare)
val RADIO_EVENT_TYPES = listOf(SolarEventType.RadioNoiseStorm, SolarEventType.SweepFrequencyRadioBurst, SolarEventType.FixedFrequencyRadioBurst)

val groupings = mapOf(
    "X-Ray" to X_RAY_EVENT_TYPES,
    "Radio" to RADIO_EVENT_TYPES,
    "Other" to listOf(
        SolarEventType.BrightSurge, SolarEventType.EruptiveProminence, SolarEventType.Filament,
        SolarEventType.FilamentDisappearance, SolarEventType.ForbushDecrease,
        SolarEventType.GroundLevelEvent, SolarEventType.LoopProminenceSystem,
        SolarEventType.OpticalFlare, SolarEventType.PolarCapAbsorption, SolarEventType.Spray
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SunEventList(
    events: Map<Int, List<SolarEventObservation>>?,
    state: LazyListState = rememberLazyListState(),
    internalPadding: PaddingValues = PaddingValues(),
    setSelectedRegion: (Int?) -> Unit = { }
) {
    var selectedEvent: Int? by remember { mutableStateOf(null) }
    var selectedGroupings: Map<String, List<SolarEventType>> by remember {
        mutableStateOf(groupings.filterKeys { it == "X-Ray" })
    }

    val filteredEvents by remember(events) {
        derivedStateOf {
            events?.filter { event ->
                event.value.any { selectedGroupings.values.flatten().contains(it.type) }
            }
        }
    }

    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        state = state,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(
            top = internalPadding.calculateTopPadding() + 16.dp,
            bottom = internalPadding.calculateBottomPadding() + 16.dp,
            start = internalPadding.calculateStartPadding(LocalLayoutDirection.current),
            end = internalPadding.calculateEndPadding(LocalLayoutDirection.current),
        )
    ) {
        item {
            MultiChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                groupings.entries.forEachIndexed { index, (key, value) ->
                    val selected = selectedGroupings.containsKey(key)
                    SegmentedButton(
                        checked = selected,
                        onCheckedChange = {
                            if (it) selectedGroupings += mapOf(key to value)
                            else selectedGroupings -= key
                        },
                        shape = when (index) {
                            0 -> RoundedCornerShape(50)
                                .copy(topEnd = ZeroCornerSize, bottomEnd = ZeroCornerSize)
                            groupings.entries.size - 1 -> RoundedCornerShape(50)
                                .copy(topStart = ZeroCornerSize, bottomStart = ZeroCornerSize)
                            else -> RectangleShape
                        }
                    ) {
                        Text(key)
                    }
                }
            }
        }
        items(
            filteredEvents?.entries?.toList() ?: emptyList(),
            key = { it.key }) { (eventId, observations) ->
            SunEventItem(
                eventId,
                observations,
                eventId == selectedEvent,
                onSelect = { eventId, regionId ->
                    selectedEvent = eventId
                    setSelectedRegion(regionId)
                })
        }
    }
    if (filteredEvents == null) {
        Row(Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) {
            LinearProgressIndicator()
        }
    } else if (filteredEvents.isNullOrEmpty()) {
        Row(Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) {
            Text("No events match the filters")
        }
    }
}

@Preview
@Composable
fun SunEventListPreview() {
    PrescientTheme {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLowest) {
            SunEventList(
                events = mapOf(
                    10 to listOf(
                        GenericSolarEvent(
                            region = 100,
                            beginQuality = null,
                            endQuality = null,
                            beginDatetime = ZonedDateTime.now(),
                            endDatetime = ZonedDateTime.now(),
                            eventId = 10,
                            type = SolarEventType.XRayEvent,
                            changeFlag = 1,
                            maxQuality = null,
                            statusCode = 10,
                            statusText = "hello",
                            maxDatetime = ZonedDateTime.now(),
                            observatory = SolarObservatory.SanVito,
                            quality = null
                        )
                    )
                )
            )
        }
    }
}

@Preview
@Composable
fun SunEventListLoadingPreview() {
    PrescientTheme {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLowest) {
            SunEventList(
                events = null
            )
        }
    }
}
