package org.cru.godtools.sync.task

import android.content.Context
import androidx.annotation.RestrictTo
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.find
import org.cru.godtools.base.util.SingletonHolder
import org.cru.godtools.model.Followup
import java.io.IOException

private val LOCK_FOLLOWUPS = Any()

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class FollowupSyncTasks private constructor(context: Context) : BaseSyncTasks(context) {
    companion object : SingletonHolder<FollowupSyncTasks, Context>(::FollowupSyncTasks)

    @Throws(IOException::class)
    fun syncFollowups() {
        synchronized(LOCK_FOLLOWUPS) {
            dao.get(Query.select<Followup>()).forEach { followup ->
                followup.languageCode?.let { followup.setLanguage(dao.find(it)) }
                followup.stashId()
                if (api.followups.subscribe(followup).execute().code() == 204) {
                    followup.restoreId()
                    dao.delete(followup)
                }
            }
        }
    }
}
