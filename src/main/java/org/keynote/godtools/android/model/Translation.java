package org.keynote.godtools.android.model;

import android.support.annotation.Nullable;

import org.ccci.gto.android.common.jsonapi.annotation.JsonApiAttribute;
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiIgnore;
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiType;

import static org.keynote.godtools.android.model.Translation.JSON_API_TYPE;

@JsonApiType(JSON_API_TYPE)
public class Translation extends Base {
    static final String JSON_API_TYPE = "translation";
    private static final String JSON_RESOURCE = "resource";
    public static final String JSON_LANGUAGE = "language";
    private static final String JSON_VERSION = "version";
    private static final String JSON_IS_PUBLISHED = "is-published";

    public static final boolean DEFAULT_PUBLISHED = false;
    public static final int DEFAULT_VERSION = 0;

    @Nullable
    @JsonApiIgnore
    private Long mResourceId;
    @Nullable
    @JsonApiAttribute(name = JSON_RESOURCE)
    private Resource mResource;
    @Nullable
    @JsonApiIgnore
    private Long mLanguageId;
    @Nullable
    @JsonApiAttribute(name = JSON_LANGUAGE)
    private Language mLanguage;

    @JsonApiAttribute(name = JSON_VERSION)
    private int mVersion = DEFAULT_VERSION;
    @JsonApiAttribute(name = JSON_IS_PUBLISHED)
    private boolean mPublished = DEFAULT_PUBLISHED;

    @JsonApiIgnore
    private boolean mDownloaded = false;

    public long getResourceId() {
        return mResourceId != null && mResourceId != Resource.INVALID_ID ? mResourceId :
                mResource != null ? mResource.getId() : Resource.INVALID_ID;
    }

    public void setResourceId(@Nullable final Long resourceId) {
        mResourceId = resourceId;
    }

    public long getLanguageId() {
        return mLanguageId != null && mLanguageId != Language.INVALID_ID ? mLanguageId :
                mLanguage != null ? mLanguage.getId() : Language.INVALID_ID;
    }

    @Nullable
    public Language getLanguage() {
        return mLanguage;
    }

    public void setLanguageId(@Nullable final Long languageId) {
        mLanguageId = languageId;
    }

    public int getVersion() {
        return mVersion;
    }

    public void setVersion(final int version) {
        mVersion = version;
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
