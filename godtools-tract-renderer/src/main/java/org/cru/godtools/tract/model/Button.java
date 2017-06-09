package org.cru.godtools.tract.model;

import android.content.res.ColorStateList;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.cru.godtools.tract.R;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import butterknife.ButterKnife;

import static org.cru.godtools.tract.Constants.XMLNS_CONTENT;
import static org.cru.godtools.tract.model.Text.XML_TEXT;

public final class Button extends Content {
    static final String XML_BUTTON = "button";
    private static final String XML_COLOR = "color";

    @Nullable
    @ColorInt
    private Integer mColor;

    @Nullable
    private Text mText;

    private Button(@NonNull final Base parent) {
        super(parent);
    }

    @ColorInt
    public int getColor() {
        return mColor != null ? mColor : Container.getPrimaryColor(getContainer());
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

        // update button styling
        final AppCompatButton button = ButterKnife.findById(view, R.id.button);
        ViewCompat.setBackgroundTintList(button, ColorStateList.valueOf(getColor()));
        Text.bind(mText, button, Container.getPrimaryTextColor(getContainer()));

        return view;
    }
}
