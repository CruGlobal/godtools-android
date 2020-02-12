package org.cru.godtools.xml.model;

import android.graphics.Color;
import android.net.Uri;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.cru.godtools.xml.R;
import org.cru.godtools.xml.model.Text.Align;
import org.jetbrains.annotations.Contract;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.ColorInt;
import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;
import androidx.annotation.WorkerThread;
import androidx.collection.SimpleArrayMap;

import static org.cru.godtools.xml.Constants.XMLNS_ARTICLE;
import static org.cru.godtools.xml.Constants.XMLNS_MANIFEST;
import static org.cru.godtools.xml.model.Utils.parseColor;
import static org.cru.godtools.xml.model.Utils.parseScaleType;
import static org.cru.godtools.xml.model.Utils.parseUrl;

public final class Manifest extends Base implements Styles {
    private static final String XML_MANIFEST = "manifest";
    private static final String XML_TYPE = "type";
    private static final String XML_TYPE_ARTICLE = "article";
    private static final String XML_TYPE_TRACT = "tract";
    private static final String XML_TITLE = "title";
    private static final String XML_NAVBAR_COLOR = "navbar-color";
    private static final String XML_NAVBAR_CONTROL_COLOR = "navbar-control-color";
    private static final String XML_CATEGORY_LABEL_COLOR = "category-label-color";
    private static final String XML_CATEGORIES = "categories";
    private static final String XML_PAGES = "pages";
    private static final String XML_PAGES_AEM_IMPORT = "aem-import";
    private static final String XML_PAGES_AEM_IMPORT_SRC = "src";
    private static final String XML_RESOURCES = "resources";

    @ColorInt
    private static final int DEFAULT_PRIMARY_COLOR = Color.argb(255, 59, 164, 219);
    @ColorInt
    private static final int DEFAULT_PRIMARY_TEXT_COLOR = Color.WHITE;
    @ColorInt
    private static final int DEFAULT_TEXT_COLOR = Color.argb(255, 90, 90, 90);
    @ColorInt
    private static final int DEFAULT_BACKGROUND_COLOR = Color.WHITE;
    private static final ImageScaleType DEFAULT_BACKGROUND_IMAGE_SCALE_TYPE = ImageScaleType.FILL;
    private static final int DEFAULT_BACKGROUND_IMAGE_GRAVITY = ImageGravity.CENTER;

    public enum Type {
        TRACT, ARTICLE, UNKNOWN;

        public static final Type DEFAULT = TRACT;

        @Nullable
        @Contract("_, !null -> !null")
        static Type parse(@Nullable final String value, @Nullable final Type defValue) {
            if (value != null) {
                switch (value) {
                    case XML_TYPE_ARTICLE:
                        return ARTICLE;
                    case XML_TYPE_TRACT:
                        return TRACT;
                    default:
                        return UNKNOWN;
                }
            }
            return defValue;
        }
    }

    @NonNull
    private final String mManifestName;

    // XXX: for now we will make this fixed
    @NonNull
    private final String mCode;
    @NonNull
    private final Locale mLocale;
    @NonNull
    private Type mType = Type.DEFAULT;

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
    private ImageScaleType mBackgroundImageScaleType = DEFAULT_BACKGROUND_IMAGE_SCALE_TYPE;

    @Nullable
    @ColorInt
    private Integer mNavBarColor;
    @Nullable
    @ColorInt
    private Integer mNavBarControlColor;

    @Nullable
    @ColorInt
    private Integer mCategoryLabelColor;

    @Nullable
    private Text mTitle;

    @NonNull
    private List<Category> mCategories = ImmutableList.of();
    @NonNull
    private List<Page> mPages = ImmutableList.of();
    @NonNull
    private List<Uri> mAemImports = ImmutableList.of();
    @VisibleForTesting
    final SimpleArrayMap<String, Resource> mResources = new SimpleArrayMap<>();

    @RestrictTo(RestrictTo.Scope.TESTS)
    public Manifest() {
        this("");
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    Manifest(@NonNull final String toolCode) {
        this("", toolCode, Locale.ENGLISH);
    }

    private Manifest(@NonNull final String manifestName, @NonNull final String toolCode, @NonNull final Locale locale) {
        super();
        mManifestName = manifestName;
        mCode = toolCode;
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

    @NonNull
    public String getCode() {
        return mCode;
    }

    @NonNull
    public Locale getLocale() {
        return mLocale;
    }

    @Nullable
    public String getTitle() {
        return Text.getText(mTitle);
    }

    @NonNull
    public Type getType() {
        return mType;
    }

    @Nullable
    public static String getTitle(@Nullable final Manifest manifest) {
        return manifest != null ? manifest.getTitle() : null;
    }

    @NonNull
    public List<Category> getCategories() {
        return mCategories;
    }

    @NonNull
    public Optional<Category> findCategory(@Nullable final String category) {
        return Stream.of(mCategories)
                .filter(c -> Objects.equal(category, c.getId()))
                .findFirst();
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

    @NonNull
    public List<Uri> getAemImports() {
        return mAemImports;
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
    public static int getPrimaryColor(@Nullable final Manifest manifest) {
        return manifest != null ? manifest.getPrimaryColor() : getDefaultPrimaryColor();
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

    public static Resource getBackgroundImageResource(@Nullable final Manifest manifest) {
        return manifest != null ? manifest.getResource(manifest.mBackgroundImage) : null;
    }

    public static int getBackgroundImageGravity(@Nullable final Manifest manifest) {
        return manifest != null ? manifest.mBackgroundImageGravity : DEFAULT_BACKGROUND_IMAGE_GRAVITY;
    }

    public static ImageScaleType getBackgroundImageScaleType(@Nullable final Manifest manifest) {
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

    @ColorInt
    public static int getCategoryLabelColor(@Nullable final Manifest manifest) {
        return manifest != null && manifest.mCategoryLabelColor != null ? manifest.mCategoryLabelColor :
                Styles.getTextColor(manifest);
    }

    @NonNull
    @WorkerThread
    public static Manifest fromXml(@NonNull final XmlPullParser parser, @NonNull final String manifestName,
                                   @NonNull final String toolCode, @NonNull final Locale locale)
            throws XmlPullParserException, IOException {
        return new Manifest(manifestName, toolCode, locale).parse(parser);
    }

    @NonNull
    @WorkerThread
    private Manifest parse(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, XMLNS_MANIFEST, XML_MANIFEST);

        // tool meta-data
        mType = Type.parse(parser.getAttributeValue(null, XML_TYPE), mType);

        mPrimaryColor = parseColor(parser, XML_PRIMARY_COLOR, mPrimaryColor);
        mPrimaryTextColor = parseColor(parser, XML_PRIMARY_TEXT_COLOR, mPrimaryTextColor);
        mTextColor = parseColor(parser, XML_TEXT_COLOR, mTextColor);
        mBackgroundColor = parseColor(parser, XML_BACKGROUND_COLOR, mBackgroundColor);
        mBackgroundImage = parser.getAttributeValue(null, XML_BACKGROUND_IMAGE);
        mBackgroundImageGravity = ImageGravity.parse(parser, XML_BACKGROUND_IMAGE_GRAVITY, mBackgroundImageGravity);
        mBackgroundImageScaleType = parseScaleType(parser, XML_BACKGROUND_IMAGE_SCALE_TYPE, mBackgroundImageScaleType);
        mNavBarColor = parseColor(parser, XML_NAVBAR_COLOR, mNavBarColor);
        mNavBarControlColor = parseColor(parser, XML_NAVBAR_CONTROL_COLOR, mNavBarControlColor);
        mCategoryLabelColor = parseColor(parser, XML_CATEGORY_LABEL_COLOR, mCategoryLabelColor);

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
                        case XML_CATEGORIES:
                            parseCategories(parser);
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

    private void parseCategories(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, XMLNS_MANIFEST, XML_CATEGORIES);

        final List<Category> categories = new ArrayList<>();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized nodes
            switch (parser.getNamespace()) {
                case XMLNS_MANIFEST:
                    switch (parser.getName()) {
                        case Category.XML_CATEGORY:
                            categories.add(Category.fromXml(this, parser));
                            continue;
                    }
                    break;
            }

            // skip unrecognized nodes
            XmlPullParserUtils.skipTag(parser);
        }
        mCategories = ImmutableList.copyOf(categories);
    }

    @WorkerThread
    private void parsePages(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, XMLNS_MANIFEST, XML_PAGES);

        // process any child elements
        final List<Uri> aemImports = new ArrayList<>();
        final List<Page> pages = new ArrayList<>();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized nodes
            switch (parser.getNamespace()) {
                case XMLNS_MANIFEST:
                    switch (parser.getName()) {
                        case Page.XML_PAGE:
                            pages.add(Page.fromManifestXml(this, pages.size(), parser));
                            continue;
                    }
                    break;
                case XMLNS_ARTICLE:
                    switch (parser.getName()) {
                        case XML_PAGES_AEM_IMPORT:
                            final Uri url = parseUrl(parser, XML_PAGES_AEM_IMPORT_SRC, null);
                            if (url != null) {
                                aemImports.add(url);
                            }
                            XmlPullParserUtils.skipTag(parser);
                            continue;
                    }
                    break;
            }

            // skip unrecognized nodes
            XmlPullParserUtils.skipTag(parser);
        }
        mPages = ImmutableList.copyOf(pages);
        mAemImports = ImmutableList.copyOf(aemImports);
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
}
