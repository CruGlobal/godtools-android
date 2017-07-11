package org.cru.godtools.tract.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.ViewGroup;

import com.annimon.stream.Stream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;

import static org.cru.godtools.tract.Constants.XMLNS_CONTENT;
import static org.cru.godtools.tract.model.Button.XML_BUTTON;
import static org.cru.godtools.tract.model.Form.XML_FORM;
import static org.cru.godtools.tract.model.Image.XML_IMAGE;
import static org.cru.godtools.tract.model.Input.XML_INPUT;
import static org.cru.godtools.tract.model.Link.XML_LINK;
import static org.cru.godtools.tract.model.Paragraph.XML_PARAGRAPH;
import static org.cru.godtools.tract.model.Tabs.XML_TABS;
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
            case XMLNS_CONTENT:
                switch (parser.getName()) {
                    case XML_PARAGRAPH:
                        return Paragraph.fromXml(parent, parser);
                    case XML_TABS:
                        return Tabs.fromXml(parent, parser);
                    case XML_TEXT:
                        return Text.fromXml(parent, parser);
                    case XML_IMAGE:
                        return Image.fromXml(parent, parser);
                    case XML_BUTTON:
                        return Button.fromXml(parent, parser);
                    case XML_FORM:
                        return Form.fromXml(parent, parser);
                    case XML_INPUT:
                        return Input.fromXml(parent, parser);
                    case XML_LINK:
                        return Link.fromXml(parent, parser);
                }
        }

        return null;
    }

    @NonNull
    abstract BaseViewHolder createViewHolder(@NonNull ViewGroup parent, @Nullable BaseViewHolder parentViewHolder);

    static void renderAll(@NonNull final ViewGroup parent, @NonNull final List<? extends Content> content) {
        Stream.of(content)
                .map(c -> {
                    final BaseViewHolder holder = c.createViewHolder(parent, null);
                    //noinspection unchecked
                    holder.bind(c);
                    return holder;
                })
                .map(vh -> vh.mRoot)
                .forEach(parent::addView);
    }
}
