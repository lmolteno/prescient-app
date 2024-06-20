package net.molteno.linus.prescient.sun

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.molteno.linus.prescient.sun.api.HpEntry
import net.molteno.linus.prescient.sun.api.models.SolarEventObservation
import net.molteno.linus.prescient.sun.api.models.SolarRegionObservation
import net.molteno.linus.prescient.ui.shared.rememberExitUntilCollapsedState
import net.molteno.linus.prescient.ui.theme.PrescientTheme

enum class SunPageTabs(val text: String) {
    ACTIVITY("Activity"),
    REGIONS("Regions"),
    EVENTS("Events"),
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SunPage(
    regions: Map<Int, List<SolarRegionObservation>>,
    solarEvents: Map<Int, List<SolarEventObservation>>?,
    currentHp: List<HpEntry>?,
) {
    val configuration = LocalConfiguration.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { SunPageTabs.entries.size } )
    var selectedTab by remember { mutableStateOf(SunPageTabs.EVENTS) }

    var selectedRegion by remember { mutableStateOf<Int?>(null) }

    val objectHeightRange = with(LocalDensity.current) {
        80.dp.roundToPx()..configuration.screenWidthDp.dp.roundToPx()
//        30.dp.roundToPx()..80.dp.roundToPx()
    }

    val objectBarState = rememberExitUntilCollapsedState(objectHeightRange)
    val regionListState = rememberLazyListState()
    val activityListState = rememberLazyListState()
    val eventListState = rememberLazyListState()
    val listState by remember(selectedTab) {
        derivedStateOf {
            when (selectedTab) {
                SunPageTabs.ACTIVITY -> activityListState
                SunPageTabs.REGIONS -> regionListState
                SunPageTabs.EVENTS -> eventListState
            }
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        selectedTab = SunPageTabs.entries.first { it.ordinal == pagerState.currentPage }
    }

    val density = LocalDensity.current

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                objectBarState.scrollTopLimitReached = listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
                objectBarState.scrollOffset -= available.y
                return Offset(0f, objectBarState.consumed)

            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                objectBarState.scrollTopLimitReached = listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
                objectBarState.scrollOffset -= available.y
                return Offset(0f, objectBarState.consumed)

            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                objectBarState.scrollTopLimitReached = listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
                objectBarState.scrollOffset -= available.y
                return super.onPostFling(consumed, available)
            }
        }
    }


    Box(Modifier.nestedScroll(nestedScrollConnection)) {
        Row(
            Modifier
                .height(with(density) { objectBarState.height.toDp() })
                .graphicsLayer { translationY = objectBarState.offset }
        ) {
            Box(Modifier.aspectRatio(1f)) {
                Sun(
                    regions.values.mapNotNull { regions -> regions.maxByOrNull { it.observedDate } },
                    selectedRegion = selectedRegion
                )
            }
        }

        Row(
            Modifier
                .fillMaxSize()
                .graphicsLayer { translationY = objectBarState.height }
                .padding(bottom = with(density) { objectBarState.height.toDp() })
        ) {
            Column {
                TabRow(selectedTab.ordinal) {
                    SunPageTabs.entries.forEach { currentTab ->
                        Tab(
                            selected = selectedTab == currentTab,
                            selectedContentColor = MaterialTheme.colorScheme.primary,
                            unselectedContentColor = MaterialTheme.colorScheme.outline,
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(currentTab.ordinal)
                                }
                            },
                            text = { Text(text = currentTab.text) },
                        )
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                ) { page ->
                    when (page) {
                        SunPageTabs.ACTIVITY.ordinal ->
                            SunActivityList(hp = currentHp, state = activityListState)

                        SunPageTabs.EVENTS.ordinal ->
                            SunEventList(events = solarEvents, state = eventListState)

                        SunPageTabs.REGIONS.ordinal -> SunRegionList(
                            regions,
                            selectedRegion = selectedRegion,
                            onRegionSelection = { selectedRegion = it },
                            state = regionListState
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun SunPagePreview() {
    PrescientTheme {
        Surface {
            SunPage(regions = emptyMap(), solarEvents = emptyMap(), currentHp = null)
        }
    }
}