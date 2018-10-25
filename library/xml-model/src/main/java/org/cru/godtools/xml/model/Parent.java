package org.cru.godtools.xml.model;

import java.util.List;

import androidx.annotation.NonNull;

public interface Parent {
    @NonNull
    List<Content> getContent();
}
