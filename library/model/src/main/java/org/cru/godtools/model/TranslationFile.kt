package org.cru.godtools.model

import org.cru.godtools.model.Base.INVALID_ID

class TranslationFile constructor(
    val translationId: Long = INVALID_ID,
    val filename: String? = null
) {
    constructor(translation: Translation?, file: LocalFile?) : this(translation?.id ?: INVALID_ID, file?.filename)
}
