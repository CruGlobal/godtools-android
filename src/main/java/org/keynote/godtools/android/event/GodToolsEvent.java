package org.keynote.godtools.android.event;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.collect.ImmutableMap;

import org.keynote.godtools.android.model.Followup;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class GodToolsEvent {
    @NonNull
    private final EventID eventID;

    @Nullable
    private String mPackageCode;
    private long mFollowUpId = Followup.INVALID_ID;
    private String mLanguage;

    private final Map<String, String> mData = new HashMap<>();

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

    public Map<String, String> getData() {
        return ImmutableMap.copyOf(mData);
    }

    public void setData(@NonNull final String key, @Nullable final String value) {
        if (value != null) {
            mData.put(key, value);
        } else {
            mData.remove(key);
        }
    }

    /*immutable*/
    public static final class EventID {
        public static final EventID SUBSCRIBE_EVENT = new EventID("followup", "subscribe");

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
            return Arrays.hashCode(new String[] {namespace.toLowerCase(), id.toLowerCase()});
        }
    }
}
