package org.cru.godtools.analytics.firebase

import android.app.Application
import android.os.Bundle
import androidx.annotation.MainThread
import androidx.annotation.VisibleForTesting
import com.google.android.gms.common.wrappers.InstantApps
import com.google.firebase.analytics.FirebaseAnalytics
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.ccci.gto.android.common.okta.oidc.OktaUserProfileProvider
import org.ccci.gto.android.common.okta.oidc.net.response.grMasterPersonId
import org.ccci.gto.android.common.okta.oidc.net.response.ssoGuid
import org.cru.godtools.analytics.BuildConfig
import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsBaseEvent
import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.analytics.model.AnalyticsSystem
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/* Value constants */
private const val USER_PROP_APP_NAME = "cru_appname"
private const val VALUE_APP_NAME_GODTOOLS = "GodTools App"

private const val USER_PROP_APP_TYPE = "godtools_app_type"
private const val VALUE_APP_TYPE_INSTANT = "instant"
private const val VALUE_APP_TYPE_INSTALLED = "installed"

private const val USER_PROP_DEBUG = "debug"
private const val USER_PROP_LOGGED_IN_STATUS = "cru_loggedinstatus"
private const val USER_PROP_SSO_GUID = "cru_ssoguid"
private const val USER_PROP_GR_MASTER_PERSON_ID = "cru_grmasterpersonid"

private const val PARAM_APP_SECTION = "cru_sitesection"
private const val PARAM_APP_SUB_SECTION = "cru_sitesubsection"
private const val PARAM_CONTENT_LANGUAGE = "cru_contentlanguage"
const val PARAM_LANGUAGE_SECONDARY = "cru_contentlanguagesecondary"

@Singleton
class FirebaseAnalyticsService @VisibleForTesting internal constructor(
    app: Application,
    eventBus: EventBus,
    oktaUserProfileProvider: OktaUserProfileProvider,
    private val firebase: FirebaseAnalytics,
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    @Inject
    @MainThread
    internal constructor(
        app: Application,
        eventBus: EventBus,
        oktaUserProfileProvider: OktaUserProfileProvider
    ) : this(app, eventBus, oktaUserProfileProvider, FirebaseAnalytics.getInstance(app))

    // region Tracking Events
    init {
        eventBus.register(this)
    }

    @MainThread
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onAnalyticsEvent(event: AnalyticsBaseEvent) {
        if (event.isForSystem(AnalyticsSystem.FIREBASE) || event.isForSystem(AnalyticsSystem.ADOBE)) when (event) {
            is AnalyticsScreenEvent -> handleScreenEvent(event)
            is AnalyticsActionEvent -> handleActionEvent(event)
        }
    }

    @MainThread
    private fun handleScreenEvent(event: AnalyticsScreenEvent) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, event.screen)
            putString(USER_PROP_APP_NAME, VALUE_APP_NAME_GODTOOLS)
            event.locale?.let { putString(PARAM_CONTENT_LANGUAGE, it.toLanguageTag()) }
            event.appSection?.let { putString(PARAM_APP_SECTION, it) }
            event.appSubSection?.let { putString(PARAM_APP_SUB_SECTION, it) }
        }
        firebase.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    @MainThread
    private fun handleActionEvent(event: AnalyticsActionEvent) {
        val params = Bundle().apply {
            event.locale?.let { putString(PARAM_CONTENT_LANGUAGE, it.toLanguageTag()) }
            event.appSection?.let { putString(PARAM_APP_SECTION, it) }
            event.appSubSection?.let { putString(PARAM_APP_SUB_SECTION, it) }
            putAll(event.firebaseParams)
        }
        firebase.logEvent(event.firebaseEventName, params)
    }

    init {
        oktaUserProfileProvider.userInfoFlow(refreshIfStale = false)
            .onEach {
                firebase.setUserId(it?.ssoGuid)
                firebase.setUserProperty(USER_PROP_LOGGED_IN_STATUS, "${it != null}")
                firebase.setUserProperty(USER_PROP_GR_MASTER_PERSON_ID, it?.grMasterPersonId)
                firebase.setUserProperty(USER_PROP_SSO_GUID, it?.ssoGuid)
            }
            .launchIn(coroutineScope)

        firebase.setUserProperty(USER_PROP_APP_NAME, VALUE_APP_NAME_GODTOOLS)
        firebase.setUserProperty(PARAM_CONTENT_LANGUAGE, Locale.getDefault().toLanguageTag())
        firebase.setUserProperty(
            USER_PROP_APP_TYPE, if (InstantApps.isInstantApp(app)) VALUE_APP_TYPE_INSTANT else VALUE_APP_TYPE_INSTALLED
        )
        firebase.setUserProperty(USER_PROP_DEBUG, BuildConfig.DEBUG.toString())
    }
}
