package net.molteno.linus.prescient.sun

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
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
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.molteno.linus.prescient.sun.api.models.FixedRadioBurstEvent
import net.molteno.linus.prescient.sun.api.models.FlareEvent
import net.molteno.linus.prescient.sun.api.models.GenericSolarEvent
import net.molteno.linus.prescient.sun.api.models.SolarEventObservation
import net.molteno.linus.prescient.sun.api.models.SolarEventType
import net.molteno.linus.prescient.sun.api.models.SolarObservatory
import net.molteno.linus.prescient.sun.api.models.SweptRadioBurstEvent
import net.molteno.linus.prescient.sun.api.models.XrayEvent
import net.molteno.linus.prescient.ui.theme.PrescientTheme
import timber.log.Timber
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

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
                        }) {
                        Text(key)
                    }
                }
            }
        }
        items(filteredEvents?.entries?.toList() ?: emptyList(), key = { it.key }) { (eventId, observations) ->
            SunEventItem(eventId, observations, eventId == selectedEvent, onSelect = { eventId, regionId ->
                selectedEvent = eventId
                setSelectedRegion(regionId)
            })
        }
    }
}

@Composable
fun SunEventItem(eventId: Int, events: List<SolarEventObservation>, selected: Boolean = false, onSelect: (Int?, Int?) -> Unit) {
    val firstBegin = remember { events.minOfOrNull { it.beginDatetime }?.withZoneSameInstant(ZoneId.systemDefault()) }
    val lastEnd = remember {
        events.map { it.endDatetime }.let { datetimes ->
            if (datetimes.none { it == null }) {
                return@let datetimes.filterNotNull().maxOrNull()
                    ?.withZoneSameInstant(ZoneId.systemDefault())
            }
            null
        }
    }
    val regionId = remember { events.firstNotNullOfOrNull { it.region } }

    ElevatedCard(
        onClick = { if (selected) onSelect(null, null) else onSelect(eventId, regionId) },
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (selected) 5.dp else 1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row {
                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.clickable { Timber.d("Selecting $regionId") }
                ) {
                    Text("region", style = MaterialTheme.typography.labelSmall)
                    Text(
                        regionId?.toString() ?: "n/a",
                        style = MaterialTheme.typography.labelLarge
                            .copy(fontWeight = if (regionId == null) FontWeight.Normal else FontWeight.Bold),
                    )
                }
                Spacer(
                    Modifier
                        .weight(1f)
                        .width(8.dp))
                if (events.any { it is XrayEvent }) {
                    val xRayEvent = events.first { it is XrayEvent } as XrayEvent
                    ScoreCard(
                        "class",
                        xRayEvent.xRayClass,
                        Alignment.End
                    )
                    Spacer(Modifier.width(8.dp))
                } else if (events.any { it is FlareEvent }) {
                    val flare = events.first { it is FlareEvent } as FlareEvent
                    ScoreCard(
                        "flare importance",
                        flare.importance.toString(),
                        Alignment.End
                    )
                    Spacer(Modifier.width(8.dp))
                }
                ScoreCard(
                    "type(s)",
                    events.map { it.type.code }.distinct().joinToString(" "),
                    Alignment.End
                )
            }
            Spacer(Modifier.height(8.dp))
            Row {
                Column(horizontalAlignment = Alignment.Start) {
                    ScoreCard(
                        "start",
                        firstBegin?.toLocalDateTime()?.format(DateTimeFormatter.ISO_DATE_TIME) ?: "unknown",
                        Alignment.Start
                    )
                }
                Spacer(Modifier.weight(1f))
                ScoreCard(
                    "end",
                    lastEnd?.toLocalTime()?.format(DateTimeFormatter.ISO_TIME) ?: "unknown",
                    Alignment.End
                )
            }
            AnimatedVisibility(visible = selected) {
                Column(
                    Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("events", style = MaterialTheme.typography.labelMedium)
                        Spacer(Modifier.width(8.dp))
                        HorizontalDivider()
                    }
                    events.forEach { event ->
                        Surface(tonalElevation = 8.dp, shadowElevation = 1.dp, shape = MaterialTheme.shapes.small) {
                            Column(
                                Modifier.padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row {
                                    ScoreCard(
                                        "type",
                                        event.type.code,
                                        Alignment.Start,
                                        Modifier.width(50.dp)
                                    )
                                    Spacer(Modifier.weight(1f))
                                    ScoreCard(
                                        "start",
                                        event.beginDatetime.withZoneSameInstant(ZoneId.systemDefault())
                                            .toLocalTime().format(DateTimeFormatter.ISO_TIME),
                                        Alignment.End,
                                        Modifier.width(75.dp)
                                    )
                                    ScoreCard(
                                        "max",
                                        event.maxDatetime?.withZoneSameInstant(ZoneId.systemDefault())
                                            ?.toLocalTime()?.format(DateTimeFormatter.ISO_TIME)
                                            ?: "--:--:--",
                                        Alignment.End,
                                        Modifier.width(75.dp)
                                    )
                                    ScoreCard(
                                        "end",
                                        event.endDatetime?.withZoneSameInstant(ZoneId.systemDefault())
                                            ?.toLocalTime()?.format(DateTimeFormatter.ISO_TIME)
                                            ?: "unk",
                                        Alignment.End,
                                        Modifier.width(75.dp)
                                    )
                                }
                                Row(
                                    Modifier.height(IntrinsicSize.Min),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        when (event) {
                                            is FlareEvent -> {
                                                ScoreCard(
                                                    "characteristics",
                                                    event.characteristics.joinToString(" ") { it.name }
                                                        .ifEmpty { "none" },
                                                    Alignment.Start,
                                                )
                                                ScoreCard(
                                                    "flare type",
                                                    "${event.importance}${event.brightness}",
                                                    Alignment.Start,
                                                )
                                            }
                                            is XrayEvent -> {
                                                ScoreCard(
                                                    "class",
                                                    event.xRayClass,
                                                    Alignment.Start,
                                                )
                                                ScoreCard(
                                                    "frequency",
                                                    event.frequency,
                                                    Alignment.Start,
                                                )
                                            }
                                            is SweptRadioBurstEvent -> {
                                                ScoreCard("frequency", value = listOf(event.frequencyRange.first, event.frequencyRange.last).joinToString("-"), alignment = Alignment.Start)
                                                ScoreCard("burst type", value = "${event.radioBurstType}/${event.intensity}", alignment = Alignment.Start)
                                            }
                                            is FixedRadioBurstEvent -> {
                                                ScoreCard("frequency", value = event.frequency.toString(), alignment = Alignment.Start)
                                                ScoreCard("brightness", value = event.maxBrightness.toString(), alignment = Alignment.Start)
                                            }
                                            else -> {
                                                Spacer(Modifier.weight(1f))
                                            }
                                        }
                                    }
                                    ScoreCard(
                                        "observer",
                                        event.observatory.toString(),
                                        Alignment.End,
                                        Modifier.width(100.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
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