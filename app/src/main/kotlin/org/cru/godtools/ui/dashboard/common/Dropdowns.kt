package org.cru.godtools.ui.dashboard.common

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.cru.godtools.base.ui.theme.GodToolsTheme

internal val dropdownButtonColors @Composable get() = ButtonDefaults.elevatedButtonColors(
    containerColor = when {
        GodToolsTheme.isLightColorSchemeActive -> MaterialTheme.colorScheme.background
        else -> Color.Unspecified
    }
)
