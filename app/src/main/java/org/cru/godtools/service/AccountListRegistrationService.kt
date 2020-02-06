package org.cru.godtools.service

import android.content.Context
import android.os.AsyncTask
import androidx.annotation.WorkerThread
import me.thekey.android.TheKey
import me.thekey.android.eventbus.event.LoginEvent
import org.cru.godtools.api.BuildConfig.CAMPAIGN_FORMS_ID
import org.cru.godtools.api.GodToolsApi
import org.cru.godtools.base.Settings
import org.cru.godtools.base.util.SingletonHolder
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import java.io.IOException

class AccountListRegistrationService private constructor(context: Context) {
    private val api = GodToolsApi.getInstance()
    private val theKey = TheKey.getInstance(context)
    private val prefs = Settings.getInstance(context)

    init {
        EventBus.getDefault().register(this)

        // add the current user to Adobe Campaigns if they haven't been added already
        theKey.defaultSessionGuid
            ?.takeUnless { prefs.isAddedToCampaign(it) }
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
                prefs.setAddedToCampaign(guid, true)
            }
        } catch (e: IOException) {
            Timber.tag("AccountListRegService")
                .d(e, "error registering user in account list")
        }

        // TODO: if this failed due to no connectivity, we should retry when we have an internet connection
    }

    companion object : SingletonHolder<AccountListRegistrationService, Context>(::AccountListRegistrationService)
}
