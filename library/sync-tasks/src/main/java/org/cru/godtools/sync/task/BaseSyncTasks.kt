package org.cru.godtools.sync.task

import android.content.ContentResolver
import android.content.Context
import android.os.Bundle
import androidx.annotation.RestrictTo
import androidx.annotation.WorkerThread
import androidx.collection.LongSparseArray
import androidx.collection.SimpleArrayMap
import org.cru.godtools.api.GodToolsApi
import org.cru.godtools.model.Base
import org.greenrobot.eventbus.EventBus
import org.keynote.godtools.android.db.GodToolsDao

@WorkerThread
@RestrictTo(RestrictTo.Scope.LIBRARY)
abstract class BaseSyncTasks internal constructor(context: Context) {
    protected val api = GodToolsApi.getInstance()
    protected val dao = GodToolsDao.getInstance(context)
    private val eventBus: EventBus = EventBus.getDefault()

    fun sendEvents(events: SimpleArrayMap<Class<*>, Any>) {
        for (i in 0 until events.size()) eventBus.post(events.valueAt(i))
        events.clear()
    }

    companion object {
        fun isForced(extras: Bundle) = extras.getBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, false)

        fun <E : Base> index(items: Collection<E>) = LongSparseArray<E>().apply {
            for (item in items) put(item.id, item)
        }

        fun coalesceEvent(events: SimpleArrayMap<Class<*>, Any>, event: Any) = events.put(event.javaClass, event)
    }
}
