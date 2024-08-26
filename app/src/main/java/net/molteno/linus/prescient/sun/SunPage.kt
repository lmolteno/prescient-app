package net.molteno.linus.prescient.sun

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import net.molteno.linus.prescient.api.models.SolarRegionObservation
import net.molteno.linus.prescient.sun.api.HpEntry
import net.molteno.linus.prescient.sun.api.models.SolarEventObservation
import net.molteno.linus.prescient.ui.shared.rememberExitUntilCollapsedState
import net.molteno.linus.prescient.ui.theme.PrescientTheme

enum class SunPageTabs(val text: String) {
    ACTIVITY("Activity"),
    REGIONS("Regions"),
    EVENTS("Events"),
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
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
        160.dp.roundToPx()..configuration.screenWidthDp.dp.roundToPx()
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
                .padding(lerp(10.dp, 40.dp, objectBarState.progress))
                .fillMaxWidth()
                .graphicsLayer { translationY = objectBarState.offset },
            horizontalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                shadowElevation = 10.dp,
                modifier = Modifier
                    .aspectRatio(1f)) {
                Sun(
                    regions.values.mapNotNull { regions -> regions.maxByOrNull { it.observedDate } },
                    selectedRegion = selectedRegion
                )
            }
        }

        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer { translationY = objectBarState.height }
                .padding(bottom = with(density) { objectBarState.height.toDp() })
        ) {
            Box(Modifier.zIndex(10f)) {
                SingleChoiceSegmentedButtonRow(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    SunPageTabs.entries.forEachIndexed { index, currentTab ->
                        SegmentedButton(
                            selected = selectedTab == currentTab,
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(currentTab.ordinal)
                                }
                            },
                            icon = { },
                            shape = when (index) {
                                0 -> RoundedCornerShape(50)
                                    .copy(topEnd = ZeroCornerSize, bottomEnd = ZeroCornerSize)

                                SunPageTabs.entries.size - 1 -> RoundedCornerShape(50)
                                    .copy(topStart = ZeroCornerSize, bottomStart = ZeroCornerSize)

                                else -> RectangleShape
                            }
                        ) {
                            Text(text = currentTab.text)
                        }
                    }
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 20.dp),
                userScrollEnabled = false
            ) { page ->
                when (page) {
                    SunPageTabs.ACTIVITY.ordinal ->
                        SunActivityList(
                            hp = currentHp,
                            state = activityListState,
                            internalPadding = PaddingValues(top = 20.dp)
                        )

                    SunPageTabs.EVENTS.ordinal ->
                        SunEventList(
                            events = solarEvents,
                            state = eventListState,
                            internalPadding = PaddingValues(top = 20.dp),
                            setSelectedRegion = { selectedRegion = it }
                        )

                    SunPageTabs.REGIONS.ordinal ->
                        SunRegionList(
                            regions,
                            selectedRegion = selectedRegion,
                            onRegionSelection = { selectedRegion = it },
                            state = regionListState,
                            internalPadding = PaddingValues(top = 20.dp)
                        )
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