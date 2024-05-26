package net.molteno.linus.prescient.ui.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.saveable.rememberSaveable

@Stable
interface ObjectBarState {
    val offset: Float
    val height: Float
    val progress: Float
    val consumed: Float
    var scrollTopLimitReached: Boolean
    var scrollOffset: Float
}

@Composable
fun rememberExitUntilCollapsedState(heightRange: IntRange): ObjectBarState {
    return rememberSaveable(saver = ExitUntilCollapsedState.Saver) {
        ExitUntilCollapsedState(heightRange)
    }
}