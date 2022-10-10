package org.cru.godtools.account.provider.okta

import okhttp3.HttpUrl

class OktaBuildConfig(
    val clientId: String,
    val discoveryUrl: HttpUrl
)
