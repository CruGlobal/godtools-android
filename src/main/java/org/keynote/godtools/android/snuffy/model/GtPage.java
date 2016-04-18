package org.keynote.godtools.android.snuffy.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.annotation.WorkerThread;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.keynote.godtools.android.event.GodToolsEvent;
import org.keynote.godtools.android.snuffy.ParserUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GtPage extends GtModel {
    static final String XML_PAGE = "page";
    static final String XML_ABOUT = "about";

    private static final String XML_ATTR_FILENAME = "filename";
    private static final String XML_ATTR_THUMBNAIL = "thumb";
    private static final String XML_ATTR_LISTENERS = "listeners";

    private boolean mLoaded = false;

    @NonNull
    private String mId = "";
    private String mFileName;
    private String mThumb;
    private String mDescription;
    @NonNull
    private Set<GodToolsEvent.EventID> mListeners = ImmutableSet.of();

    @NonNull
    private final List<GtFollowupModal> mFollowupModals = new ArrayList<>();

    @VisibleForTesting
    GtPage(@NonNull final GtManifest manifest) {
        super(manifest);
    }

    public boolean isLoaded() {
        return mLoaded;
    }

    void setId(@NonNull final String id) {
        mId = id;
    }

    @NonNull
    public String getId() {
        return mId;
    }

    public String getFileName() {
        return mFileName;
    }

    public String getThumb() {
        return mThumb;
    }

    public String getDescription() {
        return mDescription;
    }

    @NonNull
    public Set<GodToolsEvent.EventID> getListeners() {
        return mListeners;
    }

    @NonNull
    public List<GtFollowupModal> getFollowupModals() {
        return ImmutableList.copyOf(mFollowupModals);
    }

    @Nullable
    @Override
    public View render(@NonNull final ViewGroup root, final double scale, final boolean attachToRoot) {
        // TODO: should this actually render the page long term?
        return null;
    }

    @WorkerThread
    static GtPage fromManifestXml(@NonNull final GtManifest gtManifest, final XmlPullParser parser)
            throws IOException, XmlPullParserException {
        return new GtPage(gtManifest).parseManifestXml(parser);
    }

    private GtPage parseManifestXml(final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, null);
        XmlPullParserUtils.requireAnyName(parser, XML_PAGE, XML_ABOUT);

        // parse the attributes for this page
        mFileName = parser.getAttributeValue(null, XML_ATTR_FILENAME);
        if (mFileName == null) {
            throw new XmlPullParserException("Package XML does not have a filename defined for a page", parser, null);
        }
        mThumb = parser.getAttributeValue(null, XML_ATTR_THUMBNAIL);
        mListeners = ParserUtils
                .parseEvents(parser.getAttributeValue(null, XML_ATTR_LISTENERS), getManifest().getPackageCode());
        mDescription = XmlPullParserUtils.safeNextText(parser);

        return this;
    }

    @WorkerThread
    public void parsePageXml(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        try {
            if (mLoaded) {
                throw new IllegalStateException("Page already loaded!");
            }

            parser.require(XmlPullParser.START_TAG, null, XML_PAGE);

            // loop until we reach the matching end tag for this element
            while (parser.next() != XmlPullParser.END_TAG) {
                // skip anything that isn't a start tag for an element
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }

                // process recognized elements
                switch (parser.getName()) {
                    case GtFollowupModal.XML_FOLLOWUP_MODAL:
                        final GtFollowupModal modal = GtFollowupModal.fromXml(this, parser);
                        modal.setId(getId() + "-followup-" + Integer.toString(mFollowupModals.size()));
                        mFollowupModals.add(modal);
                        break;
                    default:
                        // skip unrecognized nodes
                        XmlPullParserUtils.skipTag(parser);
                }
            }
        } finally {
            mLoaded = true;
        }
    }
}
