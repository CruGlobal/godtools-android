package org.cru.godtools.account.model

data class AccountInfo internal constructor(
    val userId: String?,
    val oktaUserId: String?,
    val ssoGuid: String?,
    val grMasterPersonId: String?,
    val name: String?,
    val email: String?,
)
