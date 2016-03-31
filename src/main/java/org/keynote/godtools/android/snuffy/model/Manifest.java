package org.keynote.godtools.android.snuffy.model;

import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import com.google.common.collect.ImmutableList;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Manifest {
    private static final String XML_PACKAGE = "document";
    private static final String XML_TITLE = "packagename";
    private static final String XML_TITLE_LEGACY = "displayname";
    private static final String XML_ABOUT = "about";
    private static final String XML_PAGE = "page";
    private static final String XML_PAGE_FILENAME = "filename";
    private static final String XML_PAGE_THUMBNAIL = "thumb";

    private String mTitle;
    private Page mAbout;
    private final List<Page> mPages = new ArrayList<>();

    private Manifest() {}

    public String getTitle() {
        return mTitle;
    }

    public Page getAbout() {
        return mAbout;
    }

    public List<Page> getPages() {
        return ImmutableList.copyOf(mPages);
    }

    @NonNull
    @WorkerThread
    public static Manifest fromXml(final XmlPullParser parser) throws IOException, XmlPullParserException {
        return new Manifest().parse(parser);
    }

    private Manifest parse(final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, XML_PACKAGE);

        // loop until we reach the matching end tag for this element
        while (parser.next() != XmlPullParser.END_TAG) {
            // skip anything that isn't a start tag for an element
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized elements
            switch (parser.getName()) {
                case XML_TITLE:
                case XML_TITLE_LEGACY:
                    if (mTitle != null) {
                        // throw an error if we already parsed a title
                        throw new IllegalStateException("Package XML has more than 1 title defined");
                    }
                    mTitle = XmlPullParserUtils.safeNextText(parser);
                    continue;
                case XML_ABOUT:
                    if (mAbout != null) {
                        throw new IllegalStateException("Package XML has more than 1 about pages defined");
                    }
                    mAbout = Page.fromXml(parser, XML_ABOUT);
                    continue;
                case XML_PAGE:
                    mPages.add(Page.fromXml(parser, XML_PAGE));
                    continue;
                default:
                    // skip unrecognized nodes
                    XmlPullParserUtils.skipTag(parser);
            }
        }

        // validate state of the parsed Package
        if (mTitle == null) {
            throw new IllegalStateException("Package XML does not have a title defined");
        }
        if (mAbout == null) {
            throw new IllegalStateException("Package XML does not have an about page defined");
        }

        return this;
    }

    public static class Page {
        private String mFileName;
        private String mThumb;
        private String mDescription;
        private Set<String> mListeners;

        private Page() {}

        public String getFileName() {
            return mFileName;
        }

        public String getThumb() {
            return mThumb;
        }

        public String getDescription() {
            return mDescription;
        }

        static Page fromXml(final XmlPullParser parser, @NonNull final String type)
                throws IOException, XmlPullParserException {
            return new Page().parse(parser, type);
        }

        private Page parse(final XmlPullParser parser, @NonNull final String type)
                throws IOException, XmlPullParserException {
            parser.require(XmlPullParser.START_TAG, null, type);

            // parse the attributes for this page
            mFileName = parser.getAttributeValue(null, XML_PAGE_FILENAME);
            mThumb = parser.getAttributeValue(null, XML_PAGE_THUMBNAIL);
            // TODO: listeners
            mDescription = XmlPullParserUtils.safeNextText(parser);

            if (mFileName == null) {
                throw new IllegalStateException("Package XML does not have a filename defined for a page");
            }

            return this;
        }
    }
}
