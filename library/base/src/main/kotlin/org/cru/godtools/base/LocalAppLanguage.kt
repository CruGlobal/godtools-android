package org.cru.godtools.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

object LocalAppLanguage {
    private val LocalComposition = staticCompositionLocalOf<Locale?> { null }

    /**
     * Returns current App Language value
     */
    val current: Locale
        @Composable
        get() = LocalComposition.current
            ?: LocalContext.current.let { it.getAppLanguageFlow().collectAsState(it.appLanguage).value }

    /**
     * Associates a [LocalAppLanguage] key to a value in a call to [CompositionLocalProvider].
     */
    infix fun provides(locale: Locale) = LocalComposition.provides(locale)
}
