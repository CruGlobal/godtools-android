package org.keynote.godtools.android.content;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.ccci.gto.android.common.eventbus.content.DaoCursorEventBusLoader;
import org.keynote.godtools.android.db.GodToolsDao;
import org.keynote.godtools.android.model.Resource;

public final class ResourcesCursorLoader extends DaoCursorEventBusLoader<Resource> {
    public ResourcesCursorLoader(@NonNull final Context context, @Nullable final Bundle args) {
        super(context, GodToolsDao.getInstance(context), Resource.class, args);
        addEventBusSubscriber(new ResourceEventBusSubscriber(this));
    }
}
