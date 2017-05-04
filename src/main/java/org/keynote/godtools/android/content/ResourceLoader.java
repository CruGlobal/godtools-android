package org.keynote.godtools.android.content;

import android.content.Context;
import android.support.annotation.NonNull;

import org.ccci.gto.android.common.eventbus.content.CachingAsyncTaskEventBusLoader;
import org.keynote.godtools.android.db.GodToolsDao;
import org.keynote.godtools.android.model.Resource;

public final class ResourceLoader extends CachingAsyncTaskEventBusLoader<Resource> {
    @NonNull
    private final GodToolsDao mDao;
    private final long mId;

    public ResourceLoader(@NonNull final Context context, final long id) {
        super(context);
        mDao = GodToolsDao.getInstance(context);
        mId = id;
        addEventBusSubscriber(new ResourceEventBusSubscriber(this));
    }

    @Override
    public Resource loadInBackground() {
        return mDao.find(Resource.class, mId);
    }
}
