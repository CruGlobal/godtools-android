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
import org.ccci.gto.android.common.compat.util.LocaleCompat
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
private const val VALUE_GODTOOLS = "GodTools"
private const val USER_PROP_APP_TYPE = "godtools_app_type"
private const val VALUE_APP_TYPE_INSTANT = "instant"
private const val VALUE_APP_TYPE_INSTALLED = "installed"

private const val USER_PROP_APP_NAME = "cru_appname"
private const val USER_PROP_LOGGED_IN_STATUS = "cru_loggedinstatus"
private const val USER_PROP_SSO_GUID = "cru_ssoguid"
private const val USER_PROP_GR_MASTER_PERSON_ID = "cru_grmasterpersonid"
private const val USER_PROP_CONTENT_LANGUAGE = "cru_contentlanguage"

private const val SITE_SECTION = "cru_sitesection"
private const val SITE_SUB_SECTION = "cru_sitesubsection"

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
            putString(USER_PROP_APP_NAME, VALUE_GODTOOLS)
            event.locale?.let { putString(USER_PROP_CONTENT_LANGUAGE, LocaleCompat.toLanguageTag(it)) }
            event.appSection?.let { putString(SITE_SECTION, it) }
            event.appSubSection?.let { putString(SITE_SUB_SECTION, it) }
        }
        firebase.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    @MainThread
    private fun handleActionEvent(event: AnalyticsActionEvent) {
        val bundle = Bundle().apply {
            event.adobeAttributes?.forEach { attribute ->
                val attributeKey = attribute.key.replace(Regex("[ \\-.]"), "_")
                putString(attributeKey, attribute.value.toString())
            }
        }

        firebase.logEvent(event.firebaseEventName, bundle)
    }

    init {
        oktaUserProfileProvider.userInfoFlow(refreshIfStale = false)
            .onEach { it ->
                firebase.setUserProperty(USER_PROP_LOGGED_IN_STATUS, "${it != null}")
                firebase.setUserId(it?.ssoGuid)
                firebase.setUserProperty(USER_PROP_GR_MASTER_PERSON_ID, it?.grMasterPersonId)
                firebase.setUserProperty(USER_PROP_SSO_GUID, it?.ssoGuid)
            }
            .launchIn(coroutineScope)

        firebase.setUserProperty(USER_PROP_APP_NAME, VALUE_GODTOOLS)
        firebase.setUserProperty(USER_PROP_CONTENT_LANGUAGE, LocaleCompat.toLanguageTag(Locale.getDefault()))
        firebase.setUserProperty(
            USER_PROP_APP_TYPE, if (InstantApps.isInstantApp(app)) VALUE_APP_TYPE_INSTANT else VALUE_APP_TYPE_INSTALLED
        )
        firebase.setUserProperty("debug", BuildConfig.DEBUG.toString())
    }
}
