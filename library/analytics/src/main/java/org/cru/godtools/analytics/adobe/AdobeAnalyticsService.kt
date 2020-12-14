package org.cru.godtools.analytics.adobe

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import com.adobe.mobile.Analytics
import com.adobe.mobile.Config
import com.adobe.mobile.Visitor
import com.adobe.mobile.VisitorID.VisitorIDAuthenticationState
import com.karumi.weak.weak
import com.okta.oidc.net.response.UserInfo
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
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

/* Property Keys */
private const val ADOBE_ATTR_APP_NAME = "cru.appname"
private const val ADOBE_ATTR_MARKETING_CLOUD_ID = "cru.mcid"
private const val ADOBE_ATTR_SSO_GUID = "cru.ssoguid"
private const val ADOBE_ATTR_GR_MASTER_PERSON_ID = "cru.grmpid"
private const val ADOBE_ATTR_LOGGED_IN_STATUS = "cru.loggedinstatus"
private const val ADOBE_ATTR_SCREEN_NAME = "cru.screenname"
private const val ADOBE_ATTR_SCREEN_NAME_PREVIOUS = "cru.previousscreenname"
private const val ADOBE_ATTR_CONTENT_LANGUAGE = "cru.contentlanguage"
const val ADOBE_ATTR_LANGUAGE_SECONDARY = "cru.contentlanguagesecondary"
private const val ADOBE_ATTR_SITE_SECTION = "cru.sitesection"
private const val ADOBE_ATTR_SITE_SUB_SECTION = "cru.sitesubsection"

/* Value constants */
private const val VALUE_GODTOOLS = "GodTools"
private const val VALUE_LOGGED_IN = "logged in"
private const val VALUE_NOT_LOGGED_IN = "not logged in"

private const val VISITOR_ID_GUID = "ssoguid"
private const val VISITOR_ID_MASTER_PERSON_ID = "grmpid"
private const val VISITOR_ID_ECID = "ecid"

@Singleton
@OptIn(ExperimentalStdlibApi::class)
class AdobeAnalyticsService @Inject internal constructor(
    app: Application,
    eventBus: EventBus,
    oktaUserProfileProvider: OktaUserProfileProvider
) : ActivityLifecycleCallbacks {
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private val analyticsExecutor: Executor = Executors.newSingleThreadExecutor()

    init {
        Config.setDebugLogging(BuildConfig.DEBUG)
        Config.setContext(app)
        eventBus.register(this)
        app.registerActivityLifecycleCallbacks(this)
    }

    // region Tracking Events
    @WorkerThread
    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onAnalyticsActionEvent(event: AnalyticsActionEvent) {
        if (event.isForSystem(AnalyticsSystem.ADOBE)) event.track()
    }

    @WorkerThread
    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onAnalyticsScreenEvent(event: AnalyticsScreenEvent) {
        if (event.isForSystem(AnalyticsSystem.ADOBE)) event.track()
    }

    // region ActivityLifecycleCallbacks
    private var activeActivity: Activity? by weak()

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityStarted(activity: Activity) = Unit

    @MainThread
    override fun onActivityResumed(activity: Activity) {
        activeActivity = activity

        val userProfile = userProfileStateFlow.value
        analyticsExecutor.execute {
            Config.collectLifecycleData(activity, buildMap { baseContextData(userProfile) })
        }
    }

    @MainThread
    override fun onActivityPaused(activity: Activity) {
        if (activeActivity === activity) {
            activeActivity = null
            analyticsExecutor.execute { Config.pauseCollectingLifecycleData() }
        }
    }

    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
    // endregion ActivityLifecycleCallbacks
    // endregion Tracking Events

    private var previousScreenName: String? = null

    @AnyThread
    private fun AnalyticsActionEvent.track() {
        val userProfile = userProfileStateFlow.value
        analyticsExecutor.execute {
            Analytics.trackAction(action, buildMap {
                baseContextData(userProfile, this@track)
                previousScreenName?.let { put(ADOBE_ATTR_SCREEN_NAME, it) }
                adobeAttributes?.let { putAll(it) }
            })
        }
    }

    @AnyThread
    private fun AnalyticsScreenEvent.track() {
        val userProfile = userProfileStateFlow.value
        analyticsExecutor.execute {
            Analytics.trackState(screen, buildMap { stateContextData(userProfile, this@track) })
            previousScreenName = screen
        }
    }

    @WorkerThread
    private fun MutableMap<String, Any?>.baseContextData(userProfile: UserInfo?, event: AnalyticsBaseEvent? = null) {
        put(ADOBE_ATTR_APP_NAME, VALUE_GODTOOLS)
        put(ADOBE_ATTR_MARKETING_CLOUD_ID, adobeMarketingCloudId)

        put(ADOBE_ATTR_LOGGED_IN_STATUS, VALUE_NOT_LOGGED_IN)
        if (userProfile != null) {
            put(ADOBE_ATTR_LOGGED_IN_STATUS, VALUE_LOGGED_IN)
            put(ADOBE_ATTR_SSO_GUID, userProfile.ssoGuid)
            userProfile.grMasterPersonId?.let { put(ADOBE_ATTR_GR_MASTER_PERSON_ID, it) }
        }
        event?.run {
            locale?.let { put(ADOBE_ATTR_CONTENT_LANGUAGE, LocaleCompat.toLanguageTag(it)) }
            appSection?.let { put(ADOBE_ATTR_SITE_SECTION, it) }
            appSubSection?.let { put(ADOBE_ATTR_SITE_SUB_SECTION, it) }
        }
    }

    @WorkerThread
    private fun MutableMap<String, Any?>.stateContextData(userProfile: UserInfo?, event: AnalyticsScreenEvent) {
        baseContextData(userProfile, event)
        put(ADOBE_ATTR_SCREEN_NAME_PREVIOUS, previousScreenName)
        put(ADOBE_ATTR_SCREEN_NAME, event.screen)
    }

    // region Visitor ids
    private val userProfileStateFlow = oktaUserProfileProvider.userInfoFlow(refreshIfStale = false)
        .onEach { analyticsExecutor.execute { updateVisitorIdIdentifiers(it) } }
        .stateIn(coroutineScope, Eagerly, null)

    init {
        analyticsExecutor.execute {
            Visitor.syncIdentifier(
                VISITOR_ID_ECID,
                adobeMarketingCloudId,
                VisitorIDAuthenticationState.VISITOR_ID_AUTHENTICATION_STATE_UNKNOWN
            )
            updateVisitorIdIdentifiers(userProfileStateFlow.value)
        }
    }

    @WorkerThread
    private fun updateVisitorIdIdentifiers(userProfile: UserInfo?) {
        Visitor.syncIdentifiers(
            mutableMapOf(
                VISITOR_ID_GUID to userProfile?.ssoGuid,
                VISITOR_ID_MASTER_PERSON_ID to userProfile?.grMasterPersonId
            ), when {
                userProfile != null -> VisitorIDAuthenticationState.VISITOR_ID_AUTHENTICATION_STATE_AUTHENTICATED
                else -> VisitorIDAuthenticationState.VISITOR_ID_AUTHENTICATION_STATE_LOGGED_OUT
            }
        )
    }
    // endregion Visitor ids
}
