package org.cru.godtools.account.model

data class AccountInfo(
    val userId: String? = null,
    val oktaUserId: String? = null,
    val ssoGuid: String? = null,
    val grMasterPersonId: String? = null,
    val name: String? = null,
    val givenName: String? = null,
    val familyName: String? = null,
    val email: String? = null,
)
