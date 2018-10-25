package org.cru.godtools.xml.model;

import com.google.common.collect.ImmutableSet;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import static org.cru.godtools.xml.Constants.XMLNS_ARTICLE;
import static org.cru.godtools.xml.Constants.XMLNS_MANIFEST;

public class Category extends Base {
    static final String XML_CATEGORY = "category";
    private static final String XML_AEM_TAG = "aem-tag";
    private static final String XML_ID = "id";
    private static final String XML_LABEL = "label";
    private static final String XML_BANNER = "banner";

    @Nullable
    private String mId;
    @Nullable
    private Text mLabel;
    @Nullable
    private String mBanner;

    @NonNull
    private Set<String> mAemTags = ImmutableSet.of();

    @NonNull
    @WorkerThread
    static Category fromXml(@NonNull final Manifest manifest, @NonNull final XmlPullParser parser)
            throws XmlPullParserException, IOException {
        return new Category(manifest).parse(parser);
    }

    private Category(@NonNull final Manifest manifest) {
        super(manifest);
    }

    @Nullable
    public String getId() {
        return mId;
    }

    @Nullable
    public Text getLabel() {
        return mLabel;
    }

    @Nullable
    public Resource getBanner() {
        return getResource(mBanner);
    }

    @NonNull
    public Set<String> getAemTags() {
        return mAemTags;
    }

    @WorkerThread
    private Category parse(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, XMLNS_MANIFEST, XML_CATEGORY);

        mId = parser.getAttributeValue(null, XML_ID);
        mBanner = parser.getAttributeValue(null, XML_BANNER);

        final List<String> aemTags = new ArrayList<>();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized nodes
            switch (parser.getNamespace()) {
                case XMLNS_MANIFEST:
                    switch (parser.getName()) {
                        case XML_LABEL:
                            mLabel = Text.fromNestedXml(this, parser, XMLNS_MANIFEST, XML_LABEL);
                            continue;
                    }
                    break;
                case XMLNS_ARTICLE:
                    switch (parser.getName()) {
                        case XML_AEM_TAG:
                            aemTags.add(parser.getAttributeValue(null, XML_ID));
                            break;
                    }
                    break;
            }
            mAemTags = ImmutableSet.copyOf(aemTags);

            // skip unrecognized nodes
            XmlPullParserUtils.skipTag(parser);
        }

        return this;
    }
}
