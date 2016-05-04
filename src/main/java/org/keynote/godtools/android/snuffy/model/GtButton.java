package org.keynote.godtools.android.snuffy.model;

import android.content.Context;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.collect.ImmutableSet;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.keynote.godtools.android.R;
import org.keynote.godtools.android.event.GodToolsEvent.EventID;
import org.keynote.godtools.android.snuffy.ParserUtils;
import org.w3c.dom.Element;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GtButton extends GtModel {
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
    Mode mMode = Mode.DEFAULT;
    @NonNull
    Set<EventID> mTapEvents = ImmutableSet.of();
    String mText;

    private GtButton(@NonNull final GtModel parent) {
        super(parent);
    }

    @NonNull
    public Mode getMode() {
        return mMode;
    }

    @NonNull
    public Set<EventID> getTapEvents() {
        return mTapEvents;
    }

    public String getText() {
        return mText;
    }

    @Nullable
    @Override
    public ViewHolder render(@NonNull final Context context, @Nullable final ViewGroup parent,
                             final boolean attachToRoot) {
        final LayoutInflater inflater = LayoutInflater.from(context);

        // inflate the raw view
        final View view;
        switch (mMode) {
            case PANEL:
                return null;
            case LINK:
                view = inflater.inflate(R.layout.gt_button_link, parent, false);
                break;
            default:
                view = inflater.inflate(R.layout.gt_button_default, parent, false);
                break;
        }
        if (parent != null && attachToRoot) {
            parent.addView(view);
        }

        return new ViewHolder(view);
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
                .parseEvents(parser.getAttributeValue(null, XML_ATTR_TAP_EVENTS), getManifest().getPackageCode());
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
        mTapEvents = ParserUtils.parseEvents(node.getAttribute(XML_ATTR_TAP_EVENTS), getManifest().getPackageCode());
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

    class ViewHolder extends GtModel.ViewHolder {
        @Nullable
        @Bind(R.id.gtButton)
        TextView mButton;

        ViewHolder(@NonNull final View root) {
            super(root);
            ButterKnife.bind(this, mRoot);

            // customize button if necessary
            if (mButton != null) {
                mButton.setText(mText);

                if (mMode == Mode.LINK) {
                    mButton.setPaintFlags(mButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                }
            }
        }

        @OnClick(R.id.gtButton)
        void onClick() {
            // trigger any configured tap events
            for (final EventID event : mTapEvents) {
                onSendEvent(event);
            }
        }
    }
}
