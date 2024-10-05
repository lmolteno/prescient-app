package net.molteno.linus.prescient.moon

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.jamesyox.kastro.luna.calculateLunarIllumination
import kotlinx.datetime.Clock

@Composable
fun MoonPage() {
    val illumination = Clock.System.now().calculateLunarIllumination()

   Column {
       Box(
           Modifier
               .aspectRatio(1f)
               .padding(40.dp)
       ) {
           Surface(
               shape = RoundedCornerShape(50),
               shadowElevation = 20.dp,
               modifier = Modifier.aspectRatio(1f)
           ) {
               Moon(
                   phase = ((illumination.phase + 180) / 360).toFloat(),
               )
           }
       }
   }
}