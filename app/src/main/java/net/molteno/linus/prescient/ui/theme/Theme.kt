package net.molteno.linus.prescient.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import net.molteno.linus.prescient.R

private val DarkColorScheme = darkColorScheme(
        primary = Purple80,
        secondary = PurpleGrey80,
        tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
        primary = Purple40,
        secondary = PurpleGrey40,
        tertiary = Pink40

        /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

private val displayFamily = FontFamily(
    Font(R.font.dm_serif_display, FontWeight.Normal),
    Font(R.font.dm_serif_display_italic, FontWeight.Normal, FontStyle.Italic),
)

private val monoFamily = FontFamily(
    Font(R.font.dm_mono_regular, FontWeight.Normal),
    Font(R.font.dm_mono_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.dm_mono_light, FontWeight.Light),
)

private val sansFamily = FontFamily(
    Font(R.font.dm_sans_light, FontWeight.Light),
    Font(R.font.dm_sans_lightitalic, FontWeight.Light, FontStyle.Italic),
    Font(R.font.dm_sans_regular, FontWeight.Normal),
    Font(R.font.dm_sans_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.dm_sans_medium, FontWeight.Medium),
    Font(R.font.dm_sans_mediumitalic, FontWeight.Medium, FontStyle.Italic),
    Font(R.font.dm_sans_bold, FontWeight.Bold),
    Font(R.font.dm_sans_bolditalic, FontWeight.Bold, FontStyle.Italic),
    Font(R.font.dm_sans_extrabold, FontWeight.ExtraBold),
    Font(R.font.dm_sans_extrabolditalic, FontWeight.ExtraBold, FontStyle.Italic),
    Font(R.font.dm_sans_black, FontWeight.Black),
    Font(R.font.dm_sans_blackitalic, FontWeight.Black, FontStyle.Italic)
)

//private val typography = Typography(
//    displayLarge    = TextStyle(fontFamily = displayFamily),
//    displayMedium   = TextStyle(fontFamily = displayFamily),
//    displaySmall    = TextStyle(fontFamily = displayFamily),
//    titleLarge      = TextStyle(fontFamily = sansFamily),
//    titleMedium     = TextStyle(fontFamily = sansFamily),
//    titleSmall      = TextStyle(fontFamily = sansFamily),
//    bodyLarge       = TextStyle(fontFamily = sansFamily),
//    bodyMedium      = TextStyle(fontFamily = sansFamily),
//    bodySmall       = TextStyle(fontFamily = sansFamily),
//    labelLarge      = TextStyle(fontFamily = monoFamily),
//    labelMedium     = TextStyle(fontFamily = monoFamily),
//    labelSmall      = TextStyle(fontFamily = monoFamily),
//)

private val typography = Typography(
    displayLarge = TextStyle(
        fontFamily = displayFamily,
        fontWeight = FontWeight.Light,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = displayFamily,
        fontSize = 45.sp,
        lineHeight = 52.sp
    ),
    displaySmall = TextStyle(
        fontFamily = displayFamily,
        fontSize = 36.sp,
        lineHeight = 44.sp
    ),
    titleMedium = TextStyle(
        fontFamily = sansFamily,
        fontWeight = FontWeight(500),
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = (0.15).sp
    ),
    bodySmall = TextStyle(
        fontFamily = sansFamily,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = (0.4).sp
    ),
    bodyMedium = TextStyle(
        fontFamily = sansFamily,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = (0.25).sp
    ),
    bodyLarge = TextStyle(
        fontFamily = sansFamily,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = (0.5).sp
    ),
    labelSmall = TextStyle(
        fontFamily = monoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 8.sp,
        lineHeight = 10.sp,
        letterSpacing = (0.5).sp,
    ),
    labelMedium = TextStyle(
        fontFamily = monoFamily,
        fontWeight = FontWeight(500),
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = (0.5).sp,
        textAlign = TextAlign.Center
    ),
    labelLarge = TextStyle(
        fontFamily = monoFamily,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = (0.1).sp
    )
)

@Composable
fun PrescientTheme(
        darkTheme: Boolean = isSystemInDarkTheme(),
        content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}