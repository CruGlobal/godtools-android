package org.cru.godtools.model

import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import java.util.UUID
import kotlin.random.Random
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiAttribute
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiId
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiIgnore
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiType
import org.cru.godtools.model.Base.Companion.INVALID_ID

private const val JSON_TYPE = "resource-type"
private const val JSON_TYPE_TRACT = "tract"
private const val JSON_TYPE_ARTICLE = "article"
private const val JSON_TYPE_CYOA = "cyoa"
private const val JSON_TYPE_LESSON = "lesson"
private const val JSON_TYPE_META = "metatool"
private const val JSON_ABBREVIATION = "abbreviation"
private const val JSON_NAME = "name"
private const val JSON_CATEGORY = "attr-category"
private const val JSON_HIDDEN = "attr-hidden"
private const val JSON_SPOTLIGHT = "attr-spotlight"
private const val JSON_DESCRIPTION = "description"
private const val JSON_TOTAL_VIEWS = "total-views"
private const val JSON_BANNER = "attr-banner"
private const val JSON_DETAILS_BANNER = "attr-banner-about"
private const val JSON_DETAILS_BANNER_ANIMATION = "attr-about-banner-animation"
private const val JSON_DETAILS_BANNER_YOUTUBE = "attr-about-overview-video-youtube"
private const val JSON_DEFAULT_ORDER = "attr-default-order"
private const val JSON_INITIAL_FAVORITES_PRIORITY = "attr-initial-favorites-priority"
private const val JSON_SCREEN_SHARE_DISABLED = "attr-screen-share-disabled"

@JsonApiType(Tool.JSONAPI_TYPE)
class Tool(
    @JsonApiAttribute(JSON_ABBREVIATION)
    val code: String?,
    @JsonApiAttribute(JSON_TYPE)
    val type: Type = Type.UNKNOWN,
    @JsonApiAttribute(JSON_NAME)
    val name: String? = null,
    @JsonApiAttribute(JSON_CATEGORY)
    val category: String? = null,
    @JsonApiAttribute(JSON_DESCRIPTION)
    val description: String? = null,
    @JsonApiAttribute(JSON_BANNER)
    val bannerId: Long? = null,
    @JsonApiAttribute(JSON_DETAILS_BANNER)
    val detailsBannerId: Long? = null,
    @JsonApiAttribute(JSON_DETAILS_BANNER_ANIMATION)
    val detailsBannerAnimationId: Long? = null,
    @JsonApiAttribute(JSON_DETAILS_BANNER_YOUTUBE)
    val detailsBannerYoutubeVideoId: String? = null,
    @JsonApiAttribute(JSON_DEFAULT_ORDER)
    val defaultOrder: Int = 0,
    @JsonApiIgnore
    val order: Int = Int.MAX_VALUE,
    @JsonApiIgnore
    val isFavorite: Boolean = false,
    @JsonApiAttribute(JSON_HIDDEN)
    val isHidden: Boolean = false,
    @JsonApiAttribute(JSON_SPOTLIGHT)
    val isSpotlight: Boolean = false,
    @JsonApiAttribute(JSON_SCREEN_SHARE_DISABLED)
    val isScreenShareDisabled: Boolean = false,
    @JsonApiAttribute(JSON_TOTAL_VIEWS)
    val shares: Int = 0,
    @JsonApiIgnore
    val pendingShares: Int = 0,
) : ChangeTrackingModel {
    internal constructor() : this("")

    companion object {
        const val JSONAPI_TYPE = "resource"

        const val JSON_ATTACHMENTS = "attachments"
        const val JSON_LATEST_TRANSLATIONS = "latest-translations"
        const val JSON_METATOOL = "metatool"
        const val JSON_DEFAULT_VARIANT = "default-variant"

        const val ATTR_IS_FAVORITE = "isFavorite"

        val JSONAPI_FIELDS = arrayOf(
            JSON_TYPE,
            JSON_ABBREVIATION,
            JSON_NAME,
            JSON_DESCRIPTION,
            JSON_CATEGORY,
            JSON_TOTAL_VIEWS,
            JSON_BANNER,
            JSON_DETAILS_BANNER,
            JSON_DETAILS_BANNER_ANIMATION,
            JSON_DETAILS_BANNER_YOUTUBE,
            JSON_INITIAL_FAVORITES_PRIORITY,
            JSON_SCREEN_SHARE_DISABLED,
            JSON_DEFAULT_ORDER,
            JSON_METATOOL,
            JSON_DEFAULT_VARIANT,
            JSON_ATTACHMENTS,
            JSON_LATEST_TRANSLATIONS,
            JSON_HIDDEN,
            JSON_SPOTLIGHT
        )

        val COMPARATOR_DEFAULT_ORDER = compareBy<Tool> { it.defaultOrder }
        val COMPARATOR_FAVORITE_ORDER = compareBy<Tool> { it.order }.then(COMPARATOR_DEFAULT_ORDER)
    }

    enum class Type(val json: String?) {
        TRACT(JSON_TYPE_TRACT),
        ARTICLE(JSON_TYPE_ARTICLE),
        CYOA(JSON_TYPE_CYOA),
        LESSON(JSON_TYPE_LESSON),
        META(JSON_TYPE_META),
        UNKNOWN(null);

        val supportsParallelLanguage get() = this in setOf(TRACT, CYOA)

        companion object {
            val DEFAULT = UNKNOWN

            val NORMAL_TYPES = setOf(TRACT, CYOA, ARTICLE)

            fun fromJson(json: String?) = when (json) {
                null -> null
                else -> entries.firstOrNull { json == it.json } ?: DEFAULT
            }
        }
    }

    @JsonApiId
    private var _id: Long? = INVALID_ID
    var id: Long
        get() = _id ?: INVALID_ID
        set(id) {
            _id = id
        }

    val totalShares get() = pendingShares + shares

    @JsonApiAttribute(JSON_INITIAL_FAVORITES_PRIORITY)
    var initialFavoritesPriority: Int? = Int.MAX_VALUE

    @JsonApiAttribute(JSON_METATOOL)
    var metatool: Tool? = null
        private set
    @JsonApiIgnore
    var metatoolCode: String? = null
        get() = field ?: metatool?.code
        set(value) {
            field = value
            metatool = null
        }
    @JsonApiAttribute(JSON_DEFAULT_VARIANT)
    var defaultVariant: Tool? = null
        private set
    @JsonApiIgnore
    var defaultVariantCode: String? = null
        get() = field ?: defaultVariant?.code
        set(value) {
            field = value
            defaultVariant = null
        }
    @JsonApiAttribute(JSON_ATTACHMENTS)
    var attachments: List<Attachment>? = null
        private set
    @JsonApiAttribute(JSON_LATEST_TRANSLATIONS)
    var latestTranslations: List<Translation>? = null
        @VisibleForTesting
        internal set

    @Suppress("SENSELESS_COMPARISON")
    val isValid get() = !code.isNullOrEmpty() && type != null && type != Type.UNKNOWN && id != INVALID_ID

    // region ChangeTrackingModel
    @JsonApiIgnore
    override var changedFieldsStr = ""
    @JsonApiIgnore
    override var isTrackingChanges = false
    // endregion Change Tracking
}

// TODO: move this to testFixtures once they support Kotlin source files
@RestrictTo(RestrictTo.Scope.TESTS)
@Suppress("ktlint:standard:function-naming")
fun Tool(
    code: String,
    type: Tool.Type = Tool.Type.TRACT,
    translations: List<Translation>? = null,
    config: Tool.() -> Unit = {},
) = Tool(
    code = code,
    type = type,
    category = null,
).apply {
    id = Random.nextLong()
    latestTranslations = translations
    config()
}

// TODO: move this to testFixtures once they support Kotlin source files
@RestrictTo(RestrictTo.Scope.TESTS)
fun randomTool(
    code: String = UUID.randomUUID().toString(),
    type: Tool.Type = Tool.Type.entries.random(),
    name: String? = UUID.randomUUID().toString(),
    description: String? = UUID.randomUUID().toString(),
    isFavorite: Boolean = Random.nextBoolean(),
    isHidden: Boolean = Random.nextBoolean(),
    isSpotlight: Boolean = Random.nextBoolean(),
    config: Tool.() -> Unit = {},
) = Tool(
    code = code,
    type = type,
    name = name,
    category = UUID.randomUUID().toString(),
    description = description,
    bannerId = Random.nextLong(),
    detailsBannerId = Random.nextLong(),
    detailsBannerAnimationId = Random.nextLong(),
    detailsBannerYoutubeVideoId = UUID.randomUUID().toString(),
    defaultOrder = Random.nextInt(),
    order = Random.nextInt(),
    isFavorite = isFavorite,
    isHidden = isHidden,
    isSpotlight = isSpotlight,
    isScreenShareDisabled = Random.nextBoolean(),
    shares = Random.nextInt(),
    pendingShares = Random.nextInt(),
).apply {
    id = Random.nextLong()
    metatoolCode = UUID.randomUUID().toString()
    defaultVariantCode = UUID.randomUUID().toString()
    config()
}
