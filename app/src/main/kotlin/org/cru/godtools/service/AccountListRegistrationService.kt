package org.cru.godtools.service

import dagger.Lazy
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.cru.godtools.api.BuildConfig.CAMPAIGN_FORMS_ID
import org.cru.godtools.api.CampaignFormsApi
import org.cru.godtools.base.Settings
import org.cru.godtools.model.User
import org.cru.godtools.user.data.UserManager
import timber.log.Timber

@Singleton
class AccountListRegistrationService @Inject internal constructor(
    private val settings: Settings,
    userManager: UserManager,
    private val campaignFormsApi: Lazy<CampaignFormsApi>
) {
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    init {
        userManager.userFlow
            .onEach { it?.let { registerUser(it) } }
            .launchIn(coroutineScope)
    }

    private suspend fun registerUser(user: User) {
        val guid = user.ssoGuid
        val email = user.email ?: return
        if (settings.isAddedToCampaign(guid = guid)) return

        // trigger the signup
        try {
            val response = campaignFormsApi.get()
                .signup(CAMPAIGN_FORMS_ID, email, user.givenName, user.familyName)
            if (response.isSuccessful) settings.recordAddedToCampaign(guid = guid)
        } catch (e: IOException) {
            Timber.tag("AccountListRegService").d(e, "error registering user in account list")
        }

        // TODO: if this failed due to no connectivity, we should retry when we have an internet connection
    }
}
