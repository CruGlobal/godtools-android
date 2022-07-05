package org.cru.godtools.base.ui.util

import android.text.TextUtils
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import java.util.Locale

// TODO: Should this be moved to gto-support?
@Composable
fun ProvideLayoutDirectionFromLocale(locale: () -> Locale?, content: @Composable () -> Unit) {
    val currentLayoutDirection = LocalLayoutDirection.current
    val layoutDirection by remember {
        derivedStateOf {
            locale()?.let {
                when (TextUtils.getLayoutDirectionFromLocale(it)) {
                    View.LAYOUT_DIRECTION_RTL -> LayoutDirection.Rtl
                    View.LAYOUT_DIRECTION_LTR -> LayoutDirection.Ltr
                    else -> null
                }
            } ?: currentLayoutDirection
        }
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides layoutDirection,
        content = content
    )
}
