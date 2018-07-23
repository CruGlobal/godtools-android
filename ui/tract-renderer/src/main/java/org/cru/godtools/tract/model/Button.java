package org.cru.godtools.tract.model;

import android.content.Context;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.support.v4.view.ViewCompat;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.cru.godtools.analytics.AnalyticsService;
import org.cru.godtools.base.model.Event;
import org.cru.godtools.base.ui.util.WebUrlLauncher;
import org.cru.godtools.tract.R;
import org.cru.godtools.tract.R2;
import org.cru.godtools.tract.model.AnalyticsEvent.Trigger;
import org.cru.godtools.tract.model.Text.Align;
import org.jetbrains.annotations.Contract;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import butterknife.BindView;
import butterknife.OnClick;

import static org.cru.godtools.tract.Constants.XMLNS_ANALYTICS;
import static org.cru.godtools.tract.Constants.XMLNS_CONTENT;
import static org.cru.godtools.tract.model.Text.XML_TEXT;

public final class Button extends Content implements Styles {
    static final String XML_BUTTON = "button";
    private static final String XML_COLOR = "color";
    private static final String XML_TYPE = "type";
    private static final String XML_TYPE_EVENT = "event";
    private static final String XML_TYPE_URL = "url";
    private static final String XML_URL = "url";

    private static final Align DEFAULT_TEXT_ALIGN = Align.CENTER;

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
    Collection<AnalyticsEvent> mAnalyticsEvents = ImmutableSet.of();

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
    public int getButtonColor() {
        return mColor != null ? mColor : Styles.getButtonColor(getStylesParent());
    }

    @ColorInt
    static int getButtonColor(@Nullable final Button button) {
        return button != null ? button.getButtonColor() : Styles.getButtonColor(null);
    }

    @Override
    public int getTextColor() {
        return getPrimaryTextColor();
    }

    @NonNull
    @Override
    public Align getTextAlign() {
        return DEFAULT_TEXT_ALIGN;
    }

    @WorkerThread
    static Button fromXml(@NonNull final Base parent, @NonNull final XmlPullParser parser)
            throws IOException, XmlPullParserException {
        return new Button(parent).parse(parser);
    }

    @WorkerThread
    private Button parse(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, XMLNS_CONTENT, XML_BUTTON);
        parseAttrs(parser);

        // process any child elements
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized nodes
            switch (parser.getNamespace()) {
                case XMLNS_ANALYTICS:
                    switch (parser.getName()) {
                        case AnalyticsEvent.XML_EVENTS:
                            mAnalyticsEvents = AnalyticsEvent.fromEventsXml(parser);
                            continue;
                    }
                    break;
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

    @Override
    void parseAttrs(@NonNull final XmlPullParser parser) {
        super.parseAttrs(parser);
        mColor = Utils.parseColor(parser, XML_COLOR, mColor);
        mType = Type.parse(parser.getAttributeValue(null, XML_TYPE), mType);
        mEvents = parseEvents(parser, XML_EVENTS);
        mUrl = Utils.parseUrl(parser, XML_URL, null);
    }

    @NonNull
    @Override
    ButtonViewHolder createViewHolder(@NonNull final ViewGroup parent,
                                      @Nullable final BaseViewHolder parentViewHolder) {
        return new ButtonViewHolder(parent, parentViewHolder);
    }

    @UiThread
    static final class ButtonViewHolder extends BaseViewHolder<Button> {
        @BindView(R2.id.button)
        TextView mButton;

        ButtonViewHolder(@NonNull final ViewGroup parent, @Nullable final BaseViewHolder parentViewHolder) {
            super(Button.class, parent, R.layout.tract_content_button, parentViewHolder);
        }

        /* BEGIN lifecycle */

        @Override
        void onBind() {
            super.onBind();
            final Text text = mModel != null ? mModel.mText : null;
            Text.bind(text, mButton);
            ViewCompat.setBackgroundTintList(mButton, ColorStateList.valueOf(getButtonColor(mModel)));
        }

        /* END lifecycle */

        @OnClick(R2.id.button)
        void click() {
            if (mModel != null) {
                triggerAnalyticsEvents(mModel.mAnalyticsEvents, Trigger.SELECTED, Trigger.DEFAULT);

                switch (mModel.mType) {
                    case URL:
                        if (mModel.mUrl != null) {
                            final Context context = mRoot.getContext();
                            AnalyticsService.getInstance(context).onTrackExitUrl(mModel.mUrl);
                            WebUrlLauncher.openUrl(context, mModel.mUrl);
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
