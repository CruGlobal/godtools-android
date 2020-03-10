package org.cru.godtools.xml.model;

import android.text.TextUtils;

import com.annimon.stream.Stream;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import org.ccci.gto.android.common.util.NumberUtils;
import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.cru.godtools.analytics.model.AnalyticsSystem;
import org.jetbrains.annotations.Contract;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import static org.cru.godtools.xml.Constants.XMLNS_ANALYTICS;

public class AnalyticsEvent {
    public static final String XML_EVENTS = "events";
    private static final String XML_EVENT = "event";
    private static final String XML_ACTION = "action";
    private static final String XML_DELAY = "delay";
    private static final String XML_SYSTEM = "system";
    private static final String XML_SYSTEM_ADOBE = "adobe";
    private static final String XML_SYSTEM_APPSFLYER = "appsflyer";
    private static final String XML_SYSTEM_FACEBOOK = "facebook";
    private static final String XML_SYSTEM_FIREBASE = "firebase";
    private static final String XML_SYSTEM_SNOWPLOW = "snowplow";
    private static final String XML_TRIGGER = "trigger";
    private static final String XML_TRIGGER_SELECTED = "selected";
    private static final String XML_TRIGGER_VISIBLE = "visible";
    private static final String XML_TRIGGER_HIDDEN = "hidden";
    private static final String XML_ATTRIBUTE = "attribute";
    private static final String XML_ATTRIBUTE_KEY = "key";
    private static final String XML_ATTRIBUTE_VALUE = "value";

    public enum Trigger {
        SELECTED, VISIBLE, HIDDEN, DEFAULT, UNKNOWN;

        @Nullable
        @Contract("_,!null -> !null")
        static Trigger parse(@Nullable final String trigger, @Nullable final Trigger defValue) {
            if (trigger != null) {
                switch (trigger) {
                    case XML_TRIGGER_SELECTED:
                        return SELECTED;
                    case XML_TRIGGER_VISIBLE:
                        return VISIBLE;
                    case XML_TRIGGER_HIDDEN:
                        return HIDDEN;
                    default:
                        return UNKNOWN;
                }
            }

            return defValue;
        }
    }

    @Nullable
    private String mAction;
    private int mDelay = 0;
    @NonNull
    private Collection<AnalyticsSystem> mSystems = ImmutableSet.of();
    @NonNull
    private Trigger mTrigger = Trigger.DEFAULT;
    @NonNull
    private Map<String, String> mAttributes = ImmutableMap.of();

    @Nullable
    public String getAction() {
        return mAction;
    }

    public int getDelay() {
        return mDelay;
    }

    @NonNull
    public Map<String, String> getAttributes() {
        return mAttributes;
    }

    public boolean isTriggerType(final Trigger... types) {
        for (final Trigger type : types) {
            if (mTrigger == type) {
                return true;
            }
        }
        return false;
    }

    public boolean isForSystem(@NonNull final AnalyticsSystem system) {
        return mSystems.contains(system);
    }

    @NonNull
    @WorkerThread
    public static Collection<AnalyticsEvent> fromEventsXml(@NonNull final XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, XMLNS_ANALYTICS, XML_EVENTS);

        // process any child elements
        final ImmutableList.Builder<AnalyticsEvent> events = ImmutableList.builder();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized nodes
            switch (parser.getNamespace()) {
                case XMLNS_ANALYTICS:
                    switch (parser.getName()) {
                        case XML_EVENT:
                            events.add(AnalyticsEvent.fromXml(parser));
                            continue;
                    }
                    break;
            }

            // skip unrecognized nodes
            XmlPullParserUtils.skipTag(parser);
        }

        // return any events we parsed
        return events.build();
    }

    @NonNull
    @WorkerThread
    public static AnalyticsEvent fromXml(@NonNull final XmlPullParser parser)
            throws XmlPullParserException, IOException {
        return new AnalyticsEvent().parse(parser);
    }

    @NonNull
    @WorkerThread
    private AnalyticsEvent parse(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, XMLNS_ANALYTICS, XML_EVENT);

        mAction = parser.getAttributeValue(null, XML_ACTION);
        mDelay = NumberUtils.toInteger(parser.getAttributeValue(null, XML_DELAY), mDelay);
        mSystems = parseSystemsAttr(parser.getAttributeValue(null, XML_SYSTEM));
        mTrigger = Trigger.parse(parser.getAttributeValue(null, XML_TRIGGER), mTrigger);

        // process any child elements
        final ImmutableMap.Builder<String, String> attributes = ImmutableMap.builder();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized nodes
            switch (parser.getNamespace()) {
                case XMLNS_ANALYTICS:
                    switch (parser.getName()) {
                        case XML_ATTRIBUTE:
                            final String key = parser.getAttributeValue(null, XML_ATTRIBUTE_KEY);
                            final String value = parser.getAttributeValue(null, XML_ATTRIBUTE_VALUE);
                            attributes.put(key, Strings.nullToEmpty(value));
                            XmlPullParserUtils.skipTag(parser);
                            continue;
                    }
                    break;
            }

            // skip unrecognized nodes
            XmlPullParserUtils.skipTag(parser);
        }
        mAttributes = attributes.build();

        return this;
    }

    @NonNull
    private static Collection<AnalyticsSystem> parseSystemsAttr(@Nullable final String systems) {
        return Sets.immutableEnumSet(
                Stream.of(TextUtils.split(systems, "\\s"))
                        .map(system -> {
                            switch (Strings.nullToEmpty(system)) {
                                case XML_SYSTEM_ADOBE:
                                    return AnalyticsSystem.ADOBE;
                                case XML_SYSTEM_APPSFLYER:
                                    return AnalyticsSystem.APPSFLYER;
                                case XML_SYSTEM_FACEBOOK:
                                    return AnalyticsSystem.FACEBOOK;
                                case XML_SYSTEM_FIREBASE:
                                    return AnalyticsSystem.FIREBASE;
                                case XML_SYSTEM_SNOWPLOW:
                                    return AnalyticsSystem.SNOWPLOW;
                                default:
                                    return AnalyticsSystem.UNKNOWN;
                            }
                        })
                        .filterNot(AnalyticsSystem.UNKNOWN::equals)
                        .distinct()
                        .toList()
        );
    }
}
