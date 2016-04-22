package org.keynote.godtools.android.snuffy.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.annotation.WorkerThread;
import android.support.v4.util.SimpleArrayMap;
import android.view.ViewGroup;

import com.google.common.collect.ImmutableList;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.keynote.godtools.android.BuildConfig;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GtManifest extends GtModel {
    private static final String XML_PACKAGE = "document";
    private static final String XML_TITLE = "packagename";
    private static final String XML_TITLE_LEGACY = "displayname";

    private String mTitle;
    private GtPage mAbout;
    private final List<GtPage> mPages = new ArrayList<>();
    private final SimpleArrayMap<String, GtPage> mPagesIndex = new SimpleArrayMap<>();

    @NonNull
    private final String mPackageCode;

    @VisibleForTesting
    GtManifest(@NonNull final String code) {
        mPackageCode = code;
    }

    @NonNull
    @Override
    public GtManifest getManifest() {
        return this;
    }

    @Nullable
    @Override
    public GtPage getPage() {
        if (BuildConfig.DEBUG) {
            throw new IllegalStateException("It is impossible for a manifest to be a child of a page");
        }

        return null;
    }

    public String getTitle() {
        return mTitle;
    }

    public GtPage getAbout() {
        return mAbout;
    }

    @Nullable
    public GtPage getPage(@Nullable final String pageId) {
        return mPagesIndex.get(pageId);
    }

    public List<GtPage> getPages() {
        return ImmutableList.copyOf(mPages);
    }

    @NonNull
    public String getPackageCode() {
        return mPackageCode;
    }

    @Nullable
    @Override
    public ViewHolder render(@NonNull final Context context, @Nullable final ViewGroup parent,
                             final boolean attachToRoot) {
        if (BuildConfig.DEBUG) {
            throw new IllegalStateException("You cannot render a GtManifest!!!!");
        }
        // you can't render a GtManifest
        return null;
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
                    mAbout.setId(mPackageCode + "-about");
                    mPagesIndex.put(mAbout.getId(), mAbout);
                    continue;
                case GtPage.XML_PAGE:
                    final GtPage page = GtPage.fromManifestXml(this, parser);
                    page.setId(mPackageCode + "-" + Integer.toString(mPages.size()));
                    mPages.add(page);
                    mPagesIndex.put(page.getId(), mAbout);
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
