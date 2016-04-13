package org.keynote.godtools.android.utils;

import com.google.common.base.Objects;

/**
 * Created by dsgoers on 4/13/16.
 */
public class EventID {
    private String namespace;
    private String id;

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

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
