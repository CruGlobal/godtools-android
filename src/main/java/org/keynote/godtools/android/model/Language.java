package org.keynote.godtools.android.model;

import android.support.annotation.Nullable;

import org.ccci.gto.android.common.jsonapi.annotation.JsonApiAttribute;
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiType;

import java.util.Locale;

import static org.keynote.godtools.android.model.Language.JSON_API_TYPE;

@JsonApiType(JSON_API_TYPE)
public class Language extends Base {
    static final String JSON_API_TYPE = "language";
    private static final String JSON_LOCALE = "code";

    @Nullable
    @JsonApiAttribute(name = JSON_LOCALE)
    private Locale mLocale;

    public Locale getLocale() {
        return mLocale;
    }

    public void setLocale(@Nullable final Locale locale) {
        mLocale = locale;
    }
}
