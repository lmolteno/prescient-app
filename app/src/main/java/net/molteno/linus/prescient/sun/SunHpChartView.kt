package net.molteno.linus.prescient.sun

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import net.molteno.linus.prescient.ui.shared.Chart
import net.molteno.linus.prescient.sun.api.HpEntry
import net.molteno.linus.prescient.ui.theme.PrescientTheme
import net.molteno.linus.prescient.utils.lerp
import kotlin.time.Duration.Companion.minutes

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
    val maxHpEntry = remember(hp) { hp?.maxByOrNull { it.hp30 } }
    val maxApEntry = remember(hp) { hp?.maxByOrNull { it.ap30 } }

    Chart(
        values = hp,
        renderElementDescription = { hpEntry ->
            val timeLocal = hpEntry.time.toLocalDateTime(TimeZone.currentSystemDefault())
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
                "%02d-%02d %02d:%02d".format(timeLocal.monthNumber, timeLocal.dayOfMonth, timeLocal.hour, timeLocal.minute),
                style = MaterialTheme.typography.labelLarge
            )
        }, renderDots = { hpEntry, maxHeight ->
            if (maxApEntry != null) {
                lerp(1.0, 0.0, hpEntry.ap30 / maxApEntry.ap30)?.also {
                    ApDot(Modifier.offset(y = (it * maxHeight).dp))
                }
            }
            if (maxHpEntry != null) {
                lerp(1.0, 0.0, hpEntry.hp30 / maxHpEntry.hp30)?.also {
                    HpDot(Modifier.offset(y = (it * maxHeight).dp))
                }
            }
        })
}

@Composable
@Preview
fun SunHpChartPreview() {
    val startTime = Clock.System.now()
        .apply { minus(toLocalDateTime(TimeZone.currentSystemDefault()).minute.minutes) }
    PrescientTheme {
        Surface(
            Modifier
                .width(400.dp)
        ) {
            Box(Modifier.padding(50.dp)) {
                SunHpChartCard(hp = (0..100).map {
                    HpEntry(time = startTime.minus((it * 30L).minutes), ap30 = Math.random(), hp30 = Math.random())
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
                .height(350.dp)
        ) {
            Box(Modifier.padding(50.dp)) {
                SunHpChartCard(hp = null)
            }
        }
    }
}
