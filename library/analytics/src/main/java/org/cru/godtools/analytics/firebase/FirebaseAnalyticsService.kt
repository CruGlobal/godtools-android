package org.cru.godtools.analytics.firebase

import android.app.Application
import android.os.Bundle
import androidx.annotation.MainThread
import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import com.adobe.mobile.Visitor
import com.adobe.mobile.VisitorID
import com.google.android.gms.common.wrappers.InstantApps
import com.google.firebase.analytics.FirebaseAnalytics
import com.okta.oidc.net.response.UserInfo
import java.util.Locale
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
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

private const val VISITOR_ID_GUID = "ssoguid"
private const val VISITOR_ID_MASTER_PERSON_ID = "grmpid"

@Singleton
@OptIn(ExperimentalStdlibApi::class)
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

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private val analyticsExecutor: Executor = Executors.newSingleThreadExecutor()

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
            putString("cru_appname", VALUE_GODTOOLS)
            event.locale?.let { putString("cru_contentlanguage", LocaleCompat.toLanguageTag(it)) }
            event.adobeSiteSection?.let { putString("cru_sitesection", event.adobeSiteSection) }
            event.adobeSiteSubSection?.let { putString("cru_sitesubsection", event.adobeSiteSubSection) }
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

    // region Visitor ids
    @WorkerThread
    private fun MutableMap<String, Any?>.baseContextData(userProfile: UserInfo?, event: AnalyticsBaseEvent? = null) {
        put(USER_PROP_APP_NAME, VALUE_GODTOOLS)
        put(USER_PROP_CONTENT_LANGUAGE, LocaleCompat.toLanguageTag(Locale.getDefault()))
        put(USER_PROP_LOGGED_IN_STATUS, "false")
        if (userProfile != null) {
            put(USER_PROP_LOGGED_IN_STATUS, "true")
            put(USER_PROP_SSO_GUID, userProfile.ssoGuid)
            userProfile.grMasterPersonId?.let { put(USER_PROP_GR_MASTER_PERSON_ID, it) }
        }
    }

    private val userProfileStateFlow = oktaUserProfileProvider.userInfoFlow(refreshIfStale = false)
        .onEach { analyticsExecutor.execute { updateVisitorIdIdentifiers(it) } }
        .stateIn(coroutineScope, SharingStarted.Eagerly, null)

    @WorkerThread
    private fun updateVisitorIdIdentifiers(userProfile: UserInfo?) {
        Visitor.syncIdentifiers(
            mutableMapOf(
                VISITOR_ID_GUID to userProfile?.ssoGuid,
                VISITOR_ID_MASTER_PERSON_ID to userProfile?.grMasterPersonId
            ), when {
            userProfile != null -> VisitorID.VisitorIDAuthenticationState.VISITOR_ID_AUTHENTICATION_STATE_AUTHENTICATED
            else -> VisitorID.VisitorIDAuthenticationState.VISITOR_ID_AUTHENTICATION_STATE_LOGGED_OUT
        }
        )
    }
    // endregion Visitor ids

    init {
        oktaUserProfileProvider.userInfoFlow()
            .map { it?.ssoGuid }
            .distinctUntilChanged()
            .onEach { it ->
                firebase.setUserId(it)
                analyticsExecutor.execute {
                    updateVisitorIdIdentifiers(userProfileStateFlow.value)
                    val data = buildMap {
                        baseContextData(userProfileStateFlow.value)
                    }
                    data.mapValues { userProperty ->
                        firebase.setUserProperty(userProperty.key, userProperty.value.toString())
                    }
                }
            }
            .launchIn(coroutineScope)

        firebase.setUserProperty(
            USER_PROP_APP_TYPE, if (InstantApps.isInstantApp(app)) VALUE_APP_TYPE_INSTANT else VALUE_APP_TYPE_INSTALLED
        )
        firebase.setUserProperty("debug", BuildConfig.DEBUG.toString())
    }
}
