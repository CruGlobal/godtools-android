package org.cru.godtools.tract.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.LinearLayout;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import static org.cru.godtools.tract.Constants.XMLNS_CONTENT;
import static org.cru.godtools.tract.Constants.XMLNS_TRACT;
import static org.cru.godtools.tract.model.Paragraph.XML_PARAGRAPH;
import static org.cru.godtools.tract.model.Text.XML_TEXT;

public abstract class Content extends Base {
    Content(@NonNull final Base parent) {
        super(parent);
    }

    @Nullable
    static Content fromXml(@NonNull final Base parent, @NonNull final XmlPullParser parser)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, null);

        switch (parser.getNamespace()) {
            case XMLNS_TRACT:
                switch (parser.getName()) {
                    case XML_PARAGRAPH:
                        return Paragraph.fromXml(parent, parser);
                }
                break;
            case XMLNS_CONTENT:
                switch (parser.getName()) {
                    case XML_TEXT:
                        return Text.fromXml(parent, parser);
                }
        }

        return null;
    }

    @NonNull
    public abstract View render(@NonNull final LinearLayout container);
}
