package org.cru.godtools.tutorial.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.LineBreak
import org.cru.godtools.base.ui.theme.GodToolsTheme

// HACK: we are overriding the background color to be pure white because the animations assume the background is white
val GodToolsTheme.tutorialBackgroundColor
    @Composable
    get() = if (isLightColorSchemeActive) Color.White else MaterialTheme.colorScheme.background

/**
 * Overlay the current Material 3 theme changing the lineBreak behavior to Heading
 */
@Composable
internal fun TutorialThemeOverlay(content: @Composable () -> Unit) {
    val typography = MaterialTheme.typography
    MaterialTheme(
        typography = typography.copy(
            headlineLarge = typography.headlineLarge.copy(lineBreak = LineBreak.Heading),
            headlineMedium = typography.headlineMedium.copy(lineBreak = LineBreak.Heading),
            titleLarge = typography.titleLarge.copy(lineBreak = LineBreak.Heading),
            bodyLarge = typography.bodyLarge.copy(lineBreak = LineBreak.Heading),
            bodyMedium = typography.bodyMedium.copy(lineBreak = LineBreak.Heading),
        ),
        content = content
    )
}
