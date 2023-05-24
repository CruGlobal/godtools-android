package org.cru.godtools.tutorial.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.LineBreak
import org.cru.godtools.base.ui.theme.GodToolsTheme

@Composable
internal fun GodToolsTutorialTheme(content: @Composable () -> Unit) = GodToolsTheme {
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
