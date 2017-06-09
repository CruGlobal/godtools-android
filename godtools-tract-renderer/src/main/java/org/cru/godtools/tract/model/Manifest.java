package org.cru.godtools.tract.model;

import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.annotation.WorkerThread;
import android.support.v4.util.SimpleArrayMap;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.cru.godtools.tract.widget.ScaledPicassoImageView;
import org.cru.godtools.tract.widget.ScaledPicassoImageView.ScaleType;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.cru.godtools.tract.Constants.XMLNS_MANIFEST;
import static org.cru.godtools.tract.model.Utils.parseColor;

public final class Manifest extends Base {
    private static final String XML_MANIFEST = "manifest";
    private static final String XML_TITLE = "title";
    private static final String XML_NAVBAR_COLOR = "navbar-color";
    private static final String XML_NAVBAR_CONTROL_COLOR = "navbar-control-color";
    private static final String XML_PAGES = "pages";
    private static final String XML_RESOURCES = "resources";

    @ColorInt
    private static final int DEFAULT_PRIMARY_COLOR = Color.argb(255, 59, 164, 219);
    @ColorInt
    private static final int DEFAULT_PRIMARY_TEXT_COLOR = Color.WHITE;
    @ColorInt
    private static final int DEFAULT_TEXT_COLOR = Color.argb(255, 90, 90, 90);
    @ColorInt
    private static final int DEFAULT_BACKGROUND_COLOR = Color.WHITE;
    private static final ScaleType DEFAULT_BACKGROUND_IMAGE_SCALE_TYPE = ScaleType.FILL;
    private static final int DEFAULT_BACKGROUND_IMAGE_GRAVITY = ImageGravity.CENTER;

    // XXX: for now we will make this fixed
    @NonNull
    private final String mCode = "placeholder";

    @ColorInt
    private int mPrimaryColor = DEFAULT_PRIMARY_COLOR;
    @ColorInt
    private int mPrimaryTextColor = DEFAULT_PRIMARY_TEXT_COLOR;
    @ColorInt
    private int mTextColor = DEFAULT_TEXT_COLOR;
    @ColorInt
    private int mBackgroundColor = DEFAULT_BACKGROUND_COLOR;
    @Nullable
    private String mBackgroundImage;
    @NonNull
    private ScaleType mBackgroundImageScaleType = DEFAULT_BACKGROUND_IMAGE_SCALE_TYPE;
    private int mBackgroundImageGravity = DEFAULT_BACKGROUND_IMAGE_GRAVITY;

    @Nullable
    @ColorInt
    private Integer mNavBarColor;
    @Nullable
    @ColorInt
    private Integer mNavBarControlColor;

    @Nullable
    private Text mTitle;

    private final List<Page> mPages = new ArrayList<>();
    @VisibleForTesting
    final SimpleArrayMap<String, Resource> mResources = new SimpleArrayMap<>();

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
    String getCode() {
        return mCode;
    }

    @Nullable
    public static String getTitle(@Nullable final Manifest manifest) {
        return manifest != null && manifest.mTitle != null ? manifest.mTitle.getText() : null;
    }

    @NonNull
    public List<Page> getPages() {
        return Collections.unmodifiableList(mPages);
    }

    @Nullable
    @Override
    Resource getResource(@Nullable final String name) {
        if (name != null) {
            return mResources.get(name);
        }

        return null;
    }

    @ColorInt
    int getPrimaryColor() {
        return mPrimaryColor;
    }

    @ColorInt
    public static int getPrimaryColor(@Nullable final Manifest manifest) {
        return manifest != null ? manifest.mPrimaryColor : DEFAULT_PRIMARY_COLOR;
    }

    @ColorInt
    int getPrimaryTextColor() {
        return mPrimaryTextColor;
    }

    @ColorInt
    public static int getPrimaryTextColor(@Nullable final Manifest manifest) {
        return manifest != null ? manifest.mPrimaryTextColor : DEFAULT_PRIMARY_TEXT_COLOR;
    }

    @ColorInt
    public static int getTextColor(@Nullable final Manifest manifest) {
        return manifest != null ? manifest.mTextColor : DEFAULT_TEXT_COLOR;
    }

    @ColorInt
    public static int getBackgroundColor(@Nullable final Manifest manifest) {
        return manifest != null ? manifest.mBackgroundColor : DEFAULT_BACKGROUND_COLOR;
    }

    private static Resource getBackgroundImageResource(@Nullable final Manifest manifest) {
        return manifest != null ? manifest.getResource(manifest.mBackgroundImage) : null;
    }

    private static int getBackgroundImageGravity(@Nullable final Manifest manifest) {
        return manifest != null ? manifest.mBackgroundImageGravity : DEFAULT_BACKGROUND_IMAGE_GRAVITY;
    }

    private static ScaleType getBackgroundImageScaleType(@Nullable final Manifest manifest) {
        return manifest != null ? manifest.mBackgroundImageScaleType : DEFAULT_BACKGROUND_IMAGE_SCALE_TYPE;
    }

    @ColorInt
    public static int getNavBarColor(@Nullable final Manifest manifest) {
        return manifest != null && manifest.mNavBarColor != null ? manifest.mNavBarColor :
                Manifest.getPrimaryColor(manifest);
    }

    @ColorInt
    public static int getNavBarControlColor(@Nullable final Manifest manifest) {
        return manifest != null && manifest.mNavBarControlColor != null ? manifest.mNavBarControlColor :
                Manifest.getPrimaryTextColor(manifest);
    }

    @NonNull
    @WorkerThread
    public static Manifest fromXml(@NonNull final XmlPullParser parser) throws XmlPullParserException, IOException {
        return new Manifest().parse(parser);
    }

    @NonNull
    @WorkerThread
    private Manifest parse(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, XMLNS_MANIFEST, XML_MANIFEST);

        mPrimaryColor = parseColor(parser, XML_PRIMARY_COLOR, mPrimaryColor);
        mPrimaryTextColor = parseColor(parser, XML_PRIMARY_TEXT_COLOR, mPrimaryTextColor);
        mTextColor = parseColor(parser, XML_TEXT_COLOR, mTextColor);
        mBackgroundColor = parseColor(parser, XML_BACKGROUND_COLOR, mBackgroundColor);
        mBackgroundImage = parser.getAttributeValue(null, XML_BACKGROUND_IMAGE);
        mBackgroundImageGravity = ImageGravity.parse(parser, XML_BACKGROUND_IMAGE_GRAVITY, mBackgroundImageGravity);
        mNavBarColor = parseColor(parser, XML_NAVBAR_COLOR, mNavBarColor);
        mNavBarControlColor = parseColor(parser, XML_NAVBAR_CONTROL_COLOR, mNavBarControlColor);

        // process any child elements
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized nodes
            switch (parser.getNamespace()) {
                case XMLNS_MANIFEST:
                    switch (parser.getName()) {
                        case XML_TITLE:
                            mTitle = Text.fromNestedXml(this, parser, XMLNS_MANIFEST, XML_TITLE);
                            continue;
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

        return this;
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
                            mPages.add(Page.fromManifestXml(this, mPages.size(),  parser));
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
                            final Resource resource = Resource.fromXml(this, parser);
                            mResources.put(resource.getName(), resource);
                            continue;
                    }
                    break;
            }

            // skip unrecognized nodes
            XmlPullParserUtils.skipTag(parser);
        }
    }

    public static void bindBackgroundImage(@Nullable final Manifest manifest,
                                           @NonNull final ScaledPicassoImageView view) {
        Resource.bindBackgroundImage(view, getBackgroundImageResource(manifest), getBackgroundImageScaleType(manifest),
                                     getBackgroundImageGravity(manifest));
    }
}
