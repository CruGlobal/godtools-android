package org.cru.godtools.xml.model;

import android.net.Uri;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.cru.godtools.base.model.Event;
import org.cru.godtools.xml.model.Text.Align;
import org.jetbrains.annotations.Contract;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import static org.cru.godtools.xml.Constants.XMLNS_ANALYTICS;
import static org.cru.godtools.xml.Constants.XMLNS_CONTENT;
import static org.cru.godtools.xml.model.Text.XML_TEXT;

public final class Button extends Content implements Styles {
    static final String XML_BUTTON = "button";
    private static final String XML_COLOR = "color";
    private static final String XML_TYPE = "type";
    private static final String XML_TYPE_EVENT = "event";
    private static final String XML_TYPE_URL = "url";
    private static final String XML_URL = "url";

    private static final Align DEFAULT_TEXT_ALIGN = Align.CENTER;

    public enum Type {
        EVENT, URL, UNKNOWN;

        static final Type DEFAULT = UNKNOWN;

        @Nullable
        @Contract("_,!null -> !null")
        static Type parse(@Nullable final String type, @Nullable final Type defValue) {
            switch (Strings.nullToEmpty(type)) {
                case XML_TYPE_EVENT:
                    return EVENT;
                case XML_TYPE_URL:
                    return URL;
            }

            return defValue;
        }
    }

    @Nullable
    @ColorInt
    private Integer mColor;

    @NonNull
    private Collection<AnalyticsEvent> mAnalyticsEvents = ImmutableSet.of();

    @NonNull
    private Type mType = Type.DEFAULT;
    @NonNull
    private Set<Event.Id> mEvents = ImmutableSet.of();
    @Nullable
    private Uri mUrl;
    @Nullable
    private Text mText;

    private Button(@NonNull final Base parent) {
        super(parent);
    }

    @ColorInt
    public int getButtonColor() {
        return mColor != null ? mColor : StylesKt.getButtonColor(getStylesParent());
    }

    @ColorInt
    public static int getButtonColor(@Nullable final Button button) {
        return button != null ? button.getButtonColor() : StylesKt.getButtonColor(null);
    }

    @NonNull
    public Collection<AnalyticsEvent> getAnalyticsEvents() {
        return mAnalyticsEvents;
    }

    @NonNull
    public Type getType() {
        return mType;
    }

    @NonNull
    public Set<Event.Id> getEvents() {
        return mEvents;
    }

    @Nullable
    public Uri getUrl() {
        return mUrl;
    }

    @Nullable
    public Text getText() {
        return mText;
    }

    @Override
    public int getTextColor() {
        return getPrimaryTextColor();
    }

    @NonNull
    @Override
    public Align getTextAlign() {
        return DEFAULT_TEXT_ALIGN;
    }

    @WorkerThread
    static Button fromXml(@NonNull final Base parent, @NonNull final XmlPullParser parser)
            throws IOException, XmlPullParserException {
        return new Button(parent).parse(parser);
    }

    @WorkerThread
    private Button parse(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, XMLNS_CONTENT, XML_BUTTON);
        parseAttrs(parser);

        // process any child elements
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized nodes
            switch (parser.getNamespace()) {
                case XMLNS_ANALYTICS:
                    switch (parser.getName()) {
                        case AnalyticsEvent.XML_EVENTS:
                            mAnalyticsEvents = AnalyticsEvent.fromEventsXml(parser);
                            continue;
                    }
                    break;
                case XMLNS_CONTENT:
                    switch (parser.getName()) {
                        case XML_TEXT:
                            mText = new Text(this, parser);
                            continue;
                    }
                    break;
            }

            // skip unrecognized nodes
            XmlPullParserUtils.skipTag(parser);
        }
        return this;
    }

    @Override
    protected void parseAttrs(@NonNull final XmlPullParser parser) {
        super.parseAttrs(parser);
        mColor = Utils.parseColor(parser, XML_COLOR, mColor);
        mType = Type.parse(parser.getAttributeValue(null, XML_TYPE), mType);
        mEvents = parseEvents(parser, XML_EVENTS);
        mUrl = Utils.parseUrl(parser, XML_URL, null);
    }
}
