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
class Translation(
    @JsonApiId
    val id: Long,
    toolCode: String? = null,
    languageCode: Locale = Language.INVALID_CODE,
    @JsonApiAttribute(JSON_VERSION)
    val version: Int = DEFAULT_VERSION,
    @JsonApiAttribute(JSON_MANIFEST)
    val manifestFileName: String? = null,
    @JsonApiAttribute(JSON_NAME)
    val name: String? = null,
    @JsonApiAttribute(JSON_DESCRIPTION)
    val description: String? = null,
    @JsonApiAttribute(JSON_TAGLINE)
    val tagline: String? = null,
    @JsonApiAttribute(JSON_TOOL_DETAILS_CONVERSATION_STARTERS)
    val toolDetailsConversationStarters: String? = null,
    @JsonApiAttribute(JSON_TOOL_DETAILS_OUTLINE)
    val toolDetailsOutline: String? = null,
    @JsonApiAttribute(JSON_TOOL_DETAILS_BIBLE_REFERENCES)
    val toolDetailsBibleReferences: String? = null,
    @JsonApiIgnore
    val isDownloaded: Boolean = false,
) {
    internal constructor() : this(INVALID_ID)

    companion object {
        const val JSON_LANGUAGE = "language"
        private const val JSON_TOOL_DETAILS_CONVERSATION_STARTERS = "attr-tool-details-conversation-starters"
        private const val JSON_TOOL_DETAILS_BIBLE_REFERENCES = "attr-tool-details-bible-references"
        private const val JSON_TOOL_DETAILS_OUTLINE = "attr-tool-details-outline"

        const val DEFAULT_PUBLISHED = true
        const val DEFAULT_VERSION = 0
    }

    @JsonApiAttribute(JSON_RESOURCE)
    private val tool: Tool? = null
    @JsonApiIgnore
    val toolCode: String? = toolCode
        get() = field ?: tool?.code

    @JsonApiAttribute(JSON_LANGUAGE)
    val language: Language? = null
    @JsonApiIgnore
    val languageCode: Locale = languageCode
        get() = field.takeUnless { it == Language.INVALID_CODE } ?: language?.code ?: field

    @JsonApiAttribute(JSON_IS_PUBLISHED)
    internal var isPublished = DEFAULT_PUBLISHED
        private set

    val isValid get() = toolCode != null && languageCode != Language.INVALID_CODE && isPublished

    override fun equals(other: Any?) = when {
        this === other -> true
        javaClass != other?.javaClass -> false
        other !is Translation -> false
        id != other.id -> false
        toolCode != other.toolCode -> false
        languageCode != other.languageCode -> false
        version != other.version -> false
        manifestFileName != other.manifestFileName -> false
        name != other.name -> false
        description != other.description -> false
        tagline != other.tagline -> false
        toolDetailsConversationStarters != other.toolDetailsConversationStarters -> false
        toolDetailsOutline != other.toolDetailsOutline -> false
        toolDetailsBibleReferences != other.toolDetailsBibleReferences -> false
        isPublished != other.isPublished -> false
        isDownloaded != other.isDownloaded -> false
        else -> true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (toolCode?.hashCode() ?: 0)
        result = 31 * result + languageCode.hashCode()
        result = 31 * result + version
        result = 31 * result + isPublished.hashCode()
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (tagline?.hashCode() ?: 0)
        result = 31 * result + (toolDetailsConversationStarters?.hashCode() ?: 0)
        result = 31 * result + (toolDetailsOutline?.hashCode() ?: 0)
        result = 31 * result + (toolDetailsBibleReferences?.hashCode() ?: 0)
        result = 31 * result + (manifestFileName?.hashCode() ?: 0)
        result = 31 * result + isDownloaded.hashCode()
        return result
    }
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
    manifestFileName: String? = UUID.randomUUID().toString(),
    isDownloaded: Boolean = false,
    block: Translation.() -> Unit = {},
) = Translation(
    id = Random.nextLong(),
    toolCode = toolCode,
    languageCode = languageCode,
    version = version,
    manifestFileName = manifestFileName,
    isDownloaded = isDownloaded,
).apply(block)

// TODO: move this to testFixtures once they support Kotlin source files
@RestrictTo(RestrictTo.Scope.TESTS)
fun randomTranslation(
    toolCode: String? = UUID.randomUUID().toString(),
    languageCode: Locale = Locale.ENGLISH,
    version: Int = Random.nextInt(1..Int.MAX_VALUE),
    id: Long = Random.nextLong(),
    manifestFileName: String? = UUID.randomUUID().toString(),
    name: String? = UUID.randomUUID().toString(),
    description: String? = UUID.randomUUID().toString(),
    isDownloaded: Boolean = Random.nextBoolean(),
    config: Translation.() -> Unit = {},
) = Translation(
    id = id,
    toolCode = toolCode,
    languageCode = languageCode,
    version = version,
    manifestFileName = manifestFileName,
    name = name,
    description = description,
    tagline = UUID.randomUUID().toString(),
    toolDetailsConversationStarters = UUID.randomUUID().toString(),
    toolDetailsOutline = UUID.randomUUID().toString(),
    toolDetailsBibleReferences = UUID.randomUUID().toString(),
    isDownloaded = isDownloaded,
).apply(config)
