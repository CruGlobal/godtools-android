package org.keynote.godtools.android.content;

import android.content.Context;
import android.support.annotation.NonNull;

import org.ccci.gto.android.common.db.Query;
import org.ccci.gto.android.common.eventbus.content.CachingAsyncTaskEventBusLoader;
import org.cru.godtools.model.event.content.ToolEventBusSubscriber;
import org.keynote.godtools.android.db.Contract.ToolTable;
import org.keynote.godtools.android.db.GodToolsDao;
import org.keynote.godtools.android.model.Tool;

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
        return mDao.streamCompat(Query.select(Tool.class).where(ToolTable.FIELD_CODE.eq(mCode)))
                .findFirst().orElse(null);
        // TODO: switch back to mDao.find when we make tool code the PK
//        return mDao.find(Tool.class, mId);
    }
}
