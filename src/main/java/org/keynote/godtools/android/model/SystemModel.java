package org.keynote.godtools.android.model;

import android.support.annotation.Nullable;

import org.ccci.gto.android.common.jsonapi.annotation.JsonApiAttribute;
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiType;

import java.util.List;

import static org.keynote.godtools.android.model.SystemModel.JSON_API_TYPE;

/**
 * The system in the mobile content API that contains resources. We don't name this class "System" to avoid naming
 * conflicts with {@link System}.
 */
@JsonApiType(JSON_API_TYPE)
public class SystemModel extends Base {
    static final String JSON_API_TYPE = "system";
    private static final String JSON_NAME = "name";
    private static final String JSON_RESOURCES = "resources";

    @Nullable
    @JsonApiAttribute(name = JSON_RESOURCES)
    private List<Tool> mTools;

    @Nullable
    public List<Tool> getTools() {
        return mTools;
    }
}
