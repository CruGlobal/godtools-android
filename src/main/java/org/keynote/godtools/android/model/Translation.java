package org.keynote.godtools.android.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.ccci.gto.android.common.jsonapi.annotation.JsonApiAttribute;
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiIgnore;
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiType;

import java.util.Locale;

import static org.keynote.godtools.android.model.Translation.JSON_API_TYPE;

@JsonApiType(JSON_API_TYPE)
public class Translation extends Base {
    static final String JSON_API_TYPE = "translation";
    private static final String JSON_RESOURCE = "resource";
    public static final String JSON_LANGUAGE = "language";
    private static final String JSON_VERSION = "version";
    private static final String JSON_IS_PUBLISHED = "is-published";
    private static final String JSON_NAME = "translated-name";
    private static final String JSON_DESCRIPTION = "translated-description";

    public static final boolean DEFAULT_PUBLISHED = false;
    public static final int DEFAULT_VERSION = 0;

    @Nullable
    @JsonApiIgnore
    private Long mResourceId;
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
    @JsonApiAttribute(name = JSON_NAME)
    private String mName;
    @Nullable
    @JsonApiAttribute(name = JSON_DESCRIPTION)
    private String mDescription;

    @JsonApiAttribute(name = JSON_IS_PUBLISHED)
    private boolean mPublished = DEFAULT_PUBLISHED;

    @JsonApiIgnore
    private boolean mDownloaded = false;

    public long getResourceId() {
        return mResourceId != null && mResourceId != Tool.INVALID_ID ? mResourceId :
                mTool != null ? mTool.getId() : Tool.INVALID_ID;
    }

    public void setResourceId(@Nullable final Long resourceId) {
        mResourceId = resourceId;
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
}
