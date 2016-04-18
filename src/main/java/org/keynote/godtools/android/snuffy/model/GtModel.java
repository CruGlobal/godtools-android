package org.keynote.godtools.android.snuffy.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.view.View;
import android.view.ViewGroup;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.w3c.dom.Element;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import static org.ccci.gto.android.common.util.NumberUtils.toInteger;

public abstract class GtModel {
    private static final String XML_ATTR_WIDTH = "w";
    private static final String XML_ATTR_HEIGHT = "h";
    // x = left & right, y = top & bottom
    private static final String XML_ATTR_LEFT = "x";
    private static final String XML_ATTR_TOP = "y";
    private static final String XML_ATTR_LEFT_OFFSET = "xoffset";
    private static final String XML_ATTR_TOP_OFFSET = "yoffset";

    @NonNull
    private final GtModel mParentModel;

    @Nullable
    private Integer mWidth = null;
    @Nullable
    private Integer mHeight = null;
    @Nullable
    private Integer mLeft = null;
    @Nullable
    private Integer mTop = null;
    @Nullable
    private Integer mLeftOffset = null;
    @Nullable
    private Integer mTopOffset = null;

    // only allow GtManifest to use this constructor
    GtModel() {
        if (!(this instanceof GtManifest)) {
            throw new IllegalArgumentException("cannot use the no-args constructor for anything except a Manifest");
        }
        mParentModel = this;
    }

    GtModel(@NonNull final GtModel model) {
        mParentModel = model;
    }

    @NonNull
    public GtManifest getManifest() {
        return mParentModel.getManifest();
    }

    /**
     * @return the page that contains this model.
     */
    @Nullable
    public GtPage getPage() {
        return mParentModel.getPage();
    }

    @Nullable
    public Integer getWidth() {
        return mWidth;
    }

    @Nullable
    public Integer getHeight() {
        return mHeight;
    }

    @Nullable
    public Integer getLeft() {
        return mLeft;
    }

    @Nullable
    public Integer getTop() {
        return mTop;
    }

    @Nullable
    public Integer getLeftOffset() {
        return mLeftOffset;
    }

    @Nullable
    public Integer getTopOffset() {
        return mTopOffset;
    }

    @Nullable
    public static GtModel fromXml(@NonNull final GtModel parent, @NonNull final XmlPullParser parser)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, null);

        // parse based on the node type
        switch (parser.getName()) {
            case GtButton.XML_BUTTON:
            case GtButton.XML_LINK_BUTTON:
            case GtButton.XML_POSITIVE_BUTTON:
            case GtButton.XML_NEGATIVE_BUTTON:
                return GtButton.fromXml(parent, parser);
            case GtButtonPair.XML_BUTTON_PAIR:
                return GtButtonPair.fromXml(parent, parser);
            default:
                XmlPullParserUtils.skipTag(parser);
                return null;
        }
    }

    void parsePositionAttrs(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, null);

        mWidth = toInteger(parser.getAttributeValue(null, XML_ATTR_WIDTH), mWidth);
        mHeight = toInteger(parser.getAttributeValue(null, XML_ATTR_HEIGHT), mHeight);
        mLeft = toInteger(parser.getAttributeValue(null, XML_ATTR_LEFT), mLeft);
        mTop = toInteger(parser.getAttributeValue(null, XML_ATTR_TOP), mTop);
        mLeftOffset = toInteger(parser.getAttributeValue(null, XML_ATTR_LEFT_OFFSET), mLeftOffset);
        mTopOffset = toInteger(parser.getAttributeValue(null, XML_ATTR_TOP_OFFSET), mTopOffset);
    }

    @Nullable
    @Deprecated
    public static GtModel fromXml(@NonNull final GtModel parent, @NonNull final Element node) {
        switch (node.getTagName()) {
            case GtButton.XML_BUTTON:
            case GtButton.XML_LINK_BUTTON:
            case GtButton.XML_POSITIVE_BUTTON:
            case GtButton.XML_NEGATIVE_BUTTON:
                return GtButton.fromXml(parent, node);
            case GtButtonPair.XML_BUTTON_PAIR:
                return GtButtonPair.fromXml(parent, node);
            default:
                return null;
        }
    }

    void parsePositionAttrs(@NonNull final Element node) {
        mWidth = toInteger(node.getAttribute(XML_ATTR_WIDTH), mWidth);
        mHeight = toInteger(node.getAttribute(XML_ATTR_HEIGHT), mHeight);
        mLeft = toInteger(node.getAttribute(XML_ATTR_LEFT), mLeft);
        mTop = toInteger(node.getAttribute(XML_ATTR_TOP), mTop);
        mLeftOffset = toInteger(node.getAttribute(XML_ATTR_LEFT_OFFSET), mLeftOffset);
        mTopOffset = toInteger(node.getAttribute(XML_ATTR_TOP_OFFSET), mTopOffset);
    }

    /**
     * Render this view.
     *
     * @param root         The parent ViewGroup to inherit properties from (and optionally attach ourselves to).
     * @param attachToRoot Whether this view should be attached to the parent or not.
     * @return The view representing this model
     */
    @Nullable
    @UiThread
    public abstract View render(@NonNull ViewGroup root, double scale, boolean attachToRoot);

    protected void applyLayout(@Nullable final View view, final double scale) {
        if (view != null) {
            final ViewGroup.LayoutParams lp = view.getLayoutParams();
            if (mWidth != null && mWidth > 0) {
                lp.width = (int) Math.round(mWidth * scale);
            }
            if (mHeight != null && mHeight > 0) {
                lp.height = (int) Math.round(mHeight * scale);
            }
        }
    }
}
