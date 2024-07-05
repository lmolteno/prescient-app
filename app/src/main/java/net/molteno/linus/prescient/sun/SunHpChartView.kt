package net.molteno.linus.prescient.sun

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gigamole.composefadingedges.FadingEdgesGravity
import com.gigamole.composefadingedges.content.FadingEdgesContentType.Dynamic.Lazy
import com.gigamole.composefadingedges.horizontalFadingEdges
import net.molteno.linus.prescient.sun.api.HpEntry
import net.molteno.linus.prescient.ui.theme.PrescientTheme
import net.molteno.linus.prescient.utils.lerp
import timber.log.Timber
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.abs

@Composable
fun HpDot(modifier: Modifier = Modifier) {
    Box(
        modifier
            .border(1.dp, MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f), shape = CircleShape)
            .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f), shape = CircleShape)
            .size(8.dp)
    )
}

@Composable
fun ApDot(modifier: Modifier = Modifier) {
    Box(
        modifier
            .border(1.dp, MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f), shape = CircleShape)
            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f), shape = CircleShape)
            .size(8.dp)
    )
}

@Composable
fun SunHpChartCard(hp: List<HpEntry>?) {
    ElevatedCard {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Hp30 & ap30",
                style = MaterialTheme.typography.titleLarge
            )
        }
        HorizontalDivider(Modifier.padding(horizontal = 8.dp))

        val lazyListState = rememberLazyListState()
        val maxHpEntry = remember(hp) { hp?.maxByOrNull { it.hp30 } }
        val maxApEntry = remember(hp) { hp?.maxByOrNull { it.ap30 } }
        val maxHeight = 80 // dp
        val width = 16 // dp
        val density = LocalDensity.current
        var chartWidthPx by remember { mutableIntStateOf(0) }


        var centeredElement by remember { mutableStateOf(hp?.maxByOrNull { it.time }) }
        val view = LocalView.current

        AnimatedContent(targetState = hp != null, label = "Change from loading to chart") { loaded ->
            if (!loaded) { // hp is null
                Column(Modifier.padding(8.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                }
            } else {
                Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box {
                        LazyRow(
                            state = lazyListState,
                            reverseLayout = true,
                            modifier = Modifier
                                .onSizeChanged { chartWidthPx = it.width }
                                .horizontalFadingEdges(
                                    Lazy.List(state = lazyListState),
                                    gravity = FadingEdgesGravity.All,
                                    length = 50.dp
                                ),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            item {
                                Spacer(Modifier.width(((chartWidthPx / density.density) / 2).dp - (width / 2.0).dp))
                            }
                            items(hp?.reversed() ?: emptyList(), key = { it.time.toEpochSecond() }) { hpEntry ->
                                val elementCentered by remember {
                                    derivedStateOf {
                                        val layoutInfo = lazyListState.layoutInfo
                                        val visibleItemsInfo = layoutInfo.visibleItemsInfo
                                        val itemInfo = visibleItemsInfo.firstOrNull { it.key == hpEntry.time.toEpochSecond() }

                                        itemInfo?.let {
                                            val targetPosition = lazyListState.layoutInfo.viewportEndOffset / 2.0
                                            val distanceToTarget = abs((it.offset + it.size / 2.0) - targetPosition)
                                            Timber.d("item at ${hpEntry.time} is at ${it.offset} - viewport is ${lazyListState.layoutInfo.viewportEndOffset} wide")
                                            if (distanceToTarget < (it.size / 2.0 + (density.density * 4))) {
                                                if (hpEntry != centeredElement) {
                                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                                }
                                                centeredElement = hpEntry
                                                return@derivedStateOf true
                                            }
                                        }
                                        false
                                    }
                                }

                                val alpha by animateFloatAsState(
                                    if (elementCentered) 1f else 0.5f,
                                    animationSpec = tween(300),
                                    label = "selected element alpha"
                                )

                                Column(
                                    Modifier.width(width.dp).alpha(alpha),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Box {
                                        VerticalDivider(
                                            Modifier
                                                .height(maxHeight.dp)
                                                .align(Alignment.Center)
                                        )
                                        if (maxApEntry != null) {
                                            val ap30Position = lerp(1.0, 0.0, hpEntry.ap30 / maxApEntry.ap30) * maxHeight
                                            ApDot(Modifier.offset(y = ap30Position.dp))
                                        }
                                        if (maxHpEntry != null) {
                                            val hp30Position = lerp(1.0, 0.0, hpEntry.hp30 / maxHpEntry.hp30) * maxHeight
                                            HpDot(Modifier.offset(y = hp30Position.dp))
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                    centeredElement?.let { hpEntry ->
                        val timeLocal = hpEntry.time.withZoneSameInstant(ZoneId.systemDefault())
                        Column {
                            Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                HpDot()
                                Text(
                                    "Hp %.2f".format(hpEntry.hp30),
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                ApDot(Modifier.align(Alignment.CenterVertically))
                                Text(
                                    "ap %.0f".format(hpEntry.ap30),
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            }
                        }
                        Text(
                            "%02d-%02d %02d:%02d".format(timeLocal.monthValue, timeLocal.dayOfMonth, timeLocal.hour, timeLocal.minute),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun SunHpChartPreview() {
    val startTime = ZonedDateTime.now().apply { minusMinutes(minute.toLong()) }
    PrescientTheme {
        Surface(
            Modifier
                .width(400.dp)) {
            Box(Modifier.padding(50.dp)) {
                SunHpChartCard(hp = (0..100).map {
                    HpEntry(time = startTime.minusMinutes((it * 30L)), ap30 = Math.random(), hp30 = Math.random())
                })
            }
        }
    }
}

@Composable
@Preview
fun SunHpChartLoadingPreview() {
    PrescientTheme {
        Surface(
            Modifier
                .width(400.dp)
                .height(350.dp)) {
            Box(Modifier.padding(50.dp)) {
                SunHpChartCard(hp = null)
            }
        }
    }
}
