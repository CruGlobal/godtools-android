package org.cru.godtools.tutorial.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.LineBreak

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
