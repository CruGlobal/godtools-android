package org.cru.godtools.model

import androidx.annotation.RestrictTo
import java.util.Locale
import java.util.UUID
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.random.nextLong
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiAttribute
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiId
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiIgnore
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiType
import org.cru.godtools.model.Base.Companion.INVALID_ID

private const val JSON_API_TYPE_TRANSLATION = "translation"

private const val JSON_RESOURCE = "resource"
private const val JSON_VERSION = "version"
private const val JSON_IS_PUBLISHED = "is-published"
private const val JSON_MANIFEST = "manifest-name"
private const val JSON_NAME = "translated-name"
private const val JSON_DESCRIPTION = "translated-description"
private const val JSON_TAGLINE = "translated-tagline"

@JsonApiType(JSON_API_TYPE_TRANSLATION)
class Translation {
    companion object {
        const val JSON_LANGUAGE = "language"
        private const val JSON_TOOL_DETAILS_CONVERSATION_STARTERS = "attr-tool-details-conversation-starters"
        private const val JSON_TOOL_DETAILS_BIBLE_REFERENCES = "attr-tool-details-bible-references"
        private const val JSON_TOOL_DETAILS_OUTLINE = "attr-tool-details-outline"

        const val DEFAULT_PUBLISHED = true
        const val DEFAULT_VERSION = 0
    }

    @JsonApiId
    private var _id: Long? = INVALID_ID
    var id: Long
        get() = _id ?: INVALID_ID
        set(id) {
            _id = id
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
    internal var isPublished = DEFAULT_PUBLISHED
        private set

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

    val isValid get() = toolCode != null && languageCode != Language.INVALID_CODE && isPublished
}

fun Translation?.getName(tool: Tool?) = this?.name ?: tool?.name
fun Translation?.getDescription(tool: Tool?) = this?.description ?: tool?.description
fun Translation?.getTagline(tool: Tool?) = this?.tagline ?: getDescription(tool)

// TODO: move this to testFixtures once they support Kotlin source files
@RestrictTo(RestrictTo.Scope.TESTS)
@Suppress("ktlint:standard:function-naming")
fun Translation(
    toolCode: String = UUID.randomUUID().toString(),
    languageCode: Locale = Locale.ENGLISH,
    version: Int = Translation.DEFAULT_VERSION,
    id: Long = Random.nextLong(),
    manifestFileName: String? = UUID.randomUUID().toString(),
    isDownloaded: Boolean = false,
    block: Translation.() -> Unit = {},
) = Translation().apply {
    this.id = id
    this.toolCode = toolCode
    this.languageCode = languageCode
    this.version = version
    this.manifestFileName = manifestFileName
    this.isDownloaded = isDownloaded
    block()
}

// TODO: move this to testFixtures once they support Kotlin source files
@RestrictTo(RestrictTo.Scope.TESTS)
fun randomTranslation(
    toolCode: String = UUID.randomUUID().toString(),
    languageCode: Locale = Locale.ENGLISH,
    id: Long = Random.nextLong(),
    config: Translation.() -> Unit = {},
) = Translation(toolCode, languageCode, id = id) {
    version = Random.nextInt(1..Int.MAX_VALUE)
    name = UUID.randomUUID().toString()
    description = UUID.randomUUID().toString()
    tagline = UUID.randomUUID().toString()
    toolDetailsConversationStarters = UUID.randomUUID().toString()
    toolDetailsOutline = UUID.randomUUID().toString()
    toolDetailsBibleReferences = UUID.randomUUID().toString()
    manifestFileName = UUID.randomUUID().toString()
    isDownloaded = Random.nextBoolean()
    config()
}
