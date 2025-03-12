@file:Suppress("ktlint:compose:compositionlocal-allowlist")

package org.cru.godtools.base.ui.theme

import android.os.Build
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
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import org.cru.godtools.base.ui.compose.CompositionLocals
import org.cru.godtools.base.ui.theme.GodToolsTheme.LocalExtendedColorScheme
import org.cru.godtools.base.ui.theme.GodToolsTheme.LocalLightColorSchemeActive
import org.cru.godtools.base.ui.theme.GodToolsTheme.extendedDark
import org.cru.godtools.base.ui.theme.GodToolsTheme.extendedLight
import org.cru.godtools.ui.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
object GodToolsTheme {
    val GT_BLUE = Color(red = 0x3B, green = 0xA4, blue = 0xDB)
    val GT_RED = Color(red = 0xE5, green = 0x5B, blue = 0x36)
    val GRAY_E6 = Color(red = 0xE6, green = 0xE6, blue = 0xE6)

    @Immutable
    data class ExtendedColorScheme(val green: ColorFamily, val red: ColorFamily)
    @Immutable
    data class ColorFamily(val color: Color, val onColor: Color, val colorContainer: Color, val onColorContainer: Color)

    internal val lightColorScheme = lightColorScheme(
        primary = GT_BLUE,
        onBackground = Color(90, 90, 90),
        onSurface = Color(90, 90, 90),
        // HACK: We are currently disabling surface tint to avoid using tonal elevation on surfaces.
        //       When we transition to using tonal elevation we can revert this back to the default value
        surfaceTint = Color.White,

        // These are the generated colors for the light theme, any manually overridden values are commented out
//        primary = primaryLight,
        onPrimary = onPrimaryLight,
        primaryContainer = primaryContainerLight,
        onPrimaryContainer = onPrimaryContainerLight,
        secondary = secondaryLight,
        onSecondary = onSecondaryLight,
        secondaryContainer = secondaryContainerLight,
        onSecondaryContainer = onSecondaryContainerLight,
        // HACK: we don't currently define a tertiary color, so we will just use the default colors
//        tertiary = tertiaryLight,
//        onTertiary = onTertiaryLight,
//        tertiaryContainer = tertiaryContainerLight,
//        onTertiaryContainer = onTertiaryContainerLight,
        error = errorLight,
        onError = onErrorLight,
        errorContainer = errorContainerLight,
        onErrorContainer = onErrorContainerLight,
        background = backgroundLight,
//        onBackground = onBackgroundLight,
        surface = surfaceLight,
//        onSurface = onSurfaceLight,
        surfaceVariant = surfaceVariantLight,
        onSurfaceVariant = onSurfaceVariantLight,
        outline = outlineLight,
        outlineVariant = outlineVariantLight,
        scrim = scrimLight,
        inverseSurface = inverseSurfaceLight,
        inverseOnSurface = inverseOnSurfaceLight,
        inversePrimary = inversePrimaryLight,
        surfaceDim = surfaceDimLight,
        surfaceBright = surfaceBrightLight,
        surfaceContainerLowest = surfaceContainerLowestLight,
        surfaceContainerLow = surfaceContainerLowLight,
        surfaceContainer = surfaceContainerLight,
        surfaceContainerHigh = surfaceContainerHighLight,
        surfaceContainerHighest = surfaceContainerHighestLight,
    )

    internal val darkColorScheme = darkColorScheme(
        primary = primaryDark,
        onPrimary = onPrimaryDark,
        primaryContainer = primaryContainerDark,
        onPrimaryContainer = onPrimaryContainerDark,
        secondary = secondaryDark,
        onSecondary = onSecondaryDark,
        secondaryContainer = secondaryContainerDark,
        onSecondaryContainer = onSecondaryContainerDark,
        // HACK: we don't currently define a tertiary color, so we will just use the default colors
//        tertiary = tertiaryDark,
//        onTertiary = onTertiaryDark,
//        tertiaryContainer = tertiaryContainerDark,
//        onTertiaryContainer = onTertiaryContainerDark,
        error = errorDark,
        onError = onErrorDark,
        errorContainer = errorContainerDark,
        onErrorContainer = onErrorContainerDark,
        background = backgroundDark,
        onBackground = onBackgroundDark,
        surface = surfaceDark,
        onSurface = onSurfaceDark,
        surfaceVariant = surfaceVariantDark,
        onSurfaceVariant = onSurfaceVariantDark,
        outline = outlineDark,
        outlineVariant = outlineVariantDark,
        scrim = scrimDark,
        inverseSurface = inverseSurfaceDark,
        inverseOnSurface = inverseOnSurfaceDark,
        inversePrimary = inversePrimaryDark,
        surfaceDim = surfaceDimDark,
        surfaceBright = surfaceBrightDark,
        surfaceContainerLowest = surfaceContainerLowestDark,
        surfaceContainerLow = surfaceContainerLowDark,
        surfaceContainer = surfaceContainerDark,
        surfaceContainerHigh = surfaceContainerHighDark,
        surfaceContainerHighest = surfaceContainerHighestDark,
    )

    internal val extendedLight = ExtendedColorScheme(
        green = ColorFamily(
            greenLight,
            onGreenLight,
            greenContainerLight,
            onGreenContainerLight,
        ),
        red = ColorFamily(
            redLight,
            onRedLight,
            redContainerLight,
            onRedContainerLight,
        ),
    )

    internal val extendedDark = ExtendedColorScheme(
        green = ColorFamily(
            greenDark,
            onGreenDark,
            greenContainerDark,
            onGreenContainerDark,
        ),
        red = ColorFamily(
            redDark,
            onRedDark,
            redContainerDark,
            onRedContainerDark,
        ),
    )

    internal val typography = Typography().run {
        copy(
            titleMedium = titleMedium.copy(
                lineHeight = 22.sp
            )
        )
    }

    internal val LocalLightColorSchemeActive = staticCompositionLocalOf { false }
    internal val LocalExtendedColorScheme = staticCompositionLocalOf { extendedLight }

    val isLightColorSchemeActive: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalLightColorSchemeActive.current

    val extendedColorScheme: ExtendedColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalExtendedColorScheme.current

    // region Component Colors
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
    // endregion Component Colors
}

@Composable
fun GodToolsTheme(
    darkTheme: Boolean = isSystemInDarkTheme() && BuildConfig.DEBUG,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            darkTheme -> GodToolsTheme.darkColorScheme
            else -> GodToolsTheme.lightColorScheme
        },
        typography = GodToolsTheme.typography
    ) {
        CompositionLocals {
            CompositionLocalProvider(
                LocalLightColorSchemeActive provides !darkTheme,
                LocalExtendedColorScheme provides when {
                    darkTheme -> extendedDark
                    else -> extendedLight
                },
                LocalContentColor provides contentColorFor(MaterialTheme.colorScheme.background),
                content = content
            )
        }
    }
}
