package org.cru.godtools.global.activity.analytics.manger.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiAttribute
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiIgnore
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiType
import java.util.Date

const val JSON_TYPE_GLOBAL_ACTIVITY_ANALYTICS = "global-activity-analytics"
private const val JSON_USERS = "users"
private const val JSON_COUNTRIES = "countries"
private const val JSON_LAUNCHES = "launches"
private const val JSON_GOSPEL_PRESENTATIONS = "gospel-presentations"
private const val TABLE_NAME_GLOBAL_ACTIVITY_ANALYTICS = "global_activity_analytics"

@Entity(tableName = TABLE_NAME_GLOBAL_ACTIVITY_ANALYTICS)
@JsonApiType(JSON_TYPE_GLOBAL_ACTIVITY_ANALYTICS)
class GlobalActivityAnalytics {
    @JsonApiIgnore
    @PrimaryKey
    var id = 1

    @JsonApiAttribute(name = JSON_USERS)
    var users = 0

    @JsonApiAttribute(name = JSON_COUNTRIES)
    var countries = 0

    @JsonApiAttribute(name = JSON_LAUNCHES)
    var launches = 0

    @JsonApiAttribute(name = JSON_GOSPEL_PRESENTATIONS)
    var gospelPresentation = 0

    @JsonApiIgnore
    var lastUpdated: Date? = null
}
