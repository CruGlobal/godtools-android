package org.cru.godtools.sync.task

import android.content.ContentResolver
import android.os.Bundle
import androidx.annotation.RestrictTo
import androidx.annotation.WorkerThread
import androidx.collection.LongSparseArray
import androidx.collection.SimpleArrayMap
import org.cru.godtools.model.Base
import org.greenrobot.eventbus.EventBus

@WorkerThread
@RestrictTo(RestrictTo.Scope.LIBRARY)
abstract class BaseSyncTasks internal constructor(private val eventBus: EventBus) {
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
