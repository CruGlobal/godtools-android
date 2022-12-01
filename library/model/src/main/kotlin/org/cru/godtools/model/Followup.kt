package org.cru.godtools.model

import java.time.Instant
import java.util.Locale
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiAttribute
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiIgnore
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiType

private const val JSON_API_TYPE_FOLLOWUP = "follow_up"

private const val JSON_NAME = "name"
private const val JSON_EMAIL = "email"
private const val JSON_LANGUAGE = "language_id"
private const val JSON_DESTINATION = "destination_id"

@JsonApiType(JSON_API_TYPE_FOLLOWUP)
class Followup(
    @JsonApiIgnore
    val id: Long? = null,
    @JsonApiAttribute(JSON_DESTINATION)
    val destination: Long,
    @JsonApiIgnore
    val languageCode: Locale,
    @JsonApiAttribute(JSON_EMAIL)
    val email: String,
    @JsonApiAttribute(JSON_NAME)
    val name: String? = null,
    @JsonApiIgnore
    val createTime: Instant = Instant.now(),
) {
    @JsonApiAttribute(JSON_LANGUAGE)
    private var languageId: Long? = null

    fun setLanguage(language: Language?) {
        languageId = language?.id
    }

    val isValid get() = email.isNotEmpty()
}
