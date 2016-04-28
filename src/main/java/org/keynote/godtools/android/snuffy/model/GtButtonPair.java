package org.keynote.godtools.android.snuffy.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.keynote.godtools.android.R;
import org.keynote.godtools.android.snuffy.ParserUtils;
import org.w3c.dom.Element;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class GtButtonPair extends GtModel {
    static final String XML_BUTTON_PAIR = "button-pair";

    GtButton mPositiveButton;
    GtButton mNegativeButton;

    private GtButtonPair(@NonNull final GtModel parent) {
        super(parent);
    }

    public GtButton getPositiveButton() {
        return mPositiveButton;
    }

    public GtButton getNegativeButton() {
        return mNegativeButton;
    }

    @NonNull
    @Override
    public ViewHolder render(@NonNull final Context context, @Nullable final ViewGroup parent,
                             final boolean attachToRoot) {
        final LayoutInflater inflater = LayoutInflater.from(context);

        // inflate the raw view
        final ViewHolder holder = new ViewHolder(inflater.inflate(R.layout.gt_buttonpair, parent, false));
        if (parent != null && attachToRoot) {
            parent.addView(holder.mRoot);
        }

        return holder;
    }

    @NonNull
    public static GtButtonPair fromXml(@NonNull final GtModel parent, @NonNull final XmlPullParser parser)
            throws IOException, XmlPullParserException {
        final GtButtonPair buttonPair = new GtButtonPair(parent);
        buttonPair.parse(parser);
        return buttonPair;
    }

    private void parse(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, XML_BUTTON_PAIR);

        // loop until we reach the matching end tag for this element
        while (parser.next() != XmlPullParser.END_TAG) {
            // skip anything that isn't a start tag for an element
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized elements
            switch (parser.getName()) {
                case GtButton.XML_POSITIVE_BUTTON:
                    if (mPositiveButton != null) {
                        throw new XmlPullParserException(
                                "XML has more than 1 " + GtButton.XML_POSITIVE_BUTTON + " defined", parser, null);
                    }
                    mPositiveButton = GtButton.fromXml(this, parser);
                    break;
                case GtButton.XML_NEGATIVE_BUTTON:
                    if (mNegativeButton != null) {
                        throw new XmlPullParserException(
                                "XML has more than 1 " + GtButton.XML_NEGATIVE_BUTTON + " defined", parser, null);
                    }
                    mNegativeButton = GtButton.fromXml(this, parser);
                    break;
                default:
                    // skip unrecognized nodes
                    XmlPullParserUtils.skipTag(parser);
            }
        }
    }

    public static GtButtonPair fromXml(@NonNull final GtModel parent, @NonNull final Element node) {
        final GtButtonPair buttonPair = new GtButtonPair(parent);
        buttonPair.parse(node);
        return buttonPair;
    }

    private void parse(@NonNull final Element node) {
        final Element positive = ParserUtils.getChildElementNamed(node, GtButton.XML_POSITIVE_BUTTON);
        if (positive != null) {
            mPositiveButton = GtButton.fromXml(this, positive);
        }
        final Element negative = ParserUtils.getChildElementNamed(node, GtButton.XML_NEGATIVE_BUTTON);
        if (negative != null) {
            mNegativeButton = GtButton.fromXml(this, negative);
        }
    }

    class ViewHolder extends GtModel.ViewHolder {
        @Bind(R.id.gtButtonPair)
        LinearLayout mButtons;

        ViewHolder(@NonNull final View root) {
            super(root);
            ButterKnife.bind(this, root);

            attachButtons();
        }

        private void attachButtons() {
            for (final GtModel model : new GtModel[] {mNegativeButton, mPositiveButton}) {
                if (model != null) {
                    final GtModel.ViewHolder child = model.render(mButtons.getContext(), mButtons, true);
                    if (child != null) {
                        child.setParentHolder(this);
                        final LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) child.mRoot.getLayoutParams();
                        if (lp.width <= 0 && lp.width != WRAP_CONTENT) {
                            lp.width = 0;
                            lp.weight = 1;
                        }
                    }
                }
            }
        }

        @Override
        public void scaleViewForLegacyLayout(final double scale) {
            super.scaleViewForLegacyLayout(scale);

            // scale the positive and negative buttons
            if (mButtons != null) {
                for (int i = 0; i < mButtons.getChildCount(); i++) {
                    final Object holder = mButtons.getChildAt(i).getTag(R.id.tag_gt_model_view_holder);
                    if (holder instanceof GtModel.ViewHolder) {
                        ((GtModel.ViewHolder) holder).scaleViewForLegacyLayout(scale);
                    }
                }
            }
        }
    }
}
