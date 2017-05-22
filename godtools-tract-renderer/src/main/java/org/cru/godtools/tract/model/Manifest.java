package org.cru.godtools.tract.model;

import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.annotation.WorkerThread;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.cru.godtools.tract.util.ParserUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.cru.godtools.tract.Constants.XMLNS_MANIFEST;

public final class Manifest extends Base {
    private static final String XML_MANIFEST = "manifest";
    private static final String XML_PAGES = "pages";
    private static final String XML_RESOURCES = "resources";
    private static final String XML_BACKGROUND_COLOR = "background-color";

    @ColorInt
    private static final int DEFAULT_PRIMARY_COLOR = Color.argb(255, 59, 164, 219);
    @ColorInt
    private static final int DEFAULT_PRIMARY_TEXT_COLOR = Color.WHITE;
    @ColorInt
    private static final int DEFAULT_TEXT_COLOR = Color.argb(255, 90, 90, 90);
    @ColorInt
    public static final int DEFAULT_BACKGROUND_COLOR = Color.WHITE;

    @ColorInt
    private int mPrimaryColor = DEFAULT_PRIMARY_COLOR;
    @ColorInt
    private int mPrimaryTextColor = DEFAULT_PRIMARY_TEXT_COLOR;
    @ColorInt
    private int mTextColor = DEFAULT_TEXT_COLOR;
    @ColorInt
    private int mBackgroundColor = DEFAULT_BACKGROUND_COLOR;

    private final List<Page> mPages = new ArrayList<>();
    @VisibleForTesting
    final List<Resource> mResources = new ArrayList<>();

    @NonNull
    @WorkerThread
    public static Manifest fromXml(@NonNull final XmlPullParser parser) throws XmlPullParserException, IOException {
        final Manifest manifest = new Manifest();
        manifest.parseManifest(parser);
        return manifest;
    }

    @VisibleForTesting
    Manifest() {
        super();
    }

    @NonNull
    @Override
    protected Manifest getManifest() {
        return this;
    }

    @NonNull
    public List<Page> getPages() {
        return Collections.unmodifiableList(mPages);
    }

    @ColorInt
    public int getPrimaryColor() {
        return mPrimaryColor;
    }

    @ColorInt
    public int getPrimaryTextColor() {
        return mPrimaryTextColor;
    }

    @ColorInt
    public int getTextColor() {
        return mTextColor;
    }

    @ColorInt
    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    @WorkerThread
    private void parseManifest(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, XMLNS_MANIFEST, XML_MANIFEST);

        mBackgroundColor = ParserUtils.parseColor(parser.getAttributeValue(null, XML_BACKGROUND_COLOR), mBackgroundColor);

        // process any child elements
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized nodes
            switch (parser.getNamespace()) {
                case XMLNS_MANIFEST:
                    switch (parser.getName()) {
                        case XML_PAGES:
                            parsePages(parser);
                            continue;
                        case XML_RESOURCES:
                            parseResources(parser);
                            continue;
                    }
                    break;
            }

            // skip unrecognized nodes
            XmlPullParserUtils.skipTag(parser);
        }
    }

    @WorkerThread
    private void parsePages(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, XMLNS_MANIFEST, XML_PAGES);

        // process any child elements
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized nodes
            switch (parser.getNamespace()) {
                case XMLNS_MANIFEST:
                    switch (parser.getName()) {
                        case Page.XML_PAGE:
                            mPages.add(Page.fromManifestXml(this, parser));
                            continue;
                    }
                    break;
            }

            // skip unrecognized nodes
            XmlPullParserUtils.skipTag(parser);
        }
    }

    @WorkerThread
    private void parseResources(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, XMLNS_MANIFEST, XML_RESOURCES);

        // process any child elements
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized nodes
            switch (parser.getNamespace()) {
                case XMLNS_MANIFEST:
                    switch (parser.getName()) {
                        case Resource.XML_RESOURCE:
                            mResources.add(Resource.fromXml(this, parser));
                            continue;
                    }
                    break;
            }

            // skip unrecognized nodes
            XmlPullParserUtils.skipTag(parser);
        }
    }
}
