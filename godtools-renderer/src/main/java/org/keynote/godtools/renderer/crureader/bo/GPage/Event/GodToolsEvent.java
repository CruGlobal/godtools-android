package org.keynote.godtools.renderer.crureader.bo.GPage.Event;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.keynote.godtools.renderer.crureader.bo.GPage.Base.GBaseButtonAttributes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class GodToolsEvent {
    public static final int INVALID_ID = -1;
    @NonNull
    private final EventID eventID;
    private final Map<String, String> mFields = new HashMap<>();
    @Nullable
    private String mPackageCode;
    private long mFollowUpId = INVALID_ID;
    private String mLanguage;
    private int position = INVALID_ID;
    private GBaseButtonAttributes.ButtonMode errorMode;
    private String errorContent;
    private boolean mode;

    public GodToolsEvent(@NonNull EventID eventID) {
        this.eventID = eventID;

    }

    @NonNull
    public EventID getEventID() {
        return eventID;
    }

    @Nullable
    public String getPackageCode() {
        return mPackageCode;
    }

    public void setPackageCode(@Nullable final String packageCode) {
        mPackageCode = packageCode;
    }

    public String getLanguage() {
        return mLanguage;
    }

    public void setLanguage(@Nullable final String language) {
        mLanguage = language;
    }

    public long getFollowUpId() {
        return mFollowUpId;
    }

    public void setFollowUpId(final long id) {
        mFollowUpId = id;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public Map<String, String> getFields() {
        return mFields;
    }

    public void setField(@NonNull final String key, @Nullable final String value) {
        if (value != null) {
            mFields.put(key, value);
        } else {
            mFields.remove(key);
        }
    }

    public void setErrorMode(GBaseButtonAttributes.ButtonMode errorMode) {
        this.errorMode = errorMode;
    }

    public void setErrorContent(String errorContent) {
        this.errorContent = errorContent;
    }


    public String getErrorContent() {
        return errorContent;
    }

    public boolean getMode() {
        return mode;
    }

    /*immutable*/
    public static final class EventID {
        public static final EventID SUBSCRIBE_EVENT = new EventID("followup", "subscribe");
        public static final EventID ERROR_EVENT = new EventID("error", "errordialog");
        @NonNull
        private final String namespace;
        @NonNull
        private final String id;

        public EventID(@NonNull final String namespace, @NonNull final String id) {
            this.namespace = namespace;
            this.id = id;
        }

        @NonNull
        public String getNamespace() {
            return namespace;
        }

        @NonNull
        public String getId() {
            return id;
        }

        public boolean inNamespace(@Nullable final String namespace) {
            return this.namespace.equalsIgnoreCase(namespace);
        }

        @Override
        public boolean equals(@Nullable final Object obj) {
            return obj instanceof EventID &&
                    namespace.equalsIgnoreCase(((EventID) obj).namespace) &&
                    id.equalsIgnoreCase(((EventID) obj).id);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(new String[]{namespace.toLowerCase(), id.toLowerCase()});
        }

    }
}
