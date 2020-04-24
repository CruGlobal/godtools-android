package org.cru.godtools.service

import android.os.AsyncTask
import androidx.annotation.WorkerThread
import me.thekey.android.TheKey
import me.thekey.android.eventbus.event.LoginEvent
import org.cru.godtools.api.BuildConfig.CAMPAIGN_FORMS_ID
import org.cru.godtools.api.GodToolsApi
import org.cru.godtools.base.Settings
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountListRegistrationService @Inject internal constructor(
    eventBus: EventBus,
    private val settings: Settings,
    private val theKey: TheKey,
    private val api: GodToolsApi
) {
    init {
        eventBus.register(this)

        // add the current user to Adobe Campaigns if they haven't been added already
        theKey.defaultSessionGuid
            ?.takeUnless { settings.isAddedToCampaign(it) }
            ?.let { AsyncTask.THREAD_POOL_EXECUTOR.execute { registerUser(it) } }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onLogin(event: LoginEvent) = registerUser(event.guid)

    @WorkerThread
    private fun registerUser(guid: String) {
        val attrs = theKey.getAttributes(guid)
            .takeIf { it.areValid() && it.email != null } ?: return

        // trigger the signup
        try {
            val response = api.campaignForms
                .signup(CAMPAIGN_FORMS_ID, attrs.email, attrs.firstName, attrs.lastName)
                .execute()
            if (response.isSuccessful) {
                settings.setAddedToCampaign(guid, true)
            }
        } catch (e: IOException) {
            Timber.tag("AccountListRegService")
                .d(e, "error registering user in account list")
        }

        // TODO: if this failed due to no connectivity, we should retry when we have an internet connection
    }
}
