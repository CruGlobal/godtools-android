package org.cru.godtools.analytics;

import android.content.Context;
import android.os.AsyncTask;

import com.adobe.mobile.Visitor;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Futures;
import com.snowplowanalytics.snowplow.tracker.Emitter;
import com.snowplowanalytics.snowplow.tracker.Executor;
import com.snowplowanalytics.snowplow.tracker.Subject;
import com.snowplowanalytics.snowplow.tracker.Tracker;
import com.snowplowanalytics.snowplow.tracker.events.AbstractEvent;
import com.snowplowanalytics.snowplow.tracker.events.Event;
import com.snowplowanalytics.snowplow.tracker.events.ScreenView;
import com.snowplowanalytics.snowplow.tracker.events.Structured;
import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson;

import org.ccci.gto.android.common.okhttp3.util.OkHttpClientUtil;
import org.cru.godtools.analytics.model.AnalyticsActionEvent;
import org.cru.godtools.analytics.model.AnalyticsBaseEvent;
import org.cru.godtools.analytics.model.AnalyticsScreenEvent;
import org.cru.godtools.analytics.model.AnalyticsSystem;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import me.thekey.android.TheKey;

import static com.snowplowanalytics.snowplow.tracker.emitter.RequestSecurity.HTTPS;
import static me.thekey.android.Attributes.ATTR_GR_MASTER_PERSON_ID;
import static org.cru.godtools.analytics.BuildConfig.SNOWPLOW_APP_ID;
import static org.cru.godtools.analytics.BuildConfig.SNOWPLOW_ENDPOINT;

public class SnowplowAnalyticsService {
    /* SnowPlow value constants */
    private static final String SNOWPLOW_NAMESPACE = "godtools-android";

    private static final String CONTEXT_SCHEMA_IDS = "iglu:org.cru/ids/jsonschema/1-0-3";
    private static final String CONTEXT_SCHEMA_SCORING = "iglu:org.cru/content-scoring/jsonschema/1-0-0";

    private static final String CONTEXT_ATTR_ID_MCID = "mcid";
    private static final String CONTEXT_ATTR_ID_GUID = "sso_guid";
    private static final String CONTEXT_ATTR_ID_GR_MASTER_PERSON_ID = "gr_master_person_id";
    private static final String CONTEXT_ATTR_SCORING_URI = "uri";

    @NonNull
    private final TheKey mTheKey;
    private final RunnableFuture<Tracker> mSnowPlowTracker;

    @AnyThread
    private SnowplowAnalyticsService(@NonNull final Context context) {
        mSnowPlowTracker = new FutureTask<>(() -> {
            Tracker.close();
            // XXX: creating an Emitter will initialize the event store database on whichever thread the emitter is
            //      created on. Because of this we initialize Snowplow in a background task
            final Emitter emitter = new Emitter.EmitterBuilder(SNOWPLOW_ENDPOINT, context)
                    .security(HTTPS)
                    .client(OkHttpClientUtil.attachGlobalInterceptors(new okhttp3.OkHttpClient.Builder()).build())
                    .build();
            return new Tracker.TrackerBuilder(emitter, SNOWPLOW_NAMESPACE, SNOWPLOW_APP_ID, context)
                    .base64(false)
                    .mobileContext(true)
                    .applicationCrash(false)
                    .lifecycleEvents(true)
                    .threadCount(1)
                    .subject(new Subject.SubjectBuilder().build())
                    .build();
        });
        AsyncTask.THREAD_POOL_EXECUTOR.execute(mSnowPlowTracker);

        mTheKey = TheKey.getInstance(context);
        EventBus.getDefault().register(this);
    }

    @Nullable
    private static SnowplowAnalyticsService sInstance;

    @NonNull
    @AnyThread
    public static synchronized SnowplowAnalyticsService getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            sInstance = new SnowplowAnalyticsService(context.getApplicationContext());
        }

        return sInstance;
    }

    @WorkerThread
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onAnalyticsEvent(@NonNull final AnalyticsBaseEvent event) {
        if (event.isForSystem(AnalyticsSystem.SNOWPLOW)) {
            if (event instanceof AnalyticsScreenEvent) {
                handleScreenEvent((AnalyticsScreenEvent) event);
            } else if (event instanceof AnalyticsActionEvent) {
                handleActionEvent((AnalyticsActionEvent) event);
            }
        }
    }

    @WorkerThread
    private void handleScreenEvent(@NonNull final AnalyticsScreenEvent event) {
        final ScreenView.Builder builder = ScreenView.builder()
                .name(event.getScreen());
        sendEvent(populate(builder, event).build(), event);
    }

    @WorkerThread
    private void handleActionEvent(@NonNull final AnalyticsActionEvent event) {
        final Structured.Builder builder = Structured.builder()
                .category(event.getCategory())
                .action(event.getAction());
        final String label = event.getLabel();
        if (label != null) {
            builder.label(label);
        }

        sendEvent(populate(builder, event).build(), event);
    }

    @WorkerThread
    private <T extends AbstractEvent.Builder> T populate(@NonNull final T builder,
                                                         @NonNull final AnalyticsBaseEvent event) {
        builder.customContext(ImmutableList.of(idContext(), contentScoringContext(event)));
        return builder;
    }

    @WorkerThread
    private synchronized void sendEvent(@NonNull final Event event, @NonNull final AnalyticsBaseEvent origEvent) {
        final Tracker tracker = Futures.getUnchecked(mSnowPlowTracker);
        final Subject subject = tracker.getSubject();
        Executor.execute(() -> populateSubject(subject, origEvent));
        tracker.track(event);
        Executor.execute(() -> resetSubject(subject));
    }

    @NonNull
    @WorkerThread
    private SelfDescribingJson idContext() {
        final Map<String, String> data = new HashMap<>();
        data.put(CONTEXT_ATTR_ID_MCID, Visitor.getMarketingCloudId());

        final String guid = mTheKey.getDefaultSessionGuid();
        if (guid != null) {
            data.put(CONTEXT_ATTR_ID_GUID, guid);
            final String grMasterPersonId = mTheKey.getAttributes(guid).getAttribute(ATTR_GR_MASTER_PERSON_ID);
            if (grMasterPersonId != null) {
                data.put(CONTEXT_ATTR_ID_GR_MASTER_PERSON_ID, grMasterPersonId);
            }
        }

        return new SelfDescribingJson(CONTEXT_SCHEMA_IDS, data);
    }

    @NonNull
    private SelfDescribingJson contentScoringContext(@NonNull final AnalyticsBaseEvent event) {
        final Map<String, String> data = new HashMap<>();
        data.put(CONTEXT_ATTR_SCORING_URI, event.getSnowplowContentScoringUri().toString());

        return new SelfDescribingJson(CONTEXT_SCHEMA_SCORING, data);
    }

    private void populateSubject(@NonNull final Subject subject, @NonNull final AnalyticsBaseEvent event) {
        subject.getSubject().put("url", event.getSnowplowContentScoringUri().toString());
        subject.getSubject().put("page", event.getSnowplowPageTitle());
    }

    private void resetSubject(@NonNull final Subject subject) {
        subject.getSubject().remove("url");
        subject.getSubject().remove("page");
    }
}
