package net.molteno.linus.prescient.earth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.molteno.linus.prescient.ui.shared.rememberExitUntilCollapsedState

@Composable
fun EarthPage() {
    val viewModel: EarthViewModel = hiltViewModel()

    val coastlines by viewModel.coastlines.collectAsStateWithLifecycle()
    val forecast by viewModel.forecast.collectAsStateWithLifecycle()
    val currentLocation by viewModel.currentLocation.collectAsStateWithLifecycle()
    val subsolarPoint by viewModel.subsolarPoint.collectAsStateWithLifecycle()
    val currentTime by viewModel.currentTime.collectAsStateWithLifecycle()

    RequestLocationPermission(
        onPermissionGranted = { viewModel.locationUpdated() },
        onPermissionDenied = { viewModel.locationUpdated() },
        onPermissionsRevoked = { viewModel.locationUpdated() }
    )

    val configuration = LocalConfiguration.current

    val objectHeightRange = with(LocalDensity.current) {
        160.dp.roundToPx()..configuration.screenWidthDp.dp.roundToPx()
    }

    val objectBarState = rememberExitUntilCollapsedState(objectHeightRange)

    val density = LocalDensity.current

    val listState = rememberLazyListState()

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
            Box(
                Modifier
                    .aspectRatio(1f)
            ) {
                Earth(
                    coastlines ?: emptyList(), Modifier.padding(10.dp),
                    location = currentLocation,
                    subsolarPoint = subsolarPoint
                )
            }
        }
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer { translationY = objectBarState.height }
                .padding(bottom = with(density) { objectBarState.height.toDp() })
        ) {
            LazyColumn(
                Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 16.dp),
                state = listState
            ) {
                forecastCards(forecast, onTimeChange = { viewModel.changeTime(it) })
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        SunChart(currentTime, currentLocation, Modifier.weight(1f))
                        SunChart(currentTime, currentLocation, Modifier.weight(1f))
                    }
                }
            }
        }
    }
}