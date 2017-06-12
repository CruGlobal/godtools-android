package org.cru.godtools.tract.model;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.cru.godtools.base.model.Event;
import org.cru.godtools.tract.R;
import org.cru.godtools.tract.R2;
import org.cru.godtools.tract.model.Text.Align;
import org.jetbrains.annotations.Contract;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Set;

import butterknife.BindView;
import butterknife.OnClick;

import static org.cru.godtools.tract.Constants.XMLNS_CONTENT;
import static org.cru.godtools.tract.model.Text.XML_TEXT;

public final class Button extends Content {
    static final String XML_BUTTON = "button";
    private static final String XML_COLOR = "color";
    private static final String XML_TYPE = "type";
    private static final String XML_TYPE_EVENT = "event";
    private static final String XML_TYPE_URL = "url";
    private static final String XML_URL = "url";
    private static final String XML_EVENTS = "events";

    static final Align DEFAULT_TEXT_ALIGN = Align.CENTER;

    private enum Type {
        EVENT, URL, UNKNOWN;

        static final Type DEFAULT = UNKNOWN;

        @Nullable
        @Contract("_,!null -> !null")
        static Type parse(@Nullable final String type, @Nullable final Type defValue) {
            switch (Strings.nullToEmpty(type)) {
                case XML_TYPE_EVENT:
                    return EVENT;
                case XML_TYPE_URL:
                    return URL;
            }

            return defValue;
        }
    }

    @Nullable
    @ColorInt
    private Integer mColor;

    @NonNull
    Type mType = Type.DEFAULT;
    @NonNull
    Set<Event.Id> mEvents = ImmutableSet.of();
    @Nullable
    Uri mUrl;
    @Nullable
    Text mText;

    private Button(@NonNull final Base parent) {
        super(parent);
    }

    @ColorInt
    public int getColor() {
        return mColor != null ? mColor : Container.getPrimaryColor(getContainer());
    }

    @ColorInt
    static int getColor(@Nullable final Button button) {
        return button != null ? button.getColor() : Container.getPrimaryColor(null);
    }

    @WorkerThread
    static Button fromXml(@NonNull final Base parent, @NonNull final XmlPullParser parser)
            throws IOException, XmlPullParserException {
        return new Button(parent).parse(parser);
    }

    @WorkerThread
    private Button parse(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, XMLNS_CONTENT, XML_BUTTON);

        mColor = Utils.parseColor(parser, XML_COLOR, mColor);
        mType = Type.parse(parser.getAttributeValue(null, XML_TYPE), mType);
        mEvents = parseEvents(parser, XML_EVENTS);
        final String rawUrl = parser.getAttributeValue(null, XML_URL);
        mUrl = rawUrl != null ? Uri.parse(rawUrl) : null;

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
                            mText = Text.fromXml(this, parser);
                            continue;
                    }
                    break;
            }

            // skip unrecognized nodes
            XmlPullParserUtils.skipTag(parser);
        }
        return this;
    }

    @NonNull
    @Override
    View render(@NonNull final LinearLayout parent) {
        final View view =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.tract_content_button, parent, false);

        // create view holder and set the model for it
        new ButtonViewHolder(view).setModel(this);

        return view;
    }

    static final class ButtonViewHolder extends BaseViewHolder<Button> {
        @BindView(R2.id.button)
        TextView mButton;

        ButtonViewHolder(@NonNull final View root) {
            super(root);
        }

        @Override
        void bind() {
            super.bind();
            final Text text = mModel != null ? mModel.mText : null;
            Text.bind(text, mButton, Container.getPrimaryTextColor(getContainer(mModel)), DEFAULT_TEXT_ALIGN);
            ViewCompat.setBackgroundTintList(mButton, ColorStateList.valueOf(getColor(mModel)));
        }

        @OnClick(R2.id.button)
        void click() {
            if (mModel != null) {
                switch (mModel.mType) {
                    case URL:
                        if (mModel.mUrl != null) {
                            mRoot.getContext().startActivity(new Intent(Intent.ACTION_VIEW, mModel.mUrl));
                        }
                        break;
                    case EVENT:
                        sendEvents(mModel.mEvents);
                        break;
                }
            }
        }
    }
}
