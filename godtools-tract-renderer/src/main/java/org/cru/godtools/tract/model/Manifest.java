package org.cru.godtools.tract.model;

import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.cru.godtools.tract.Constants.XMLNS_MANIFEST;

public class Manifest extends Base {
    private static final String XML_MANIFEST = "manifest";
    private static final String XML_PAGES = "pages";
    private static final String XML_RESOURCES = "resources";

    private List<Page> mPages = new ArrayList<>();
    private List<Resource> mResources = new ArrayList<>();

    @NonNull
    @WorkerThread
    public static Manifest fromXml(@NonNull final XmlPullParser parser) throws XmlPullParserException, IOException {
        final Manifest manifest = new Manifest();
        manifest.parseManifest(parser);
        return manifest;
    }

    private Manifest() {
        super();
    }

    @NonNull
    @Override
    protected Manifest getManifest() {
        return this;
    }

    @WorkerThread
    private void parseManifest(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, XMLNS_MANIFEST, XML_MANIFEST);

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
