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

// @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
// @Composable
// private fun detectTablet(): Boolean {
//    val context = LocalContext.current
//    val activity = context as? Activity ?: return false
//
//    val windowSizeClass = calculateWindowSizeClass(activity = activity)
//
//    val detectedTablet = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Medium ||
//            windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
//
//    return detectedTablet
// }
//
// @Composable
// fun isTablet(): Boolean {
//    val hasRun = remember { mutableStateOf(false) }
//    val isTablet = remember { mutableStateOf(false) }
//
//    if (!hasRun.value) {
//        val detectedTablet = detectTablet()
//        isTablet.value = detectedTablet
//        hasRun.value = true
//    }
//    return isTablet.value
// }
