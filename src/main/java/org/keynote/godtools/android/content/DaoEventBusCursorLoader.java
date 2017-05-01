package org.keynote.godtools.android.content;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.ccci.gto.android.common.db.support.v4.content.DaoCursorLoader;
import org.ccci.gto.android.common.eventbus.content.EventBusLoaderHelper;
import org.ccci.gto.android.common.eventbus.content.EventBusSubscriber;
import org.greenrobot.eventbus.EventBus;
import org.keynote.godtools.android.db.GodToolsDao;

public class DaoEventBusCursorLoader<T> extends DaoCursorLoader<T> implements EventBusLoaderHelper.Interface {
    private final EventBusLoaderHelper mHelper;

    public DaoEventBusCursorLoader(@NonNull final Context context, final Class<T> clazz, @Nullable final Bundle args) {
        super(context, GodToolsDao.getInstance(context), clazz, args);
        mHelper = new EventBusLoaderHelper(this, EventBus.getDefault());
    }

    /* BEGIN lifecycle */

    @Override
    protected void onStartLoading() {
        mHelper.onStartLoading();
        super.onStartLoading();
    }

    @Override
    protected void onReset() {
        super.onReset();
        mHelper.onReset();
    }

    @Override
    protected void onAbandon() {
        super.onAbandon();
        mHelper.onAbandon();
    }

    /* END lifecycle */

    @Override
    public void addEventBusSubscriber(@NonNull final EventBusSubscriber subscriber) {
        mHelper.addEventBusSubscriber(subscriber);
    }

    @Override
    public void removeEventBusSubscriber(@NonNull final EventBusSubscriber subscriber) {
        mHelper.removeEventBusSubscriber(subscriber);
    }
}
