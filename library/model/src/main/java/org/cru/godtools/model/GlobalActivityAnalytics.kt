package org.cru.godtools.model

import org.ccci.gto.android.common.jsonapi.annotation.JsonApiAttribute
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiType

private const val JSON_API_TYPE_GLOBAL_ANALYTICS = "global-activity-analytics"

private const val JSON_USERS = "users"
private const val JSON_COUNTRIES = "countries"
private const val JSON_LAUNCHES = "launches"
private const val JSON_GOSPEL_PRESENTATION = "gospel-presentations"

@JsonApiType(JSON_API_TYPE_GLOBAL_ANALYTICS)
class GlobalActivityAnalytics : Base() {
    @JsonApiAttribute(JSON_USERS)
    var users = 0
    @JsonApiAttribute(JSON_COUNTRIES)
    var countries = 0
    @JsonApiAttribute(JSON_LAUNCHES)
    var launches = 0
    @JsonApiAttribute(JSON_GOSPEL_PRESENTATION)
    var gospelPresentation = 0
}
