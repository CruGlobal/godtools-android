package org.cru.godtools.tract.model;

import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.cru.godtools.tract.R;
import org.cru.godtools.tract.R2;
import org.cru.godtools.tract.model.Page.PageViewHolder;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import butterknife.BindView;

import static org.cru.godtools.tract.Constants.XMLNS_TRACT;
import static org.cru.godtools.tract.model.Utils.parseColor;

public final class Header extends Base implements Styles {
    static final String XML_HEADER = "header";
    private static final String XML_NUMBER = "number";
    private static final String XML_TITLE = "title";

    @Nullable
    @ColorInt
    private Integer mBackgroundColor = null;
    @Nullable
    Text mNumber;
    @Nullable
    Text mTitle;

    private Header(@NonNull final Page parent) {
        super(parent);
    }

    @ColorInt
    @Override
    public int getTextColor() {
        return getPrimaryTextColor();
    }

    @DimenRes
    @Override
    public int getTextSize() {
        return R.dimen.text_size_header;
    }

    @ColorInt
    public int getBackgroundColor() {
        return mBackgroundColor != null ? mBackgroundColor : getPage().getPrimaryColor();
    }

    public static int getBackgroundColor(@Nullable final Header header) {
        return header != null ? header.getBackgroundColor() : Color.TRANSPARENT;
    }

    static Header fromXml(@NonNull final Page parent, @NonNull final XmlPullParser parser)
            throws IOException, XmlPullParserException {
        return new Header(parent).parse(parser);
    }

    private Header parse(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, XMLNS_TRACT, XML_HEADER);

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
                        case XML_NUMBER:
                            mNumber = Text.fromNestedXml(this, parser, XMLNS_TRACT, XML_NUMBER);
                            continue;
                        case XML_TITLE:
                            mTitle = Text.fromNestedXml(this, parser, XMLNS_TRACT, XML_TITLE);
                            continue;
                    }
                    break;
            }

            // skip unrecognized nodes
            XmlPullParserUtils.skipTag(parser);
        }

        return this;
    }

    static final class HeaderViewHolder extends BaseViewHolder<Header> {
        @BindView(R2.id.header_number)
        TextView mHeaderNumber;
        @BindView(R2.id.header_title)
        TextView mHeaderTitle;

        HeaderViewHolder(@NonNull final View root, @Nullable final PageViewHolder parentViewHolder) {
            super(Header.class, root, parentViewHolder);
        }

        @NonNull
        public static HeaderViewHolder forView(@NonNull final View root,
                                               @Nullable final PageViewHolder parentViewHolder) {
            final HeaderViewHolder holder = forView(root, HeaderViewHolder.class);
            return holder != null ? holder : new HeaderViewHolder(root, parentViewHolder);
        }

        /* BEGIN lifecycle */

        @Override
        void onBind() {
            super.onBind();
            bindHeader();
            bindNumber();
            bindTitle();
        }

        /* END lifecycle */

        private void bindHeader() {
            mRoot.setVisibility(mModel != null ? View.VISIBLE : View.GONE);
            mRoot.setBackgroundColor(Header.getBackgroundColor(mModel));
        }

        private void bindNumber() {
            final Text number = mModel != null ? mModel.mNumber : null;
            mHeaderNumber.setVisibility(number != null ? View.VISIBLE : View.GONE);
            Text.bind(number, mHeaderNumber, R.dimen.text_size_header_number, null);
        }

        private void bindTitle() {
            final Text title = mModel != null ? mModel.mTitle : null;
            mHeaderTitle.setVisibility(title != null ? View.VISIBLE : View.GONE);
            Text.bind(title, mHeaderTitle);
        }
    }
}
