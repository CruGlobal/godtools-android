package org.keynote.godtools.android.snuffy.model;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.annotation.WorkerThread;

import com.google.common.collect.ImmutableList;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GtManifest {
    private static final String XML_PACKAGE = "document";
    private static final String XML_TITLE = "packagename";
    private static final String XML_TITLE_LEGACY = "displayname";

    private String mTitle;
    private GtPage mAbout;
    private final List<GtPage> mPages = new ArrayList<>();

    private final String mAppPackage;

    @VisibleForTesting
    private GtManifest(@NonNull final String appPackage) {
        this.mAppPackage = appPackage;
    }

    public String getTitle() {
        return mTitle;
    }

    public GtPage getAbout() {
        return mAbout;
    }

    public List<GtPage> getPages() {
        return ImmutableList.copyOf(mPages);
    }

    public String getAppPackage() {
        return mAppPackage;
    }

    @NonNull
    @WorkerThread
    public static GtManifest fromXml(final XmlPullParser parser, @NonNull final String appPackage) throws IOException,
            XmlPullParserException {
        return new GtManifest(appPackage).parse(parser);
    }

    private GtManifest parse(final XmlPullParser parser) throws IOException, XmlPullParserException {
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
                        throw new XmlPullParserException("Package XML has more than 1 title defined", parser, null);
                    }
                    mTitle = XmlPullParserUtils.safeNextText(parser);
                    continue;
                case GtPage.XML_ABOUT:
                    if (mAbout != null) {
                        throw new XmlPullParserException("Package XML has more than 1 about page defined", parser,
                                                         null);
                    }
                    mAbout = GtPage.fromManifestXml(this, parser);
                    continue;
                case GtPage.XML_PAGE:
                    mPages.add(GtPage.fromManifestXml(this, parser));
                    continue;
                default:
                    // skip unrecognized nodes
                    XmlPullParserUtils.skipTag(parser);
            }
        }

        // validate state of the parsed Package
        if (mTitle == null) {
            throw new XmlPullParserException("Package XML does not have a title defined");
        }
        if (mAbout == null) {
            throw new XmlPullParserException("Package XML does not have an about page defined");
        }

        return this;
    }
}
