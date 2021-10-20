package org.cru.godtools.model

data class TranslationFile(val translationId: Long, val filename: String) {
    constructor(translation: Translation, filename: String) : this(translation.id, filename)
}
