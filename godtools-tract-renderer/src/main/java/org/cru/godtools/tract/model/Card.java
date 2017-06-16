package org.cru.godtools.tract.model;

import android.support.annotation.CallSuper;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.collect.ImmutableList;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.cru.godtools.tract.R;
import org.cru.godtools.tract.R2;
import org.cru.godtools.tract.util.AutoAttachingGlobalLayoutListener;
import org.cru.godtools.tract.widget.PageContentLayout;
import org.cru.godtools.tract.widget.ScaledPicassoImageView.ScaleType;
import org.cru.godtools.tract.widget.TractPicassoImageView;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;

import static org.cru.godtools.tract.Constants.XMLNS_TRACT;
import static org.cru.godtools.tract.model.Utils.parseColor;
import static org.cru.godtools.tract.model.Utils.parseScaleType;
import static org.cru.godtools.tract.util.ViewUtils.getTopOffset;

public final class Card extends Base implements StylesParent, Parent {
    static final String XML_CARD = "card";
    private static final String XML_LABEL = "label";

    private static final ScaleType DEFAULT_BACKGROUND_IMAGE_SCALE_TYPE = ScaleType.FILL_X;
    private static final int DEFAULT_BACKGROUND_IMAGE_GRAVITY = ImageGravity.CENTER;

    @Nullable
    @ColorInt
    private Integer mBackgroundColor = null;
    @Nullable
    private String mBackgroundImage;
    private int mBackgroundImageGravity = DEFAULT_BACKGROUND_IMAGE_GRAVITY;
    @NonNull
    private ScaleType mBackgroundImageScaleType = DEFAULT_BACKGROUND_IMAGE_SCALE_TYPE;

    @Nullable
    Text mLabel;

    @NonNull
    private List<Content> mContent = ImmutableList.of();

    private Card(@NonNull final Base parent) {
        super(parent);
    }

    @Override
    public int getPrimaryColor() {
        return StylesParent.getPrimaryColor(getStylesParent());
    }

    @Override
    public int getPrimaryTextColor() {
        return StylesParent.getPrimaryTextColor(getStylesParent());
    }

    @Override
    public int getTextColor() {
        return StylesParent.getTextColor(getStylesParent());
    }

    @ColorInt
    private int getBackgroundColor() {
        // TODO: implement card-background-color on Page & Manifest
        return mBackgroundColor != null ? mBackgroundColor : Manifest.getBackgroundColor(getManifest());
    }

    @ColorInt
    static int getBackgroundColor(@Nullable final Card card) {
        // TODO: implement card-background-color on Page & Manifest
        return card != null ? card.getBackgroundColor() : Manifest.getBackgroundColor(null);
    }

    static Resource getBackgroundImageResource(@Nullable final Card card) {
        return card != null ? card.getResource(card.mBackgroundImage) : null;
    }

    static int getBackgroundImageGravity(@Nullable final Card card) {
        return card != null ? card.mBackgroundImageGravity : DEFAULT_BACKGROUND_IMAGE_GRAVITY;
    }

    static ScaleType getBackgroundImageScaleType(@Nullable final Card card) {
        return card != null ? card.mBackgroundImageScaleType : DEFAULT_BACKGROUND_IMAGE_SCALE_TYPE;
    }

    @NonNull
    @Override
    public List<Content> getContent() {
        return mContent;
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
        mBackgroundImage = parser.getAttributeValue(null, XML_BACKGROUND_IMAGE);
        mBackgroundImageGravity = ImageGravity.parse(parser, XML_BACKGROUND_IMAGE_GRAVITY, mBackgroundImageGravity);
        mBackgroundImageScaleType = parseScaleType(parser, XML_BACKGROUND_IMAGE_SCALE_TYPE, mBackgroundImageScaleType);

        // process any child elements
        final ImmutableList.Builder<Content> contentList = ImmutableList.builder();
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
                contentList.add(content);
                continue;
            }

            // skip unrecognized nodes
            XmlPullParserUtils.skipTag(parser);
        }
        mContent = contentList.build();

        return this;
    }

    @NonNull
    public static CardViewHolder createViewHolder(@NonNull final ViewGroup parent) {
        return new CardViewHolder(parent);
    }

    @UiThread
    public static final class CardViewHolder extends ParentViewHolder<Card> {
        @BindView(R2.id.background_image)
        TractPicassoImageView mBackgroundView;
        @BindView(R2.id.card)
        CardView mCardView;
        @BindView(R2.id.label)
        TextView mLabel;
        @BindView(R2.id.label_divider)
        View mDivider;

        private final float mLabelTextSize;

        CardViewHolder(@NonNull final ViewGroup parent) {
            super(Card.class, parent, R.layout.tract_content_card, null);
            AutoAttachingGlobalLayoutListener.attach(mRoot, this::updatePeekHeights);
            mLabelTextSize = mRoot.getResources().getDimension(R.dimen.text_size_card_label);
        }

        @Override
        @CallSuper
        void onBind() {
            super.onBind();
            bindBackground();
            bindLabel();
        }

        private void bindBackground() {
            mCardView.setCardBackgroundColor(Card.getBackgroundColor(mModel));
            Resource.bindBackgroundImage(mBackgroundView, getBackgroundImageResource(mModel),
                                         getBackgroundImageScaleType(mModel), getBackgroundImageGravity(mModel));
        }

        private void bindLabel() {
            final Text label = mModel != null ? mModel.mLabel : null;
            Text.bind(label, mLabel, StylesParent.getPrimaryColor(mModel), mLabelTextSize);
            mDivider.setBackgroundColor(StylesParent.getTextColor(mModel));
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
