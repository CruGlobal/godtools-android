package org.cru.godtools.sync.task

import android.content.ContentResolver
import android.os.Bundle
import androidx.annotation.RestrictTo
import androidx.annotation.WorkerThread
import androidx.collection.LongSparseArray
import org.cru.godtools.model.Base

@WorkerThread
@RestrictTo(RestrictTo.Scope.LIBRARY)
abstract class BaseSyncTasks internal constructor() {
    companion object {
        fun isForced(extras: Bundle) = extras.getBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, false)

        fun <E : Base> index(items: Collection<E>) = LongSparseArray<E>().apply {
            for (item in items) put(item.id, item)
        }
    }
}
