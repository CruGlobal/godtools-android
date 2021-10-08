package org.cru.godtools.analytics.snowplow

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.WorkerThread
import com.snowplowanalytics.snowplow.Snowplow
import com.snowplowanalytics.snowplow.configuration.NetworkConfiguration
import com.snowplowanalytics.snowplow.configuration.SubjectConfiguration
import com.snowplowanalytics.snowplow.configuration.TrackerConfiguration
import com.snowplowanalytics.snowplow.event.AbstractEvent
import com.snowplowanalytics.snowplow.event.Event
import com.snowplowanalytics.snowplow.payload.SelfDescribingJson
import com.snowplowanalytics.snowplow.tracker.LogLevel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import okhttp3.OkHttpClient
import org.ccci.gto.android.common.okta.oidc.OktaUserProfileProvider
import org.ccci.gto.android.common.okta.oidc.net.response.grMasterPersonId
import org.ccci.gto.android.common.okta.oidc.net.response.ssoGuid
import org.ccci.gto.android.common.snowplow.events.CustomEventBuilder
import org.ccci.gto.android.common.snowplow.events.CustomScreenView
import org.ccci.gto.android.common.snowplow.events.CustomStructured
import org.ccci.gto.android.common.snowplow.utils.TimberLogger
import org.cru.godtools.analytics.BuildConfig
import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsBaseEvent
import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.analytics.model.AnalyticsSystem
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

private const val SNOWPLOW_NAMESPACE = "godtools-android"

private const val CONTEXT_SCHEMA_IDS = "iglu:org.cru/ids/jsonschema/1-0-3"
private const val CONTEXT_SCHEMA_SCORING = "iglu:org.cru/content-scoring/jsonschema/1-0-0"

private const val CONTEXT_ATTR_ID_GUID = "sso_guid"
private const val CONTEXT_ATTR_ID_GR_MASTER_PERSON_ID = "gr_master_person_id"
private const val CONTEXT_ATTR_SCORING_URI = "uri"

@Singleton
@SuppressLint("RestrictedApi")
class SnowplowAnalyticsService @Inject internal constructor(
    @ApplicationContext context: Context,
    eventBus: EventBus,
    okhttp: OkHttpClient,
    oktaUserProfileProvider: OktaUserProfileProvider
) {
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val snowplowTracker = Snowplow.createTracker(
        context,
        SNOWPLOW_NAMESPACE,
        NetworkConfiguration(BuildConfig.SNOWPLOW_ENDPOINT).apply {
            okHttpClient = okhttp
        },
        TrackerConfiguration(BuildConfig.SNOWPLOW_APP_ID).apply {
            base64encoding = false
            platformContext = true
            lifecycleAutotracking = true
            exceptionAutotracking = false

            loggerDelegate = TimberLogger
            logLevel = if (BuildConfig.DEBUG) LogLevel.DEBUG else LogLevel.ERROR
            diagnosticAutotracking = true
        },
        SubjectConfiguration()
    )

    // region Tracking Events
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
        CustomScreenView.builder()
            .name(event.screen)
            .populate(event)
            .build()
            .send()
    }

    @WorkerThread
    private fun handleActionEvent(event: AnalyticsActionEvent) {
        CustomStructured.builder()
            .category(event.snowplowCategory)
            .action(event.action)
            .apply { event.label?.let { label(it) } }
            .populate(event)
            .build()
            .send()
    }

    @WorkerThread
    private fun <T> T.populate(
        event: AnalyticsBaseEvent
    ) where T : AbstractEvent.Builder<T>, T : CustomEventBuilder<T> =
        contexts(listOf(idContext(), event.contentScoringContext()))
            .attribute("url", "${event.snowplowContentScoringUri}")
            .attribute("page", event.snowplowPageTitle)

    private fun Event.send() = snowplowTracker.track(this)

    // region Contexts
    private val userProfileStateFlow = oktaUserProfileProvider.userInfoFlow(refreshIfStale = false)
        .stateIn(coroutineScope, SharingStarted.Eagerly, null)

    @WorkerThread
    @OptIn(ExperimentalStdlibApi::class)
    private fun idContext() = SelfDescribingJson(
        CONTEXT_SCHEMA_IDS,
        buildMap<String, String> {
            userProfileStateFlow.value?.let { profile ->
                profile.ssoGuid?.let { put(CONTEXT_ATTR_ID_GUID, it) }
                profile.grMasterPersonId?.let { put(CONTEXT_ATTR_ID_GR_MASTER_PERSON_ID, it) }
            }
        }
    )

    private fun AnalyticsBaseEvent.contentScoringContext() = SelfDescribingJson(
        CONTEXT_SCHEMA_SCORING, mapOf(CONTEXT_ATTR_SCORING_URI to snowplowContentScoringUri.toString())
    )
    // endregion Contexts

    init {
        // register the service with eventbus as the last thing during object initialization to avoid a partially
        // initialized object processing an EventBus event.
        // see: https://jira.cru.org/browse/GT-1303
        eventBus.register(this)
    }
}
