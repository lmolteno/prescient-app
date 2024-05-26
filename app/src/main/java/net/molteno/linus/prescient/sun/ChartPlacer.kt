package net.molteno.linus.prescient.sun

import com.patrykandpatrick.vico.core.cartesian.CartesianDrawContext
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasureContext
import com.patrykandpatrick.vico.core.cartesian.HorizontalDimensions
import com.patrykandpatrick.vico.core.cartesian.axis.AxisItemPlacer
import timber.log.Timber

//class PrescientAxisValueOverrider : AxisValueOverrider {
//    override fun getMaxX(minX: Float, maxX: Float, extraStore: ExtraStore): Float {
//        val minXDate = Instant.ofEpochSecond((minX * (30 * 60)).roundToLong())
//        val maxXDate = Instant.ofEpochSecond((maxX * (30 * 60)).roundToLong())
//        return maxX
//    }
//    override fun getMinX(minX: Float, maxX: Float, extraStore: ExtraStore): Float {
//        val minXDate = Instant.ofEpochSecond((minX * (30 * 60)).roundToLong())
//        val maxXDate = Instant.ofEpochSecond((maxX * (30 * 60)).roundToLong())
//        return maxX - 12f
//    }
//}

class ChartPlacer : AxisItemPlacer.Horizontal {
    override fun getEndHorizontalAxisInset(
        context: CartesianMeasureContext,
        horizontalDimensions: HorizontalDimensions,
        tickThickness: Float,
        maxLabelWidth: Float
    ): Float = horizontalDimensions.endPadding

    override fun getHeightMeasurementLabelValues(
        context: CartesianMeasureContext,
        horizontalDimensions: HorizontalDimensions,
        fullXRange: ClosedFloatingPointRange<Float>,
        maxLabelWidth: Float
    ): List<Float> = listOf(context.chartValues.let { (it.maxX + it.minX) / 2 })

    override fun getLabelValues(
        context: CartesianDrawContext,
        visibleXRange: ClosedFloatingPointRange<Float>,
        fullXRange: ClosedFloatingPointRange<Float>,
        maxLabelWidth: Float
    ): List<Float> = listOf(context.chartValues.let { (it.maxX + it.minX) / 2 })

    override fun getStartHorizontalAxisInset(
        context: CartesianMeasureContext,
        horizontalDimensions: HorizontalDimensions,
        tickThickness: Float,
        maxLabelWidth: Float
    ): Float = horizontalDimensions.startPadding

    override fun getWidthMeasurementLabelValues(
        context: CartesianMeasureContext,
        horizontalDimensions: HorizontalDimensions,
        fullXRange: ClosedFloatingPointRange<Float>
    ): List<Float> {
        Timber.d("Context ${context.layoutDirectionMultiplier}")
        return listOf(context.chartValues.let { (it.maxX + it.minX) / 2 })
    }

}