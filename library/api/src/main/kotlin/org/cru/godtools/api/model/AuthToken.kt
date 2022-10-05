package org.cru.godtools.api.model

import org.ccci.gto.android.common.jsonapi.annotation.JsonApiAttribute
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiType

private const val ATTR_OKTA_TOKEN = "okta_access_token"
private const val ATTR_USER_ID = "user-id"
private const val ATTR_TOKEN = "token"

@JsonApiType("auth-token")
class AuthToken {
    @JsonApiAttribute(ATTR_USER_ID)
    var userId: String? = null

    @JsonApiAttribute(ATTR_TOKEN)
    var token: String? = null

    @JsonApiType("auth-token-request")
    class Request(@JsonApiAttribute(ATTR_OKTA_TOKEN) val oktaAccessToken: String)
}
