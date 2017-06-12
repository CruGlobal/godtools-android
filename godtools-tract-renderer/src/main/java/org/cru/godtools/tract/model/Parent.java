package org.cru.godtools.tract.model;

import android.support.annotation.NonNull;

import java.util.List;

interface Parent {
    @NonNull
    List<Content> getContent();
}
