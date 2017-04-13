package org.keynote.godtools.android.model;

import org.ccci.gto.android.common.jsonapi.annotation.JsonApiAttribute;
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiType;

import static org.keynote.godtools.android.model.Translation.JSON_API_TYPE;

@JsonApiType(JSON_API_TYPE)
public class Translation extends Base {
    static final String JSON_API_TYPE = "translation";
    private static final String JSON_VERSION = "version";
    private static final String JSON_IS_PUBLISHED = "is-published";

    private static final boolean PUBLISHED_DEFAULT = false;

    @JsonApiAttribute(name = JSON_VERSION)
    private int mVersion = 0;
    @JsonApiAttribute(name = JSON_IS_PUBLISHED)
    private boolean mPublished = false;

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
}
