package org.cru.godtools.model;

import org.ccci.gto.android.common.jsonapi.annotation.JsonApiAttribute;
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiIgnore;
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiType;

import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static org.cru.godtools.model.Translation.JSON_API_TYPE;

@JsonApiType(JSON_API_TYPE)
public class Translation extends Base {
    static final String JSON_API_TYPE = "translation";
    private static final String JSON_RESOURCE = "resource";
    public static final String JSON_LANGUAGE = "language";
    private static final String JSON_VERSION = "version";
    private static final String JSON_IS_PUBLISHED = "is-published";
    private static final String JSON_MANIFEST = "manifest-name";
    private static final String JSON_NAME = "translated-name";
    private static final String JSON_DESCRIPTION = "translated-description";
    private static final String JSON_TAGLINE = "translated-tagline";

    public static final boolean DEFAULT_PUBLISHED = false;
    public static final int DEFAULT_VERSION = 0;
    public static final Date DEFAULT_LAST_ACCESSED = new Date(0);

    @Nullable
    @JsonApiIgnore
    private String mToolCode;
    @Nullable
    @JsonApiAttribute(name = JSON_RESOURCE)
    private Tool mTool;
    @Nullable
    @JsonApiIgnore
    private Locale mLanguageCode;
    @Nullable
    @JsonApiAttribute(name = JSON_LANGUAGE)
    private Language mLanguage;
    @JsonApiAttribute(name = JSON_VERSION)
    private int mVersion = DEFAULT_VERSION;

    @Nullable
    @JsonApiAttribute(name = JSON_MANIFEST)
    private String mManifestFileName;

    @Nullable
    @JsonApiAttribute(name = JSON_NAME)
    private String mName;
    @Nullable
    @JsonApiAttribute(name = JSON_DESCRIPTION)
    private String mDescription;
    @Nullable
    @JsonApiAttribute(name = JSON_TAGLINE)
    private String mTagline;

    @JsonApiAttribute(name = JSON_IS_PUBLISHED)
    private boolean mPublished = DEFAULT_PUBLISHED;

    @JsonApiIgnore
    private boolean mDownloaded = false;
    @NonNull
    @JsonApiIgnore
    private Date mLastAccessed = DEFAULT_LAST_ACCESSED;

    @Nullable
    public String getToolCode() {
        return mToolCode != null ? mToolCode :
                mTool != null ? mTool.getCode() : Tool.INVALID_CODE;
    }

    public void setToolCode(@Nullable final String code) {
        mToolCode = code;
    }

    @NonNull
    public Locale getLanguageCode() {
        return mLanguageCode != null && !mLanguageCode.equals(Language.INVALID_CODE) ? mLanguageCode :
                mLanguage != null ? mLanguage.getCode() : Language.INVALID_CODE;
    }

    @Nullable
    public Language getLanguage() {
        return mLanguage;
    }

    public void setLanguageCode(@Nullable Locale code) {
        mLanguageCode = code;
    }

    public int getVersion() {
        return mVersion;
    }

    public void setVersion(final int version) {
        mVersion = version;
    }

    @Nullable
    public String getManifestFileName() {
        return mManifestFileName;
    }

    public void setManifestFileName(@Nullable final String name) {
        mManifestFileName = name;
    }

    @Nullable
    public String getName() {
        return mName;
    }

    public void setName(@Nullable final String name) {
        mName = name;
    }

    @Nullable
    public String getDescription() {
        return mDescription;
    }

    public void setDescription(@Nullable final String description) {
        mDescription = description;
    }

    @Nullable
    public String getTagline() {
        return mTagline;
    }

    public void setTagline(@Nullable final String tagline) {
        mTagline = tagline;
    }

    public boolean isPublished() {
        return mPublished;
    }

    public void setPublished(final boolean published) {
        mPublished = published;
    }

    public boolean isDownloaded() {
        return mDownloaded;
    }

    public void setDownloaded(final boolean state) {
        mDownloaded = state;
    }

    @NonNull
    public Date getLastAccessed() {
        return mLastAccessed;
    }

    public void setLastAccessed() {
        setLastAccessed(new Date());
    }

    public void setLastAccessed(@NonNull final Date lastAccessed) {
        mLastAccessed = lastAccessed;
    }
}
