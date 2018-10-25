package org.cru.godtools.model;

import android.content.Context;

import org.ccci.gto.android.common.jsonapi.annotation.JsonApiAttribute;
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiIgnore;
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiType;
import org.cru.godtools.base.util.LocaleUtils;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static org.cru.godtools.model.Language.JSON_API_TYPE;

@JsonApiType(JSON_API_TYPE)
public class Language extends Base {
    static final String JSON_API_TYPE = "language";
    private static final String JSON_CODE = "code";
    private static final String JSON_NAME = "name";

    public static final Locale INVALID_CODE = new Locale("x", "inv");

    @Nullable
    @JsonApiAttribute(name = JSON_CODE)
    private Locale mCode;

    @Nullable
    @JsonApiAttribute(name = JSON_NAME)
    private String mName;

    @JsonApiIgnore
    private boolean mAdded = false;

    @NonNull
    public Locale getCode() {
        return mCode != null ? mCode : INVALID_CODE;
    }

    public void setCode(@Nullable final Locale code) {
        mCode = code;
    }

    @Nullable
    public String getName() {
        return mName;
    }

    public void setName(@Nullable String name) {
        mName = name;
    }

    public boolean isAdded() {
        return mAdded;
    }

    public void setAdded(final boolean state) {
        mAdded = state;
    }

    @NonNull
    public String getDisplayName(@Nullable final Context context) {
        if (mCode == null) {
            return "";
        }

        return LocaleUtils.getDisplayName(mCode, context, mName, null);
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
