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
    public static final String JSON_LATEST_TRANSLATIONS = "latest-translations";

    @Nullable
    @JsonApiAttribute(name = JSON_LATEST_TRANSLATIONS)
    private List<Translation> mLatestTranslations;

    @Nullable
    @JsonApiAttribute(name = JSON_NAME)
    private String mName;

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

    public boolean isAdded() {
        return mAdded;
    }

    public void setAdded(final boolean state) {
        mAdded = state;
    }
}
