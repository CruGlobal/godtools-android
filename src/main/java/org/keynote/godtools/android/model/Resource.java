package org.keynote.godtools.android.model;

import android.support.annotation.Nullable;

import org.ccci.gto.android.common.jsonapi.annotation.JsonApiAttribute;
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiIgnore;
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiType;

import java.util.List;

import static org.keynote.godtools.android.model.Resource.JSON_API_TYPE;

@JsonApiType(JSON_API_TYPE)
public class Resource extends Base {
    static final String JSON_API_TYPE = "resource";
    private static final String JSON_NAME = "name";
    private static final String JSON_ABBREVIATION = "abbreviation";
    private static final String JSON_DESCRIPTION = "description";
    private static final String JSON_TOTAL_VIEWS = "total-views";
    private static final String JSON_COPYRIGHT = "attr-copyright";
    public static final String JSON_LATEST_TRANSLATIONS = "latest-translations";

    @Nullable
    @JsonApiAttribute(name = JSON_LATEST_TRANSLATIONS)
    private List<Translation> mLatestTranslations;

    @Nullable
    @JsonApiAttribute(name = JSON_NAME)
    private String mName;
    @Nullable
    @JsonApiAttribute(name = JSON_DESCRIPTION)
    private String mDescription;

    @JsonApiAttribute(name = JSON_TOTAL_VIEWS)
    private int mShares = 0;

    @Nullable
    @JsonApiAttribute(name = JSON_COPYRIGHT)
    private String mCopyright;

    @JsonApiIgnore
    private boolean mAdded = false;

    @Nullable
    public List<Translation> getLatestTranslations() {
        return mLatestTranslations;
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

    public int getShares() {
        return mShares;
    }

    public void setShares(final int shares) {
        mShares = shares;
    }

    @Nullable
    public String getCopyright() {
        return mCopyright;
    }

    public void setCopyright(@Nullable final String copyright) {
        mCopyright = copyright;
    }

    public boolean isAdded() {
        return mAdded;
    }

    public void setAdded(final boolean state) {
        mAdded = state;
    }
}
