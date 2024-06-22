package net.molteno.linus.prescient.sun

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import net.molteno.linus.prescient.sun.api.HpEntry

@Composable
fun SunActivityList(
    hp: List<HpEntry>?,
    state: LazyListState = rememberLazyListState(),
    internalPadding: PaddingValues = PaddingValues()
) {
    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        state = state,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(
            top = internalPadding.calculateTopPadding() + 16.dp,
            bottom = internalPadding.calculateBottomPadding() + 16.dp,
            start = internalPadding.calculateStartPadding(LocalLayoutDirection.current),
            end = internalPadding.calculateEndPadding(LocalLayoutDirection.current),
        )
    ) {
        item {
            SunHpChartCard(hp)
        }
    }
}
