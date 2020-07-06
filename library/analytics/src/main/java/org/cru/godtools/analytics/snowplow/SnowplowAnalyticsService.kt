package org.cru.godtools.analytics.snowplow

import android.content.Context
import androidx.annotation.WorkerThread
import com.snowplowanalytics.snowplow.tracker.Emitter.EmitterBuilder
import com.snowplowanalytics.snowplow.tracker.Executor
import com.snowplowanalytics.snowplow.tracker.Subject
import com.snowplowanalytics.snowplow.tracker.Subject.SubjectBuilder
import com.snowplowanalytics.snowplow.tracker.Tracker
import com.snowplowanalytics.snowplow.tracker.Tracker.TrackerBuilder
import com.snowplowanalytics.snowplow.tracker.emitter.RequestSecurity
import com.snowplowanalytics.snowplow.tracker.events.AbstractEvent
import com.snowplowanalytics.snowplow.tracker.events.Event
import com.snowplowanalytics.snowplow.tracker.events.ScreenView
import com.snowplowanalytics.snowplow.tracker.events.Structured
import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson
import me.thekey.android.Attributes
import me.thekey.android.TheKey
import okhttp3.OkHttpClient
import org.cru.godtools.analytics.BuildConfig
import org.cru.godtools.analytics.adobe.adobeMarketingCloudId
import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsBaseEvent
import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.analytics.model.AnalyticsSystem
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "SnwplwAnalyticsService"

private const val SNOWPLOW_NAMESPACE = "godtools-android"

private const val CONTEXT_SCHEMA_IDS = "iglu:org.cru/ids/jsonschema/1-0-3"
private const val CONTEXT_SCHEMA_SCORING = "iglu:org.cru/content-scoring/jsonschema/1-0-0"

private const val CONTEXT_ATTR_ID_MCID = "mcid"
private const val CONTEXT_ATTR_ID_GUID = "sso_guid"
private const val CONTEXT_ATTR_ID_GR_MASTER_PERSON_ID = "gr_master_person_id"
private const val CONTEXT_ATTR_SCORING_URI = "uri"

@Singleton
class SnowplowAnalyticsService @Inject internal constructor(
    context: Context,
    eventBus: EventBus,
    okhttp: OkHttpClient,
    private val theKey: TheKey
) {
    private val snowplowTracker: Tracker

    init {
        Executor.setThreadCount(1)
        Tracker.close()
        val emitter = EmitterBuilder(BuildConfig.SNOWPLOW_ENDPOINT, context)
            .security(RequestSecurity.HTTPS)
            .client(okhttp)
            .build()
        snowplowTracker = TrackerBuilder(emitter,
            SNOWPLOW_NAMESPACE,
            BuildConfig.SNOWPLOW_APP_ID, context)
            .base64(false)
            .mobileContext(true)
            .applicationCrash(false)
            .lifecycleEvents(true)
            .threadCount(1)
            .subject(SubjectBuilder().build())
            .build()
    }

    // region Tracking Events
    init {
        eventBus.register(this)
    }

    @WorkerThread
    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onAnalyticsEvent(event: AnalyticsBaseEvent) {
        if (event.isForSystem(AnalyticsSystem.SNOWPLOW)) when (event) {
            is AnalyticsScreenEvent -> handleScreenEvent(event)
            is AnalyticsActionEvent -> handleActionEvent(event)
        }
    }
    // endregion Tracking Events

    @WorkerThread
    private fun handleScreenEvent(event: AnalyticsScreenEvent) {
        ScreenView.builder()
            .name(event.screen)
            .populate(event)
            .build()
            .send(event)
    }

    @WorkerThread
    private fun handleActionEvent(event: AnalyticsActionEvent) {
        Structured.builder()
            .action(event.action)
            .apply { event.label?.let { label(it) } }
            .populate(event)
            .build()
            .send(event)
    }

    @WorkerThread
    private fun <T : AbstractEvent.Builder<*>> T.populate(event: AnalyticsBaseEvent) =
        apply { customContext(listOf(idContext(), event.contentScoringContext())) }

    @WorkerThread
    @Synchronized
    private fun Event.send(event: AnalyticsBaseEvent) {
        val subject = snowplowTracker.subject
        Executor.execute(TAG) { subject.populateUrl(event) }
        snowplowTracker.track(this)
        Executor.execute(TAG) { subject.resetUrl() }
    }

    private fun Subject.populateUrl(event: AnalyticsBaseEvent) {
        subject["url"] = "${event.snowplowContentScoringUri}"
        subject["page"] = event.snowplowPageTitle
    }

    private fun Subject.resetUrl() {
        subject.remove("url")
        subject.remove("page")
    }

    // region Contexts
    @WorkerThread
    @OptIn(ExperimentalStdlibApi::class)
    private fun idContext() = SelfDescribingJson(CONTEXT_SCHEMA_IDS, buildMap<String, String> {
        adobeMarketingCloudId?.let { put(CONTEXT_ATTR_ID_MCID, it) }

        theKey.defaultSessionGuid?.let { guid ->
            put(CONTEXT_ATTR_ID_GUID, guid)
            theKey.getAttributes(guid).getAttribute(Attributes.ATTR_GR_MASTER_PERSON_ID)?.let {
                put(CONTEXT_ATTR_ID_GR_MASTER_PERSON_ID, it)
            }
        }
    })

    @OptIn(ExperimentalStdlibApi::class)
    private fun AnalyticsBaseEvent.contentScoringContext() = SelfDescribingJson(
        CONTEXT_SCHEMA_SCORING, mapOf(CONTEXT_ATTR_SCORING_URI to snowplowContentScoringUri.toString())
    )
    // endregion Contexts
}
