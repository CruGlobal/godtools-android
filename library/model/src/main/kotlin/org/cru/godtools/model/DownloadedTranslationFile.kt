package org.cru.godtools.model

data class DownloadedTranslationFile(val translationId: Long, val filename: String) {
    constructor(translation: Translation, filename: String) : this(translation.id, filename)
}

@Deprecated(
    "Use DownloadedTranslationFile instead.",
    ReplaceWith("DownloadedTranslationFile", "org.cru.godtools.model.DownloadedTranslationFile")
)
typealias TranslationFile = DownloadedTranslationFile
