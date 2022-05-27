package org.cru.godtools.base.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GT_BLUE = Color(red = 0x3B, green = 0xA4, blue = 0xDB)

private val GodToolsLightColorScheme = lightColorScheme(primary = GT_BLUE)

@Composable
fun GodToolsTheme(content: @Composable () -> Unit) = MaterialTheme(
    colorScheme = GodToolsLightColorScheme,
    content = content
)
