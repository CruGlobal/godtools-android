package org.cru.godtools.model;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.ccci.gto.android.common.jsonapi.annotation.JsonApiAttribute;
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiIgnore;
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiType;
import org.keynote.godtools.android.model.Language;

import java.util.Date;
import java.util.Locale;

import static org.cru.godtools.model.Followup.JSON_API_TYPE;

@JsonApiType(JSON_API_TYPE)
public final class Followup extends Base {
    static final String JSON_API_TYPE = "follow_up";
    private static final String JSON_NAME = "name";
    private static final String JSON_EMAIL = "email";
    private static final String JSON_LANGUAGE = "language_id";
    private static final String JSON_DESTINATION = "destination_id";

    @Nullable
    @JsonApiAttribute(name = JSON_NAME)
    private String mName;
    @Nullable
    @JsonApiAttribute(name = JSON_EMAIL)
    private String mEmail;
    @Nullable
    @JsonApiIgnore
    private Locale mLanguageCode;
    @Nullable
    @JsonApiAttribute(name = JSON_LANGUAGE)
    private Long mLanguageId;
    @Nullable
    @JsonApiAttribute(name = JSON_DESTINATION)
    private Long mDestination;
    @Nullable
    @JsonApiIgnore
    private Date mCreateTime = new Date();

    @Nullable
    public String getName() {
        return mName;
    }

    public void setName(@Nullable final String name) {
        mName = name;
    }

    @Nullable
    public String getEmail() {
        return mEmail;
    }

    public void setEmail(@Nullable final String email) {
        mEmail = email;
    }

    @Nullable
    public Locale getLanguageCode() {
        return mLanguageCode;
    }

    public void setLanguageCode(@Nullable final Locale code) {
        mLanguageCode = code;
    }

    public void setLanguage(@Nullable final Language language) {
        mLanguageId = language != null ? language.getId() : null;
    }

    @Nullable
    public Long getDestination() {
        return mDestination;
    }

    public void setDestination(@Nullable final Long destination) {
        mDestination = destination;
    }

    @Nullable
    public Date getCreateTime() {
        return mCreateTime;
    }

    public void setCreateTime(@Nullable final Date time) {
        mCreateTime = time != null ? time : new Date();
    }

    public boolean isValid() {
        return mDestination != null && mLanguageCode != null && !TextUtils.isEmpty(mEmail);
    }
}
