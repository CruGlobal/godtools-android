package org.cru.godtools.util

import android.app.Activity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
private fun detectTablet(): Boolean {
    val context = LocalContext.current
    val activity = context as? Activity ?: return false

    val windowSizeClass = calculateWindowSizeClass(activity = activity)

    val detectedTablet = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Medium ||
            windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded

    return detectedTablet
}

@Composable
fun isTablet(): Boolean {
    val hasRun = remember { mutableStateOf(false) }
    val isTablet = remember { mutableStateOf(false) }

    if (!hasRun.value) {
        val detectedTablet = detectTablet()
        isTablet.value = detectedTablet
        hasRun.value = true
    }
    return isTablet.value
}
