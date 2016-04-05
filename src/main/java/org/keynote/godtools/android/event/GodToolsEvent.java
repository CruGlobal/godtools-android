package org.keynote.godtools.android.event;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Created by dsgoers on 4/5/16.
 */
public class GodToolsEvent {

    private String eventId;
    private String namespace;
    private int followUpId;
    private Map<String, String> data = Maps.newHashMap();

    public GodToolsEvent(String eventId)
    {
        this.eventId = eventId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public int getFollowUpId() {
        return followUpId;
    }

    public void setFollowUpId(int followUpId) {
        this.followUpId = followUpId;
    }

    public Map<String, String> getData()
    {
        return data;
    }
}
