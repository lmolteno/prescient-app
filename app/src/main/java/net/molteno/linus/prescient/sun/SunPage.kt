package net.molteno.linus.prescient.sun

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.molteno.linus.prescient.sun.api.HpEntry
import net.molteno.linus.prescient.sun.api.models.SolarRegionObservation
import net.molteno.linus.prescient.ui.shared.rememberExitUntilCollapsedState
import net.molteno.linus.prescient.ui.theme.PrescientTheme

enum class SunPageTabs(val text: String) {
    ACTIVITY("Activity"),
    REGIONS("Regions"),
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SunPage(
    regions: Map<Int, List<SolarRegionObservation>>,
    currentHp: List<HpEntry>?,
) {
    val configuration = LocalConfiguration.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { SunPageTabs.entries.size })
    var selectedTab by remember { mutableStateOf(SunPageTabs.ACTIVITY) }

    var selectedRegion by remember { mutableStateOf<Int?>(null) }

    val objectHeightRange = with(LocalDensity.current) {
        40.dp.roundToPx()..configuration.screenWidthDp.dp.roundToPx()
    }

    val objectBarState = rememberExitUntilCollapsedState(objectHeightRange)
    val listState = rememberLazyListState()

    val density = LocalDensity.current

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
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
            Sun(
                regions.values.mapNotNull { regions -> regions.maxByOrNull { it.observedDate } },
                selectedRegion = selectedRegion
            )
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
                                    selectedTab = currentTab
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
                        .weight(1f)
                ) { page ->
                            when (page) {
                                SunPageTabs.ACTIVITY.ordinal ->
                                    Column {
                                        Row(
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "Hp30 & Ap30",
                                                style = MaterialTheme.typography.titleLarge
                                            )
                                            Text(
                                                "from the university of potsdam",
                                                color = MaterialTheme.colorScheme.outline,
                                                style = MaterialTheme.typography.bodyMedium,
                                                textAlign = TextAlign.End
                                            )
                                        }
                                        HorizontalDivider(Modifier.padding(horizontal = 8.dp))
                                        Box(Modifier.padding(8.dp)) {
                                            SunHpChart(currentHp)
                                        }
                                        Spacer(Modifier.weight(1f, true))
                                    }

                                SunPageTabs.REGIONS.ordinal -> SunRegionList(
                                    regions,
                                    onRegionSelection = { selectedRegion = it },
                                    state = listState
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
            SunPage(regions = emptyMap(), currentHp = null)
        }
    }
}