package org.cru.godtools.model

import org.ccci.gto.android.common.jsonapi.annotation.JsonApiAttribute
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiType

private const val JSON_API_TYPE_GLOBAL_ANALYTICS = "global-activity-analytics"

private const val JSON_USERS = "users"
private const val JSON_COUNTRIES = "countries"
private const val JSON_LAUNCHES = "launches"
private const val JSON_GOSPEL_PRESENTATIONS = "gospel-presentations"

@JsonApiType(JSON_API_TYPE_GLOBAL_ANALYTICS)
data class GlobalActivityAnalytics(
    @JsonApiAttribute(JSON_USERS)
    var users: Int = 0,
    @JsonApiAttribute(JSON_COUNTRIES)
    var countries: Int = 0,
    @JsonApiAttribute(JSON_LAUNCHES)
    var launches: Int = 0,
    @JsonApiAttribute(JSON_GOSPEL_PRESENTATIONS)
    var gospelPresentations: Int = 0,
)
