package org.cru.godtools.xml.model;

import android.support.annotation.NonNull;

import java.util.List;

public interface Parent {
    @NonNull
    List<Content> getContent();
}
