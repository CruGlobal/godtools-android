package org.cru.godtools.ui.languages.app

import java.util.Locale

sealed interface AppLanguageEvent {
    class LanguageSelected(val language: Locale) : AppLanguageEvent
}
