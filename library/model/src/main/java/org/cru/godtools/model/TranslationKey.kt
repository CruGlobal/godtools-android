package org.cru.godtools.model

import java.util.Locale

data class TranslationKey(val tool: String?, val locale: Locale?) {
    constructor(translation: Translation) : this(translation.toolCode, translation.languageCode)
}
