package org.cru.godtools.tract.model;

import android.support.annotation.CallSuper;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.cru.godtools.tract.R;
import org.cru.godtools.tract.R2;
import org.cru.godtools.tract.util.AutoAttachingGlobalLayoutListener;
import org.cru.godtools.tract.widget.PageContentLayout;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;

import static org.cru.godtools.tract.Constants.XMLNS_TRACT;
import static org.cru.godtools.tract.model.Utils.parseColor;
import static org.cru.godtools.tract.util.ViewUtils.getTopOffset;

public final class Card extends Base {
    static final String XML_CARD = "card";
    private static final String XML_LABEL = "label";

    @Nullable
    @ColorInt
    private Integer mBackgroundColor = null;

    @Nullable
    Text mLabel;

    @NonNull
    final List<Content> mContent = new ArrayList<>();

    private Card(@NonNull final Base parent) {
        super(parent);
    }

    @ColorInt
    private int getBackgroundColor() {
        // XXX: this may need to inherit differently
        return mBackgroundColor != null ? mBackgroundColor : Manifest.getBackgroundColor(getManifest());
    }

    @ColorInt
    static int getBackgroundColor(@Nullable final Card card) {
        // XXX: this may need to inherit differently
        return card != null ? card.getBackgroundColor() : Manifest.getBackgroundColor(null);
    }

    @NonNull
    static Card fromXml(@NonNull final Base parent, @NonNull final XmlPullParser parser)
            throws IOException, XmlPullParserException {
        return new Card(parent).parse(parser);
    }

    @NonNull
    private Card parse(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, XMLNS_TRACT, XML_CARD);

        mBackgroundColor = parseColor(parser, XML_BACKGROUND_COLOR, mBackgroundColor);

        // process any child elements
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized nodes
            switch (parser.getNamespace()) {
                case XMLNS_TRACT:
                    switch (parser.getName()) {
                        case XML_LABEL:
                            mLabel = Text.fromNestedXml(this, parser, XMLNS_TRACT, XML_LABEL);
                            continue;
                    }
                    break;
            }

            // try parsing this child element as a content node
            final Content content = Content.fromXml(this, parser);
            if (content != null) {
                mContent.add(content);
                continue;
            }

            // skip unrecognized nodes
            XmlPullParserUtils.skipTag(parser);
        }

        return this;
    }

    @NonNull
    public static CardViewHolder createViewHolder(@NonNull final ViewGroup parent) {
        return new CardViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.tract_content_card, parent, false));
    }

    public static final class CardViewHolder extends BaseViewHolder<Card> {
        @BindView(R2.id.card)
        CardView mCardView;
        @BindView(R2.id.label)
        TextView mLabel;
        @BindView(R2.id.label_divider)
        View mDivider;
        @BindView(R2.id.content)
        LinearLayout mContent;

        private final float mLabelTextSize;

        CardViewHolder(@NonNull final View root) {
            super(root);
            AutoAttachingGlobalLayoutListener.attach(mRoot, this::updatePeekHeights);
            mLabelTextSize = root.getResources().getDimension(R.dimen.text_size_card_label);
        }

        @Override
        @CallSuper
        void bind() {
            super.bind();
            bindBackground();
            bindLabel();
            bindContent();
        }

        private void bindBackground() {
            mCardView.setCardBackgroundColor(Card.getBackgroundColor(mModel));
        }

        private void bindLabel() {
            final Page page = mModel != null ? mModel.getPage() : null;
            final Text label = mModel != null ? mModel.mLabel : null;
            Text.bind(label, mLabel, Page.getPrimaryColor(page), mLabelTextSize);
            mDivider.setBackgroundColor(Page.getTextColor(page));
        }

        private void bindContent() {
            mContent.removeAllViews();
            Content.renderAll(mContent, mModel != null ? mModel.mContent : Collections.emptyList());
        }

        // XXX: this should be handled by PageContentLayout utilizing configuration within the LayoutParams
        // XXX: we can attach/detach the GlobalLayoutListner in the PCL onAttach/onDetach.
        // XXX: we can also track which view ids to use to calculate the peek heights
        void updatePeekHeights() {
            final ViewGroup.LayoutParams rawLp = mRoot.getLayoutParams();
            if (rawLp instanceof PageContentLayout.LayoutParams) {
                final PageContentLayout.LayoutParams lp = (PageContentLayout.LayoutParams) rawLp;

                // update card peek height & padding values
                final int cardPeekPadding = getTopOffset((ViewGroup) mRoot, mCardView);
                final int cardPeekHeight = getTopOffset(mCardView, mDivider);
                if (cardPeekPadding != lp.cardPeekPadding || cardPeekHeight != lp.cardPeekHeight) {
                    lp.cardPeekPadding = cardPeekPadding;
                    lp.cardPeekHeight = cardPeekHeight;
                    mRoot.requestLayout();
                }
            }
        }
    }
}
