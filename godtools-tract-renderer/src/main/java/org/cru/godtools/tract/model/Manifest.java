package org.cru.godtools.tract.model;

import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.annotation.WorkerThread;
import android.support.v4.util.SimpleArrayMap;

import com.annimon.stream.Stream;
import com.google.common.collect.ImmutableList;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.cru.godtools.tract.R;
import org.cru.godtools.tract.model.Text.Align;
import org.cru.godtools.tract.widget.ScaledPicassoImageView;
import org.cru.godtools.tract.widget.ScaledPicassoImageView.ScaleType;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static org.cru.godtools.tract.Constants.XMLNS_MANIFEST;
import static org.cru.godtools.tract.model.Utils.parseColor;
import static org.cru.godtools.tract.model.Utils.parseScaleType;

public final class Manifest extends Base implements Styles {
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

    @NonNull
    private final String mManifestName;

    // XXX: for now we will make this fixed
    @NonNull
    private final String mCode = "kgp";
    @Deprecated
    private final long mToolId;
    @NonNull
    private final Locale mLocale;

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
    private int mBackgroundImageGravity = DEFAULT_BACKGROUND_IMAGE_GRAVITY;
    @NonNull
    private ScaleType mBackgroundImageScaleType = DEFAULT_BACKGROUND_IMAGE_SCALE_TYPE;

    @Nullable
    @ColorInt
    private Integer mNavBarColor;
    @Nullable
    @ColorInt
    private Integer mNavBarControlColor;

    @Nullable
    private Text mTitle;

    @NonNull
    private List<Page> mPages = ImmutableList.of();
    @VisibleForTesting
    final SimpleArrayMap<String, Resource> mResources = new SimpleArrayMap<>();

    @VisibleForTesting
    Manifest(@NonNull final String manifestName, final long toolId, @NonNull final Locale locale) {
        super();
        mManifestName = manifestName;
        mToolId = toolId;
        mLocale = locale;
    }

    @NonNull
    @Override
    public Manifest getManifest() {
        return this;
    }

    @NonNull
    public String getManifestName() {
        return mManifestName;
    }

    @Deprecated
    public long getToolId() {
        return mToolId;
    }

    @NonNull
    String getCode() {
        return mCode;
    }

    @NonNull
    public Locale getLocale() {
        return mLocale;
    }

    @Nullable
    public static String getTitle(@Nullable final Manifest manifest) {
        return manifest != null && manifest.mTitle != null ? manifest.mTitle.getText() : null;
    }

    @NonNull
    public List<Page> getPages() {
        return mPages;
    }

    @Nullable
    public Page findPage(@Nullable final String id) {
        return Stream.of(mPages)
                .filter(p -> p.getId().equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);
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
    @Override
    public int getPrimaryColor() {
        return mPrimaryColor;
    }

    @ColorInt
    static int getDefaultPrimaryColor() {
        return DEFAULT_PRIMARY_COLOR;
    }

    @ColorInt
    @Override
    public int getPrimaryTextColor() {
        return mPrimaryTextColor;
    }

    @ColorInt
    static int getDefaultPrimaryTextColor() {
        return DEFAULT_PRIMARY_TEXT_COLOR;
    }

    @ColorInt
    @Override
    public int getTextColor() {
        return mTextColor;
    }

    @ColorInt
    static int getDefaultTextColor() {
        return DEFAULT_TEXT_COLOR;
    }

    @DimenRes
    static int getDefaultTextSize() {
        return R.dimen.text_size_base;
    }

    @NonNull
    static Align getDefaultTextAlign() {
        return Align.DEFAULT;
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
                Styles.getPrimaryColor(manifest);
    }

    @ColorInt
    public static int getNavBarControlColor(@Nullable final Manifest manifest) {
        return manifest != null && manifest.mNavBarControlColor != null ? manifest.mNavBarControlColor :
                Styles.getPrimaryTextColor(manifest);
    }

    @NonNull
    @WorkerThread
    public static Manifest fromXml(@NonNull final XmlPullParser parser, @NonNull final String manifestName,
                                   final long toolId, @NonNull final Locale locale)
            throws XmlPullParserException, IOException {
        return new Manifest(manifestName, toolId, locale).parse(parser);
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
        mBackgroundImageScaleType = parseScaleType(parser, XML_BACKGROUND_IMAGE_SCALE_TYPE, mBackgroundImageScaleType);
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
        final ImmutableList.Builder<Page> pages = ImmutableList.builder();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized nodes
            switch (parser.getNamespace()) {
                case XMLNS_MANIFEST:
                    switch (parser.getName()) {
                        case Page.XML_PAGE:
                            pages.add(Page.fromManifestXml(this, mPages.size(), parser));
                            continue;
                    }
                    break;
            }

            // skip unrecognized nodes
            XmlPullParserUtils.skipTag(parser);
        }
        mPages = pages.build();
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
