package org.keynote.godtools.android.snuffy.model;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.annotation.WorkerThread;
import android.view.ViewGroup;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.keynote.godtools.renderer.crureader.bo.GPage.Event.GodToolsEvent;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.keynote.godtools.android.snuffy.ParserUtils.safeParseColor;

public class GtPage extends GtModel {
    static final String XML_PAGE = "page";
    static final String XML_ABOUT = "about";

    private static final String XML_ATTR_FILENAME = "filename";
    private static final String XML_ATTR_THUMBNAIL = "thumb";
    static final String XML_ATTR_LISTENERS = "listeners";

    private static final String XML_ATTR_BACKGROUND = "backgroundimage";
    private static final String XML_ATTR_BACKGROUND_COLOR = "color";
    private static final String XML_ATTR_WATERMARK = "watermark";
    private static final String XML_ATTR_PAGE_SHADOWS = "shadows";

    private static final Pattern PATTERN_FILENAME_UUID =
            Pattern.compile("^([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})\\.xml$",
                            Pattern.CASE_INSENSITIVE);

    private boolean mLoaded = false;

    @NonNull
    private final String mId;
    @VisibleForTesting
    String mFileName;
    private String mThumb;
    private String mDescription;

    /* Background properties */
    @ColorInt
    @Nullable
    private Integer mBackgroundColor;
    @Nullable
    private String mBackground;
    @Nullable
    private String mWatermark;
    private boolean mPageShadows = true;

    @NonNull
    Set<GodToolsEvent.EventID> mListeners = ImmutableSet.of();

    @NonNull
    private final List<GtFollowupModal> mFollowupModals = new ArrayList<>();

    @VisibleForTesting
    GtPage(@NonNull final GtManifest manifest, @NonNull final String uniqueId) {
        super(manifest);
        mId = manifest.getPackageCode() + "-" + uniqueId;
    }

    GtPage(@NonNull final GtFollowupModal modal, @NonNull final String uniqueId) {
        super(modal);
        mId = modal.getId() + "-" + uniqueId;
    }

    @NonNull
    @Override
    public GtPage getPage() {
        return this;
    }

    @NonNull
    public String getId() {
        return mId;
    }

    @Nullable
    public String getUuid() {
        if (mFileName != null) {
            final Matcher matcher = PATTERN_FILENAME_UUID.matcher(mFileName);
            if (matcher.matches()) {
                return matcher.group(1);
            }
        }

        return null;
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

    @ColorInt
    @Nullable
    public Integer getBackgroundColor() {
        return mBackgroundColor;
    }

    @Nullable
    public String getBackground() {
        return mBackground;
    }

    @Nullable
    public String getWatermark() {
        return mWatermark;
    }

    public boolean hasPageShadows() {
        return mPageShadows;
    }

    @NonNull
    public Set<GodToolsEvent.EventID> getListeners() {
        return ImmutableSet.copyOf(mListeners);
    }

    @NonNull
    public List<GtFollowupModal> getFollowupModals() {
        return ImmutableList.copyOf(mFollowupModals);
    }

    @Nullable
    public GtFollowupModal getFollowupModal(@Nullable final String id) {
        for (final GtFollowupModal modal : mFollowupModals) {
            if (modal.getId().equals(id)) {
                return modal;
            }
        }

        return null;
    }

    @Nullable
    @Override
    public ViewHolder render(@NonNull final Context context, @Nullable final ViewGroup parent,
                             final boolean attachToRoot) {
        // TODO: should this actually render the page long term?
        return null;
    }

    @WorkerThread
    static GtPage fromManifestXml(@NonNull final GtManifest manifest, @NonNull final String id,
                                  final XmlPullParser parser)
            throws IOException, XmlPullParserException {
        final GtPage page = new GtPage(manifest, id);
        page.parseManifestXml(parser);
        return page;
    }

    private void parseManifestXml(final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, null);
        XmlPullParserUtils.requireAnyName(parser, XML_PAGE, XML_ABOUT);

        // parse the attributes for this page
        mFileName = parser.getAttributeValue(null, XML_ATTR_FILENAME);
        if (mFileName == null) {
            throw new XmlPullParserException("Package XML does not have a filename defined for a page", parser, null);
        }
        mThumb = parser.getAttributeValue(null, XML_ATTR_THUMBNAIL);
        /*mListeners = ParserUtils
                .parseEvents(parser.getAttributeValue(null, XML_ATTR_LISTENERS), getManifest().getPackageCode());*/

        mDescription = XmlPullParserUtils.safeNextText(parser);
    }

    @WorkerThread
    public void parsePageXml(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        try {
            if (mLoaded) {
                throw new IllegalStateException("Page already loaded!");
            }

            parser.require(XmlPullParser.START_TAG, null, XML_PAGE);

            // handle any local attributes
            mBackgroundColor = safeParseColor(parser.getAttributeValue(null, XML_ATTR_BACKGROUND_COLOR), null);
            mBackground = parser.getAttributeValue(null, XML_ATTR_BACKGROUND);
            mWatermark = parser.getAttributeValue(null, XML_ATTR_WATERMARK);
            // pageShadows is false only if it is set to "no" in the page XML, otherwise default to true
            mPageShadows = !"no".equals(parser.getAttributeValue(null, XML_ATTR_PAGE_SHADOWS));

            // parse any page content
            parseContentXml(parser);
        } finally {
            mLoaded = true;
        }
    }

    void parseContentXml(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        // loop until we reach the matching end tag for this element
        while (parser.next() != XmlPullParser.END_TAG) {
            // skip anything that isn't a start tag for an element
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized elements
            switch (parser.getName()) {
                case GtFollowupModal.XML_FOLLOWUP_MODAL:
                    final GtFollowupModal modal =
                            GtFollowupModal.fromXml(this, Integer.toString(mFollowupModals.size() + 1), parser);
                    mFollowupModals.add(modal);
                    break;
                default:
                    // skip unrecognized nodes
                    XmlPullParserUtils.skipTag(parser);
            }
        }
    }
}
