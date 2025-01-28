package org.cru.godtools.ui.dashboard.filters

import androidx.compose.foundation.clickable
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
internal fun FilterMenuItem(
    label: String,
    supportingText: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) = FilterMenuItem(
    label = { Text(label) },
    supportingText = supportingText,
    onClick = onClick,
    modifier = modifier,
)

@Composable
internal fun FilterMenuItem(
    label: @Composable () -> Unit,
    supportingText: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) = ListItem(
    headlineContent = label,
    supportingContent = supportingText?.let { { Text(it) } },
    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    modifier = modifier.clickable(onClick = onClick)
)
