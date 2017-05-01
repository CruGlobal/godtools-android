package org.keynote.godtools.android.content;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.keynote.godtools.android.model.Resource;

public final class ResourcesCursorLoader extends DaoEventBusCursorLoader<Resource> {
    public ResourcesCursorLoader(@NonNull final Context context, @Nullable final Bundle args) {
        super(context, Resource.class, args);
        addEventBusSubscriber(new ResourceEventBusSubscriber(this));
    }
}
