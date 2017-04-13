package org.keynote.godtools.android.model;

import android.support.annotation.Nullable;

import org.ccci.gto.android.common.jsonapi.annotation.JsonApiAttribute;
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiType;

import java.util.List;

import static org.keynote.godtools.android.model.System.JSON_API_TYPE;

@JsonApiType(JSON_API_TYPE)
public class System extends Base {
    static final String JSON_API_TYPE = "system";
    private static final String JSON_NAME = "name";
    private static final String JSON_RESOURCES = "resources";

    @Nullable
    @JsonApiAttribute(name = JSON_RESOURCES)
    private List<Resource> mResources;
}
