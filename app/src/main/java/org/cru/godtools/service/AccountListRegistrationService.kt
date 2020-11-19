package org.cru.godtools.service

import com.okta.oidc.net.response.UserInfo
import dagger.Lazy
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.ccci.gto.android.common.okta.oidc.OktaUserProfileProvider
import org.ccci.gto.android.common.okta.oidc.net.response.email
import org.ccci.gto.android.common.okta.oidc.net.response.familyName
import org.ccci.gto.android.common.okta.oidc.net.response.givenName
import org.ccci.gto.android.common.okta.oidc.net.response.oktaUserId
import org.ccci.gto.android.common.okta.oidc.net.response.ssoGuid
import org.cru.godtools.api.BuildConfig.CAMPAIGN_FORMS_ID
import org.cru.godtools.api.CampaignFormsApi
import org.cru.godtools.base.Settings
import timber.log.Timber

@Singleton
class AccountListRegistrationService @Inject internal constructor(
    private val settings: Settings,
    oktaUserProfileProvider: OktaUserProfileProvider,
    private val campaignFormsApi: Lazy<CampaignFormsApi>
) {
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    init {
        oktaUserProfileProvider.userInfoFlow()
            .onEach { it?.let { registerUser(it) } }
            .launchIn(coroutineScope)
    }

    private suspend fun registerUser(userInfo: UserInfo) {
        val oktaId = userInfo.oktaUserId
        val guid = userInfo.ssoGuid
        val email = userInfo.email ?: return
        if (settings.isAddedToCampaign(oktaId = oktaId, guid = guid)) return

        // trigger the signup
        try {
            val response = campaignFormsApi.get()
                .signup(CAMPAIGN_FORMS_ID, email, userInfo.givenName, userInfo.familyName)
            if (response.isSuccessful) settings.recordAddedToCampaign(oktaId = oktaId, guid = guid)
        } catch (e: IOException) {
            Timber.tag("AccountListRegService")
                .d(e, "error registering user in account list")
        }

        // TODO: if this failed due to no connectivity, we should retry when we have an internet connection
    }
}
