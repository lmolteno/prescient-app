package net.molteno.linus.prescient.ui.shared

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.gigamole.composefadingedges.FadingEdgesGravity
import com.gigamole.composefadingedges.content.FadingEdgesContentType.Dynamic.Lazy
import com.gigamole.composefadingedges.horizontalFadingEdges
import kotlinx.datetime.Instant
import kotlin.math.abs

interface TimeKeyed {
    val time: Instant
}

@Composable
fun <T : TimeKeyed>Chart(
    values: List<T>?,
    modifier: Modifier = Modifier,
    title: String? = null,
    maxHeight: Int = 80,
    width: Int = 16,
    reversed: Boolean = true,
    lazyListState: LazyListState = rememberLazyListState(),
    onCenteredElementChange: (centeredElement: T) -> Unit = { },
    renderElementDescription: @Composable (value: T) -> Unit,
    renderDots: @Composable BoxScope.(value: T, maxHeight: Int) -> Unit,
) {
    val density = LocalDensity.current
    var chartWidthPx by remember { mutableIntStateOf(0) }
//    var centeredElement by remember { mutableStateOf<T?>(null) }
    val view = LocalView.current

    val centeredElement by remember(lazyListState, values) {
        derivedStateOf {
            val layoutInfo = lazyListState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            val centerOffset = layoutInfo.viewportEndOffset / 2
            var closestItem: Pair<LazyListItemInfo, Int>? = null
            var minDistance = Int.MAX_VALUE

            for (item in visibleItemsInfo) {
                val itemCenter = item.offset + item.size / 2
                val distance = abs(itemCenter - centerOffset)
                if (distance < minDistance) {
                    minDistance = distance
                    closestItem = item to distance
                } else {
                    break
                }
            }

            values?.firstOrNull { it.time.toEpochMilliseconds() == closestItem?.first?.key }
        }
    }

    LaunchedEffect(centeredElement) {
        centeredElement?.let {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            onCenteredElementChange(it)
        }
    }


    ElevatedCard(modifier) {
        if (title != null) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            HorizontalDivider(Modifier.padding(horizontal = 8.dp))
        }

        AnimatedContent(targetState = values != null, label = "Change from loading to chart") { loaded ->
            if (!loaded) {
                Column(
                    Modifier
                        .padding(8.dp)
                        .fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                }
            } else {
                val widthOffset = maxOf(((chartWidthPx / density.density) / 2).dp - (width / 2.0).dp, 0.dp)
                Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box {
                        LazyRow(
                            state = lazyListState,
                            reverseLayout = reversed,
                            modifier = Modifier
                                .onSizeChanged { chartWidthPx = it.width }
                                .horizontalFadingEdges(
                                    Lazy.List(state = lazyListState),
                                    gravity = FadingEdgesGravity.All,
                                    length = 50.dp
                                ),
                            horizontalArrangement = Arrangement.spacedBy(0.dp),
                        ) {
                            item {
                                Spacer(modifier = Modifier.width(widthOffset))
                            }
                            items(values?.reversed() ?: emptyList(), key = { it.time.toEpochMilliseconds() }) { value ->
                                val elementCentered = centeredElement?.time == value.time

                                val alpha by animateFloatAsState(
                                    if (elementCentered) 1f else 0.5f,
                                    animationSpec = tween(300),
                                    label = "selected element alpha"
                                )

                                Column(
                                    Modifier
                                        .width(width.dp)
                                        .alpha(alpha),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Box {
                                        VerticalDivider(
                                            Modifier
                                                .height(maxHeight.dp)
                                                .align(Alignment.Center)
                                        )
                                        renderDots(value, maxHeight)
                                    }
                                    Spacer(Modifier.height(8.dp))
                                }
                            }
                            item {
                                Spacer(modifier = Modifier.width(widthOffset))
                            }
                        }
                    }
                    centeredElement?.let { element ->
                        renderElementDescription(element)
                    }
                }
            }
        }
    }
}