package org.keynote.godtools.android.event;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Created by dsgoers on 4/5/16.
 */
public class GodToolsEvent {

    private final EventID eventID;
    private long followUpId;
    private Map<String, String> data = Maps.newHashMap();

    public GodToolsEvent(EventID eventID) {
        this.eventID = eventID;
    }

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
        private final String namespace;
        private final String id;

        public EventID(String namespace, String id) {
            this.namespace = namespace;
            this.id = id;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof EventID && namespace.equals(((EventID) obj).getNamespace()) && id.equals(((EventID)
                    obj).getId());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(namespace, id);
        }

        public String getNamespace() {
            return namespace;
        }

        public String getId() {
            return id;
        }
    }
}
