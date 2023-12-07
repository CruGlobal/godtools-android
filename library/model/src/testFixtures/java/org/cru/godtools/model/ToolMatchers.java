package org.cru.godtools.model;

import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsEqual.equalTo;

import org.hamcrest.Matcher;

public class ToolMatchers {
    public static Matcher<Tool> tool(Tool tool) {
        return allOf(
                hasProperty("apiId", equalTo(tool.getApiId())),
                hasProperty("code", equalTo(tool.getCode()))
        );
    }
}
