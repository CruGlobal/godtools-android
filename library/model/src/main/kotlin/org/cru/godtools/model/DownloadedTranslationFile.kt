package org.cru.godtools.model

data class DownloadedTranslationFile(val translationId: Long, val filename: String) {
    constructor(translation: Translation, filename: String) : this(translation.id, filename)
}
