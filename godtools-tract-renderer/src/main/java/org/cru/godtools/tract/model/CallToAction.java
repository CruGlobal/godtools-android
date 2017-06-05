package org.cru.godtools.tract.model;

import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ImageView;
import android.widget.TextView;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.cru.godtools.tract.util.DrawableUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import static org.cru.godtools.tract.Constants.XMLNS_CONTENT;
import static org.cru.godtools.tract.Constants.XMLNS_TRACT;
import static org.cru.godtools.tract.model.Text.XML_TEXT;

public final class CallToAction extends Base {
    static final String XML_CALL_TO_ACTION = "call-to-action";
    private static final String XML_EVENT = "event";

    @Nullable
    private Text mLabel;

    private CallToAction(@NonNull final Base parent) {
        super(parent);
    }

    @ColorInt
    private static int getArrowColor(@Nullable final CallToAction callToAction) {
        return Page.getPrimaryColor(callToAction != null ? callToAction.getPage() : null);
    }

    static CallToAction fromXml(@NonNull final Base parent, @NonNull final XmlPullParser parser)
            throws IOException, XmlPullParserException {
        return new CallToAction(parent).parse(parser);
    }

    private CallToAction parse(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, XMLNS_TRACT, XML_CALL_TO_ACTION);

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

    public static void bindLabel(@Nullable final CallToAction callToAction, @Nullable final TextView mLabel) {
        if (mLabel != null) {
            Text.bind(callToAction != null ? callToAction.mLabel : null, mLabel);
        }
    }

    public static void bindArrow(@Nullable final CallToAction callToAction, @Nullable final ImageView arrow) {
        if (arrow != null) {
            arrow.setImageDrawable(DrawableUtils.tint(arrow.getDrawable(), CallToAction.getArrowColor(callToAction)));
        }
    }
}
