package org.cru.godtools.xml.model;

import com.google.common.collect.ImmutableList;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;

import androidx.annotation.NonNull;

import static org.cru.godtools.xml.Constants.XMLNS_CONTENT;

public final class Paragraph extends Content implements Parent {
    static final String XML_PARAGRAPH = "paragraph";

    @NonNull
    private List<Content> mContent = ImmutableList.of();

    private Paragraph(@NonNull final Base parent) {
        super(parent);
    }

    @NonNull
    @Override
    public List<Content> getContent() {
        return mContent;
    }

    @NonNull
    static Paragraph fromXml(@NonNull final Base parent, @NonNull final XmlPullParser parser)
            throws IOException, XmlPullParserException {
        return new Paragraph(parent).parse(parser);
    }

    @NonNull
    private Paragraph parse(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, XMLNS_CONTENT, XML_PARAGRAPH);
        parseAttrs(parser);

        // process any child elements
        final ImmutableList.Builder<Content> contentList = ImmutableList.builder();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // try parsing this child element as a content node
            final Content content = Content.fromXml(this, parser);
            if (content != null) {
                if (!content.isIgnored()) {
                    contentList.add(content);
                }
                continue;
            }

            // skip unrecognized nodes
            XmlPullParserUtils.skipTag(parser);
        }
        mContent = contentList.build();

        return this;
    }
}
