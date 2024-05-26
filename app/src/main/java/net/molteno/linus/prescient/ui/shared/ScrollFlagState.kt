package net.molteno.linus.prescient.ui.shared

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.setValue

abstract class ScrollFlagState(heightRange: IntRange) : ObjectBarState {

    init {
        require(heightRange.first >= 0 && heightRange.last >= heightRange.first) {
            "The lowest height value must be >= 0 and the highest height value must be >= the lowest value."
        }
    }

    protected val minHeight = heightRange.first
    protected val maxHeight = heightRange.last
    protected val rangeDifference = maxHeight - minHeight
    protected var _consumed: Float = 0f

    protected abstract var _scrollOffset: Float

    final override val height: Float
        get() = (maxHeight - scrollOffset).coerceIn(minHeight.toFloat(), maxHeight.toFloat())

    final override val progress: Float
        get() = 1 - (maxHeight - height) / rangeDifference

    final override val consumed: Float
        get() = _consumed

    final override var scrollTopLimitReached: Boolean = true
}

abstract class FixedScrollFlagState(heightRange: IntRange) : ScrollFlagState(heightRange) {
    final override val offset: Float = 0f
}

class ExitUntilCollapsedState(
    heightRange: IntRange,
    scrollOffset: Float = 0f
) : FixedScrollFlagState(heightRange) {
    override var _scrollOffset by mutableFloatStateOf(scrollOffset.coerceIn(0f, rangeDifference.toFloat()))

    override var scrollOffset: Float
        get() = _scrollOffset
        set(value) {
            if (scrollTopLimitReached) {
                val oldOffset = _scrollOffset
                _scrollOffset = value.coerceIn(0f, rangeDifference.toFloat())
                _consumed = oldOffset - _scrollOffset
            } else {
                _consumed = 0f
            }
        }

    companion object {
        val Saver = run {

            val minHeightKey = "MinHeight"
            val maxHeightKey = "MaxHeight"
            val scrollOffsetKey = "ScrollOffset"

            mapSaver(
                save = {
                    mapOf(
                        minHeightKey to it.minHeight,
                        maxHeightKey to it.maxHeight,
                        scrollOffsetKey to it.scrollOffset
                    )
                },
                restore = {
                    ExitUntilCollapsedState(
                        heightRange = (it[minHeightKey] as Int)..(it[maxHeightKey] as Int),
                        scrollOffset = it[scrollOffsetKey] as Float,
                    )
                }
            )
        }
    }
}