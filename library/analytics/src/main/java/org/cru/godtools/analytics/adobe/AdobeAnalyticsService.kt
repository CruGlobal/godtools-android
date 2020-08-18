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
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton
import me.thekey.android.Attributes
import me.thekey.android.TheKey
import me.thekey.android.eventbus.event.TheKeyEvent
import org.ccci.gto.android.common.compat.util.LocaleCompat
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
    private val theKey: TheKey
) : ActivityLifecycleCallbacks {
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

        val guid = theKey.defaultSessionGuid
        analyticsExecutor.execute { Config.collectLifecycleData(activity, buildMap { baseContextData(guid) }) }
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
        val guid = theKey.defaultSessionGuid
        analyticsExecutor.execute {
            Analytics.trackAction(action, buildMap {
                baseContextData(guid, this@track)
                previousScreenName?.let { put(ADOBE_ATTR_SCREEN_NAME, it) }
                adobeAttributes?.let { putAll(it) }
            })
        }
    }

    @AnyThread
    private fun AnalyticsScreenEvent.track() {
        val guid = theKey.defaultSessionGuid
        analyticsExecutor.execute {
            Analytics.trackState(screen, buildMap { stateContextData(guid, this@track) })
            previousScreenName = screen
        }
    }

    /**
     * Visitor.getMarketingCloudId() may be blocking. So, we need to call it on a worker thread.
     */
    @WorkerThread
    private fun MutableMap<String, Any?>.baseContextData(guid: String?, event: AnalyticsBaseEvent? = null) {
        put(ADOBE_ATTR_APP_NAME, VALUE_GODTOOLS)
        put(ADOBE_ATTR_MARKETING_CLOUD_ID, Visitor.getMarketingCloudId())

        put(ADOBE_ATTR_LOGGED_IN_STATUS, VALUE_NOT_LOGGED_IN)
        if (guid != null) {
            put(ADOBE_ATTR_LOGGED_IN_STATUS, VALUE_LOGGED_IN)
            put(ADOBE_ATTR_SSO_GUID, guid)
            theKey.getAttributes(guid).getAttribute(Attributes.ATTR_GR_MASTER_PERSON_ID)
                ?.let { put(ADOBE_ATTR_GR_MASTER_PERSON_ID, it) }
        }
        event?.run {
            locale?.let { put(ADOBE_ATTR_CONTENT_LANGUAGE, LocaleCompat.toLanguageTag(it)) }
            adobeSiteSection?.let { put(ADOBE_ATTR_SITE_SECTION, it) }
            adobeSiteSubSection?.let { put(ADOBE_ATTR_SITE_SUB_SECTION, it) }
        }
    }

    @WorkerThread
    private fun MutableMap<String, Any?>.stateContextData(guid: String?, event: AnalyticsScreenEvent) {
        baseContextData(guid, event)
        put(ADOBE_ATTR_SCREEN_NAME_PREVIOUS, previousScreenName)
        put(ADOBE_ATTR_SCREEN_NAME, event.screen)
    }

    // region Visitor ids
    init {
        val guid = theKey.defaultSessionGuid
        analyticsExecutor.execute {
            Visitor.syncIdentifier(
                VISITOR_ID_ECID,
                adobeMarketingCloudId,
                VisitorIDAuthenticationState.VISITOR_ID_AUTHENTICATION_STATE_UNKNOWN
            )
            updateVisitorIdIdentifiers(guid)
        }
    }

    @WorkerThread
    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onTheKeyEvent(@Suppress("UNUSED_PARAMETER") event: TheKeyEvent) {
        val guid = theKey.defaultSessionGuid
        analyticsExecutor.execute { updateVisitorIdIdentifiers(guid) }
    }

    @WorkerThread
    private fun updateVisitorIdIdentifiers(guid: String?) {
        Visitor.syncIdentifiers(
            mutableMapOf(
                VISITOR_ID_GUID to guid,
                VISITOR_ID_MASTER_PERSON_ID to theKey.getAttributes(guid)
                    .getAttribute(Attributes.ATTR_GR_MASTER_PERSON_ID)
            ), when {
                guid != null -> VisitorIDAuthenticationState.VISITOR_ID_AUTHENTICATION_STATE_AUTHENTICATED
                else -> VisitorIDAuthenticationState.VISITOR_ID_AUTHENTICATION_STATE_LOGGED_OUT
            }
        )
    }
    // endregion Visitor ids
}
