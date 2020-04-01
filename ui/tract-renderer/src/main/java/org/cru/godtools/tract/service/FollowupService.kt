package org.cru.godtools.tract.service

import android.content.Context
import android.os.AsyncTask
import androidx.annotation.WorkerThread
import org.cru.godtools.base.model.Event
import org.cru.godtools.base.util.SingletonHolder
import org.cru.godtools.model.Followup
import org.cru.godtools.sync.syncFollowups
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.keynote.godtools.android.db.GodToolsDao

private const val FIELD_NAME = "name"
private const val FIELD_EMAIL = "email"
private const val FIELD_DESTINATION = "destination_id"

class FollowupService private constructor(private val context: Context) {
    companion object : SingletonHolder<FollowupService, Context>({ FollowupService(it.applicationContext) })

    private val dao by lazy { GodToolsDao.getInstance(context) }

    init {
        EventBus.getDefault().register(this)

        // sync any currently pending followups
        AsyncTask.THREAD_POOL_EXECUTOR.execute { syncPendingFollowups() }
    }

    @WorkerThread
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onContentEvent(event: Event) {
        if (event.id == Event.Id.FOLLOWUP_EVENT) {
            val followup = Followup().apply {
                name = event.fields[FIELD_NAME]
                email = event.fields[FIELD_EMAIL]
                languageCode = event.locale
                destination = event.fields[FIELD_DESTINATION]?.toLongOrNull()
            }

            // only store this followup if it's valid
            if (followup.isValid) {
                dao.insertNew(followup)
                syncPendingFollowups()
            }
        }
    }

    @WorkerThread
    private fun syncPendingFollowups() = context.syncFollowups().sync()
}
