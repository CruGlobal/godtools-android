package org.cru.godtools.xml.service

import android.content.Context
import androidx.annotation.WorkerThread
import org.cru.godtools.model.Translation
import org.cru.godtools.model.event.TranslationUpdateEvent
import org.greenrobot.eventbus.EventBus
import org.keynote.godtools.android.db.Contract.TranslationTable
import org.keynote.godtools.android.db.GodToolsDao

open class KotlinManifestManager(@JvmField protected val context: Context) {
    @JvmField
    protected val dao = GodToolsDao.getInstance(context)

    @WorkerThread
    protected open fun brokenManifest(manifestName: String) {
        dao.update(
            Translation().apply { isDownloaded = false },
            TranslationTable.FIELD_MANIFEST.eq(manifestName),
            TranslationTable.COLUMN_DOWNLOADED
        )
        EventBus.getDefault().post(TranslationUpdateEvent)
    }
}
