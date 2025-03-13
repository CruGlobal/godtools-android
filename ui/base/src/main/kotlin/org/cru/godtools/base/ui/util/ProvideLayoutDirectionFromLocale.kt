package org.cru.godtools.base.ui.util

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.text.layoutDirection
import java.util.Locale

// TODO: Should this be moved to gto-support?
@Composable
fun ProvideLayoutDirectionFromLocale(locale: Locale?, content: @Composable () -> Unit) = CompositionLocalProvider(
    LocalLayoutDirection provides when (locale?.layoutDirection) {
        View.LAYOUT_DIRECTION_RTL -> LayoutDirection.Rtl
        View.LAYOUT_DIRECTION_LTR -> LayoutDirection.Ltr
        else -> LocalLayoutDirection.current
    },
    content = content
)
