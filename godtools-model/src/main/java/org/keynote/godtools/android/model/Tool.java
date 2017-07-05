package org.keynote.godtools.android.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.ccci.gto.android.common.jsonapi.annotation.JsonApiAttribute;
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiIgnore;
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiType;
import org.cru.godtools.model.Attachment;
import org.cru.godtools.model.Base;
import org.cru.godtools.model.Translation;

import java.util.List;

import static org.keynote.godtools.android.model.Tool.JSON_API_TYPE;

@JsonApiType(JSON_API_TYPE)
public class Tool extends Base {
    static final String JSON_API_TYPE = "resource";
    private static final String JSON_NAME = "name";
    private static final String JSON_TYPE = "resource-type";
    private static final String JSON_TYPE_TRACT = "tract";
    private static final String JSON_TYPE_ARTICLE = "tract";
    private static final String JSON_ABBREVIATION = "abbreviation";
    private static final String JSON_DESCRIPTION = "description";
    private static final String JSON_TOTAL_VIEWS = "total-views";
    private static final String JSON_BANNER = "attr-banner";
    private static final String JSON_BANNER_DETAILS = "attr-banner-about";
    private static final String JSON_COPYRIGHT = "attr-copyright";
    public static final String JSON_LATEST_TRANSLATIONS = "latest-translations";
    public static final String JSON_ATTACHMENTS = "attachments";

    public enum Type {
        TRACT(JSON_TYPE_TRACT), ARTICLE(JSON_TYPE_ARTICLE), UNKNOWN(null);

        @Nullable
        private final String mJson;

        Type(final String json) {
            mJson = json;
        }

        @Nullable
        public static Type fromJson(@Nullable final String json) {
            if (json == null) {
                return null;
            }
            for (final Type type : values()) {
                if (json.equals(type.mJson)) {
                    return type;
                }
            }
            return UNKNOWN;
        }

        @Nullable
        public String toJson() {
            return mJson;
        }
    }

    @Nullable
    @JsonApiAttribute(name = JSON_LATEST_TRANSLATIONS)
    private List<Translation> mLatestTranslations;
    @Nullable
    @JsonApiAttribute(name = JSON_ATTACHMENTS)
    private List<Attachment> mAttachments;

    @Nullable
    @JsonApiAttribute(name = JSON_ABBREVIATION)
    private String mCode;
    @Nullable
    @JsonApiAttribute(name = JSON_TYPE)
    private Type mType;

    @Nullable
    @JsonApiAttribute(name = JSON_NAME)
    private String mName;
    @Nullable
    @JsonApiAttribute(name = JSON_DESCRIPTION)
    private String mDescription;

    @JsonApiAttribute(name = JSON_TOTAL_VIEWS)
    private int mShares = 0;
    @JsonApiIgnore
    private int mPendingShares = 0;

    @JsonApiAttribute(name = JSON_BANNER)
    private long mBannerId = Attachment.INVALID_ID;
    @JsonApiAttribute(name = JSON_BANNER_DETAILS)
    private long mDetailsBannerId = Attachment.INVALID_ID;
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
    public List<Attachment> getAttachments() {
        return mAttachments;
    }

    @Nullable
    public String getCode() {
        return mCode;
    }

    public void setCode(@Nullable final String code) {
        mCode = code;
    }

    @NonNull
    public Type getType() {
        return mType != null ? mType : Type.UNKNOWN;
    }

    public void setType(@Nullable final Type type) {
        mType = type;
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

    public int getPendingShares() {
        return mPendingShares;
    }

    public void setPendingShares(final int shares) {
        mPendingShares = shares;
    }

    public int getTotalShares() {
        return mPendingShares + mShares;
    }

    public long getBannerId() {
        return mBannerId;
    }

    public void setBannerId(final long attachmentId) {
        mBannerId = attachmentId;
    }

    public long getDetailsBannerId() {
        return mDetailsBannerId;
    }

    public void setDetailsBannerId(final long attachmentId) {
        mDetailsBannerId = attachmentId;
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
