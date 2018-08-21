package org.cru.godtools.tract.model;

import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.collect.ImmutableSet;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.cru.godtools.base.model.Event;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Set;

import static org.cru.godtools.tract.model.Text.XML_TEXT;
import static org.cru.godtools.tract.model.Utils.parseColor;
import static org.cru.godtools.xml.Constants.XMLNS_CONTENT;
import static org.cru.godtools.xml.Constants.XMLNS_TRACT;

public final class CallToAction extends Base {
    static final String XML_CALL_TO_ACTION = "call-to-action";
    private static final String XML_EVENTS = "events";
    private static final String XML_CONTROL_COLOR = "control-color";

    @Nullable
    private Text mLabel;

    @Nullable
    @ColorInt
    private Integer mControlColor;

    @NonNull
    private Set<Event.Id> mEvents = ImmutableSet.of();

    CallToAction(@NonNull final Base parent) {
        super(parent);
    }

    @Nullable
    public Text getLabel() {
        return mLabel;
    }

    @ColorInt
    public static int getControlColor(@Nullable final CallToAction callToAction) {
        return callToAction != null ? callToAction.getControlColor() : Styles.getPrimaryColor(null);
    }

    @ColorInt
    private int getControlColor() {
        return mControlColor != null ? mControlColor : Styles.getPrimaryColor(getPage());
    }

    @NonNull
    public Set<Event.Id> getEvents() {
        return mEvents;
    }

    @NonNull
    static CallToAction fromXml(@NonNull final Base parent, @NonNull final XmlPullParser parser)
            throws IOException, XmlPullParserException {
        return new CallToAction(parent).parse(parser);
    }

    @NonNull
    private CallToAction parse(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, XMLNS_TRACT, XML_CALL_TO_ACTION);

        mEvents = parseEvents(parser, XML_EVENTS);

        mControlColor = parseColor(parser, XML_CONTROL_COLOR, null);

        // process any child elements
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized nodes
            switch (parser.getNamespace()) {
                case XMLNS_CONTENT:
                    switch (parser.getName()) {
                        case XML_TEXT:
                            mLabel = Text.fromXml(this, parser);
                            continue;
                    }
                    break;
            }

            // skip unrecognized nodes
            XmlPullParserUtils.skipTag(parser);
        }

        return this;
    }
}
