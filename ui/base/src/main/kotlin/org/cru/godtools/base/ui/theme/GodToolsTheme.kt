package org.cru.godtools.base.ui.theme

import androidx.compose.material3.ExperimentalMaterial3Api
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
val GT_RED = Color(red = 0xE5, green = 0x5B, blue = 0x36)
val GRAY_E6 = Color(red = 0xE6, green = 0xE6, blue = 0xE6)

private val GodToolsLightColorScheme = lightColorScheme(
    primary = GT_BLUE,
    onBackground = Color(90, 90, 90),
    onSurface = Color(90, 90, 90),
    // HACK: We are currently disabling surface tint to avoid using tonal elevation on surfaces.
    //       When we transition to using tonal elevation we can revert this back to the default value
    surfaceTint = Color.White,
    // The following colors were automatically generated using the material 3 theme builder:
    // https://m3.material.io/theme-builder#/custom
    // Primary: #3ba4db
    // Secondary: #3ba4db
    // Neutral: #939094
    primaryContainer = Color(0xffc7e7ff),
    onPrimaryContainer = Color(0xff001e2e),
    secondary = Color(0xff00658e),
    onSecondary = Color(0xffffffff),
    secondaryContainer = Color(0xffc7e7ff),
    onSecondaryContainer = Color(0xff001e2e),
    background = Color(0xffFFFBFE),
    surface = Color(0xffFFFBFE),
    surfaceVariant = Color(0xffE7E0EC),
    onSurfaceVariant = Color(0xff49454F),
    outline = Color(0xff79747E)
)

private val GodToolsTypography = Typography().run {
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

@OptIn(ExperimentalMaterial3Api::class)
val GodToolsAppBarColors @Composable get() = TopAppBarDefaults.smallTopAppBarColors(
    containerColor = MaterialTheme.colorScheme.primary,
    scrolledContainerColor = MaterialTheme.colorScheme.primary,
    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
    titleContentColor = MaterialTheme.colorScheme.onPrimary,
    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
)
