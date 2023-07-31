package org.cru.godtools.base.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarColors
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Typography
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import org.cru.godtools.base.ui.compose.CompositionLocals
import org.cru.godtools.base.ui.theme.GodToolsTheme.LocalLightColorSchemeActive
import org.cru.godtools.ui.BuildConfig

const val DisabledAlpha = 0.38f

@OptIn(ExperimentalMaterial3Api::class)
object GodToolsTheme {
    val GT_BLUE = Color(red = 0x3B, green = 0xA4, blue = 0xDB)
    val GT_DARK_GREEN = Color(red = 0x58, green = 0xAA, blue = 0x42)
    val GT_RED = Color(red = 0xE5, green = 0x5B, blue = 0x36)
    val GRAY_E6 = Color(red = 0xE6, green = 0xE6, blue = 0xE6)

    internal val lightColorScheme = lightColorScheme(
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
        // Neutral: #8f9193
        primaryContainer = Color(0xffc7e7ff),
        onPrimaryContainer = Color(0xff001e2e),
        secondary = Color(0xff00658e),
        onSecondary = Color(0xffffffff),
        secondaryContainer = Color(0xffc7e7ff),
        onSecondaryContainer = Color(0xff001e2e),
        background = Color(0xfffcfcff),
        surface = Color(0xfffcfcff),
        surfaceVariant = Color(0xffdde3ea),
        onSurfaceVariant = Color(0xff41484d),
        outline = Color(0xff71787e),
        // HACK: This sets the surfaceTint to what should be the primary color.
        // surfaceTint = Color(0xff00658e),
    )

    internal val darkColorScheme = darkColorScheme(
        primary = Color(0xff83cfff),
        onPrimary = Color(0xff00344c),
        primaryContainer = Color(0xff004c6c),
        onPrimaryContainer = Color(0xffc7e7ff),
        secondary = Color(0xff83cfff),
        onSecondary = Color(0xff00344c),
        secondaryContainer = Color(0xff004c6c),
        onSecondaryContainer = Color(0xffc7e7ff),
        background = Color(0xff191c1e),
        onBackground = Color(0xffe2e2e5),
        surface = Color(0xff191c1e),
        onSurface = Color(0xffe2e2e5),
        surfaceVariant = Color(0xff41484d),
        onSurfaceVariant = Color(0xffc1c7ce),
        outline = Color(0xff8b9198)
    )

    internal val typography = Typography().run {
        copy(
            titleMedium = titleMedium.copy(
                lineHeight = 22.sp
            )
        )
    }

    internal val LocalLightColorSchemeActive = staticCompositionLocalOf { false }

    val isLightColorSchemeActive: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalLightColorSchemeActive.current

    val searchBarColors: SearchBarColors
        @Composable
        get() = when {
            isLightColorSchemeActive -> SearchBarDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
            else -> SearchBarDefaults.colors()
        }

    val topAppBarColors
        @Composable
        get() = when {
            isLightColorSchemeActive -> TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                scrolledContainerColor = MaterialTheme.colorScheme.primary,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
            )
            else -> TopAppBarDefaults.topAppBarColors()
        }
}

@Composable
fun GodToolsTheme(content: @Composable () -> Unit) {
    val isDarkTheme = isSystemInDarkTheme() && BuildConfig.DEBUG
    MaterialTheme(
        colorScheme = when {
            isDarkTheme -> GodToolsTheme.darkColorScheme
            else -> GodToolsTheme.lightColorScheme
        },
        typography = GodToolsTheme.typography
    ) {
        CompositionLocals {
            CompositionLocalProvider(
                LocalLightColorSchemeActive provides !isDarkTheme,
                LocalContentColor provides contentColorFor(MaterialTheme.colorScheme.background),
                content = content
            )
        }
    }
}
