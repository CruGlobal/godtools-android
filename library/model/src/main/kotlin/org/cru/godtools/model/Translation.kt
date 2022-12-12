package org.cru.godtools.model

import java.util.Date
import java.util.Locale
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiAttribute
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiIgnore
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiType

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
        private const val JSON_TOOL_DETAILS_CONVERSATION_STARTERS = "attr-tool-details-conversation-starters"
        private const val JSON_TOOL_DETAILS_BIBLE_REFERENCES = "attr-tool-details-bible-references"
        private const val JSON_TOOL_DETAILS_OUTLINE = "attr-tool-details-outline"

        const val DEFAULT_PUBLISHED = false
        const val DEFAULT_VERSION = 0
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
        private set
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
    @JsonApiAttribute(JSON_TOOL_DETAILS_CONVERSATION_STARTERS)
    var toolDetailsConversationStarters: String? = null
    @JsonApiAttribute(JSON_TOOL_DETAILS_OUTLINE)
    var toolDetailsOutline: String? = null
    @JsonApiAttribute(JSON_TOOL_DETAILS_BIBLE_REFERENCES)
    var toolDetailsBibleReferences: String? = null

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

fun Translation?.getName(tool: Tool?) = this?.name ?: tool?.name
fun Translation?.getDescription(tool: Tool?) = this?.description ?: tool?.description
fun Translation?.getTagline(tool: Tool?) = this?.tagline ?: getDescription(tool)
