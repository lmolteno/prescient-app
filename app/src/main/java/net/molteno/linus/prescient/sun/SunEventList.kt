package net.molteno.linus.prescient.sun

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.molteno.linus.prescient.sun.api.models.FixedRadioBurstEvent
import net.molteno.linus.prescient.sun.api.models.FlareEvent
import net.molteno.linus.prescient.sun.api.models.SolarEventObservation
import net.molteno.linus.prescient.sun.api.models.SweptRadioBurstEvent
import net.molteno.linus.prescient.sun.api.models.XrayEvent
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun SunEventList(
    events: Map<Int, List<SolarEventObservation>>?,
    state: LazyListState = rememberLazyListState()
) {
    var selectedEvent: Int? by remember { mutableStateOf(null) }

    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        state = state,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        items(events?.entries?.toList() ?: emptyList(), key = { it.key }) { (eventId, observations) ->
            SunEventItem(eventId, observations, eventId == selectedEvent, onSelect = { selectedEvent = it })
        }
    }
}

@Composable
fun SunEventItem(eventId: Int, events: List<SolarEventObservation>, selected: Boolean = false, onSelect: (Int?) -> Unit) {
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
        onClick = { onSelect(if (selected) null else eventId) },
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (selected) 5.dp else 1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row {
                Column(horizontalAlignment = Alignment.Start) {
                    Text("region", style = MaterialTheme.typography.labelSmall)
                    Text(
                        regionId?.toString() ?: "n/a",
                        style = MaterialTheme.typography.labelLarge
                            .copy(fontWeight = if (regionId == null) FontWeight.Normal else FontWeight.Bold)
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
                                    /*
                                    ScoreCard("quality", event.quality.toString(), Alignment.End)
                                     */
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}