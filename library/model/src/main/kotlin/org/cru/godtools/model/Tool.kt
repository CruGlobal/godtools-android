package org.cru.godtools.model

import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import java.util.UUID
import kotlin.random.Random
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiAttribute
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiIgnore
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiType

private const val JSON_API_TYPE = "resource"

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

@JsonApiType(JSON_API_TYPE)
class Tool : Base(), ChangeTrackingModel {
    companion object {
        const val JSON_ATTACHMENTS = "attachments"
        const val JSON_LATEST_TRANSLATIONS = "latest-translations"
        const val JSON_METATOOL = "metatool"
        const val JSON_DEFAULT_VARIANT = "default-variant"

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

            fun fromJson(json: String?) = when (json) {
                null -> null
                else -> values().firstOrNull { json == it.json } ?: DEFAULT
            }
        }
    }

    @JsonApiAttribute(JSON_ABBREVIATION)
    var code: String? = null

    @JsonApiAttribute(JSON_TYPE)
    private var _type: Type? = null
    var type: Type
        get() = _type ?: Type.DEFAULT
        set(type) {
            _type = type
        }

    @JsonApiAttribute(JSON_NAME)
    var name: String? = null
    @JsonApiAttribute(JSON_CATEGORY)
    var category: String? = null
    @JsonApiAttribute(JSON_DESCRIPTION)
    var description: String? = null

    @JsonApiAttribute(JSON_TOTAL_VIEWS)
    var shares = 0
    @JsonApiIgnore
    var pendingShares = 0
    val totalShares get() = pendingShares + shares

    @JsonApiAttribute(JSON_BANNER)
    var bannerId: Long? = null

    @JsonApiAttribute(JSON_DETAILS_BANNER)
    var detailsBannerId: Long? = null
    @JsonApiAttribute(JSON_DETAILS_BANNER_ANIMATION)
    var detailsBannerAnimationId: Long? = null
    @JsonApiAttribute(JSON_DETAILS_BANNER_YOUTUBE)
    var detailsBannerYoutubeVideoId: String? = null

    @JsonApiAttribute(JSON_INITIAL_FAVORITES_PRIORITY)
    var initialFavoritesPriority: Int? = Int.MAX_VALUE

    @JsonApiAttribute(JSON_SCREEN_SHARE_DISABLED)
    var isScreenShareDisabled = false

    @JsonApiAttribute(JSON_DEFAULT_ORDER)
    var defaultOrder = 0
    @JsonApiIgnore
    var order = Int.MAX_VALUE

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

    @JsonApiIgnore
    var isFavorite = false
    @JsonApiAttribute(JSON_HIDDEN)
    var isHidden = false
    @JsonApiAttribute(JSON_SPOTLIGHT)
    var isSpotlight = false

    val isValid get() = code != null && id != INVALID_ID

    // region ChangeTrackingModel
    @JsonApiIgnore
    override var changedFieldsStr = ""
    @JsonApiIgnore
    override var isTrackingChanges = false
    // endregion Change Tracking
}

// TODO: move this to testFixtures once they support Kotlin source files
@RestrictTo(RestrictTo.Scope.TESTS)
fun Tool(
    code: String,
    type: Tool.Type = Tool.Type.TRACT,
    translations: List<Translation>? = null,
    category: String? = null,
    config: Tool.() -> Unit = {},
) = Tool().apply {
    id = Random.nextLong()
    this.code = code
    this.type = type
    latestTranslations = translations
    this.category = category
    config()
}

// TODO: move this to testFixtures once they support Kotlin source files
@RestrictTo(RestrictTo.Scope.TESTS)
fun randomTool(
    code: String = UUID.randomUUID().toString(),
    type: Tool.Type = Tool.Type.values().random(),
    config: Tool.() -> Unit = {},
) = Tool(code, type) {
    id = Random.nextLong()
    this.code = code
    this.type = type
    name = UUID.randomUUID().toString()
    category = UUID.randomUUID().toString()
    description = UUID.randomUUID().toString()
    shares = Random.nextInt()
    pendingShares = Random.nextInt()
    bannerId = Random.nextLong()
    detailsBannerId = Random.nextLong()
    detailsBannerAnimationId = Random.nextLong()
    detailsBannerYoutubeVideoId = UUID.randomUUID().toString()
    isScreenShareDisabled = Random.nextBoolean()
    defaultOrder = Random.nextInt()
    order = Random.nextInt()
    metatoolCode = UUID.randomUUID().toString()
    defaultVariantCode = UUID.randomUUID().toString()
    isFavorite = Random.nextBoolean()
    isHidden = Random.nextBoolean()
    isSpotlight = Random.nextBoolean()
    config()
}
