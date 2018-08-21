package org.cru.godtools.tract.model;

import android.support.annotation.NonNull;

import com.google.common.collect.ImmutableList;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.cru.godtools.tract.model.Tab.XML_TAB;
import static org.cru.godtools.xml.Constants.XMLNS_CONTENT;

public final class Tabs extends Content {
    static final String XML_TABS = "tabs";

    @NonNull
    private List<Tab> mTabs = ImmutableList.of();

    private Tabs(@NonNull final Base parent) {
        super(parent);
    }

    @NonNull
    public List<Tab> getTabs() {
        return mTabs;
    }

    @NonNull
    static Tabs fromXml(@NonNull final Base parent, @NonNull final XmlPullParser parser)
            throws IOException, XmlPullParserException {
        return new Tabs(parent).parse(parser);
    }

    @NonNull
    private Tabs parse(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, XMLNS_CONTENT, XML_TABS);
        parseAttrs(parser);

        // process any child elements
        final List<Tab> tabs = new ArrayList<>();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized nodes
            switch (parser.getNamespace()) {
                case XMLNS_CONTENT:
                    switch (parser.getName()) {
                        case XML_TAB:
                            tabs.add(Tab.fromXml(this, parser, tabs.size()));
                            continue;
                    }
                    break;
            }

            // skip unrecognized nodes
            XmlPullParserUtils.skipTag(parser);
        }
        mTabs = ImmutableList.copyOf(tabs);

        return this;
    }
}
