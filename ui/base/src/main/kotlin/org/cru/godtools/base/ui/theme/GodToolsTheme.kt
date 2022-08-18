package org.cru.godtools.base.ui.theme

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Typography
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import org.cru.godtools.base.ui.compose.CompositionLocals

const val DisabledAlpha = 0.38f

private val GT_BLUE = Color(red = 0x3B, green = 0xA4, blue = 0xDB)
private val GRAY_BE = Color(red = 0xBE, green = 0xBE, blue = 0xBE)
val GRAY_E6 = Color(red = 0xE6, green = 0xE6, blue = 0xE6)

private val GodToolsLightColorScheme = lightColorScheme(
    primary = GT_BLUE,
    background = Color.White,
    onBackground = Color(90, 90, 90),
    surface = Color.White,
    onSurface = Color(90, 90, 90),
    surfaceVariant = Color(243, 243, 243),
    onSurfaceVariant = Color(90, 90, 90),
    // HACK: We are currently disabling surface tint to avoid using tonal elevation on surfaces.
    //       When we transition to using tonal elevation we can revert this back to the default value
    surfaceTint = Color.White,
    outline = GRAY_BE,
)

private val GodToolsTypography = with(Typography()) {
    copy(
        titleMedium = titleMedium.copy(
            lineHeight = 22.sp
        )
    )
}

@Composable
fun GodToolsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GodToolsLightColorScheme,
        typography = GodToolsTypography
    ) {
        CompositionLocals {
            CompositionLocalProvider(
                LocalContentColor provides contentColorFor(MaterialTheme.colorScheme.background),
                content = content
            )
        }
    }
}

val GodToolsAppBarColors @Composable get() = TopAppBarDefaults.smallTopAppBarColors(
    containerColor = MaterialTheme.colorScheme.primary,
    scrolledContainerColor = MaterialTheme.colorScheme.primary,
    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
    titleContentColor = MaterialTheme.colorScheme.onPrimary,
    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
)
