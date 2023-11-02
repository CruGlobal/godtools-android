package org.cru.godtools.api.model

import org.ccci.gto.android.common.jsonapi.annotation.JsonApiAttribute
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiType

private const val ATTR_FACEBOOK_ACCESS_TOKEN = "facebook_access_token"
private const val ATTR_GOOGLE_ID_TOKEN = "google_id_token"
private const val ATTR_OKTA_TOKEN = "okta_access_token"
private const val ATTR_USER_ID = "user-id"
private const val ATTR_TOKEN = "token"

@JsonApiType("auth-token")
data class AuthToken(
    @JsonApiAttribute(ATTR_USER_ID)
    var userId: String? = null,
    @JsonApiAttribute(ATTR_TOKEN)
    var token: String? = null
) {
    companion object {
        const val ERROR_USER_ALREADY_EXISTS = "user_already_exists"
        const val ERROR_USER_NOT_FOUND = "user_not_found"
    }

    @JsonApiType("auth-token-request")
    data class Request(
        @JsonApiAttribute(ATTR_FACEBOOK_ACCESS_TOKEN) val fbAccessToken: String? = null,
        @JsonApiAttribute(ATTR_GOOGLE_ID_TOKEN) val googleIdToken: String? = null,
        @JsonApiAttribute(ATTR_OKTA_TOKEN) val oktaAccessToken: String? = null,
    )
}
