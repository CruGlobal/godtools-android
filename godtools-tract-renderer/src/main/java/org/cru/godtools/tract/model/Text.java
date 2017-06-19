package org.cru.godtools.tract.model;

import android.support.annotation.ColorInt;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;

import org.ccci.gto.android.common.util.NumberUtils;
import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.cru.godtools.tract.R;
import org.cru.godtools.tract.R2;
import org.cru.godtools.tract.model.Parent.ParentViewHolder;
import org.jetbrains.annotations.Contract;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import butterknife.BindView;

import static android.util.TypedValue.COMPLEX_UNIT_PX;
import static org.cru.godtools.tract.Constants.XMLNS_CONTENT;
import static org.cru.godtools.tract.model.Utils.parseColor;

public final class Text extends Content {
    static final String XML_TEXT = "text";
    private static final String XML_TEXT_ALIGN = "text-align";
    private static final String XML_TEXT_ALIGN_START = "start";
    private static final String XML_TEXT_ALIGN_CENTER = "center";
    private static final String XML_TEXT_ALIGN_END = "end";
    private static final String XML_TEXT_SCALE = "text-scale";

    private static final Align DEFAULT_TEXT_ALIGN = Align.START;
    private static final double DEFAULT_TEXT_SCALE = 1.0;

    enum Align {
        START(Gravity.START), CENTER(Gravity.CENTER_HORIZONTAL), END(Gravity.END);

        final int mGravity;

        Align(final int gravity) {
            mGravity = gravity;
        }

        @Nullable
        @Contract("_, !null -> !null")
        static Align parse(@Nullable final String value, @Nullable final Align defValue) {
            if (value != null) {
                switch (value) {
                    case XML_TEXT_ALIGN_START:
                        return START;
                    case XML_TEXT_ALIGN_CENTER:
                        return CENTER;
                    case XML_TEXT_ALIGN_END:
                        return END;
                }
            }
            return defValue;
        }
    }

    @Nullable
    private Align mTextAlign = null;
    @ColorInt
    @Nullable
    private Integer mTextColor = null;
    @Nullable
    private Double mTextScale = null;

    @Nullable
    private String mText;

    private Text(@NonNull final Base parent) {
        super(parent);
    }

    @NonNull
    private Align getTextAlign(@NonNull final Align defAlign) {
        return mTextAlign != null ? mTextAlign : defAlign;
    }

    @Override
    public int getTextColor() {
        return mTextColor != null ? mTextColor : Styles.getTextColor(getStylesParent());
    }

    @ColorInt
    private int getTextColor(@ColorInt final int defColor) {
        return mTextColor != null ? mTextColor : defColor;
    }

    @ColorInt
    static int getTextColor(@Nullable final Text text) {
        return text != null ? text.getTextColor() : Styles.getTextColor(null);
    }

    private double getTextScale(final double defScale) {
        return mTextScale != null ? mTextScale : defScale;
    }

    @Nullable
    public String getText() {
        return mText;
    }

    @Nullable
    static String getText(@Nullable final Text text) {
        return text != null ? text.getText() : null;
    }

    static Text fromXml(@NonNull final Base parent, @NonNull final XmlPullParser parser)
            throws IOException, XmlPullParserException {
        return new Text(parent).parse(parser);
    }

    @Nullable
    static Text fromNestedXml(@NonNull final Base parent, @NonNull final XmlPullParser parser,
                              @Nullable final String parentNamespace, @NonNull final String parentName)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, parentNamespace, parentName);

        // process any child elements
        Text text = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized nodes
            switch (parser.getNamespace()) {
                case XMLNS_CONTENT:
                    switch (parser.getName()) {
                        case XML_TEXT:
                            text = fromXml(parent, parser);
                            continue;
                    }
                    break;
            }

            // skip unrecognized nodes
            XmlPullParserUtils.skipTag(parser);
        }

        return text;
    }

    @WorkerThread
    private Text parse(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, XMLNS_CONTENT, XML_TEXT);

        mTextAlign = Align.parse(parser.getAttributeValue(null, XML_TEXT_ALIGN), mTextAlign);
        mTextColor = parseColor(parser, XML_TEXT_COLOR, mTextColor);
        mTextScale = NumberUtils.toDouble(parser.getAttributeValue(null, XML_TEXT_SCALE), mTextScale);

        mText = XmlPullParserUtils.safeNextText(parser);
        return this;
    }

    @ColorInt
    private static int defaultTextColor(@Nullable final Text text) {
        return Styles.getTextColor(getStylesParent(text));
    }

    @DimenRes
    private static int textSize(@Nullable final Text text) {
        return Styles.getTextSize(getStylesParent(text));
    }

    static void bind(@Nullable final Text text, @Nullable final TextView view) {
        bind(text, view, defaultTextColor(text), DEFAULT_TEXT_ALIGN, textSize(text), DEFAULT_TEXT_SCALE);
    }

    static void bind(@Nullable final Text text, @Nullable final TextView view, @ColorInt final int defaultTextColor,
                     final double defaultTextScale) {
        bind(text, view, defaultTextColor, DEFAULT_TEXT_ALIGN, textSize(text), defaultTextScale);
    }

    static void bind(@Nullable final Text text, @Nullable final TextView view, @NonNull final Align defaultAlign) {
        bind(text, view, defaultTextColor(text), defaultAlign, textSize(text), DEFAULT_TEXT_SCALE);
    }

    static void bind(@Nullable final Text text, @Nullable final TextView view, final double defaultTextScale) {
        bind(text, view, defaultTextColor(text), DEFAULT_TEXT_ALIGN, textSize(text), defaultTextScale);
    }

    static void bind(@Nullable final Text text, @Nullable final TextView view, @ColorInt final int defaultTextColor,
                     @DimenRes final int textSize) {
        bind(text, view, defaultTextColor, DEFAULT_TEXT_ALIGN, textSize, DEFAULT_TEXT_SCALE);
    }

    private static void bind(@Nullable final Text text, @Nullable final TextView view,
                             @ColorInt final int defaultTextColor, @NonNull final Align defaultAlign,
                             @DimenRes final int textSize, final double defaultTextScale) {
        if (view != null) {
            final float size = view.getContext().getResources().getDimension(textSize);
            final Align align;
            if (text != null) {
                view.setText(text.mText);
                view.setTextSize(COMPLEX_UNIT_PX, (float) (size * text.getTextScale(defaultTextScale)));
                view.setTextColor(text.getTextColor(defaultTextColor));
                align = text.getTextAlign(defaultAlign);
            } else {
                view.setText(null);
                view.setTextSize(COMPLEX_UNIT_PX, (float) (size * defaultTextScale));
                view.setTextColor(defaultTextColor);
                align = defaultAlign;
            }

            // set the alignment for the text
            view.setGravity((view.getGravity() & Gravity.VERTICAL_GRAVITY_MASK) | align.mGravity);
        }
    }

    @NonNull
    @Override
    TextViewHolder createViewHolder(@NonNull final ViewGroup parent,
                                    @Nullable final ParentViewHolder parentViewHolder) {
        return new TextViewHolder(parent, parentViewHolder);
    }

    @UiThread
    static final class TextViewHolder extends BaseViewHolder<Text> {
        @BindView(R2.id.content)
        TextView mText;

        TextViewHolder(@NonNull final ViewGroup parent, @Nullable final ParentViewHolder parentViewHolder) {
            super(Text.class, parent, R.layout.tract_content_text, parentViewHolder);
        }

        @Override
        void onBind() {
            super.onBind();
            Text.bind(mModel, mText);
        }
    }
}
