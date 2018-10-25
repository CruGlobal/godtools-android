package org.keynote.godtools.android.content;

import android.content.Context;

import org.ccci.gto.android.common.eventbus.content.CachingAsyncTaskEventBusLoader;
import org.cru.godtools.model.Tool;
import org.cru.godtools.model.event.content.ToolEventBusSubscriber;
import org.keynote.godtools.android.db.GodToolsDao;

import androidx.annotation.NonNull;

public final class ToolLoader extends CachingAsyncTaskEventBusLoader<Tool> {
    @NonNull
    private final GodToolsDao mDao;
    @NonNull
    private final String mCode;

    public ToolLoader(@NonNull final Context context, @NonNull final String code) {
        super(context);
        mDao = GodToolsDao.getInstance(context);
        mCode = code;
        addEventBusSubscriber(new ToolEventBusSubscriber(this));
    }

    @Override
    public Tool loadInBackground() {
        return mDao.find(Tool.class, mCode);
    }
}
