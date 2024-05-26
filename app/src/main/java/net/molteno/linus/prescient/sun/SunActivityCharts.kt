package net.molteno.linus.prescient.sun

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberEndAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineSpec
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.shader.color
import com.patrykandpatrick.vico.core.cartesian.HorizontalLayout
import com.patrykandpatrick.vico.core.cartesian.Scroll
import com.patrykandpatrick.vico.core.cartesian.Zoom
import com.patrykandpatrick.vico.core.cartesian.axis.AxisPosition
import com.patrykandpatrick.vico.core.cartesian.axis.BaseAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.shader.DynamicShader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.molteno.linus.prescient.sun.api.HpEntry
import net.molteno.linus.prescient.ui.theme.PrescientTheme
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

val xToDateMapKey = ExtraStore.Key<Map<Float, Instant>>()

val midDayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH")
val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM")

@Composable
fun SunHpChart(hp: List<HpEntry>?) {
    val modelProducer = remember {
        CartesianChartModelProducer.build {
            if (hp == null) return@build
            val hpData = hp.associate { it.time.toInstant() to it.hp30 }
            val apData = hp.associate { it.time.toInstant() to it.ap30 }
            val xToDates = hpData.keys.associateBy { (it.epochSecond / (30 * 60)).toFloat() }
            lineSeries { series(xToDates.keys, hpData.values) }
            lineSeries { series(xToDates.keys, apData.values) }
            updateExtras { it[xToDateMapKey] = xToDates }
        }
    }
    LaunchedEffect(hp) {
        withContext(Dispatchers.Default) {
            if (hp == null) return@withContext
            val hpData = hp.associate { it.time.toInstant() to it.hp30 }
            val apData = hp.associate { it.time.toInstant() to it.ap30 }
            val xToDates = hpData.keys.associateBy { (it.epochSecond / (30 * 60)).toFloat() }
            modelProducer.tryRunTransaction {
                lineSeries { series(xToDates.keys, hpData.values) }
                lineSeries { series(xToDates.keys, apData.values) }
                updateExtras { it[xToDateMapKey] = xToDates }
            }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(listOf(
                rememberLineSpec(DynamicShader.color(Color(0xffa485e0))),
            ), verticalAxisPosition = AxisPosition.Vertical.Start),
            rememberLineCartesianLayer(listOf(
                rememberLineSpec(DynamicShader.color(Color(0xa4ff85e0))),
            ), verticalAxisPosition = AxisPosition.Vertical.End),
            startAxis = rememberStartAxis(title = "Hp30", titleComponent = rememberTextComponent()),
            endAxis = rememberEndAxis(title = "Ap30", titleComponent = rememberTextComponent()),
            bottomAxis = rememberBottomAxis(
                valueFormatter = { x, chartValues, _ ->
                    (chartValues.model.extraStore[xToDateMapKey][x] ?: Instant.ofEpochSecond(x.toLong()))
                        .atZone(ZoneId.systemDefault())
                        .let {
                            it.format(if (it.hour != 0) midDayFormatter else dateTimeFormatter)
                        }
                },
                sizeConstraint = BaseAxis.SizeConstraint.TextWidth("25/50")
            )
        ),
        getXStep = { _ -> 12f },
        placeholder = { Surface(Modifier.fillMaxSize(), tonalElevation = 5.dp, shape = RoundedCornerShape(8.dp)) { } },
        scrollState = rememberVicoScrollState(initialScroll = Scroll.Absolute.End),
        horizontalLayout = HorizontalLayout.FullWidth(),
        modelProducer = modelProducer,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        zoomState = rememberVicoZoomState(zoomEnabled = false, initialZoom = Zoom.x(60f)),
    )
}

@Composable
@Preview
fun SunHpChartPreview() {
    PrescientTheme {
        Surface(Modifier.width(300.dp)) {
            SunHpChart(hp = listOf(
                HpEntry(time = ZonedDateTime.now().minusMinutes(180), ap30 = 0.5, hp30 = 2.0 ),
                HpEntry(time = ZonedDateTime.now().minusMinutes(150), ap30 = 0.5, hp30 = 3.0 ),
                HpEntry(time = ZonedDateTime.now().minusMinutes(120), ap30 = 0.5, hp30 = 2.0 ),
                HpEntry(time = ZonedDateTime.now().minusMinutes(90),  ap30 = 0.5, hp30 = 1.0 ),
                HpEntry(time = ZonedDateTime.now().minusMinutes(60),  ap30 = 0.5, hp30 = 3.0 ),
                HpEntry(time = ZonedDateTime.now().minusMinutes(30),  ap30 = 0.5, hp30 = 2.0 ),
                HpEntry(time = ZonedDateTime.now(),                           ap30 = 0.5, hp30 = 5.0 ),
                HpEntry(time = ZonedDateTime.now().plusMinutes(30),   ap30 = 0.5, hp30 = 5.0 ),
            ))
        }
    }
}