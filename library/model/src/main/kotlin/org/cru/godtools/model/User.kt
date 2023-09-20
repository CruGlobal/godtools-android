package org.cru.godtools.model

import java.time.Instant
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiAttribute
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiId
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiIgnore
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiType

private const val JSON_API_TYPE = "user"

private const val JSON_SSO_GUID = "sso-guid"
private const val JSON_NAME = "name"
private const val JSON_GIVEN_NAME = "given-name"
private const val JSON_FAMILY_NAME = "family-name"
private const val JSON_EMAIL = "email"
private const val JSON_CREATED_AT = "created-at"
private const val JSON_FAVORITE_TOOLS = "favorite-tools"

@JsonApiType(JSON_API_TYPE)
data class User @JvmOverloads constructor(
    @JsonApiId
    val id: String = "",
    @JsonApiAttribute(JSON_SSO_GUID)
    val ssoGuid: String? = null,
    @JsonApiAttribute(JSON_CREATED_AT)
    val createdAt: Instant? = null,
    @JsonApiIgnore
    val grMasterPersonId: String? = null,
    @JsonApiAttribute(JSON_NAME)
    val name: String? = null,
    @JsonApiAttribute(JSON_GIVEN_NAME)
    val givenName: String? = null,
    @JsonApiAttribute(JSON_FAMILY_NAME)
    val familyName: String? = null,
    @JsonApiAttribute(JSON_EMAIL)
    val email: String? = null,
) {
    @JsonApiAttribute(JSON_FAVORITE_TOOLS)
    val apiFavoriteTools: List<Tool> = emptyList()
}
