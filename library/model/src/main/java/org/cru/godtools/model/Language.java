package org.cru.godtools.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.ccci.gto.android.common.jsonapi.annotation.JsonApiAttribute;
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiIgnore;
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiType;

import java.util.Locale;

import static org.cru.godtools.model.Language.JSON_API_TYPE;

@JsonApiType(JSON_API_TYPE)
public class Language extends Base {
    static final String JSON_API_TYPE = "language";
    private static final String JSON_CODE = "code";

    public static final Locale INVALID_CODE = new Locale("x", "inv");

    @Nullable
    @JsonApiAttribute(name = JSON_CODE)
    private Locale mCode;

    @JsonApiIgnore
    private boolean mAdded = false;

    @NonNull
    public Locale getCode() {
        return mCode != null ? mCode : INVALID_CODE;
    }

    public void setCode(@Nullable final Locale code) {
        mCode = code;
    }

    public boolean isAdded() {
        return mAdded;
    }

    public void setAdded(final boolean state) {
        mAdded = state;
    }

    @NonNull
    public String getDisplayName() {
        return mCode != null ? mCode.getDisplayName() : "";
    }

    @Override
    public String toString() {
        // XXX: output the language id and code for debugging purposes
        return "Language{" +
                "id=" + getId() +
                ", code=" + mCode +
                '}';
    }
}
