package org.cru.godtools.model

import org.ccci.gto.android.common.jsonapi.annotation.JsonApiAttribute
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiIgnore
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiType

private const val JSON_API_TYPE = "resource"

private const val JSON_NAME = "name"
private const val JSON_TYPE = "resource-type"
private const val JSON_TYPE_TRACT = "tract"
private const val JSON_TYPE_ARTICLE = "article"
private const val JSON_ABBREVIATION = "abbreviation"
private const val JSON_DESCRIPTION = "description"
private const val JSON_TOTAL_VIEWS = "total-views"
private const val JSON_BANNER = "attr-banner"
private const val JSON_BANNER_DETAILS = "attr-banner-about"
private const val JSON_OVERVIEW_VIDEO = "attr-about-overview-video-youtube"
private const val JSON_DEFAULT_ORDER = "attr-default-order"

@JsonApiType(JSON_API_TYPE)
class Tool : Base() {
    companion object {
        const val JSON_ATTACHMENTS = "attachments"
        const val JSON_LATEST_TRANSLATIONS = "latest-translations"
    }

    enum class Type(val json: String?) {
        TRACT(JSON_TYPE_TRACT), ARTICLE(JSON_TYPE_ARTICLE), UNKNOWN(null);

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
    @JsonApiAttribute(JSON_DESCRIPTION)
    var description: String? = null

    @JsonApiAttribute(JSON_TOTAL_VIEWS)
    var shares = 0
    @JsonApiIgnore
    var pendingShares = 0
    val totalShares get() = pendingShares + shares

    @JsonApiAttribute(JSON_BANNER)
    var bannerId: Long? = null
    @JsonApiAttribute(JSON_BANNER_DETAILS)
    var detailsBannerId = Attachment.INVALID_ID

    @JsonApiAttribute(JSON_OVERVIEW_VIDEO)
    var overviewVideo: String? = null

    @JsonApiAttribute(JSON_DEFAULT_ORDER)
    var defaultOrder: Int? = 0
    @JsonApiIgnore
    var order = Int.MAX_VALUE

    @JsonApiAttribute(JSON_ATTACHMENTS)
    var attachments: List<Attachment>? = null
        private set
    @JsonApiAttribute(JSON_LATEST_TRANSLATIONS)
    var latestTranslations: List<Translation>? = null
        private set

    @JsonApiIgnore
    var isAdded = false
}
