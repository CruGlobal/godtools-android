package org.keynote.godtools.android.snuffy.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.common.collect.ImmutableSet;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.keynote.godtools.android.R;
import org.keynote.godtools.android.event.GodToolsEvent;
import org.keynote.godtools.android.snuffy.ParserUtils;
import org.w3c.dom.Element;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Set;

import static butterknife.ButterKnife.findById;

public class GtButton extends GtModel implements View.OnClickListener {
    public enum Mode {
        LINK, DEFAULT, UNKNOWN, PANEL;

        private static final String MODE_LINK = "link";

        @NonNull
        static Mode fromXmlAttr(@Nullable final String mode, @NonNull final Mode defValue) {
            if (mode != null) {
                switch (mode) {
                    case MODE_LINK:
                        return LINK;
                    default:
                        // there was a mode, but we don't recognize it
                        return UNKNOWN;
                }
            }

            return defValue;
        }
    }

    static final String XML_BUTTON = "button";
    static final String XML_POSITIVE_BUTTON = "positive-button";
    static final String XML_NEGATIVE_BUTTON = "negative-button";
    static final String XML_LINK_BUTTON = "link-button";

    private static final String XML_TEXT = "buttontext";
    private static final String XML_ATTR_MODE = "mode";
    private static final String XML_ATTR_TAP_EVENTS = "tap-events";

    @NonNull
    private Mode mMode = Mode.DEFAULT;
    @NonNull
    private Set<GodToolsEvent.EventID> mTapEvents = ImmutableSet.of();
    private String mText;

    private GtButton(@NonNull final GtModel parent) {
        super(parent);
    }

    @NonNull
    public Mode getMode() {
        return mMode;
    }

    @NonNull
    public Set<GodToolsEvent.EventID> getTapEvents() {
        return mTapEvents;
    }

    public String getText() {
        return mText;
    }

    @NonNull
    @Override
    public View render(@NonNull final ViewGroup parent, final boolean attachToParent) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        // inflate the raw view
        final View view;
        switch (mMode) {
            default:
                view = inflater.inflate(R.layout.gt_button_default, parent, false);
                parent.addView(view);
                break;
        }

        // customize button if necessary
        final Button button = findById(view, R.id.gtButton);
        if (button != null) {
            button.setText(mText);
            button.setOnClickListener(this);
        }

        return view;
    }

    @Override
    public void onClick(@NonNull final View v) {
        // TODO
    }

    @NonNull
    public static GtButton fromXml(@NonNull final GtModel parent, @NonNull final XmlPullParser parser)
            throws IOException, XmlPullParserException {
        final GtButton button = new GtButton(parent);
        button.parse(parser);
        return button;
    }

    @NonNull
    public static GtButton fromXml(@NonNull final GtModel parent, @NonNull final Element node) {
        final GtButton button = new GtButton(parent);
        button.parse(node);
        return button;
    }

    private void parse(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, null);
        XmlPullParserUtils
                .requireAnyName(parser, XML_BUTTON, XML_POSITIVE_BUTTON, XML_NEGATIVE_BUTTON, XML_LINK_BUTTON);

        // determine the button mode
        switch (parser.getName()) {
            case XML_BUTTON:
                mMode = Mode.PANEL;
                break;
            case XML_LINK_BUTTON:
                mMode = Mode.LINK;
                break;
            default:
                mMode = Mode.DEFAULT;
        }
        mMode = Mode.fromXmlAttr(parser.getAttributeValue(null, XML_ATTR_MODE), mMode);
        mTapEvents = ParserUtils
                .parseEvents(parser.getAttributeValue(null, XML_ATTR_TAP_EVENTS), getManifest().getAppPackage());
        parsePositionAttrs(parser);

        switch (mMode) {
            // don't process unknown button modes
            case UNKNOWN:
                XmlPullParserUtils.skipTag(parser);
                break;
            // This is a panel button, so extract the embedded content
            case PANEL:
                // loop until we reach the matching end tag for this element
                while (parser.next() != XmlPullParser.END_TAG) {
                    // skip anything that isn't a start tag for an element
                    if (parser.getEventType() != XmlPullParser.START_TAG) {
                        continue;
                    }

                    // process recognized elements
                    switch (parser.getName()) {
                        case XML_TEXT:
                            mText = XmlPullParserUtils.safeNextText(parser);
                            break;
                        default:
                            // skip unrecognized nodes
                            XmlPullParserUtils.skipTag(parser);
                    }
                }
                break;
            // Otherwise, just extract the text content
            default:
                mText = XmlPullParserUtils.safeNextText(parser);
        }
    }

    private void parse(@NonNull final Element node) {
        // determine the button mode
        switch (node.getTagName()) {
            case XML_BUTTON:
                mMode = Mode.PANEL;
                break;
            case XML_LINK_BUTTON:
                mMode = Mode.LINK;
                break;
            default:
                mMode = Mode.DEFAULT;
        }
        if (node.hasAttribute(XML_ATTR_MODE)) {
            mMode = Mode.fromXmlAttr(node.getAttribute(XML_ATTR_MODE), mMode);
        }
        mTapEvents = ParserUtils.parseEvents(node.getAttribute(XML_ATTR_TAP_EVENTS), getManifest().getAppPackage());
        parsePositionAttrs(node);

        switch (mMode) {
            case UNKNOWN:
                break;
            case PANEL:
                final Element textNode = ParserUtils.getChildElementNamed(node, XML_TEXT);
                if (textNode != null) {
                    mText = ParserUtils.getTextContentImmediate(textNode);
                }
                break;
            default:
                mText = ParserUtils.getTextContentImmediate(node);
        }
    }
}
