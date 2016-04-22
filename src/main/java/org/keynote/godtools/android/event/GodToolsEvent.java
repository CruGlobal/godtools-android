package org.keynote.godtools.android.event;

import android.support.annotation.NonNull;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Created by dsgoers on 4/5/16.
 */
public class GodToolsEvent {
    @NonNull
    private final EventID eventID;
    private long followUpId;
    private Map<String, String> data = Maps.newHashMap();

    public GodToolsEvent(@NonNull EventID eventID) {
        this.eventID = eventID;
    }

    @NonNull
    public EventID getEventID() {
        return eventID;
    }

    public long getFollowUpId() {
        return followUpId;
    }

    public void setFollowUpId(long followUpId) {
        this.followUpId = followUpId;
    }

    public Map<String, String> getData()
    {
        return data;
    }

    /*immutable*/
    public static final class EventID {
        public static final EventID SUBSCRIBE_EVENT = new EventID("followup", "subscribe");

        @NonNull
        private final String namespace;
        @NonNull
        private final String id;

        public EventID(@NonNull String namespace, @NonNull String id) {
            this.namespace = namespace;
            this.id = id;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof EventID && namespace.equalsIgnoreCase(((EventID) obj).getNamespace()) && id
                    .equalsIgnoreCase(((EventID) obj).getId());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(namespace.toLowerCase(), id.toLowerCase());
        }

        @NonNull
        public String getNamespace() {
            return namespace;
        }

        @NonNull
        public String getId() {
            return id;
        }
    }
}
