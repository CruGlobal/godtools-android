package org.cru.godtools.model

import org.ccci.gto.android.common.jsonapi.annotation.JsonApiAttribute
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiIgnore
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiType
import java.util.Date
import java.util.Locale

private const val JSON_API_TYPE_FOLLOWUP = "follow_up"

private const val JSON_NAME = "name"
private const val JSON_EMAIL = "email"
private const val JSON_LANGUAGE = "language_id"
private const val JSON_DESTINATION = "destination_id"

@JsonApiType(JSON_API_TYPE_FOLLOWUP)
class Followup : Base() {
    @JsonApiAttribute(JSON_DESTINATION)
    var destination: Long? = null

    @JsonApiAttribute(JSON_NAME)
    var name: String? = null
    @JsonApiAttribute(JSON_EMAIL)
    var email: String? = null

    @JsonApiIgnore
    var languageCode: Locale? = null
    @JsonApiAttribute(JSON_LANGUAGE)
    private var languageId: Long? = null

    fun setLanguage(language: Language?) {
        languageId = language?.id
    }

    @JsonApiIgnore
    var createTime: Date? = Date()
        set(time) {
            field = time ?: Date()
        }

    val isValid get() = destination != null && languageCode != null && !email.isNullOrEmpty()
}
