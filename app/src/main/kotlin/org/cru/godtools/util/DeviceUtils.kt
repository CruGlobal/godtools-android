package org.cru.godtools.util

import androidx.activity.compose.LocalActivity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable

private val TABLET_SIZE_CLASSES = setOf(
    WindowWidthSizeClass.Medium,
    WindowWidthSizeClass.Expanded
)

@Composable
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
fun isTablet(): Boolean {
    val activity = LocalActivity.current ?: return false
    return calculateWindowSizeClass(activity).widthSizeClass in TABLET_SIZE_CLASSES
}
