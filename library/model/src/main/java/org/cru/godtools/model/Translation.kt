package org.cru.godtools.model

import org.ccci.gto.android.common.jsonapi.annotation.JsonApiAttribute
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiIgnore
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiType
import java.util.Date
import java.util.Locale

private const val JSON_API_TYPE_TRANSLATION = "translation"

private const val JSON_RESOURCE = "resource"
private const val JSON_VERSION = "version"
private const val JSON_IS_PUBLISHED = "is-published"
private const val JSON_MANIFEST = "manifest-name"
private const val JSON_NAME = "translated-name"
private const val JSON_DESCRIPTION = "translated-description"
private const val JSON_TAGLINE = "translated-tagline"

@JsonApiType(JSON_API_TYPE_TRANSLATION)
class Translation : Base() {
    companion object {
        const val JSON_LANGUAGE = "language"
        const val DEFAULT_PUBLISHED = false
        const val DEFAULT_VERSION = 0
        @JvmField
        val DEFAULT_LAST_ACCESSED = Date(0)
    }

    @JsonApiAttribute(JSON_RESOURCE)
    private var tool: Tool? = null
    @JsonApiIgnore
    private var _toolCode: String? = null
    var toolCode: String?
        get() = _toolCode ?: tool?.code
        set(code) {
            _toolCode = code
        }

    @JsonApiAttribute(JSON_LANGUAGE)
    var language: Language? = null
    @JsonApiIgnore
    private var _languageCode: Locale? = null
    var languageCode: Locale
        get() = _languageCode?.takeUnless { it == Language.INVALID_CODE } ?: language?.code ?: Language.INVALID_CODE
        set(code) {
            _languageCode = code
        }

    @JsonApiAttribute(JSON_VERSION)
    var version = DEFAULT_VERSION
    @JsonApiAttribute(JSON_IS_PUBLISHED)
    var isPublished = DEFAULT_PUBLISHED
    @JsonApiAttribute(JSON_NAME)
    var name: String? = null
    @JsonApiAttribute(JSON_DESCRIPTION)
    var description: String? = null
    @JsonApiAttribute(JSON_TAGLINE)
    var tagline: String? = null
    @JsonApiAttribute(JSON_MANIFEST)
    var manifestFileName: String? = null

    @JsonApiIgnore
    var isDownloaded = false
    @JsonApiIgnore
    var lastAccessed = DEFAULT_LAST_ACCESSED

    fun updateLastAccessed() {
        lastAccessed = Date()
    }
}
