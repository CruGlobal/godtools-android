package org.keynote.godtools.android.content;

import android.content.Context;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import org.ccci.gto.android.common.eventbus.content.CachingAsyncTaskEventBusLoader;
import org.cru.godtools.model.Attachment;
import org.cru.godtools.model.event.content.AttachmentEventBusSubscriber;
import org.keynote.godtools.android.db.GodToolsDao;

public final class AttachmentLoader extends CachingAsyncTaskEventBusLoader<Attachment> {
    @NonNull
    private final GodToolsDao mDao;
    private long mId = Attachment.INVALID_ID;

    public AttachmentLoader(@NonNull final Context context) {
        super(context);
        mDao = GodToolsDao.getInstance(context);
        addEventBusSubscriber(new AttachmentEventBusSubscriber(this));
    }

    @MainThread
    public void setId(final long id) {
        final long oldId = mId;
        mId = id;
        if (oldId != mId) {
            onContentChanged();
        }
    }

    @Override
    public Attachment loadInBackground() {
        return mId != Attachment.INVALID_ID ? mDao.find(Attachment.class, mId) : null;
    }
}
