package org.cru.godtools.model

import org.cru.godtools.model.Base.INVALID_ID

data class TranslationFile(
    val translationId: Long = INVALID_ID,
    val filename: String
) {
    constructor(translation: Translation?, filename: String) : this(translation?.id ?: INVALID_ID, filename)
}
